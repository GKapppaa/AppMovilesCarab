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
    private val documentosDao: DocumentoDao,
    private val documentosRemotos: ApiDocumentosRepository,
) {
    private fun estadoSync(remoto: ResultadoSync) =
        textos.get(if (remoto.sincronizado) R.string.sync_remoto else R.string.sync_pendiente)

    suspend fun agregarAlumno(profesorId: Int, codigoAlumno: String, token: String?): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { textos.get(R.string.aula_solo_profesor) }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { textos.get(R.string.aula_formato_id) }
        val codigo = codigoAlumno.trim().uppercase()
        val remoteToken = token ?: error(textos.get(R.string.sesion_remota_requerida))
        val alumnoRemoto = documentosRemotos.vincularAlumno(remoteToken, codigo)
        val alumnoLocal = aulaDao.buscarAlumno(codigo)
        if (alumnoLocal != null && alumnoLocal.id != profesorId) {
            aulaDao.asignarAlumno(AlumnoProfesor(profesorId, alumnoLocal.id))
        }
        val remote = sincronizacion.sincronizar(
            "secciones",
            "$profesorId-$codigo",
            JSONObject().put("profesorId", profesorId).put("codigoAlumno", codigo).toString(),
        )
        "${textos.get(R.string.aula_alumno_agregado, alumnoRemoto.nombre)} ${estadoSync(remote)}"
    }

    suspend fun listarAlumnosRemotos(token: String): List<AlumnoAula> =
        documentosRemotos.listarAlumnos(token).map { AlumnoAula(it.codigoPerfil, it.nombre) }

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

    suspend fun compartirMaterial(
        profesorId: Int,
        codigoAlumno: String,
        documentoId: Int,
        token: String?,
    ): String = withContext(Dispatchers.IO) {
        check(usuarioDao.buscarPorId(profesorId)?.role == "PROFESOR") { textos.get(R.string.aula_solo_profesor) }
        require(ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) { textos.get(R.string.aula_formato_id) }
        val remoteToken = token ?: error(textos.get(R.string.sesion_remota_requerida))
        val documento = documentosDao.buscarPorId(documentoId)
            ?: error(textos.get(R.string.aula_solo_documento_propio))
        check(documento.usuarioId == profesorId) { textos.get(R.string.aula_solo_documento_propio) }
        val idRemoto = documento.idRemoto
            ?: error(textos.get(R.string.aula_pdf_debe_subirse))
        documentosRemotos.compartir(remoteToken, idRemoto, codigoAlumno.trim().uppercase())
        val alumnoLocal = aulaDao.buscarAlumno(codigoAlumno.trim().uppercase())
        if (alumnoLocal != null && aulaDao.alumnoAsignado(profesorId, alumnoLocal.id)) {
            val id = aulaDao.compartirMaterial(
                MaterialCompartido(
                    profesorId = profesorId,
                    alumnoId = alumnoLocal.id,
                    documentoId = documentoId,
                ),
            )
            sincronizacion.sincronizar(
                "materiales",
                id.toString(),
                JSONObject()
                    .put("profesorId", profesorId)
                    .put("codigoAlumno", codigoAlumno.trim().uppercase())
                    .put("documentoId", documentoId)
                    .put("documentoRemotoId", idRemoto)
                    .toString(),
            )
        }
        textos.get(R.string.aula_material_compartido)
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
