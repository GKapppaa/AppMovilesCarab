package com.example.appcarab

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appcarab.modelo.AppDatabase
import com.example.appcarab.modelo.Usuario
import com.example.appcarab.modelo.UsuarioDao
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UsuarioDaoInstrumentedTest {
    private lateinit var db: AppDatabase
    private val dao: UsuarioDao get() = db.usuarioDao()

    @Before
    fun abrirBase() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).allowMainThreadQueries().build()
    }

    @After
    fun cerrarBase() {
        db.close()
    }
    @Test
    fun insertarYBuscarPorEmail() = runBlocking {
        // Arrange
        val usuario = Usuario(nombre = "Ana", email = "ana@correo.com", passwordHash = "clave", passwordSalt = "sal")
        // Act
        dao.insertar(usuario)
        val encontrado = dao.buscarPorEmail("ana@correo.com")
        // Assert
        assertEquals("Ana", encontrado?.nombre)
        assertEquals("ana@correo.com", encontrado?.email)
    }
    @Test
    fun buscarCorreoQueNoExiste() = runBlocking {
        // Arrange
        val correo = "nadie@correo.com"
        // Act
        val encontrado = dao.buscarPorEmail(correo)
        // Assert
        assertNull(encontrado)
    }
    @Test
    fun actualizarDatosDelUsuario() = runBlocking {
        // Arrange
        val id = dao.insertar(Usuario(nombre = "Ana", email = "ana@correo.com", passwordHash = "clave", passwordSalt = "sal")).toInt()
        val original = dao.buscarPorId(id)!!
        // Act
        dao.actualizar(original.copy(nombre = "Ana María", email = "ana.maria@correo.com"))
        // Assert
        assertEquals("Ana María", dao.buscarPorId(id)?.nombre)
        assertEquals("ana.maria@correo.com", dao.buscarPorId(id)?.email)
    }
}
