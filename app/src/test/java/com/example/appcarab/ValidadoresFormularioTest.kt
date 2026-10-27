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
}
