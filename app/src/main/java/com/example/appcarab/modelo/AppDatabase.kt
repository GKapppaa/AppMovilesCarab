package com.example.appcarab.modelo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
//donde saque esto
//https://developer.android.com/codelabs/basic-android-kotlin-compose-persisting-data-room?hl=es-419#6

// Define las entidades y la versión de la base de datos.
@Database(
    entities = [Usuario::class],
    version = 2
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
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuario ADD COLUMN passwordSalt TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_usuario_email ON usuario(email)")
            }
        }
    }
}
