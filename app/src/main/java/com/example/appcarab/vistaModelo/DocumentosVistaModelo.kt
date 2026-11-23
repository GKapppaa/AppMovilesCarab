package com.example.appcarab.vistaModelo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.R
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.Textos
import com.example.appcarab.modelo.DocumentoDao
import com.example.appcarab.modelo.DocumentoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class EstadoDocumentos(
    val documentos: List<Documento> = emptyList(),
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val mensajeEsError: Boolean = false,
)

class DocumentosVistaModelo(
    private val repository: DocumentoRepository,
    private val dao: DocumentoDao,
    private val textos: Textos,
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoDocumentos())
    val estado: StateFlow<EstadoDocumentos> = _estado.asStateFlow()
    private var usuarioObservado: Int? = null
    private var tokenObservado: String? = null
    private var observacionJob: Job? = null

    fun observar(usuarioId: Int, codigoPerfil: String, token: String?) {
        if (usuarioObservado != usuarioId) {
            usuarioObservado = usuarioId
            tokenObservado = null
            observacionJob?.cancel()
            _estado.value = EstadoDocumentos()
            observacionJob = viewModelScope.launch {
                dao.observarDocumentos(usuarioId).collectLatest { documentos ->
                    _estado.value = _estado.value.copy(documentos = documentos)
                }
            }
        }
        if (token != null && token != tokenObservado) {
            tokenObservado = token
            viewModelScope.launch {
                runCatching { repository.importarCompartidos(usuarioId, token) }
                    .onFailure {
                        _estado.value = _estado.value.copy(
                            mensaje = textos.get(R.string.doc_error_compartidos),
                            mensajeEsError = true,
                        )
                    }
            }
        }
    }

    fun agregar(usuarioId: Int, codigoPerfil: String, token: String?, uri: Uri) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.agregarPdf(usuarioId, codigoPerfil, token, uri) }
                .onSuccess { documento ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = textos.get(
                            if (documento.idRemoto != null) R.string.doc_agregado_remoto
                            else R.string.doc_agregado_local,
                        ),
                        mensajeEsError = false,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = it.message ?: textos.get(R.string.doc_error_procesar),
                        mensajeEsError = true,
                    )
                }
        }
    }

    fun uriTemporalEscaneo(usuarioId: Int): Uri = repository.uriTemporalEscaneo(usuarioId)

    fun agregarEscaneo(usuarioId: Int, codigoPerfil: String, token: String?, uri: Uri) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.agregarEscaneo(usuarioId, codigoPerfil, token, uri) }
                .onSuccess { documento ->
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = textos.get(
                            if (documento.idRemoto != null) R.string.doc_escaneo_guardado
                            else R.string.doc_escaneo_local,
                        ),
                        mensajeEsError = false,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(cargando = false, mensaje = it.message ?: textos.get(R.string.doc_error_escaneo), mensajeEsError = true)
                }
        }
    }

    fun eliminar(documento: Documento, codigoPerfil: String, token: String?) {
        viewModelScope.launch {
            runCatching { repository.eliminar(documento, codigoPerfil, token) }
                .onSuccess { _estado.value = _estado.value.copy(mensaje = textos.get(R.string.doc_eliminado), mensajeEsError = false) }
                .onFailure { _estado.value = _estado.value.copy(mensaje = textos.get(R.string.doc_error_eliminar), mensajeEsError = true) }
        }
    }

    fun subirAlServidor(documento: Documento, codigoPerfil: String, token: String?) {
        if (token == null) {
            _estado.value = _estado.value.copy(
                mensaje = textos.get(R.string.sesion_remota_requerida),
                mensajeEsError = true,
            )
            return
        }
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.subirExistente(documento, codigoPerfil, token) }
                .onSuccess {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = textos.get(R.string.doc_agregado_remoto),
                        mensajeEsError = false,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = it.message ?: textos.get(R.string.doc_error_procesar),
                        mensajeEsError = true,
                    )
                }
        }
    }

    fun uriParaAbrir(documento: Documento) = repository.uriParaAbrir(documento)

    fun cambiarCategoria(documentoId: Int, categoria: String) {
        viewModelScope.launch { dao.actualizarCategoria(documentoId, categoria) }
    }

}
