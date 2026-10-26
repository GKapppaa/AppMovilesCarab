package com.example.appcarab.modelo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UsuarioDao {

    // Inserta una persona en la tabla.
    @Insert
    suspend fun insertar(usuario: Usuario)

    // Recupera todas las personas.
    @Query("SELECT * FROM usuario")
    suspend fun listar(): List<Usuario>

    // Función clave para validar el inicio de sesión para el login
    @Query("SELECT * FROM usuario WHERE email = :email AND password = :password LIMIT 1")
    suspend fun validarLogin(email: String, password: String): Usuario?
}