package com.example.appcarab

import com.example.appcarab.modelo.ActualizarRegistroRequest
import com.example.appcarab.modelo.ApiRestRepository
import com.example.appcarab.modelo.EsfocarApi
import com.example.appcarab.modelo.RegistroApi
import com.example.appcarab.modelo.RegistroApiRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class ApiRestRepositoryTest {

    // API falsa: cuenta las llamadas y devuelve lo que le indicamos desde cada prueba.
    class FakeEsfocarApi : EsfocarApi {
        var llamadasPost = 0
        var llamadasPut = 0
        var llamadasGet = 0
        var responderCon409 = false
        var lanzarError = false
        var lanzarCancelacion = false
        var contenidoDevuelto = ""

        override suspend fun crear(tipo: String, body: RegistroApiRequest): RegistroApi {
            llamadasPost++
            if (lanzarCancelacion) throw CancellationException("cancelado")
            if (lanzarError) throw IllegalStateException("sin conexión")
            if (responderCon409) {
                val respuesta = Response.error<RegistroApi>(409, "conflicto".toResponseBody())
                throw HttpException(respuesta)
            }
            return RegistroApi(1L, tipo, body.clave, body.contenido)
        }

        override suspend fun actualizar(
            tipo: String,
            clave: String,
            body: ActualizarRegistroRequest,
        ): RegistroApi {
            llamadasPut++
            return RegistroApi(1L, tipo, clave, body.contenido)
        }

        override suspend fun obtener(tipo: String, clave: String): RegistroApi {
            llamadasGet++
            return RegistroApi(1L, tipo, clave, contenidoDevuelto)
        }

        override suspend fun eliminar(tipo: String, clave: String) {
            // No hace falta para estas pruebas.
        }
    }

    @Test
    fun sincronizaCorrectamenteConPost() = runBlocking {
        val api = FakeEsfocarApi()
        val contenido = "datos del perfil"
        api.contenidoDevuelto = contenido
        val repositorio = ApiRestRepository(api)

        val resultado = repositorio.sincronizar("perfiles", "EST-000123", contenido)

        assertTrue(resultado.sincronizado)
        assertEquals("Sincronizado con el servicio remoto.", resultado.mensaje)
        assertEquals(1, api.llamadasPost)
        assertEquals(0, api.llamadasPut)
        assertEquals(1, api.llamadasGet)
    }

    @Test
    fun actualizaCuandoElServidorRespondeConflicto409() = runBlocking {
        val api = FakeEsfocarApi()
        val contenido = "datos actualizados"
        api.responderCon409 = true
        api.contenidoDevuelto = contenido
        val repositorio = ApiRestRepository(api)

        val resultado = repositorio.sincronizar("perfiles", "EST-000124", contenido)

        assertTrue(resultado.sincronizado)
        assertEquals(1, api.llamadasPost)
        assertEquals(1, api.llamadasPut)
        assertEquals(1, api.llamadasGet)
    }

    @Test
    fun marcaComoNoSincronizadoSiElContenidoVerificadoNoCoincide() = runBlocking {
        val api = FakeEsfocarApi()
        api.contenidoDevuelto = "contenido distinto"
        val repositorio = ApiRestRepository(api)

        val resultado = repositorio.sincronizar("perfiles", "EST-000125", "contenido original")

        assertFalse(resultado.sincronizado)
        assertEquals(0, api.llamadasPut)
        // El mensaje sigue diciendo "Sincronizado" aunque la verificación no coincida.
        assertEquals("Sincronizado con el servicio remoto.", resultado.mensaje)
    }

    @Test
    fun guardaLocalmenteSiLaApiFalla() = runBlocking {
        val api = FakeEsfocarApi()
        api.lanzarError = true
        val repositorio = ApiRestRepository(api)

        val resultado = repositorio.sincronizar("perfiles", "EST-000126", "contenido")

        assertFalse(resultado.sincronizado)
        assertEquals("Guardado localmente; se sincronizará al volver la conexión.", resultado.mensaje)
        assertEquals(0, api.llamadasGet)
    }


}
