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
            Text("Práctica complementaria en línea", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Pregunta de trivia desde Open Trivia DB. Si no hay conexión, tus documentos locales siguen disponibles.", style = MaterialTheme.typography.bodySmall)
            estado.pregunta?.let { pregunta ->
                Text(pregunta.question, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
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
                Text(if (estado.cargando) "Buscando pregunta…" else if (estado.pregunta == null) "Obtener trivia" else "Otra pregunta")
            }
        }
    }
}
