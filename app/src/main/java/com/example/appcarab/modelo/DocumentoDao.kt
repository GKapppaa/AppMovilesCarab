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

    @Query("SELECT * FROM documento WHERE usuarioId = :usuarioId ORDER BY creadoEn DESC")
    suspend fun documentosDeUsuario(usuarioId: Int): List<Documento>

    @Query("SELECT * FROM documento WHERE idRemoto = :idRemoto LIMIT 1")
    suspend fun buscarPorIdRemoto(idRemoto: String): Documento?

    @Query("SELECT * FROM documento WHERE id = :documentoId LIMIT 1")
    suspend fun buscarPorId(documentoId: Int): Documento?

    @Query("UPDATE documento SET idRemoto = :idRemoto, propietarioRemoto = :propietarioRemoto WHERE id = :documentoId")
    suspend fun vincularRemoto(documentoId: Int, idRemoto: String, propietarioRemoto: String)

    @Insert
    suspend fun insertar(documento: Documento): Long

    @Delete
    suspend fun eliminar(documento: Documento)

    @Query("UPDATE documento SET categoria = :categoria WHERE id = :documentoId")
    suspend fun actualizarCategoria(documentoId: Int, categoria: String)

    @Query("UPDATE documento SET textoExtraido = :texto WHERE id = :documentoId")
    suspend fun actualizarTextoExtraido(documentoId: Int, texto: String)
}
