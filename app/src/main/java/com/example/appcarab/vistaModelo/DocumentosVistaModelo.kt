package com.example.appcarab.vistaModelo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.modelo.Documento
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

class DocumentosVistaModelo(private val repository: DocumentoRepository, private val dao: DocumentoDao) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoDocumentos())
    val estado: StateFlow<EstadoDocumentos> = _estado.asStateFlow()
    private var usuarioObservado: Int? = null
    private var observacionJob: Job? = null

    fun observar(usuarioId: Int) {
        if (usuarioObservado == usuarioId) return
        usuarioObservado = usuarioId
        observacionJob?.cancel()
        _estado.value = EstadoDocumentos()
        observacionJob = viewModelScope.launch {
            dao.observarDocumentos(usuarioId).collectLatest { documentos ->
                _estado.value = _estado.value.copy(documentos = documentos)
            }
        }
    }

    fun agregar(usuarioId: Int, uri: Uri) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.agregarPdf(usuarioId, uri) }
                .onSuccess {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = "PDF agregado y resumido en el dispositivo.",
                        mensajeEsError = false,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(
                        cargando = false,
                        mensaje = it.message ?: "No se pudo procesar el PDF.",
                        mensajeEsError = true,
                    )
                }
        }
    }

    fun eliminar(documento: Documento) {
        viewModelScope.launch {
            runCatching { repository.eliminar(documento) }
                .onSuccess { _estado.value = _estado.value.copy(mensaje = "Documento eliminado.", mensajeEsError = false) }
                .onFailure { _estado.value = _estado.value.copy(mensaje = "No se pudo eliminar el documento.", mensajeEsError = true) }
        }
    }

    fun uriParaAbrir(documento: Documento) = repository.uriParaAbrir(documento)

}
