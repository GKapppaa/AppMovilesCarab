package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.modelo.AulaDao
import com.example.appcarab.modelo.AulaRepository
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.MensajeDocente
import com.example.appcarab.modelo.TareaDocente
import com.example.appcarab.modelo.Usuario
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class EstadoAula(
    val alumnos: List<Usuario> = emptyList(),
    val mensajes: List<MensajeDocente> = emptyList(),
    val tareas: List<TareaDocente> = emptyList(),
    val materiales: List<Documento> = emptyList(),
    val mensaje: String? = null,
    val error: Boolean = false,
)

class AulaVistaModelo(private val repo: AulaRepository, private val dao: AulaDao) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoAula())
    val estado: StateFlow<EstadoAula> = _estado.asStateFlow()
    private val jobs = mutableListOf<Job>()

    fun observar(usuarioId: Int, rol: String) {
        jobs.forEach(Job::cancel)
        jobs.clear()
        _estado.value = EstadoAula()
        val flujo = if (rol == "PROFESOR") dao.observarAlumnos(usuarioId) else null
        if (flujo != null) jobs += viewModelScope.launch { flujo.collectLatest { _estado.value = _estado.value.copy(alumnos = it) } }
        if (rol == "ESTUDIANTE") {
            jobs += viewModelScope.launch { dao.observarMensajes(usuarioId).collectLatest { _estado.value = _estado.value.copy(mensajes = it) } }
            jobs += viewModelScope.launch { dao.observarTareas(usuarioId).collectLatest { _estado.value = _estado.value.copy(tareas = it) } }
            jobs += viewModelScope.launch { dao.observarMaterialCompartido(usuarioId).collectLatest { _estado.value = _estado.value.copy(materiales = it) } }
        }
    }

    fun agregarAlumno(profesorId: Int, codigo: String) = ejecutar { repo.agregarAlumno(profesorId, codigo) }
    fun enviarMensaje(profesorId: Int, codigo: String, texto: String) = ejecutar { repo.enviarMensaje(profesorId, codigo, texto); "Mensaje enviado." }
    fun compartirMaterial(profesorId: Int, codigo: String, documentoId: Int) = ejecutar { repo.compartirMaterial(profesorId, codigo, documentoId); "Material compartido." }
    fun asignarTarea(profesorId: Int, codigo: String, titulo: String, descripcion: String) = ejecutar { repo.asignarTarea(profesorId, codigo, titulo, descripcion); "Tarea asignada." }

    private fun ejecutar(accion: suspend () -> String) {
        viewModelScope.launch {
            runCatching { accion() }
                .onSuccess { _estado.value = _estado.value.copy(mensaje = it, error = false) }
                .onFailure { _estado.value = _estado.value.copy(mensaje = it.message ?: "No se pudo completar la acción.", error = true) }
        }
    }
}
