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
    entities = [Usuario::class, Documento::class, AlumnoProfesor::class, MensajeDocente::class, MaterialCompartido::class, TareaDocente::class],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {

    // Proporciona acceso a las operaciones del DAO.
    abstract fun usuarioDao(): UsuarioDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun aulaDao(): AulaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Nombre del archivo de la base de datos en el teléfono
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8).build()
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

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE documento ADD COLUMN categoria TEXT NOT NULL DEFAULT 'General'")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_usuario_codigoPerfil ON usuario(codigoPerfil)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS alumno_profesor (profesorId INTEGER NOT NULL, alumnoId INTEGER NOT NULL, asignadoEn INTEGER NOT NULL, PRIMARY KEY(profesorId, alumnoId), FOREIGN KEY(profesorId) REFERENCES usuario(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(alumnoId) REFERENCES usuario(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_alumno_profesor_alumnoId ON alumno_profesor(alumnoId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS mensaje_docente (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, profesorId INTEGER NOT NULL, alumnoId INTEGER NOT NULL, texto TEXT NOT NULL, enviadoEn INTEGER NOT NULL, FOREIGN KEY(alumnoId) REFERENCES usuario(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_mensaje_docente_alumnoId ON mensaje_docente(alumnoId)")
                db.execSQL("CREATE TABLE IF NOT EXISTS material_compartido (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, profesorId INTEGER NOT NULL, alumnoId INTEGER NOT NULL, documentoId INTEGER NOT NULL, compartidoEn INTEGER NOT NULL, FOREIGN KEY(alumnoId) REFERENCES usuario(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(documentoId) REFERENCES documento(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_material_compartido_alumnoId ON material_compartido(alumnoId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_material_compartido_documentoId ON material_compartido(documentoId)")
            }
        }

        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS tarea_docente (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, profesorId INTEGER NOT NULL, alumnoId INTEGER NOT NULL, titulo TEXT NOT NULL, descripcion TEXT NOT NULL, creadaEn INTEGER NOT NULL, FOREIGN KEY(alumnoId) REFERENCES usuario(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tarea_docente_alumnoId ON tarea_docente(alumnoId)")
            }
        }
    }
}
