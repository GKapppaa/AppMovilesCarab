package com.example.appcarab.modelo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AulaRepository(private val aulaDao: AulaDao, private val usuarioDao: UsuarioDao) {
    suspend fun agregarAlumno(profesorId: Int, codigoAlumno: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase())
            ?: error("No existe un estudiante con ese ID.")
        check(alumno.id != profesorId) { "No puedes asignarte a tu propia sección." }
        val result = aulaDao.asignarAlumno(AlumnoProfesor(profesorId, alumno.id))
        if (result == -1L) "El estudiante ya pertenece a tu sección." else "${alumno.nombre} agregado a tu sección."
    }

    suspend fun enviarMensaje(profesorId: Int, codigoAlumno: String, texto: String) = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error("No existe un estudiante con ese ID.")
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { "Primero agrega al estudiante a tu sección." }
        require(ValidadoresFormulario.mensajeValido(texto)) { "El mensaje debe tener entre 1 y 500 caracteres." }
        aulaDao.enviarMensaje(MensajeDocente(profesorId = profesorId, alumnoId = alumno.id, texto = texto.trim()))
    }

    suspend fun compartirMaterial(profesorId: Int, codigoAlumno: String, documentoId: Int) = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error("No existe un estudiante con ese ID.")
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { "Primero agrega al estudiante a tu sección." }
        check(aulaDao.documentoDelProfesor(documentoId, profesorId)) { "Solo puedes compartir un documento propio." }
        aulaDao.compartirMaterial(MaterialCompartido(profesorId = profesorId, alumnoId = alumno.id, documentoId = documentoId))
    }

    suspend fun asignarTarea(profesorId: Int, codigoAlumno: String, titulo: String, descripcion: String) = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { "Esta acción es solo para perfiles de profesor." }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { "El ID debe tener formato EST-######." }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error("No existe un estudiante con ese ID.")
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { "Primero agrega al estudiante a tu sección." }
        require(ValidadoresFormulario.tituloTareaValido(titulo)) { "El título debe tener entre 3 y 80 caracteres." }
        require(ValidadoresFormulario.descripcionTareaValida(descripcion)) { "La descripción debe tener entre 5 y 1000 caracteres." }
        aulaDao.crearTarea(TareaDocente(profesorId = profesorId, alumnoId = alumno.id, titulo = titulo.trim(), descripcion = descripcion.trim()))
    }
}
