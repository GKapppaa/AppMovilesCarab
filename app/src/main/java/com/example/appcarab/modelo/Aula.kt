package com.example.appcarab.modelo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alumno_profesor",
    primaryKeys = ["profesorId", "alumnoId"],
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["profesorId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["alumnoId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("alumnoId")],
)
data class AlumnoProfesor(val profesorId: Int, val alumnoId: Int, val asignadoEn: Long = System.currentTimeMillis())

@Entity(
    tableName = "mensaje_docente",
    foreignKeys = [ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["alumnoId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("alumnoId")],
)
data class MensajeDocente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profesorId: Int,
    val alumnoId: Int,
    val texto: String,
    val enviadoEn: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "material_compartido",
    foreignKeys = [
        ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["alumnoId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Documento::class, parentColumns = ["id"], childColumns = ["documentoId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("alumnoId"), Index("documentoId")],
)
data class MaterialCompartido(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profesorId: Int,
    val alumnoId: Int,
    val documentoId: Int,
    val compartidoEn: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "tarea_docente",
    foreignKeys = [ForeignKey(entity = Usuario::class, parentColumns = ["id"], childColumns = ["alumnoId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("alumnoId")],
)
data class TareaDocente(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profesorId: Int,
    val alumnoId: Int,
    val titulo: String,
    val descripcion: String,
    val creadaEn: Long = System.currentTimeMillis(),
)
