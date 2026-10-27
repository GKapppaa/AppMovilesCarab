package com.example.appcarab

import com.example.appcarab.modelo.ResumenLocal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumenLocalTest {
    @Test
    fun resumenRecogeHastaCuatroOracionesIniciales() {
        val texto = """
            La formación inicial combina contenidos académicos y prácticos para preparar a los estudiantes.
            El material de estudio incluye normativa, procedimientos y actividades de aprendizaje.
            Las autoevaluaciones permiten repasar conocimientos y reconocer avances en cada unidad.
            La retroalimentación ayuda a comprender las respuestas y reforzar los contenidos revisados.
            Esta quinta oración no debe formar parte del resumen generado automáticamente.
        """.trimIndent()

        val resumen = ResumenLocal.crear(texto)

        assertTrue(resumen.contains("formación inicial"))
        assertTrue(resumen.contains("retroalimentación"))
        assertTrue(!resumen.contains("quinta oración"))
    }

    @Test
    fun textoEscaneadoIndicaQueNoSeDetectoTexto() {
        assertEquals(
            "No se detectó texto seleccionable. Si el PDF es escaneado, necesitaría OCR para resumirlo.",
            ResumenLocal.crear(" \n "),
        )
    }
}
