package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.R
import com.example.appcarab.modelo.AlumnoAula
import com.example.appcarab.modelo.AulaDao
import com.example.appcarab.modelo.Textos
import com.example.appcarab.modelo.AulaRepository
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.MensajeDocente
import com.example.appcarab.modelo.TareaDocente
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class EstadoAula(
    val alumnos: List<AlumnoAula> = emptyList(),
    val mensajes: List<MensajeDocente> = emptyList(),
    val tareas: List<TareaDocente> = emptyList(),
    val materiales: List<Documento> = emptyList(),
    val mensaje: String? = null,
    val error: Boolean = false,
)

class AulaVistaModelo(
    private val repo: AulaRepository,
    private val dao: AulaDao,
    private val textos: Textos,
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoAula())
    val estado: StateFlow<EstadoAula> = _estado.asStateFlow()
    private val jobs = mutableListOf<Job>()
    private var alumnosLocales: List<AlumnoAula> = emptyList()
    private var alumnosRemotos: List<AlumnoAula> = emptyList()

    fun observar(usuarioId: Int, rol: String, tokenRemoto: String?) {
        jobs.forEach(Job::cancel)
        jobs.clear()
        alumnosLocales = emptyList()
        alumnosRemotos = emptyList()
        _estado.value = EstadoAula()
        val flujo = if (rol == "PROFESOR") dao.observarAlumnos(usuarioId) else null
        if (flujo != null) {
            jobs += viewModelScope.launch {
                flujo.collectLatest { alumnos ->
                    alumnosLocales = alumnos.map { AlumnoAula(it.codigoPerfil, it.nombre, aulaLocal = true) }
                    publicarAlumnos()
                }
            }
        }
        if (rol == "PROFESOR" && tokenRemoto != null) {
            jobs += viewModelScope.launch {
                runCatching { repo.listarAlumnosRemotos(tokenRemoto) }
                    .onSuccess {
                        alumnosRemotos = it
                        publicarAlumnos()
                    }
            }
        }
        if (rol == "ESTUDIANTE") {
            jobs += viewModelScope.launch { dao.observarMensajes(usuarioId).collectLatest { _estado.value = _estado.value.copy(mensajes = it) } }
            jobs += viewModelScope.launch { dao.observarTareas(usuarioId).collectLatest { _estado.value = _estado.value.copy(tareas = it) } }
            jobs += viewModelScope.launch { dao.observarMaterialCompartido(usuarioId).collectLatest { _estado.value = _estado.value.copy(materiales = it) } }
        }
    }

    fun agregarAlumno(profesorId: Int, codigo: String, tokenRemoto: String?) {
        ejecutar {
            val mensaje = repo.agregarAlumno(profesorId, codigo, tokenRemoto)
            if (tokenRemoto != null) {
                runCatching { repo.listarAlumnosRemotos(tokenRemoto) }
                    .onSuccess {
                        alumnosRemotos = it
                        publicarAlumnos()
                    }
            }
            mensaje
        }
    }
    fun enviarMensaje(profesorId: Int, codigo: String, texto: String) = ejecutar { repo.enviarMensaje(profesorId, codigo, texto) }
    fun compartirMaterial(profesorId: Int, codigo: String, documentoId: Int, tokenRemoto: String?) =
        ejecutar { repo.compartirMaterial(profesorId, codigo, documentoId, tokenRemoto) }
    fun asignarTarea(profesorId: Int, codigo: String, titulo: String, descripcion: String) = ejecutar { repo.asignarTarea(profesorId, codigo, titulo, descripcion) }

    private fun publicarAlumnos() {
        _estado.value = _estado.value.copy(
            alumnos = (alumnosLocales + alumnosRemotos).distinctBy(AlumnoAula::codigoPerfil),
        )
    }
    private fun ejecutar(accion: suspend () -> String) {
        viewModelScope.launch {
            runCatching { accion() }
                .onSuccess { _estado.value = _estado.value.copy(mensaje = it, error = false) }
                .onFailure { _estado.value = _estado.value.copy(mensaje = it.message ?: textos.get(R.string.aula_error_generico), error = true) }
        }
    }
}
