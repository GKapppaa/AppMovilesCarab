package com.example.appcarab

import com.example.appcarab.modelo.ValidadoresFormulario
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadoresFormularioTest {
    @Test
    fun validaNombresCompletosYRechazaNumerosOSimbolos() {
        assertTrue(ValidadoresFormulario.nombreValido("María José Pérez"))
        assertTrue(ValidadoresFormulario.nombreValido("Juan Pérez-Gómez"))
        assertFalse(ValidadoresFormulario.nombreValido("Juan"))
        assertFalse(ValidadoresFormulario.nombreValido("Juan 123"))
        assertFalse(ValidadoresFormulario.nombreValido("Juan @ Pérez"))
    }

    @Test
    fun contrasenaExigeMayusculaMinusculaNumeroYCaracterEspecial() {
        assertTrue(ValidadoresFormulario.contrasenaValida("ClaveSegura1!"))
        assertFalse(ValidadoresFormulario.contrasenaValida("clavesegura1!"))
        assertFalse(ValidadoresFormulario.contrasenaValida("ClaveSegura!!"))
        assertFalse(ValidadoresFormulario.contrasenaValida("ClaveSegura1"))
        assertFalse(ValidadoresFormulario.contrasenaValida("C1! a"))
    }

    @Test
    fun validaIdsMensajesYCamposDeTareas() {
        assertTrue(ValidadoresFormulario.codigoAlumnoValido("EST-000123"))
        assertFalse(ValidadoresFormulario.codigoAlumnoValido("PROF-000123"))
        assertTrue(ValidadoresFormulario.mensajeValido("Revisa el material de la próxima clase."))
        assertFalse(ValidadoresFormulario.mensajeValido(" "))
        assertTrue(ValidadoresFormulario.tituloTareaValido("Repaso unidad uno"))
        assertFalse(ValidadoresFormulario.tituloTareaValido("ok"))
        assertTrue(ValidadoresFormulario.descripcionTareaValida("Resolver y comentar el caso práctico de la unidad."))
        assertFalse(ValidadoresFormulario.descripcionTareaValida("abc"))
    }
}
