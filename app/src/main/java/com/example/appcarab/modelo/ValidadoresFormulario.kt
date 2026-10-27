package com.example.appcarab.modelo

import android.util.Patterns

/** Reglas compartidas por registro, inicio de sesión y edición de perfil. */
object ValidadoresFormulario {
    fun nombreValido(valor: String): Boolean = valor.trim().length >= 3
    fun correoValido(valor: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(valor.trim()).matches()
    fun contrasenaValida(valor: String): Boolean =
        valor.length >= 8 && valor.any(Char::isLetter) && valor.any(Char::isDigit)

    fun contrasenasCoinciden(contrasena: String, confirmacion: String): Boolean =
        contrasena == confirmacion

    fun codigoProfesorValido(codigo: String): Boolean = codigo.trim() == CODIGO_PROFESOR_DEMO

    const val CODIGO_PROFESOR_DEMO = "DOCENTE-DEMO-2026"
}
