package com.example.appcarab.vistaModelo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.R
import com.example.appcarab.modelo.Textos
import com.example.appcarab.modelo.UsuarioRepository
import com.example.appcarab.modelo.ValidadoresFormulario
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
    val codigoPerfil: String = "",
    val rolUsuario: String = "ESTUDIANTE",
)

class InicioSesionVistaModelo(
    private val repository: UsuarioRepository,
    private val textos: Textos,
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoAcceso())
    val estado: StateFlow<EstadoAcceso> = _estado.asStateFlow()

    fun irA(pantalla: PantallaAcceso) {
        _estado.value = _estado.value.copy(pantalla = pantalla, mensaje = null)
    }

    fun iniciarSesion(email: String, password: String) {
        if (!ValidadoresFormulario.correoValido(email) || password.isBlank()) {
            _estado.value = _estado.value.copy(pantalla = PantallaAcceso.INICIAR_SESION, mensaje = textos.get(R.string.vm_revisa_correo_contrasena))
            return
        }
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.validarSesion(email, password) }
                .onSuccess { usuario ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (usuario == null) PantallaAcceso.INICIAR_SESION else PantallaAcceso.INICIO,
                        cargando = false,
                        mensaje = if (usuario == null) textos.get(R.string.vm_credenciales_incorrectas) else null,
                        correoUsuario = usuario?.email,
                        usuarioId = usuario?.id,
                        nombreUsuario = usuario?.nombre.orEmpty(),
                        codigoPerfil = usuario?.codigoPerfil.orEmpty(),
                        rolUsuario = usuario?.role ?: "ESTUDIANTE",
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló el inicio de sesión", error)
                    _estado.value = _estado.value.copy(cargando = false, mensaje = textos.get(R.string.vm_error_inicio))
                }
        }
    }

    fun registrar(nombre: String, email: String, password: String, role: String, codigoProfesor: String) {
        if (!ValidadoresFormulario.nombreValido(nombre) ||
            !ValidadoresFormulario.correoValido(email) ||
            !ValidadoresFormulario.contrasenaValida(password) ||
            role !in setOf("ESTUDIANTE", "PROFESOR") ||
            (role == "PROFESOR" && !ValidadoresFormulario.codigoProfesorValido(codigoProfesor))
        ) {
            val mensaje = if (role == "PROFESOR" && !ValidadoresFormulario.codigoProfesorValido(codigoProfesor)) {
                textos.get(R.string.vm_codigo_docente_invalido)
            } else textos.get(R.string.vm_revisa_datos)
            _estado.value = _estado.value.copy(pantalla = PantallaAcceso.REGISTRO, mensaje = mensaje)
            return
        }
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.registrarUsuario(nombre, email, password, role) }
                .onSuccess { usuario ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (usuario != null) PantallaAcceso.INICIO else PantallaAcceso.REGISTRO,
                        cargando = false,
                        mensaje = if (usuario != null) null else textos.get(R.string.vm_correo_existente),
                        correoUsuario = usuario?.email,
                        usuarioId = usuario?.id,
                        nombreUsuario = usuario?.nombre.orEmpty(),
                        codigoPerfil = usuario?.codigoPerfil.orEmpty(),
                        rolUsuario = usuario?.role ?: role,
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló el registro de usuario", error)
                    _estado.value = _estado.value.copy(cargando = false, mensaje = textos.get(R.string.vm_error_registro))
                }
        }
    }

    fun guardarPerfil(nombre: String, email: String) {
        val usuarioId = _estado.value.usuarioId ?: return
        if (!ValidadoresFormulario.nombreValido(nombre) || !ValidadoresFormulario.correoValido(email)) {
            _estado.value = _estado.value.copy(pantalla = PantallaAcceso.EDITAR_PERFIL, mensaje = textos.get(R.string.vm_revisa_nombre_correo))
            return
        }
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null)
            runCatching { repository.actualizarPerfil(usuarioId, nombre, email) }
                .onSuccess { usuario ->
                    _estado.value = _estado.value.copy(
                        pantalla = if (usuario == null) PantallaAcceso.EDITAR_PERFIL else PantallaAcceso.INICIO,
                        cargando = false,
                        mensaje = if (usuario == null) textos.get(R.string.vm_correo_en_uso) else null,
                        correoUsuario = usuario?.email ?: _estado.value.correoUsuario,
                        nombreUsuario = usuario?.nombre ?: _estado.value.nombreUsuario,
                    )
                }
                .onFailure { error ->
                    Log.e(TAG, "Falló la actualización del perfil", error)
                    _estado.value = _estado.value.copy(cargando = false, mensaje = textos.get(R.string.vm_error_perfil))
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
