package com.example.appcarab.modelo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documento",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("usuarioId")],
)
data class Documento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val archivoInterno: String,
    val resumen: String,
    val creadoEn: Long = System.currentTimeMillis(),
)
