package com.example.appcarab

import com.example.appcarab.modelo.OpenLibraryDoc
import com.example.appcarab.modelo.OpenLibraryRepository
import com.example.appcarab.modelo.OpenLibrarySearchResponse
import com.example.appcarab.modelo.OpenLibraryService
import com.example.appcarab.modelo.Textos
import com.example.appcarab.vistaModelo.OpenLibraryVistaModelo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenLibraryTest {

    class FakeOpenLibraryService : OpenLibraryService {
        var queryRecibida: String? = null
        var lanzarError = false
        var docsADevolver = listOf(
            OpenLibraryDoc(
                key = "/works/OL12345W",
                title = "Derecho Penal Chileno",
                authorName = listOf("Juan Pérez"),
                firstPublishYear = 2020,
                coverI = 999L,
                subject = listOf("Derecho", "Leyes"),
            ),
        )

        override suspend fun buscarLibros(query: String, limit: Int, fields: String): OpenLibrarySearchResponse {
            queryRecibida = query
            if (lanzarError) throw IllegalStateException("Error de red simulado")
            return OpenLibrarySearchResponse(numFound = docsADevolver.size, docs = docsADevolver)
        }
    }

    class FakeTextos : Textos {
        override fun get(id: Int, vararg args: Any): String = "Error simulado"
    }

    @Test
    fun repository_buscarLibros_devuelveListaFormateada() = runBlocking {
        val service = FakeOpenLibraryService()
        val repository = OpenLibraryRepository(service)

        val resultado = repository.buscarLibros("Derecho")

        assertEquals(1, resultado.size)
        val libro = resultado.first()
        assertEquals("/works/OL12345W", libro.key)
        assertEquals("Derecho Penal Chileno", libro.titulo)
        assertEquals("Juan Pérez", libro.autores)
        assertEquals(2020, libro.anoPublicacion)
        assertEquals("https://covers.openlibrary.org/b/id/999-M.jpg", libro.urlPortada)
        assertEquals("https://openlibrary.org/works/OL12345W", libro.urlWeb)
    }

    @Test
    fun repository_queryVacio_retornaListaVaciaSinLlamarService() = runBlocking {
        val service = FakeOpenLibraryService()
        val repository = OpenLibraryRepository(service)

        val resultado = repository.buscarLibros("   ")

        assertTrue(resultado.isEmpty())
        assertNull(service.queryRecibida)
    }

    @Test
    fun viewModel_buscarExitoso_actualizaEstadoCorrectamente() = runBlocking {
        val service = FakeOpenLibraryService()
        val repository = OpenLibraryRepository(service)
        val viewModel = OpenLibraryVistaModelo(repository, FakeTextos(), this)

        viewModel.cambiarQuery("Seguridad")
        viewModel.buscar("Seguridad").join()

        val estado = viewModel.estado.value
        assertEquals("Seguridad", estado.query)
        assertFalse(estado.cargando)
        assertTrue(estado.busquedaRealizada)
        assertNull(estado.mensajeError)
        assertEquals(1, estado.libros.size)
    }

    @Test
    fun viewModel_buscarError_actualizaMensajeError() = runBlocking {
        val service = FakeOpenLibraryService().apply { lanzarError = true }
        val repository = OpenLibraryRepository(service)
        val viewModel = OpenLibraryVistaModelo(repository, FakeTextos(), this)

        viewModel.buscar("Criminología").join()

        val estado = viewModel.estado.value
        assertFalse(estado.cargando)
        assertTrue(estado.busquedaRealizada)
        assertEquals("Error de red simulado", estado.mensajeError)
        assertTrue(estado.libros.isEmpty())
    }
}
