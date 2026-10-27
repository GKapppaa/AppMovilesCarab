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
    entities = [Usuario::class, Documento::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {

    // Proporciona acceso a las operaciones del DAO.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nombre del archivo de la base de datos en el teléfono
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE usuario_nuevo (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        nombre TEXT NOT NULL DEFAULT 'Estudiante',
                        email TEXT NOT NULL,
                        password TEXT NOT NULL,
                        passwordSalt TEXT NOT NULL,
                        role TEXT NOT NULL
                    )""".trimIndent()
                )
                db.execSQL(
                    """INSERT INTO usuario_nuevo (id, nombre, email, password, passwordSalt, role)
                        SELECT id, 'Estudiante', email, password, passwordSalt, role FROM usuario""".trimIndent()
                )
                db.execSQL("DROP TABLE usuario")
                db.execSQL("ALTER TABLE usuario_nuevo RENAME TO usuario")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_usuario_email ON usuario(email)")
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS documento (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        usuarioId INTEGER NOT NULL,
                        nombre TEXT NOT NULL,
                        archivoInterno TEXT NOT NULL,
                        resumen TEXT NOT NULL,
                        creadoEn INTEGER NOT NULL,
                        FOREIGN KEY(usuarioId) REFERENCES usuario(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )""".trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_documento_usuarioId ON documento(usuarioId)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE usuario ADD COLUMN codigoPerfil TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE usuario SET codigoPerfil = CASE WHEN role = 'PROFESOR' THEN 'PROF-' ELSE 'EST-' END || printf('%06d', id)")
            }
        }
    }
}
