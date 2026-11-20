package com.example.appcarab.vista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.Usuario
import com.example.appcarab.modelo.ValidadoresFormulario
import com.example.appcarab.vistaModelo.EstadoAula

private enum class AccionDocente(val titulo: String) {
    COMPARTIR("Compartir"),
    MENSAJE("Mensaje"),
    TAREA("Tarea"),
}

@Composable
internal fun AulaPanel(
    esProfesor: Boolean,
    estado: EstadoAula,
    documentosPropios: List<Documento>,
    alAgregarAlumno: (String) -> Unit,
    alEnviarMensaje: (String, String) -> Unit,
    alCompartir: (String, Int) -> Unit,
    alAsignarTarea: (String, String, String) -> Unit,
    alAbrirMaterial: (Documento) -> Unit,
) {
    if (esProfesor) {
        PanelDocente(
            estado = estado,
            documentosPropios = documentosPropios,
            alAgregarAlumno = alAgregarAlumno,
            alEnviarMensaje = alEnviarMensaje,
            alCompartir = alCompartir,
            alAsignarTarea = alAsignarTarea,
        )
    } else {
        PanelEstudiante(estado, alAbrirMaterial)
    }
}

@Composable
private fun PanelDocente(
    estado: EstadoAula,
    documentosPropios: List<Documento>,
    alAgregarAlumno: (String) -> Unit,
    alEnviarMensaje: (String, String) -> Unit,
    alCompartir: (String, Int) -> Unit,
    alAsignarTarea: (String, String, String) -> Unit,
) {
    var codigoAlumno by rememberSaveable { mutableStateOf("") }
    var envioCodigoIntentado by rememberSaveable { mutableStateOf(false) }
    var alumnoSeleccionadoId by rememberSaveable { mutableIntStateOf(0) }
    var accionSeleccionada by rememberSaveable { mutableStateOf(AccionDocente.COMPARTIR.name) }
    var menuAlumnosAbierto by remember { mutableStateOf(false) }
    val alumnoSeleccionado = estado.alumnos.firstOrNull { it.id == alumnoSeleccionadoId }
    val accion = AccionDocente.valueOf(accionSeleccionada)

    LaunchedEffect(estado.alumnos) {
        if (estado.alumnos.none { it.id == alumnoSeleccionadoId }) {
            alumnoSeleccionadoId = estado.alumnos.firstOrNull()?.id ?: 0
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CardAula {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EncabezadoAccion(
                    icono = Icons.Outlined.PersonAdd,
                    titulo = "Vincula a tus estudiantes",
                    descripcion = "Hazlo una vez. Después podrás compartir recursos y asignar trabajo sin volver a escribir su ID.",
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = codigoAlumno,
                        onValueChange = { codigoAlumno = it.uppercase() },
                        modifier = Modifier.weight(1f),
                        label = { Text("ID del alumno") },
                        placeholder = { Text("EST-…") },
                        singleLine = true,
                        isError = envioCodigoIntentado && !ValidadoresFormulario.codigoAlumnoValido(codigoAlumno),
                        supportingText = if (envioCodigoIntentado && !ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) {
                            ({ ValidationHint("Usa el ID completo que aparece en su perfil.") })
                        } else null,
                    )
                    Button(
                        onClick = {
                            envioCodigoIntentado = true
                            if (ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) {
                                alAgregarAlumno(codigoAlumno.trim())
                                codigoAlumno = ""
                                envioCodigoIntentado = false
                            }
                        },
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Vincular")
                    }
                }

                if (estado.alumnos.isEmpty()) {
                    Text(
                        "Aún no has vinculado estudiantes. Pide que te compartan su ID de perfil.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text(
                        "Estudiantes vinculados · ${estado.alumnos.size}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box {
                        OutlinedButton(
                            onClick = { menuAlumnosAbierto = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text(
                                alumnoSeleccionado?.let { "${it.nombre} · ${it.codigoPerfil}" }
                                    ?: "Selecciona un estudiante",
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                            )
                            Text("Cambiar")
                        }
                        DropdownMenu(
                            expanded = menuAlumnosAbierto,
                            onDismissRequest = { menuAlumnosAbierto = false },
                        ) {
                            estado.alumnos.forEach { alumno ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(alumno.nombre, fontWeight = FontWeight.SemiBold)
                                            Text(alumno.codigoPerfil, style = MaterialTheme.typography.bodySmall)
                                        }
                                    },
                                    onClick = {
                                        alumnoSeleccionadoId = alumno.id
                                        menuAlumnosAbierto = false
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (estado.alumnos.isEmpty()) {
            CardAula {
                Text(
                    "Cuando vincules un estudiante, aquí podrás compartirle material, enviarle mensajes y asignarle tareas.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (alumnoSeleccionado != null) {
            CardAula {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "Acciones para ${alumnoSeleccionado.nombre}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccionDocente.entries.forEach { item ->
                            FilterChip(
                                selected = accion == item,
                                onClick = { accionSeleccionada = item.name },
                                label = { Text(item.titulo) },
                            )
                        }
                    }
                    key(alumnoSeleccionado.id) {
                        when (accion) {
                            AccionDocente.COMPARTIR -> CompartirConAlumno(
                                alumno = alumnoSeleccionado,
                                documentos = documentosPropios,
                                onShare = { documentoId ->
                                    alCompartir(alumnoSeleccionado.codigoPerfil, documentoId)
                                },
                            )

                            AccionDocente.MENSAJE -> MensajeParaAlumno(
                                onSend = { texto -> alEnviarMensaje(alumnoSeleccionado.codigoPerfil, texto) },
                            )

                            AccionDocente.TAREA -> TareaParaAlumno(
                                onAssign = { titulo, descripcion ->
                                    alAsignarTarea(alumnoSeleccionado.codigoPerfil, titulo, descripcion)
                                },
                            )
                        }
                    }
                }
            }
        }

        estado.mensaje?.let { InlineMessage(it, estado.error) }
    }
}

@Composable
private fun CompartirConAlumno(
    alumno: Usuario,
    documentos: List<Documento>,
    onShare: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Elige un PDF para compartir con ${alumno.nombre}.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (documentos.isEmpty()) {
            Text(
                "Primero carga un PDF desde Material.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            documentos.forEach { documento ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        Icons.Outlined.FolderOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        documento.nombre,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                    )
                    Button(
                        onClick = { onShare(documento.id) },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.Share, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Compartir")
                    }
                }
            }
        }
    }
}

@Composable
private fun MensajeParaAlumno(onSend: (String) -> Unit) {
    var mensaje by rememberSaveable { mutableStateOf("") }
    var envioIntentado by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EncabezadoAccion(
            icono = Icons.Outlined.ChatBubbleOutline,
            titulo = "Enviar un mensaje",
            descripcion = "El destinatario ya está seleccionado.",
        )
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mensaje") },
            isError = envioIntentado && !ValidadoresFormulario.mensajeValido(mensaje),
            supportingText = if (envioIntentado && !ValidadoresFormulario.mensajeValido(mensaje)) {
                ({ ValidationHint("Escribe un mensaje de hasta 500 caracteres.") })
            } else null,
        )
        Button(
            onClick = {
                envioIntentado = true
                if (ValidadoresFormulario.mensajeValido(mensaje)) {
                    onSend(mensaje)
                    mensaje = ""
                    envioIntentado = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Enviar mensaje")
        }
    }
}

@Composable
private fun TareaParaAlumno(onAssign: (String, String) -> Unit) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var envioIntentado by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        EncabezadoAccion(
            icono = Icons.AutoMirrored.Outlined.Assignment,
            titulo = "Nueva tarea",
            descripcion = "Se asignará al estudiante seleccionado.",
        )
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Título") },
            singleLine = true,
            isError = envioIntentado && !ValidadoresFormulario.tituloTareaValido(titulo),
            supportingText = if (envioIntentado && !ValidadoresFormulario.tituloTareaValido(titulo)) {
                ({ ValidationHint("Usa entre 3 y 80 caracteres.") })
            } else null,
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Instrucciones") },
            isError = envioIntentado && !ValidadoresFormulario.descripcionTareaValida(descripcion),
            supportingText = if (envioIntentado && !ValidadoresFormulario.descripcionTareaValida(descripcion)) {
                ({ ValidationHint("Usa entre 5 y 1000 caracteres.") })
            } else null,
        )
        Button(
            onClick = {
                envioIntentado = true
                if (ValidadoresFormulario.tituloTareaValido(titulo) &&
                    ValidadoresFormulario.descripcionTareaValida(descripcion)
                ) {
                    onAssign(titulo, descripcion)
                    titulo = ""
                    descripcion = ""
                    envioIntentado = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
        ) {
            Text("Asignar tarea")
        }
    }
}

@Composable
private fun PanelEstudiante(estado: EstadoAula, alAbrirMaterial: (Documento) -> Unit) {
    CardAula {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Mensajes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (estado.mensajes.isEmpty()) {
                Text("No tienes mensajes nuevos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                estado.mensajes.forEach { mensaje ->
                    TarjetaAulaEstudiante { Text(mensaje.texto, style = MaterialTheme.typography.bodyMedium) }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Tareas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (estado.tareas.isEmpty()) {
                Text("No tienes tareas asignadas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                estado.tareas.forEach { tarea ->
                    TarjetaAulaEstudiante {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tarea.titulo, style = MaterialTheme.typography.titleSmall)
                            Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text("Material compartido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
}

@Composable
private fun CardAula(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = { content() })
    }
}

@Composable
private fun TarjetaAulaEstudiante(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(14.dp), content = { content() })
    }
}

@Composable
private fun EncabezadoAccion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    descripcion: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        androidx.compose.material3.Icon(
            icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
