package com.example.appcarab.vista

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.appcarab.modelo.Documento
import com.example.appcarab.vistaModelo.EstadoAula
import com.example.appcarab.ui.theme.EsfocarGreen
import com.example.appcarab.ui.theme.EsfocarMuted
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun InicioEstudiante(
    nombre: String,
    codigoPerfil: String,
    correo: String,
    rol: String,
    estadoAula: EstadoAula,
    documentos: List<Documento>,
    documentosCargando: Boolean,
    documentosMensaje: String?,
    documentosMensajeEsError: Boolean,
    onChoosePdf: () -> Unit,
    onScan: () -> Unit,
    onDeleteDocument: (Documento) -> Unit,
    onChangeCategory: (Int, String) -> Unit,
    onOpenDocument: (Documento) -> Unit,
    onAddStudent: (String) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onShareMaterial: (String, Int) -> Unit,
    onAssignTask: (String, String, String) -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    var documentoAEliminar by remember { mutableStateOf<Documento?>(null) }
    var resumenExpandido by rememberSaveable { mutableIntStateOf(-1) }

    AuthPage {
        LogoInstitucional()
        Spacer(Modifier.height(24.dp))
        Text("Hola, $nombre", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(if (rol == "PROFESOR") "Perfil de profesor" else "Perfil de estudiante", color = EsfocarMuted)
        Text("ID de perfil: $codigoPerfil", color = EsfocarGreen, style = MaterialTheme.typography.labelLarge)
        Text(correo, color = EsfocarMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onEditProfile, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Editar datos de usuario", color = EsfocarGreen)
        }
        Spacer(Modifier.height(18.dp))
        AulaPanel(
            esProfesor = rol == "PROFESOR",
            estado = estadoAula,
            documentosPropios = documentos,
            alAgregarAlumno = onAddStudent,
            alEnviarMensaje = onSendMessage,
            alCompartir = onShareMaterial,
            alAsignarTarea = onAssignTask,
            alAbrirMaterial = onOpenDocument,
        )
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Material de estudio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Los PDF quedan en este dispositivo. Se extrae un resumen de texto de las primeras páginas; archivos escaneados requieren OCR.",
                    color = EsfocarMuted,
                )
                Button(
                    onClick = onChoosePdf,
                    enabled = !documentosCargando,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text(if (documentosCargando) "Procesando documento…" else "Cargar documento PDF")
                }
                OutlinedButton(onClick = onScan, enabled = !documentosCargando, modifier = Modifier.fillMaxWidth()) {
                    Text("Escanear con cámara", color = EsfocarGreen)
                }
                documentosMensaje?.let { InlineMessage(it, documentosMensajeEsError) }
                Text("Mis documentos (${documentos.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (documentos.isEmpty()) {
                    Text("Aún no tienes documentos. Carga un PDF para empezar.", color = EsfocarMuted)
                } else {
                    documentos.forEach { documento ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(documento.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Carpeta: ${documento.categoria}", color = EsfocarMuted, style = MaterialTheme.typography.labelMedium)
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    listOf("General", "Clases", "Normativa", "Tareas", "Escaneos").forEach { categoria ->
                                        FilterChip(
                                            selected = documento.categoria == categoria,
                                            onClick = { onChangeCategory(documento.id, categoria) },
                                            label = { Text(categoria) },
                                        )
                                    }
                                }
                                Text("Resumen automático · primeros apartados", color = EsfocarGreen, style = MaterialTheme.typography.labelMedium)
                                Text(
                                    documento.resumen,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = if (resumenExpandido == documento.id) Int.MAX_VALUE else 4,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    TextButton(onClick = {
                                        resumenExpandido = if (resumenExpandido == documento.id) -1 else documento.id
                                    }) {
                                        Text(if (resumenExpandido == documento.id) "Ver menos" else "Ver resumen")
                                    }
                                    TextButton(onClick = { onOpenDocument(documento) }) {
                                        Text("Abrir PDF", color = EsfocarGreen)
                                    }
                                    TextButton(onClick = { documentoAEliminar = documento }) {
                                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Cerrar sesión", color = EsfocarGreen)
        }
    }

    documentoAEliminar?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoAEliminar = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Quieres eliminar ‘${documento.nombre}’ del dispositivo?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDocument(documento)
                    documentoAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { documentoAEliminar = null }) { Text("Cancelar") } },
        )
    }
}
