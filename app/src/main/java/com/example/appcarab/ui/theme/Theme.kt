package com.example.appcarab.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = EsfocarGreenNight,
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF0F4D2A),
    onPrimaryContainer = Color(0xFFBFEFCF),
    secondary = EsfocarGold,
    onSecondary = EsfocarInk,
    secondaryContainer = Color(0xFF1F3A29),
    onSecondaryContainer = Color(0xFFCFEBD8),
    tertiary = EsfocarGold,
    onTertiary = EsfocarInk,
    background = EsfocarNightBackground,
    onBackground = EsfocarNightInk,
    surface = EsfocarNightSurface,
    onSurface = EsfocarNightInk,
    surfaceVariant = EsfocarNightSurfaceHigh,
    onSurfaceVariant = EsfocarNightMuted,
    surfaceContainer = EsfocarNightSurface,
    surfaceContainerHigh = EsfocarNightSurfaceHigh,
    surfaceContainerLow = EsfocarNightSurface,
    outline = Color(0xFF6E7F73),
    outlineVariant = EsfocarNightBorder,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private val LightColorScheme = lightColorScheme(
    primary = EsfocarGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEFE2),
    onPrimaryContainer = EsfocarGreenDark,
    secondary = EsfocarGold,
    onSecondary = EsfocarInk,
    secondaryContainer = Color(0xFFDCEFE2),
    onSecondaryContainer = EsfocarGreenDark,
    tertiary = EsfocarGold,
    onTertiary = EsfocarInk,
    background = EsfocarBackground,
    onBackground = EsfocarInk,
    surface = Color.White,
    onSurface = EsfocarInk,
    surfaceVariant = Color(0xFFEBF1EC),
    onSurfaceVariant = EsfocarMuted,
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White,
    surfaceContainerLow = Color.White,
    outline = Color(0xFF8A988E),
    outlineVariant = EsfocarBorder,
    error = Color(0xFFB3261E),
)

/** Colores de marca que Material 3 no cubre: la tarjeta destacada y el acento dorado. */
@Immutable
data class EsfocarColores(
    val heroInicio: Color,
    val heroFin: Color,
    val dorado: Color,
    val sobreDorado: Color,
    val doradoSuave: Color,
    val exito: Color,
    val exitoContenedor: Color,
    val esOscuro: Boolean,
)

private val ColoresClaros = EsfocarColores(
    heroInicio = EsfocarGreenDeep,
    heroFin = EsfocarGreen,
    dorado = EsfocarGold,
    sobreDorado = EsfocarInk,
    doradoSuave = EsfocarGoldSoft,
    exito = EsfocarGreen,
    exitoContenedor = Color(0xFFDCEFE2),
    esOscuro = false,
)

private val ColoresOscuros = EsfocarColores(
    heroInicio = Color(0xFF00321A),
    heroFin = EsfocarGreenDark,
    dorado = EsfocarGold,
    sobreDorado = EsfocarInk,
    doradoSuave = Color(0xFF3A3410),
    exito = EsfocarGreenNight,
    exitoContenedor = Color(0xFF0F4D2A),
    esOscuro = true,
)

val LocalEsfocarColores = staticCompositionLocalOf { ColoresClaros }

object Esfocar {
    val colores: EsfocarColores
        @Composable get() = LocalEsfocarColores.current
}

private val FormasEsfocar = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun AppCarabTheme(
    darkTheme: Boolean,
    escalaTexto: Float = 1f,
    content: @Composable () -> Unit,
) {
    val densidad = LocalDensity.current
    CompositionLocalProvider(
        LocalEsfocarColores provides if (darkTheme) ColoresOscuros else ColoresClaros,
        // La preferencia de la app se suma a la escala de fuente elegida en Android.
        LocalDensity provides Density(densidad.density, densidad.fontScale * escalaTexto),
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            shapes = FormasEsfocar,
            content = content,
        )
    }
}
