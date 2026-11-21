package com.example.appcarab.vista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.appcarab.R
import com.example.appcarab.ui.theme.Esfocar
import com.example.appcarab.vistaModelo.EstadoTrivia

@Composable
internal fun TriviaPanel(estado: EstadoTrivia, alCargar: () -> Unit, alResponder: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoEnCaja(Icons.Outlined.Quiz)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.trivia_titulo), style = MaterialTheme.typography.titleMedium)
                }
            }
            Text(
                stringResource(R.string.trivia_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            estado.pregunta?.let { pregunta ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pregunta.question, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            stringResource(R.string.trivia_fuente, pregunta.sourceDocument),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                pregunta.answers.forEach { answer ->
                    OpcionRespuesta(
                        texto = answer,
                        respondida = estado.respuestaElegida != null,
                        esCorrecta = answer == pregunta.correctAnswer,
                        fueElegida = answer == estado.respuestaElegida,
                        onClick = { alResponder(answer) },
                    )
                }
            }
            estado.mensaje?.let {
                val acierto = estado.pregunta != null && estado.respuestaElegida == estado.pregunta.correctAnswer
                InlineMessage(it, isError = !acierto)
            }
            Button(
                onClick = alCargar,
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(
                        when {
                            estado.cargando -> R.string.trivia_creando
                            estado.pregunta == null -> R.string.trivia_generar
                            else -> R.string.trivia_otra
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun OpcionRespuesta(
    texto: String,
    respondida: Boolean,
    esCorrecta: Boolean,
    fueElegida: Boolean,
    onClick: () -> Unit,
) {
    val colorEstado = when {
        respondida && esCorrecta -> Esfocar.colores.exito
        respondida && fueElegida -> MaterialTheme.colorScheme.error
        else -> null
    }
    val fondo = when {
        respondida && esCorrecta -> Esfocar.colores.exitoContenedor
        respondida && fueElegida -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }
    OutlinedButton(
        onClick = onClick,
        enabled = !respondida,
        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(if (colorEstado != null) 2.dp else 1.dp, colorEstado ?: MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = fondo,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = fondo,
            disabledContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(texto, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
        if (colorEstado != null) {
            Box(Modifier.padding(start = 8.dp)) {
                Icon(
                    if (esCorrecta) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                    contentDescription = null,
                    tint = colorEstado,
                )
            }
        }
    }
}
