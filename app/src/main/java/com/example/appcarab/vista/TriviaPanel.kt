package com.example.appcarab.vista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcarab.ui.theme.EsfocarGreen
import com.example.appcarab.vistaModelo.EstadoTrivia

@Composable
internal fun TriviaPanel(estado: EstadoTrivia, alCargar: () -> Unit, alResponder: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Trivia de tus documentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Las preguntas se crean en este dispositivo a partir del texto de tus PDF. El contenido no se envía a servicios externos; los escaneos necesitan OCR.",
                style = MaterialTheme.typography.bodySmall,
            )
            estado.pregunta?.let { pregunta ->
                Text(pregunta.question, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("Fuente: ${pregunta.sourceDocument}", style = MaterialTheme.typography.labelMedium)
                pregunta.answers.forEach { answer ->
                    OutlinedButton(
                        onClick = { alResponder(answer) },
                        enabled = estado.respuestaElegida == null,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(answer) }
                }
            }
            estado.mensaje?.let { Text(it, color = if (it.startsWith("Respuesta correcta")) EsfocarGreen else MaterialTheme.colorScheme.error) }
            Button(onClick = alCargar, enabled = !estado.cargando, modifier = Modifier.fillMaxWidth()) {
                Text(if (estado.cargando) "Creando pregunta…" else if (estado.pregunta == null) "Generar trivia desde PDF" else "Otra pregunta")
            }
        }
    }
}
