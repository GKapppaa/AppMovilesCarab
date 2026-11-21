package com.example.appcarab

import com.example.appcarab.modelo.GeneradorTriviaPdf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriviaPdfTest {
    @Test
    fun generatesQuestionAndOptionsFromSuppliedPdfText() {
        val text = """
            La formación policial desarrolla competencias profesionales mediante doctrina institucional.
            La doctrina institucional orienta procedimientos operativos y protege derechos humanos en cada intervención.
            Los procedimientos operativos establecen protocolos de actuación para responder ante situaciones de emergencia.
            La capacitación continua fortalece el liderazgo responsable y la disciplina institucional.
        """.trimIndent()

        val question = GeneradorTriviaPdf.generar("Manual de formación.pdf", text)

        assertTrue(question.question.contains("Manual de formación.pdf"))
        assertTrue(question.question.contains("_____"))
        assertEquals(4, question.answers.size)
        assertTrue(question.answers.contains(question.correctAnswer))
        assertTrue(text.contains(question.correctAnswer, ignoreCase = true))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPdfTextWithoutEnoughQuestionContent() {
        GeneradorTriviaPdf.generar("Escaneo.pdf", " ")
    }
}
