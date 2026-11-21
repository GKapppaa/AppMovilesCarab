package com.example.appcarab.vista

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Animation
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appcarab.BuildConfig
import com.example.appcarab.R
import com.example.appcarab.modelo.IdiomaApp
import com.example.appcarab.modelo.IdiomaAplicacion
import com.example.appcarab.modelo.PreferenciasApp
import com.example.appcarab.modelo.TamanoTexto
import com.example.appcarab.modelo.TemaApp

private val OPCIONES_TEMA = listOf(
    TemaApp.SISTEMA to R.string.tema_sistema,
    TemaApp.CLARO to R.string.tema_claro,
    TemaApp.OSCURO to R.string.tema_oscuro,
)

private val OPCIONES_TEXTO = listOf(
    TamanoTexto.ESTANDAR to R.string.texto_estandar,
    TamanoTexto.GRANDE to R.string.texto_grande,
    TamanoTexto.MUY_GRANDE to R.string.texto_muy_grande,
)

private val OPCIONES_IDIOMA = listOf(
    IdiomaApp.SISTEMA to R.string.idioma_sistema,
    IdiomaApp.ESPANOL to R.string.idioma_es,
    IdiomaApp.INGLES to R.string.idioma_en,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PantallaConfiguracion(
    preferencias: PreferenciasApp,
    onTema: (TemaApp) -> Unit,
    onTamanoTexto: (TamanoTexto) -> Unit,
    onReducirMovimiento: (Boolean) -> Unit,
    onRestablecer: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    // El idioma lo guarda Android; al cambiarlo la actividad se recrea y este valor se relee.
    val idiomaActual = IdiomaAplicacion.actual(context)
    var restablecido by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EncabezadoApp {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.accion_volver),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    stringResource(R.string.accion_configuracion),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    ) { insets ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.config_subtitulo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                SeccionAjustes(R.string.config_apariencia) {
                    CabeceraAjuste(Icons.Outlined.Palette, R.string.config_tema)
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        OPCIONES_TEMA.forEachIndexed { indice, (tema, etiqueta) ->
                            SegmentedButton(
                                selected = preferencias.tema == tema,
                                onClick = { onTema(tema) },
                                shape = SegmentedButtonDefaults.itemShape(indice, OPCIONES_TEMA.size),
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    activeBorderColor = MaterialTheme.colorScheme.primary,
                                ),
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) {
                                Text(stringResource(etiqueta), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    CabeceraAjuste(Icons.Outlined.TextFields, R.string.config_texto)
                    Column(Modifier.selectableGroup()) {
                        OPCIONES_TEXTO.forEach { (tamano, etiqueta) ->
                            FilaOpcion(
                                texto = stringResource(etiqueta),
                                seleccionada = preferencias.tamanoTexto == tamano,
                                onClick = { onTamanoTexto(tamano) },
                                // Cada opción se muestra a su propio tamaño a modo de vista previa.
                                estilo = MaterialTheme.typography.bodyLarge.let {
                                    it.copy(fontSize = it.fontSize * (tamano.escala / preferencias.tamanoTexto.escala))
                                },
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.config_texto_vista_previa),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SeccionAjustes(R.string.config_idioma) {
                    Column(Modifier.selectableGroup()) {
                        OPCIONES_IDIOMA.forEach { (idioma, etiqueta) ->
                            FilaOpcion(
                                texto = stringResource(etiqueta),
                                seleccionada = idiomaActual == idioma,
                                onClick = { if (idioma != idiomaActual) IdiomaAplicacion.aplicar(context, idioma) },
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.config_idioma_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                SeccionAjustes(R.string.config_accesibilidad) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = preferencias.reducirMovimiento,
                                role = Role.Switch,
                                onValueChange = onReducirMovimiento,
                            )
                            .heightIn(min = 56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconoEnCaja(Icons.Outlined.Animation, tamano = 40.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text(stringResource(R.string.config_movimiento), style = MaterialTheme.typography.titleSmall)
                            Text(
                                stringResource(R.string.config_movimiento_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = preferencias.reducirMovimiento,
                            onCheckedChange = null,
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                }

                SeccionAjustes(R.string.config_privacidad) {
                    FilaInformativa(
                        icono = Icons.Outlined.Lock,
                        titulo = stringResource(R.string.config_datos_locales),
                        descripcion = stringResource(R.string.config_datos_locales_desc),
                    )
                }

                SeccionAjustes(R.string.config_acerca) {
                    FilaInformativa(
                        icono = Icons.Outlined.Info,
                        titulo = stringResource(R.string.config_version, BuildConfig.VERSION_NAME),
                        descripcion = stringResource(R.string.config_aviso),
                    )
                }

                OutlinedButton(
                    onClick = {
                        onRestablecer()
                        restablecido = true
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(Icons.Outlined.RestartAlt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.config_restablecer))
                }
                Text(
                    stringResource(R.string.config_restablecer_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (restablecido) InlineMessage(stringResource(R.string.config_restablecido), isError = false)
            }
        }
    }
}

@Composable
private fun SeccionAjustes(@StringRes titulo: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(titulo).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp),
        )
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@Composable
private fun CabeceraAjuste(icono: ImageVector, @StringRes titulo: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoEnCaja(icono, tamano = 40.dp)
        Text(
            stringResource(titulo),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
private fun FilaOpcion(
    texto: String,
    seleccionada: Boolean,
    onClick: () -> Unit,
    estilo: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = seleccionada,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary),
        )
        Text(texto, style = estilo, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun FilaInformativa(icono: ImageVector, titulo: String, descripcion: String) {
    Row(verticalAlignment = Alignment.Top) {
        IconoEnCaja(icono, tamano = 40.dp)
        Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
