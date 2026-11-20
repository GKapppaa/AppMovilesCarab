package com.example.appcarab.vista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.ValidadoresFormulario
import com.example.appcarab.vistaModelo.EstadoAula

@Composable
internal fun AulaPanel(
    esProfesor: Boolean,
    estado: EstadoAula,
    documentosPropios: List<Documento>,
    alAgregarAlumno: (String) -> Unit,
    alEnviarMensaje: (String, String) -> Unit,
    alCompartir: (String, Int) -> Unit,
    alAsignarTarea: (String, String, String) -> Unit,
    alComprobarApi: () -> Unit,
    alAbrirMaterial: (Documento) -> Unit,
) {
    var codigoAlumno by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var tituloTarea by remember { mutableStateOf("") }
    var detalleTarea by remember { mutableStateOf("") }
    var envioCodigoIntentado by remember { mutableStateOf(false) }
    var envioMensajeIntentado by remember { mutableStateOf(false) }
    var envioTareaIntentado by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedButton(
                onClick = alComprobarApi,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                Text("Comprobar conexión con API")
            }
            if (esProfesor) {
                Text("Estudiantes", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Text("Agrega estudiantes usando el ID que aparece en su perfil.", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codigoAlumno,
                        onValueChange = { codigoAlumno = it.uppercase() },
                        modifier = Modifier.weight(1f),
                        label = { Text("ID del alumno") },
                        singleLine = true,
                        isError = envioCodigoIntentado && !ValidadoresFormulario.codigoAlumnoValido(codigoAlumno),
                        supportingText = if (envioCodigoIntentado && !ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) {
                            ({ ValidationHint("Formato requerido: EST-######.") })
                        } else null,
                    )
                    Button(onClick = {
                        envioCodigoIntentado = true
                        if (ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) alAgregarAlumno(codigoAlumno)
                    }) { Text("Agregar") }
                }
                estado.alumnos.forEach { alumno ->
                    Text("${alumno.nombre} · ${alumno.codigoPerfil}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("Enviar información", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = mensaje,
                    onValueChange = { mensaje = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Mensaje para el ID indicado") },
                    isError = envioMensajeIntentado && !ValidadoresFormulario.mensajeValido(mensaje),
                    supportingText = if (envioMensajeIntentado && !ValidadoresFormulario.mensajeValido(mensaje)) {
                        ({ ValidationHint("El mensaje admite entre 1 y 500 caracteres.") })
                    } else null,
                )
                Button(onClick = {
                    envioMensajeIntentado = true
                    if (ValidadoresFormulario.codigoAlumnoValido(codigoAlumno) && ValidadoresFormulario.mensajeValido(mensaje)) {
                        alEnviarMensaje(codigoAlumno, mensaje)
                        mensaje = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) {
                    Text("Enviar mensaje")
                }
                Text("Compartir material", style = MaterialTheme.typography.titleMedium)
                documentosPropios.forEach { documento ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(documento.nombre, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { alCompartir(codigoAlumno, documento.id) }) { Text("Compartir") }
                    }
                }
                Text("Asignar tarea", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    tituloTarea, { tituloTarea = it }, Modifier.fillMaxWidth(),
                    label = { Text("Título") }, singleLine = true,
                    isError = envioTareaIntentado && !ValidadoresFormulario.tituloTareaValido(tituloTarea),
                    supportingText = if (envioTareaIntentado && !ValidadoresFormulario.tituloTareaValido(tituloTarea)) {
                        ({ ValidationHint("Usa entre 3 y 80 caracteres.") })
                    } else null,
                )
                OutlinedTextField(
                    detalleTarea, { detalleTarea = it }, Modifier.fillMaxWidth(),
                    label = { Text("Instrucciones") },
                    isError = envioTareaIntentado && !ValidadoresFormulario.descripcionTareaValida(detalleTarea),
                    supportingText = if (envioTareaIntentado && !ValidadoresFormulario.descripcionTareaValida(detalleTarea)) {
                        ({ ValidationHint("Usa entre 5 y 1000 caracteres.") })
                    } else null,
                )
                Button(onClick = {
                    envioTareaIntentado = true
                    if (ValidadoresFormulario.codigoAlumnoValido(codigoAlumno) &&
                        ValidadoresFormulario.tituloTareaValido(tituloTarea) &&
                        ValidadoresFormulario.descripcionTareaValida(detalleTarea)
                    ) {
                        alAsignarTarea(codigoAlumno, tituloTarea, detalleTarea)
                        tituloTarea = ""
                        detalleTarea = ""
                    }
                }, modifier = Modifier.fillMaxWidth()) { Text("Asignar tarea") }
            } else {
                AulaEstudiante(estado, alAbrirMaterial)
            }
            estado.mensaje?.let { InlineMessage(it, estado.error) }
        }
    }
}

@Composable
private fun AulaEstudiante(estado: EstadoAula, alAbrirMaterial: (Documento) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Mensajes", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        if (estado.mensajes.isEmpty()) {
            Text("No tienes mensajes nuevos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            estado.mensajes.forEach { mensaje ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Text(mensaje.texto, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Tareas", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        if (estado.tareas.isEmpty()) {
            Text("No tienes tareas asignadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            estado.tareas.forEach { tarea ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(tarea.titulo, style = MaterialTheme.typography.titleSmall)
                        Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Text("Material compartido", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        if (estado.materiales.isEmpty()) {
            Text("Aún no te han compartido material.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            estado.materiales.forEach { documento ->
                TextButton(
                    onClick = { alAbrirMaterial(documento) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) { Text("Abrir ${documento.nombre}") }
            }
        }
    }
}
