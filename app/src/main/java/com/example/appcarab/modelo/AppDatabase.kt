package com.example.appcarab.modelo

import androidx.room.Database
import androidx.room.RoomDatabase

// Define las entidades y la versión de la base de datos.
@Database(
    entities = [Usuario::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    // Proporciona acceso a las operaciones del DAO.
    abstract fun usuarioDao(): UsuarioDao
}