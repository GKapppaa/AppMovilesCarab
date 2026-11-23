package com.example.appcarab.modelo

import androidx.room.Entity
import androidx.room.ColumnInfo
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
    indices = [Index("usuarioId"), Index(value = ["idRemoto"], unique = true)],
)
data class Documento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val archivoInterno: String,
    val resumen: String,
    @ColumnInfo(defaultValue = "''") val textoExtraido: String = "",
    @ColumnInfo(defaultValue = "'General'") val categoria: String = "General",
    @ColumnInfo(defaultValue = "NULL") val idRemoto: String? = null,
    @ColumnInfo(defaultValue = "NULL") val propietarioRemoto: String? = null,
    val creadoEn: Long = System.currentTimeMillis(),
)
