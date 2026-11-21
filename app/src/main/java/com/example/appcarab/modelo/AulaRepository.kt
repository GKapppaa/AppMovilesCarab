package com.example.appcarab.modelo

import com.example.appcarab.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class AulaRepository(
    private val aulaDao: AulaDao,
    private val usuarioDao: UsuarioDao,
    private val sincronizacion: SincronizacionPendiente,
    private val textos: Textos,
) {
    private fun estadoSync(remoto: ResultadoSync) =
        textos.get(if (remoto.sincronizado) R.string.sync_remoto else R.string.sync_pendiente)

    suspend fun agregarAlumno(profesorId: Int, codigoAlumno: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { textos.get(R.string.aula_solo_profesor) }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { textos.get(R.string.aula_formato_id) }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase())
            ?: error(textos.get(R.string.aula_estudiante_no_existe))
        check(alumno.id != profesorId) { textos.get(R.string.aula_no_autoasignar) }
        val result = aulaDao.asignarAlumno(AlumnoProfesor(profesorId, alumno.id))
        if (result == -1L) textos.get(R.string.aula_ya_vinculado)
        else {
            val remote = sincronizacion.sincronizar("secciones", "$profesorId-${alumno.id}", JSONObject()
                .put("profesorId", profesorId).put("alumnoId", alumno.id).put("codigoAlumno", alumno.codigoPerfil).toString())
            "${textos.get(R.string.aula_alumno_agregado, alumno.nombre)} ${estadoSync(remote)}"
        }
    }

    suspend fun enviarMensaje(profesorId: Int, codigoAlumno: String, texto: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { textos.get(R.string.aula_solo_profesor) }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { textos.get(R.string.aula_formato_id) }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error(textos.get(R.string.aula_estudiante_no_existe))
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { textos.get(R.string.aula_primero_vincular) }
        require(ValidadoresFormulario.mensajeValido(texto)) { textos.get(R.string.aula_mensaje_largo) }
        val mensaje = MensajeDocente(profesorId = profesorId, alumnoId = alumno.id, texto = texto.trim())
        val id = aulaDao.enviarMensaje(mensaje)
        val remoto = sincronizacion.sincronizar("mensajes", id.toString(), JSONObject()
            .put("profesorId", profesorId).put("alumnoId", alumno.id).put("texto", texto.trim()).toString())
        "${textos.get(R.string.aula_mensaje_enviado)} ${estadoSync(remoto)}"
    }

    suspend fun compartirMaterial(profesorId: Int, codigoAlumno: String, documentoId: Int): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { textos.get(R.string.aula_solo_profesor) }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { textos.get(R.string.aula_formato_id) }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error(textos.get(R.string.aula_estudiante_no_existe))
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { textos.get(R.string.aula_primero_vincular) }
        check(aulaDao.documentoDelProfesor(documentoId, profesorId)) { textos.get(R.string.aula_solo_documento_propio) }
        val id = aulaDao.compartirMaterial(MaterialCompartido(profesorId = profesorId, alumnoId = alumno.id, documentoId = documentoId))
        val remoto = sincronizacion.sincronizar("materiales", id.toString(), JSONObject()
            .put("profesorId", profesorId).put("alumnoId", alumno.id).put("documentoId", documentoId).toString())
        "${textos.get(R.string.aula_material_compartido)} ${estadoSync(remoto)}"
    }

    suspend fun asignarTarea(profesorId: Int, codigoAlumno: String, titulo: String, descripcion: String): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { textos.get(R.string.aula_solo_profesor) }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { textos.get(R.string.aula_formato_id) }
        val alumno = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase()) ?: error(textos.get(R.string.aula_estudiante_no_existe))
        check(aulaDao.alumnoAsignado(profesorId, alumno.id)) { textos.get(R.string.aula_primero_vincular) }
        require(ValidadoresFormulario.tituloTareaValido(titulo)) { textos.get(R.string.aula_titulo_largo) }
        require(ValidadoresFormulario.descripcionTareaValida(descripcion)) { textos.get(R.string.aula_descripcion_larga) }
        val tarea = TareaDocente(profesorId = profesorId, alumnoId = alumno.id, titulo = titulo.trim(), descripcion = descripcion.trim())
        val id = aulaDao.crearTarea(tarea)
        val remoto = sincronizacion.sincronizar("tareas", id.toString(), JSONObject()
            .put("profesorId", profesorId).put("alumnoId", alumno.id)
            .put("titulo", titulo.trim()).put("descripcion", descripcion.trim()).toString())
        "${textos.get(R.string.aula_tarea_asignada)} ${estadoSync(remoto)}"
    }
}
