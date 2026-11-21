package com.example.appcarab.modelo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(usuario: Usuario): Long

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :usuarioId LIMIT 1")
    suspend fun buscarPorId(usuarioId: Int): Usuario?

    @Query("UPDATE usuario SET nombre = :nombre, email = :email WHERE id = :usuarioId")
    suspend fun actualizarPerfil(usuarioId: Int, nombre: String, email: String): Int
}
