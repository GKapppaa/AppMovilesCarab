package com.example.appcarab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.appcarab.vista.Bienvenida
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BienvenidaScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val contexto = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun muestraLosTextosYBotonesDeBienvenida() {
        // Pintamos la pantalla sin animaciones y comprobamos lo principal que ve el usuario.
        composeRule.setContent {
            Bienvenida(
                reducirMovimiento = true,
                onLogin = {},
                onRegister = {},
                onSettings = {},
            )
        }

        composeRule.onNodeWithText(contexto.getString(R.string.bienvenida_titulo)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.bienvenida_subtitulo)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.iniciar_sesion)).assertIsDisplayed()
        composeRule.onNodeWithText(contexto.getString(R.string.crear_cuenta)).assertIsDisplayed()
    }

    @Test
    fun alPulsarIniciarSesionSeAvisaAlCallback() {
        var inicioSesionPulsado = false
        composeRule.setContent {
            Bienvenida(
                reducirMovimiento = true,
                onLogin = { inicioSesionPulsado = true },
                onRegister = {},
                onSettings = {},
            )
        }

        // El botón principal debe avisar de que se ha pulsado.
        composeRule.onNodeWithText(contexto.getString(R.string.iniciar_sesion)).performClick()
        assertTrue(inicioSesionPulsado)
    }
}
