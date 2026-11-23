package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.R
import com.example.appcarab.modelo.ApiDocumentosRepository
import com.example.appcarab.modelo.Textos
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoSesionRemota(
    val codigoPerfil: String? = null,
    val token: String? = null,
    val cargando: Boolean = false,
    val error: String? = null,
)

class SesionRemotaVistaModelo(
    private val api: ApiDocumentosRepository,
    private val textos: Textos,
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoSesionRemota())
    val estado: StateFlow<EstadoSesionRemota> = _estado.asStateFlow()

    fun abrir(codigoPerfil: String, rol: String, reintentar: Boolean = false) {
        if (!reintentar && _estado.value.codigoPerfil == codigoPerfil &&
            (_estado.value.token != null || _estado.value.cargando)
        ) return

        viewModelScope.launch {
            _estado.value = EstadoSesionRemota(codigoPerfil = codigoPerfil, cargando = true)
            try {
                val sesion = api.abrirSesion(codigoPerfil, rol)
                _estado.value = EstadoSesionRemota(codigoPerfil = codigoPerfil, token = sesion.token)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _estado.value = EstadoSesionRemota(
                    codigoPerfil = codigoPerfil,
                    error = textos.get(R.string.sesion_remota_no_disponible),
                )
            }
        }
    }

    fun cerrar() {
        val token = _estado.value.token
        _estado.value = EstadoSesionRemota()
        if (token != null) {
            viewModelScope.launch {
                runCatching { api.cerrarSesion(token) }
            }
        }
    }
}
