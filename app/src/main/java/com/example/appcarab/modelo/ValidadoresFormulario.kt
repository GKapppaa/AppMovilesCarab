package com.example.appcarab.modelo

/** Reglas compartidas por registro, inicio de sesión y edición de perfil. */
object ValidadoresFormulario {
    private val palabraNombre = Regex("\\p{L}[\\p{L}\\p{M}]*(?:[-'’][\\p{L}\\p{M}]+)*")
    private val correo = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)

    fun nombreValido(valor: String): Boolean {
        val partes = valor.trim().split(Regex("\\s+")).filter(String::isNotBlank)
        return partes.size in 2..5 && partes.all(palabraNombre::matches)
    }

    fun correoValido(valor: String): Boolean = correo.matches(valor.trim())

    fun contrasenaValida(valor: String): Boolean =
        valor.length in 8..64 &&
            !valor.any(Char::isWhitespace) &&
            valor.any(Char::isUpperCase) &&
            valor.any(Char::isLowerCase) &&
            valor.any(Char::isDigit) &&
            valor.any { !it.isLetterOrDigit() }

    fun contrasenasCoinciden(contrasena: String, confirmacion: String): Boolean =
        contrasena == confirmacion

    fun codigoProfesorValido(codigo: String): Boolean = codigo.trim() == CODIGO_PROFESOR_DEMO

    const val CODIGO_PROFESOR_DEMO = "DOCENTE-DEMO-2026"
}
