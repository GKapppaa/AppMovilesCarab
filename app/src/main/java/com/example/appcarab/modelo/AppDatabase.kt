package com.example.appcarab.modelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
//donde saque esto
//https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?hl=es-419#6

// Define las entidades y la versión de la base de datos.
@Database(
    entities = [Usuario::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {

    // Proporciona acceso a las operaciones del DAO.
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nombre del archivo de la base de datos en el teléfono
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}