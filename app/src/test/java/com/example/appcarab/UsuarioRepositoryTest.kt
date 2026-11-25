package com.example.appcarab

import com.example.appcarab.modelo.UsuarioRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UsuarioRepositoryTest {

    @Test
    fun registraEstudianteYGeneraCodigoEST() = runBlocking {
        val dao = FakeUsuarioDao()
        val sincronizador = FakeSincronizador()
        val repositorio = UsuarioRepository(dao, sincronizador)

        val usuario = repositorio.registrarUsuario(
            "Ana Pérez",
            "  ANA@Correo.com  ",
            "ClaveSegura1!",
            "ESTUDIANTE",
        )

        assertTrue(usuario != null)
        assertEquals("Ana Pérez", usuario!!.nombre)
        assertEquals("ana@correo.com", usuario.email) // el correo se guarda en minúsculas
        assertEquals("ESTUDIANTE", usuario.role)
        assertTrue(usuario.codigoPerfil.startsWith("EST-"))
        assertEquals(1, dao.usuarios.size)
        assertEquals("perfiles", sincronizador.tipos[0])
        assertEquals(usuario.codigoPerfil, sincronizador.claves[0])
    }

    @Test
    fun registraProfesorYGeneraCodigoPROF() = runBlocking {
        val dao = FakeUsuarioDao()
        val sincronizador = FakeSincronizador()
        val repositorio = UsuarioRepository(dao, sincronizador)

        val usuario = repositorio.registrarUsuario(
            "Luis Gómez",
            "luis@correo.com",
            "ClaveSegura1!",
            "PROFESOR",
        )

        assertTrue(usuario != null)
        assertEquals("PROFESOR", usuario!!.role)
        assertTrue(usuario.codigoPerfil.startsWith("PROF-"))
        assertEquals(1, dao.usuarios.size)
        assertEquals(1, sincronizador.llamadas)
    }

    @Test
    fun rechazaRegistroCuandoElCorreoYaExiste() = runBlocking {
        val dao = FakeUsuarioDao()
        val sincronizador = FakeSincronizador()
        val repositorio = UsuarioRepository(dao, sincronizador)
        repositorio.registrarUsuario("Ana Pérez", "ana@correo.com", "ClaveSegura1!", "ESTUDIANTE")
        sincronizador.llamadas = 0

        val otro = repositorio.registrarUsuario(
            "Ana Otro",
            "  ANA@CORREO.COM ",
            "ClaveSegura1!",
            "ESTUDIANTE",
        )

        assertTrue(otro == null)
        assertEquals(1, dao.usuarios.size) // no se guardó el segundo
        assertEquals(0, sincronizador.llamadas) // tampoco se sincronizó
    }

    @Test
    fun iniciaSesionConContrasenaCorrecta() = runBlocking {
        val dao = FakeUsuarioDao()
        val repositorio = UsuarioRepository(dao, FakeSincronizador())
        val creado = repositorio.registrarUsuario(
            "Ana Pérez",
            "ana@correo.com",
            "ClaveSegura1!",
            "ESTUDIANTE",
        )
        dao.consultasPorEmail = 0 // contamos solo la búsqueda del inicio de sesión

        val usuario = repositorio.validarSesion("  ANA@CORREO.COM ", "ClaveSegura1!")

        assertTrue(usuario != null)
        assertEquals(creado!!.id, usuario!!.id)
        assertEquals("ana@correo.com", usuario.email)
        assertEquals(1, dao.consultasPorEmail)
    }

    @Test
    fun rechazaInicioDeSesionConContrasenaIncorrecta() = runBlocking {
        val repositorio = UsuarioRepository(FakeUsuarioDao(), FakeSincronizador())
        repositorio.registrarUsuario("Ana Pérez", "ana@correo.com", "ClaveSegura1!", "ESTUDIANTE")

        val usuario = repositorio.validarSesion("ana@correo.com", "OtraClave1!")

        assertTrue(usuario == null)
    }

    @Test
    fun rechazaInicioDeSesionSiElCorreoNoExiste() = runBlocking {
        val repositorio = UsuarioRepository(FakeUsuarioDao(), FakeSincronizador())

        val usuario = repositorio.validarSesion("noexiste@correo.com", "ClaveSegura1!")

        assertTrue(usuario == null)
    }

    @Test
    fun actualizaPerfilYCambiaNombreYCorreo() = runBlocking {
        val dao = FakeUsuarioDao()
        val sincronizador = FakeSincronizador()
        val repositorio = UsuarioRepository(dao, sincronizador)
        val creado = repositorio.registrarUsuario(
            "Ana Pérez",
            "ana@correo.com",
            "ClaveSegura1!",
            "ESTUDIANTE",
        )!!
        sincronizador.llamadas = 0 // contamos solo la sincronización del cambio de perfil

        val actualizado = repositorio.actualizarPerfil(
            creado.id,
            "  Ana María Pérez  ",
            "  NUEVA@Correo.com ",
        )

        assertTrue(actualizado != null)
        assertEquals("Ana María Pérez", actualizado!!.nombre)
        assertEquals("nueva@correo.com", actualizado.email)
        assertEquals("Ana María Pérez", dao.usuarios[0].nombre)
        assertEquals("nueva@correo.com", dao.usuarios[0].email)
        assertEquals(1, dao.consultasPorId)
        assertEquals(1, sincronizador.llamadas)
    }

    @Test
    fun rechazaActualizarPerfilSiElCorreoEsDeOtroUsuario() = runBlocking {
        val dao = FakeUsuarioDao()
        val repositorio = UsuarioRepository(dao, FakeSincronizador())
        val ana = repositorio.registrarUsuario(
            "Ana Pérez",
            "ana@correo.com",
            "ClaveSegura1!",
            "ESTUDIANTE",
        )!!
        repositorio.registrarUsuario("Luis Gómez", "luis@correo.com", "ClaveSegura1!", "ESTUDIANTE")

        val resultado = repositorio.actualizarPerfil(ana.id, "Ana Pérez", "LUIS@correo.com")

        assertTrue(resultado == null)
        assertEquals("ana@correo.com", dao.usuarios[0].email) // sigue con su correo
        assertFalse(dao.usuarios[1].email == "ana@correo.com")
    }
}
