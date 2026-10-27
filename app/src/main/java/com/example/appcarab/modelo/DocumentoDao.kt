package com.example.appcarab.modelo

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
    @Query("SELECT * FROM documento WHERE usuarioId = :usuarioId ORDER BY creadoEn DESC")
    fun observarDocumentos(usuarioId: Int): Flow<List<Documento>>

    @Insert
    suspend fun insertar(documento: Documento): Long

    @Delete
    suspend fun eliminar(documento: Documento)
}
