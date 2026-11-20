package com.example.appcarab.vista

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcarab.modelo.Documento
import com.example.appcarab.ui.theme.EsfocarBackground
import com.example.appcarab.ui.theme.EsfocarGold
import com.example.appcarab.ui.theme.EsfocarGreen
import com.example.appcarab.ui.theme.EsfocarGreenDark
import com.example.appcarab.ui.theme.EsfocarInk
import com.example.appcarab.ui.theme.EsfocarMuted
import com.example.appcarab.vistaModelo.EstadoAula
import com.example.appcarab.vistaModelo.EstadoTrivia

private enum class SeccionMovil(
    val titulo: String,
    val icono: ImageVector,
) {
    INICIO("Inicio", Icons.Outlined.Home),
    AULA("Aula", Icons.Outlined.School),
    MATERIAL("Material", Icons.Outlined.FolderOpen),
    REPASO("Repaso", Icons.Outlined.Quiz),
}

@Composable
internal fun InicioEstudiante(
    nombre: String,
    codigoPerfil: String,
    correo: String,
    rol: String,
    estadoAula: EstadoAula,
    estadoTrivia: EstadoTrivia,
    onLoadTrivia: () -> Unit,
    onAnswerTrivia: (String) -> Unit,
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
    onCheckApi: () -> Unit,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    var seccionGuardada by rememberSaveable { mutableStateOf(SeccionMovil.INICIO.name) }
    var perfilMenuAbierto by remember { mutableStateOf(false) }
    var documentoAEliminar by remember { mutableStateOf<Documento?>(null) }
    var resumenExpandidoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val seccion = SeccionMovil.valueOf(seccionGuardada)
    val esProfesor = rol == "PROFESOR"

    Scaffold(
        containerColor = EsfocarBackground,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LogoInstitucional(modifier = Modifier.weight(1f), compact = true)
                    Box {
                        IconButton(
                            onClick = { perfilMenuAbierto = true },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = "Abrir menú de perfil",
                                tint = EsfocarGreenDark,
                                modifier = Modifier.size(28.dp),
                            )
                        }
                        DropdownMenu(
                            expanded = perfilMenuAbierto,
                            onDismissRequest = { perfilMenuAbierto = false },
                        ) {
                            Text(
                                nombre,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                correo,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = EsfocarMuted,
                            )
                            DropdownMenuItem(
                                text = { Text("Editar perfil") },
                                leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                                onClick = {
                                    perfilMenuAbierto = false
                                    onEditProfile()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Cerrar sesión") },
                                onClick = {
                                    perfilMenuAbierto = false
                                    onLogout()
                                },
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
            ) {
                SeccionMovil.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = seccion == destino,
                        onClick = { seccionGuardada = destino.name },
                        icon = { Icon(destino.icono, contentDescription = null) },
                        label = { Text(destino.titulo) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EsfocarGreenDark,
                            selectedTextColor = EsfocarGreenDark,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = EsfocarMuted,
                            unselectedTextColor = EsfocarMuted,
                        ),
                    )
                }
            }
        },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (seccion) {
                SeccionMovil.INICIO -> PanelInicio(
                    nombre = nombre,
                    esProfesor = esProfesor,
                    estadoAula = estadoAula,
                    documentos = documentos,
                    irA = { seccionGuardada = it.name },
                    alAbrirDocumento = onOpenDocument,
                )

                SeccionMovil.AULA -> {
                    EncabezadoSeccion(
                        titulo = if (esProfesor) "Tu aula" else "Mi aula",
                        descripcion = if (esProfesor) {
                            "Organiza el acompañamiento y comparte información con tus estudiantes."
                        } else {
                            "Revisa las novedades, tareas y material compartido por tu profesor."
                        },
                    )
                    AulaPanel(
                        esProfesor = esProfesor,
                        estado = estadoAula,
                        documentosPropios = documentos,
                        alAgregarAlumno = onAddStudent,
                        alEnviarMensaje = onSendMessage,
                        alCompartir = onShareMaterial,
                        alAsignarTarea = onAssignTask,
                        alComprobarApi = onCheckApi,
                        alAbrirMaterial = onOpenDocument,
                    )
                }

                SeccionMovil.MATERIAL -> {
                    EncabezadoSeccion(
                        titulo = "Material de estudio",
                        descripcion = "Tus documentos se guardan en este dispositivo y están disponibles sin conexión.",
                    )
                    AccionesMaterial(
                        documentos = documentos,
                        cargando = documentosCargando,
                        mensaje = documentosMensaje,
                        mensajeEsError = documentosMensajeEsError,
                        onChoosePdf = onChoosePdf,
                        onScan = onScan,
                    )
                    if (documentos.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Outlined.Description,
                            titulo = "Aún no hay documentos",
                            descripcion = "Carga un PDF para crear tu biblioteca de estudio.",
                        )
                    } else {
                        Text(
                            "Mis documentos · ${documentos.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = EsfocarInk,
                        )
                        documentos.forEach { documento ->
                            DocumentoCard(
                                documento = documento,
                                resumenExpandido = resumenExpandidoId == documento.id,
                                alExpandir = {
                                    resumenExpandidoId = if (resumenExpandidoId == documento.id) null else documento.id
                                },
                                alCambiarCategoria = { onChangeCategory(documento.id, it) },
                                alAbrir = { onOpenDocument(documento) },
                                alEliminar = { documentoAEliminar = documento },
                            )
                        }
                    }
                }

                SeccionMovil.REPASO -> {
                    EncabezadoSeccion(
                        titulo = "Repasa tus contenidos",
                        descripcion = "Genera preguntas a partir del texto de los PDF que has cargado.",
                    )
                    TriviaPanel(estadoTrivia, onLoadTrivia, onAnswerTrivia)
                }
            }
        }
    }

    documentoAEliminar?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoAEliminar = null },
            title = { Text("Eliminar documento") },
            text = { Text("¿Quieres eliminar «${documento.nombre}» de este dispositivo?") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDocument(documento)
                    documentoAEliminar = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentoAEliminar = null }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun PanelInicio(
    nombre: String,
    esProfesor: Boolean,
    estadoAula: EstadoAula,
    documentos: List<Documento>,
    irA: (SeccionMovil) -> Unit,
    alAbrirDocumento: (Documento) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                if (esProfesor) "PANEL DOCENTE" else "TU ESPACIO DE FORMACIÓN",
                style = MaterialTheme.typography.labelMedium,
                color = EsfocarGreen,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Hola, ${nombre.substringBefore(' ')}",
                style = MaterialTheme.typography.headlineMedium,
                color = EsfocarInk,
                fontWeight = FontWeight.Bold,
            )
            Text("¿Qué te gustaría hacer hoy?", color = EsfocarMuted, style = MaterialTheme.typography.bodyLarge)
        }

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = EsfocarGreenDark),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Surface(shape = RoundedCornerShape(50), color = EsfocarGold) {
                    Text(
                        "APRENDIZAJE A TU RITMO",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = EsfocarInk,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    "Todo tu material, en un solo lugar.",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Estudia tus PDF, revisa el aula y practica con preguntas basadas en tus documentos.",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = { irA(SeccionMovil.MATERIAL) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Ir a mis materiales", color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = EsfocarGold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (esProfesor) {
                ResumenContador(
                    valor = estadoAula.alumnos.size,
                    etiqueta = "Estudiantes",
                    icono = Icons.Outlined.School,
                    modifier = Modifier.weight(1f),
                )
            } else {
                ResumenContador(
                    valor = estadoAula.mensajes.size,
                    etiqueta = "Mensajes",
                    icono = Icons.Outlined.ChatBubbleOutline,
                    modifier = Modifier.weight(1f),
                )
                ResumenContador(
                    valor = estadoAula.tareas.size,
                    etiqueta = "Tareas",
                    icono = Icons.Outlined.Description,
                    modifier = Modifier.weight(1f),
                )
            }
            ResumenContador(
                valor = documentos.size,
                etiqueta = "PDF",
                icono = Icons.Outlined.PictureAsPdf,
                modifier = Modifier.weight(1f),
            )
        }

        Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AccesoRapido(
                titulo = "Aula",
                descripcion = if (esProfesor) "Estudiantes y comunicación" else "Mensajes y tareas",
                icono = Icons.Outlined.School,
                onClick = { irA(SeccionMovil.AULA) },
            )
            AccesoRapido(
                titulo = "Repasar",
                descripcion = "Preguntas desde tus PDF",
                icono = Icons.Outlined.Quiz,
                onClick = { irA(SeccionMovil.REPASO) },
            )
        }

        if (documentos.isNotEmpty()) {
            Text("Últimos documentos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            documentos.take(2).forEach { documento ->
                AccesoRapido(
                    titulo = documento.nombre,
                    descripcion = "PDF · ${documento.categoria}",
                    icono = Icons.Outlined.PictureAsPdf,
                    onClick = { alAbrirDocumento(documento) },
                )
            }
        }
    }
}

@Composable
private fun ResumenContador(
    valor: Int,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(icono, contentDescription = null, tint = EsfocarGreen, modifier = Modifier.size(21.dp))
            Text(valor.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = EsfocarMuted)
        }
    }
}

@Composable
private fun AccesoRapido(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = EsfocarGreen)
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = EsfocarMuted, maxLines = 1)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = EsfocarMuted)
        }
    }
}

@Composable
private fun EncabezadoSeccion(titulo: String, descripcion: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(titulo, style = MaterialTheme.typography.headlineSmall, color = EsfocarInk, fontWeight = FontWeight.Bold)
        Text(descripcion, style = MaterialTheme.typography.bodyMedium, color = EsfocarMuted)
    }
}

@Composable
private fun AccionesMaterial(
    documentos: List<Documento>,
    cargando: Boolean,
    mensaje: String?,
    mensajeEsError: Boolean,
    onChoosePdf: () -> Unit,
    onScan: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(46.dp),
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = null, tint = EsfocarGreen)
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Biblioteca personal", fontWeight = FontWeight.SemiBold)
                    Text("${documentos.size} documentos guardados", style = MaterialTheme.typography.bodySmall, color = EsfocarMuted)
                }
            }
            Button(
                onClick = onChoosePdf,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.Description, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (cargando) "Procesando PDF…" else "Cargar documento PDF")
            }
            OutlinedButton(
                onClick = onScan,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Outlined.PictureAsPdf, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Escanear con cámara")
            }
            mensaje?.let { InlineMessage(it, mensajeEsError) }
        }
    }
}

@Composable
private fun DocumentoCard(
    documento: Documento,
    resumenExpandido: Boolean,
    alExpandir: () -> Unit,
    alCambiarCategoria: (String) -> Unit,
    alAbrir: () -> Unit,
    alEliminar: () -> Unit,
) {
    var menuCategoriaAbierto by remember(documento.id) { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.PictureAsPdf, contentDescription = null, tint = EsfocarGreen)
                    }
                }
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(documento.nombre, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Box {
                        TextButton(
                            onClick = { menuCategoriaAbierto = true },
                            modifier = Modifier.height(44.dp),
                        ) {
                            Text("Carpeta: ${documento.categoria}")
                        }
                        DropdownMenu(
                            expanded = menuCategoriaAbierto,
                            onDismissRequest = { menuCategoriaAbierto = false },
                        ) {
                            listOf("General", "Clases", "Normativa", "Tareas", "Escaneos").forEach { categoria ->
                                DropdownMenuItem(
                                    text = { Text(categoria) },
                                    onClick = {
                                        menuCategoriaAbierto = false
                                        alCambiarCategoria(categoria)
                                    },
                                )
                            }
                        }
                    }
                }
            }
            Text("Resumen del documento", color = EsfocarGreen, style = MaterialTheme.typography.labelLarge)
            Text(
                documento.resumen,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (resumenExpandido) Int.MAX_VALUE else 3,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = alExpandir, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(if (resumenExpandido) "Ver menos" else "Ver resumen")
                }
                OutlinedButton(onClick = alAbrir, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text("Abrir PDF")
                }
                IconButton(onClick = alEliminar, modifier = Modifier.size(48.dp)) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Eliminar ${documento.nombre}",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(icono: ImageVector, titulo: String, descripcion: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(modifier = Modifier.size(54.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = EsfocarGreen, modifier = Modifier.size(26.dp))
                }
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(descripcion, color = EsfocarMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
