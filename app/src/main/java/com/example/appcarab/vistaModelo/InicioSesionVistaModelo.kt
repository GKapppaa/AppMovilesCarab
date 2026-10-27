package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.modelo.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PantallaAcceso { BIENVENIDA, INICIAR_SESION, REGISTRO, INICIO }

data class EstadoAcceso(
    val pantalla: PantallaAcceso = PantallaAcceso.BIENVENIDA,
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val correoUsuario: String? = null,
)

class InicioSesionVistaModelo(private val repository: UsuarioRepository) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoAcceso())
    val estado: StateFlow<EstadoAcceso> = _estado.asStateFlow()

    fun irA(pantalla: PantallaAcceso) {
        _estado.value = _estado.value.copy(pantalla = pantalla, mensaje = null)
    }

    fun iniciarSesion(email: String, password: String) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.validarSesion(email, password) }
                .onSuccess { usuario ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (usuario == null) PantallaAcceso.INICIAR_SESION else PantallaAcceso.INICIO,
                        cargando = false,
                        mensaje = if (usuario == null) "Correo o contraseña incorrectos." else null,
                        correoUsuario = usuario?.email,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(cargando = false, mensaje = "No se pudo iniciar sesión. Intenta nuevamente.")
                }
        }
    }

    fun registrar(email: String, password: String) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.registrarUsuario(email, password) }
                .onSuccess { registrado ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (registrado) PantallaAcceso.INICIO else PantallaAcceso.REGISTRO,
                        cargando = false,
                        mensaje = if (registrado) null else "Ya existe una cuenta con ese correo.",
                        correoUsuario = if (registrado) email.trim().lowercase() else null,
                    )
                }
                .onFailure {
                    _estado.value = _estado.value.copy(cargando = false, mensaje = "No se pudo crear la cuenta. Intenta nuevamente.")
                }
        }
    }
}
