package com.example.appcarab.modelo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AulaDao {
    @Query("SELECT usuario.* FROM usuario INNER JOIN alumno_profesor ON usuario.id = alumno_profesor.alumnoId WHERE alumno_profesor.profesorId = :profesorId ORDER BY usuario.nombre")
    fun observarAlumnos(profesorId: Int): Flow<List<Usuario>>

    @Query("SELECT * FROM mensaje_docente WHERE alumnoId = :alumnoId ORDER BY enviadoEn DESC")
    fun observarMensajes(alumnoId: Int): Flow<List<MensajeDocente>>

    @Query("SELECT * FROM tarea_docente WHERE alumnoId = :alumnoId ORDER BY creadaEn DESC")
    fun observarTareas(alumnoId: Int): Flow<List<TareaDocente>>

    @Query("SELECT documento.* FROM documento INNER JOIN material_compartido ON documento.id = material_compartido.documentoId WHERE material_compartido.alumnoId = :alumnoId ORDER BY material_compartido.compartidoEn DESC")
    fun observarMaterialCompartido(alumnoId: Int): Flow<List<Documento>>

    @Query("SELECT * FROM usuario WHERE codigoPerfil = :codigo AND role = 'ESTUDIANTE' LIMIT 1")
    suspend fun buscarAlumno(codigo: String): Usuario?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun asignarAlumno(relacion: AlumnoProfesor): Long

    @Insert
    suspend fun enviarMensaje(mensaje: MensajeDocente)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun compartirMaterial(material: MaterialCompartido): Long

    @Insert
    suspend fun crearTarea(tarea: TareaDocente)

    @Query("SELECT EXISTS(SELECT 1 FROM documento WHERE id = :documentoId AND usuarioId = :profesorId)")
    suspend fun documentoDelProfesor(documentoId: Int, profesorId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM alumno_profesor WHERE profesorId = :profesorId AND alumnoId = :alumnoId)")
    suspend fun alumnoAsignado(profesorId: Int, alumnoId: Int): Boolean
}
