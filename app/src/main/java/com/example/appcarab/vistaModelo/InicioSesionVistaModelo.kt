package com.example.appcarab.vistaModelo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.modelo.UsuarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PantallaAcceso { BIENVENIDA, INICIAR_SESION, REGISTRO, EDITAR_PERFIL, INICIO }

data class EstadoAcceso(
    val pantalla: PantallaAcceso = PantallaAcceso.BIENVENIDA,
    val cargando: Boolean = false,
    val mensaje: String? = null,
    val correoUsuario: String? = null,
    val usuarioId: Int? = null,
    val nombreUsuario: String = "",
    val rolUsuario: String = "ESTUDIANTE",
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
                        usuarioId = usuario?.id,
                        nombreUsuario = usuario?.nombre.orEmpty(),
                        rolUsuario = usuario?.role ?: "ESTUDIANTE",
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló el inicio de sesión", error)
                    _estado.value = _estado.value.copy(cargando = false, mensaje = "No se pudo iniciar sesión. Intenta nuevamente.")
                }
        }
    }

    fun registrar(nombre: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.registrarUsuario(nombre, email, password, role) }
                .onSuccess { usuario ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (usuario != null) PantallaAcceso.INICIO else PantallaAcceso.REGISTRO,
                        cargando = false,
                        mensaje = if (usuario != null) null else "Ya existe una cuenta con ese correo.",
                        correoUsuario = usuario?.email,
                        usuarioId = usuario?.id,
                        nombreUsuario = usuario?.nombre.orEmpty(),
                        rolUsuario = usuario?.role ?: role,
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló el registro de usuario", error)
                    _estado.value = _estado.value.copy(cargando = false, mensaje = "No se pudo crear la cuenta. Intenta nuevamente.")
                }
        }
    }

    fun guardarPerfil(nombre: String, email: String) {
        val usuarioId = _estado.value.usuarioId ?: return
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.actualizarPerfil(usuarioId, nombre, email) }
                .onSuccess { usuario ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (usuario == null) PantallaAcceso.EDITAR_PERFIL else PantallaAcceso.INICIO,
                        cargando = false,
                        mensaje = if (usuario == null) "Ese correo ya está asociado a otra cuenta." else null,
                        correoUsuario = usuario?.email ?: _estado.value.correoUsuario,
                        nombreUsuario = usuario?.nombre ?: _estado.value.nombreUsuario,
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló la actualización del perfil", error)
                    _estado.value = _estado.value.copy(cargando = false, mensaje = "No se pudo guardar el perfil.")
                }
        }
    }

    fun cerrarSesion() {
        _estado.value = EstadoAcceso(pantalla = PantallaAcceso.BIENVENIDA)
    }

    private companion object {
        const val TAG = "InicioSesionVM"
    }
}
