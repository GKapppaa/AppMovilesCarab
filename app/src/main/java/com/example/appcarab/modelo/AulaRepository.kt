package com.example.appcarab.modelo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AulaRepository(
    private val aulaDao: AulaDao,
    private val usuarioDao: UsuarioDao,
    private val api: ApiRestRepository,
) {
    suspend fun verificarApi(): String {
        return api.health().fold(
            onSuccess = { "API conectada: ${it.service}" },
            onFailure = { "API no disponible; la app continúa en modo local." },
        )
    }

    suspend fun agregarAlumno(profesorId: Int, codigoAlumno: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase())
            ?: error("No existe un estudiante con ese ID.")
        check(alumno.id != profesorId) { "No puedes asignarte a tu propia sección." }
        val result = aulaDao.asignarAlumno(AlumnoProfesor(profesorId, alumno.id))
        if (result == -1L) "El estudiante ya pertenece a tu sección."
        else {
            val remote = api.sincronizar("secciones", "$profesorId-${alumno.id}", JSONObject()
                .put("profesorId", profesorId).put("alumnoId", alumno.id).put("codigoAlumno", alumno.codigoPerfil).toString())
            "${alumno.nombre} agregado. ${remote.mensaje}"
        }
    }

    suspend fun enviarMensaje(profesorId: Int, codigoAlumno: String, texto: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error("No existe un estudiante con ese ID.")
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { "Primero agrega al estudiante a tu sección." }
        require(ValidadoresFormulario.mensajeValido(texto)) { "El mensaje debe tener entre 1 y 500 caracteres." }
        val mensaje = MensajeDocente(profesorId = profesorId, alumnoId = alumno.id, texto = texto.trim())
        val id = aulaDao.enviarMensaje(mensaje)
        val remoto = api.sincronizar("mensajes", id.toString(), JSONObject()
            .put("profesorId", profesorId).put("alumnoId", alumno.id).put("texto", texto.trim()).toString())
        if (remoto.sincronizado) "Mensaje enviado y sincronizado." else "Mensaje guardado localmente. ${remoto.mensaje}"
    }

    suspend fun compartirMaterial(profesorId: Int, codigoAlumno: String, documentoId: Int): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error("No existe un estudiante con ese ID.")
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { "Primero agrega al estudiante a tu sección." }
        check(aulaDao.documentoDelProfesor(documentoId, profesorId)) { "Solo puedes compartir un documento propio." }
        val id = aulaDao.compartirMaterial(MaterialCompartido(profesorId = profesorId, alumnoId = alumno.id, documentoId = documentoId))
        val remoto = api.sincronizar("materiales", id.toString(), JSONObject()
            .put("profesorId", profesorId).put("alumnoId", alumno.id).put("documentoId", documentoId).toString())
        if (remoto.sincronizado) "Material compartido y sincronizado." else "Material compartido localmente. ${remoto.mensaje}"
    }

    suspend fun asignarTarea(profesorId: Int, codigoAlumno: String, titulo: String, descripcion: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error("No existe un estudiante con ese ID.")
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { "Primero agrega al estudiante a tu sección." }
        require(ValidadoresFormulario.tituloTareaValido(titulo)) { "El título debe tener entre 3 y 80 caracteres." }
        require(ValidadoresFormulario.descripcionTareaValida(descripcion)) { "La descripción debe tener entre 5 y 1000 caracteres." }
        val tarea = TareaDocente(profesorId = profesorId, alumnoId = alumno.id, titulo = titulo.trim(), descripcion = descripcion.trim())
        val id = aulaDao.crearTarea(tarea)
        val remoto = api.sincronizar("tareas", id.toString(), JSONObject()
            .put("profesorId", profesorId).put("alumnoId", alumno.id)
            .put("titulo", titulo.trim()).put("descripcion", descripcion.trim()).toString())
        if (remoto.sincronizado) "Tarea asignada y sincronizada." else "Tarea guardada localmente. ${remoto.mensaje}"
    }
}
