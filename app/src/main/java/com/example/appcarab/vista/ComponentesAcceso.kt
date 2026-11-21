package com.example.appcarab.vista

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.appcarab.R
import com.example.appcarab.ui.theme.Esfocar

/**
 * Cabecera que se dibuja debajo de la barra de estado y del recorte de cámara (notch).
 * La app es edge-to-edge, así que el fondo se extiende detrás de la barra del sistema
 * pero el contenido queda dentro de la zona segura en cualquier densidad u orientación.
 */
@Composable
internal fun EncabezadoApp(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(modifier = modifier, color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = content,
            )
            FranjaDorada()
        }
    }
}

/** Motivo institucional: la franja dorada del uniforme, usada como separador en toda la app. */
@Composable
internal fun FranjaDorada(modifier: Modifier = Modifier, alto: Dp = 3.dp) {
    Row(modifier.fillMaxWidth().height(alto)) {
        Box(Modifier.weight(1f).fillMaxSize().background(MaterialTheme.colorScheme.primary))
        Box(Modifier.weight(0.38f).fillMaxSize().background(Esfocar.colores.dorado))
    }
}

@Composable
internal fun BotonConfiguracion(onClick: () -> Unit, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(
            Icons.Outlined.Settings,
            contentDescription = stringResource(R.string.accion_configuracion),
            tint = tint,
        )
    }
}

@Composable
internal fun AuthPage(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.accion_volver))
                }
                Spacer(Modifier.weight(1f))
                BotonConfiguracion(onSettings)
            }
            Spacer(Modifier.height(8.dp))
            FranjaDorada(alto = 5.dp, modifier = Modifier.padding(horizontal = 2.dp))
            Spacer(Modifier.height(24.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
internal fun LogoInstitucional(modifier: Modifier = Modifier, compact: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = if (compact) 1f else 0.92f),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_esfocar),
            contentDescription = stringResource(R.string.logo_desc),
            modifier = Modifier
                .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = 5.dp)
                .width(if (compact) 148.dp else 202.dp)
                .height(if (compact) 32.dp else 44.dp),
        )
    }
}

/** Título de sección con antetítulo en mayúsculas, el patrón de jerarquía de toda la app. */
@Composable
internal fun EncabezadoSeccion(titulo: String, descripcion: String, antetitulo: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        antetitulo?.let {
            Text(
                it.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(titulo, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Ícono dentro de un recuadro tenue de marca; mantiene el mismo ritmo visual en tarjetas y listas. */
@Composable
internal fun IconoEnCaja(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    tamano: Dp = 44.dp,
) {
    Surface(
        modifier = modifier.size(tamano),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(tamano * 0.5f))
        }
    }
}
