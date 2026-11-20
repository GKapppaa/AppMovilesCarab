package com.example.appcarab.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF68D391),
    onPrimary = Color(0xFF00391A),
    secondary = Color(0xFFF0D84F),
    background = Color(0xFF101510),
    onBackground = Color(0xFFE1E8E1),
    surface = Color(0xFF18201A),
    onSurface = Color(0xFFE1E8E1),
    primaryContainer = Color(0xFF2C4935),
    onPrimaryContainer = Color(0xFFDDEFE1),
    secondaryContainer = Color(0xFF3C4A3D),
    onSecondaryContainer = Color(0xFFE2F1E4),
    surfaceVariant = Color(0xFF29362C),
    onSurfaceVariant = Color(0xFFC1CEC3),
    outline = Color(0xFF87968A),
    outlineVariant = Color(0xFF3A493D),
)

private val LightColorScheme = lightColorScheme(
    primary = EsfocarGreen,
    onPrimary = Color.White,
    secondary = EsfocarGold,
    onSecondary = EsfocarInk,
    primaryContainer = Color(0xFFDCEFE2),
    onPrimaryContainer = EsfocarGreenDark,
    secondaryContainer = Color(0xFFDCEFE2),
    onSecondaryContainer = EsfocarGreenDark,
    background = EsfocarBackground,
    onBackground = EsfocarInk,
    surface = Color.White,
    onSurface = EsfocarInk,
    surfaceVariant = Color(0xFFEBF1EC),
    onSurfaceVariant = EsfocarMuted,
    outline = EsfocarBorder,
    outlineVariant = EsfocarBorder,
    error = Color(0xFFB3261E),
)

@Composable
fun AppCarabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
