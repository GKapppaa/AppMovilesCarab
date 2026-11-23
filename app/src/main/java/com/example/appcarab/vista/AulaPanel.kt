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
import androidx.compose.foundation.layout.size
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
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.appcarab.R
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.AlumnoAula
import com.example.appcarab.modelo.ValidadoresFormulario
import com.example.appcarab.vistaModelo.EstadoAula

private enum class AccionDocente(@StringRes val titulo: Int) {
    COMPARTIR(R.string.accion_compartir),
    MENSAJE(R.string.accion_mensaje),
    TAREA(R.string.accion_tarea),
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    var alumnoSeleccionadoCodigo by rememberSaveable { mutableStateOf("") }
    var accionSeleccionada by rememberSaveable { mutableStateOf(AccionDocente.COMPARTIR.name) }
    var menuAlumnosAbierto by remember { mutableStateOf(false) }
    val alumnoSeleccionado = estado.alumnos.firstOrNull { it.codigoPerfil == alumnoSeleccionadoCodigo }
    val accion = AccionDocente.valueOf(accionSeleccionada)

    LaunchedEffect(estado.alumnos) {
        if (estado.alumnos.none { it.codigoPerfil == alumnoSeleccionadoCodigo }) {
            alumnoSeleccionadoCodigo = estado.alumnos.firstOrNull()?.codigoPerfil.orEmpty()
        }
    }
    LaunchedEffect(alumnoSeleccionadoCodigo) {
        accionSeleccionada = AccionDocente.COMPARTIR.name
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        CardAula {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EncabezadoAccion(
                    icono = Icons.Outlined.PersonAdd,
                    titulo = stringResource(R.string.vincular_titulo),
                    descripcion = stringResource(R.string.vincular_desc),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = codigoAlumno,
                        onValueChange = { codigoAlumno = it.uppercase() },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(R.string.campo_id_alumno)) },
                        placeholder = { Text("EST-…") },
                        singleLine = true,
                        isError = envioCodigoIntentado && !ValidadoresFormulario.codigoAlumnoValido(codigoAlumno),
                        supportingText = if (envioCodigoIntentado && !ValidadoresFormulario.codigoAlumnoValido(codigoAlumno)) {
                            ({ ValidationHint(stringResource(R.string.error_id_alumno)) })
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
                        modifier = Modifier.heightIn(min = 56.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(stringResource(R.string.vincular))
                    }
                }

                if (estado.alumnos.isEmpty()) {
                    Text(
                        stringResource(R.string.sin_estudiantes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text(
                        stringResource(R.string.estudiantes_vinculados, estado.alumnos.size),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Box {
                        OutlinedButton(
                            onClick = { menuAlumnosAbierto = true },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(
                                alumnoSeleccionado?.let { "${it.nombre} · ${it.codigoPerfil}" }
                                    ?: stringResource(R.string.selecciona_estudiante),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(stringResource(R.string.cambiar), color = MaterialTheme.colorScheme.primary)
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
                                        alumnoSeleccionadoCodigo = alumno.codigoPerfil
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
                    stringResource(R.string.aula_sin_vinculos_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else if (alumnoSeleccionado != null) {
            CardAula {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        stringResource(R.string.acciones_para, alumnoSeleccionado.nombre),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val accionesDisponibles = if (alumnoSeleccionado.aulaLocal) {
                            AccionDocente.entries
                        } else {
                            listOf(AccionDocente.COMPARTIR)
                        }
                        accionesDisponibles.forEach { item ->
                            FilterChip(
                                selected = accion == item,
                                onClick = { accionSeleccionada = item.name },
                                label = { Text(stringResource(item.titulo)) },
                                colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                            )
                        }
                    }
                    key(alumnoSeleccionado.codigoPerfil) {
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
    alumno: AlumnoAula,
    documentos: List<Documento>,
    onShare: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            stringResource(R.string.compartir_elige, alumno.nombre),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (documentos.isEmpty()) {
            Text(
                stringResource(R.string.compartir_sin_pdf),
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
                        onClick = {
                            if (documento.idRemoto != null) onShare(documento.id)
                        },
                        enabled = documento.idRemoto != null,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        if (documento.idRemoto == null) {
                            Text(stringResource(R.string.aula_pdf_pendiente))
                        } else {
                            androidx.compose.material3.Icon(Icons.Outlined.Share, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.accion_compartir))
                        }
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
            titulo = stringResource(R.string.mensaje_titulo),
            descripcion = stringResource(R.string.mensaje_desc),
        )
        OutlinedTextField(
            value = mensaje,
            onValueChange = { mensaje = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_mensaje)) },
            isError = envioIntentado && !ValidadoresFormulario.mensajeValido(mensaje),
            supportingText = if (envioIntentado && !ValidadoresFormulario.mensajeValido(mensaje)) {
                ({ ValidationHint(stringResource(R.string.error_mensaje)) })
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.enviar_mensaje))
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
            titulo = stringResource(R.string.tarea_titulo),
            descripcion = stringResource(R.string.tarea_desc),
        )
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_titulo)) },
            singleLine = true,
            isError = envioIntentado && !ValidadoresFormulario.tituloTareaValido(titulo),
            supportingText = if (envioIntentado && !ValidadoresFormulario.tituloTareaValido(titulo)) {
                ({ ValidationHint(stringResource(R.string.error_titulo)) })
            } else null,
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_instrucciones)) },
            isError = envioIntentado && !ValidadoresFormulario.descripcionTareaValida(descripcion),
            supportingText = if (envioIntentado && !ValidadoresFormulario.descripcionTareaValida(descripcion)) {
                ({ ValidationHint(stringResource(R.string.error_instrucciones)) })
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(stringResource(R.string.asignar_tarea))
        }
    }
}

@Composable
private fun PanelEstudiante(estado: EstadoAula, alAbrirMaterial: (Documento) -> Unit) {
    CardAula {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.est_mensajes), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (estado.mensajes.isEmpty()) {
                Text(stringResource(R.string.est_sin_mensajes), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                estado.mensajes.forEach { mensaje ->
                    TarjetaAulaEstudiante { Text(mensaje.texto, style = MaterialTheme.typography.bodyMedium) }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.est_tareas), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (estado.tareas.isEmpty()) {
                Text(stringResource(R.string.est_sin_tareas), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            Text(stringResource(R.string.est_material), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (estado.materiales.isEmpty()) {
                Text(stringResource(R.string.est_sin_material), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                estado.materiales.forEach { documento ->
                    OutlinedButton(
                        onClick = { alAbrirMaterial(documento) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        androidx.compose.material3.Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(documento.nombre, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
private fun CardAula(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = { content() })
    }
}

@Composable
private fun TarjetaAulaEstudiante(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
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
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconoEnCaja(icono, tamano = 40.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
