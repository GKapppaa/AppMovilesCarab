package com.example.appcarab.vista

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.appcarab.R
import com.example.appcarab.modelo.Documento
import com.example.appcarab.ui.theme.Esfocar
import com.example.appcarab.vistaModelo.EstadoAula
import com.example.appcarab.vistaModelo.EstadoTrivia

private enum class SeccionMovil(
    @StringRes val titulo: Int,
    val icono: ImageVector,
) {
    INICIO(R.string.nav_inicio, Icons.Outlined.Home),
    AULA(R.string.nav_aula, Icons.Outlined.School),
    MATERIAL(R.string.nav_material, Icons.Outlined.FolderOpen),
    REPASO(R.string.nav_repaso, Icons.Outlined.Quiz),
}

/** Las categorías se guardan en Room con su clave en español; aquí solo se traduce la etiqueta. */
private val CATEGORIAS = listOf(
    "General" to R.string.categoria_general,
    "Clases" to R.string.categoria_clases,
    "Normativa" to R.string.categoria_normativa,
    "Tareas" to R.string.categoria_tareas,
    "Escaneos" to R.string.categoria_escaneos,
)

@Composable
private fun nombreCategoria(clave: String): String =
    CATEGORIAS.firstOrNull { it.first == clave }?.let { stringResource(it.second) } ?: clave

@Composable
internal fun InicioEstudiante(
    nombre: String,
    codigoPerfil: String,
    correo: String,
    rol: String,
    estadoAula: EstadoAula,
    estadoTrivia: EstadoTrivia,
    sesionRemotaError: String?,
    onRetrySesionRemota: () -> Unit,
    onLoadTrivia: () -> Unit,
    onAnswerTrivia: (String) -> Unit,
    documentos: List<Documento>,
    documentosCargando: Boolean,
    documentosMensaje: String?,
    documentosMensajeEsError: Boolean,
    onChoosePdf: () -> Unit,
    onScan: () -> Unit,
    onDeleteDocument: (Documento) -> Unit,
    onUploadDocument: (Documento) -> Unit,
    onChangeCategory: (Int, String) -> Unit,
    onOpenDocument: (Documento) -> Unit,
    onAddStudent: (String) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onShareMaterial: (String, Int) -> Unit,
    onAssignTask: (String, String, String) -> Unit,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    var seccionGuardada by rememberSaveable { mutableStateOf(SeccionMovil.INICIO.name) }
    var documentoAEliminar by remember { mutableStateOf<Documento?>(null) }
    var resumenExpandidoId by rememberSaveable { mutableStateOf<Int?>(null) }
    val seccion = SeccionMovil.valueOf(seccionGuardada)
    val esProfesor = rol == "PROFESOR"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EncabezadoApp {
                LogoInstitucional(compact = true)
                Spacer(Modifier.weight(1f))
                BotonConfiguracion(onSettings)
                MenuPerfil(
                    nombre = nombre,
                    correo = correo,
                    codigoPerfil = codigoPerfil,
                    onEditProfile = onEditProfile,
                    onSettings = onSettings,
                    onLogout = onLogout,
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                SeccionMovil.entries.forEach { destino ->
                    NavigationBarItem(
                        selected = seccion == destino,
                        onClick = { seccionGuardada = destino.name },
                        icon = { Icon(destino.icono, contentDescription = null) },
                        label = { Text(stringResource(destino.titulo), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { insets ->
        Box(
            modifier = Modifier
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
                    .padding(top = 20.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                if (sesionRemotaError != null) {
                    InlineMessage(sesionRemotaError, isError = true)
                    OutlinedButton(
                        onClick = onRetrySesionRemota,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text(stringResource(R.string.sesion_remota_reintentar))
                    }
                }
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
                            antetitulo = stringResource(R.string.nav_aula),
                            titulo = stringResource(if (esProfesor) R.string.aula_titulo_profesor else R.string.aula_titulo_estudiante),
                            descripcion = stringResource(if (esProfesor) R.string.aula_desc_profesor else R.string.aula_desc_estudiante),
                        )
                        AulaPanel(
                            esProfesor = esProfesor,
                            estado = estadoAula,
                            documentosPropios = documentos,
                            alAgregarAlumno = onAddStudent,
                            alEnviarMensaje = onSendMessage,
                            alCompartir = onShareMaterial,
                            alAsignarTarea = onAssignTask,
                            alAbrirMaterial = onOpenDocument,
                        )
                    }

                    SeccionMovil.MATERIAL -> {
                        EncabezadoSeccion(
                            antetitulo = stringResource(R.string.nav_material),
                            titulo = stringResource(R.string.material_titulo),
                            descripcion = stringResource(R.string.material_desc),
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
                                titulo = stringResource(R.string.material_vacio_titulo),
                                descripcion = stringResource(R.string.material_vacio_desc),
                            )
                        } else {
                            TituloBloque(stringResource(R.string.material_mis_documentos, documentos.size))
                            documentos.forEach { documento ->
                                DocumentoCard(
                                    documento = documento,
                                    codigoPerfil = codigoPerfil,
                                    resumenExpandido = resumenExpandidoId == documento.id,
                                    alExpandir = {
                                        resumenExpandidoId = if (resumenExpandidoId == documento.id) null else documento.id
                                    },
                                    alCambiarCategoria = { onChangeCategory(documento.id, it) },
                                    alAbrir = { onOpenDocument(documento) },
                                    alEliminar = { documentoAEliminar = documento },
                                    alSubir = { onUploadDocument(documento) },
                                )
                            }
                        }
                    }

                    SeccionMovil.REPASO -> {
                        EncabezadoSeccion(
                            antetitulo = stringResource(R.string.nav_repaso),
                            titulo = stringResource(R.string.repaso_titulo),
                            descripcion = stringResource(R.string.repaso_desc),
                        )
                        TriviaPanel(estadoTrivia, onLoadTrivia, onAnswerTrivia)
                    }
                }
            }
        }
    }

    documentoAEliminar?.let { documento ->
        AlertDialog(
            onDismissRequest = { documentoAEliminar = null },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.eliminar_titulo)) },
            text = { Text(stringResource(R.string.eliminar_confirmar, documento.nombre)) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteDocument(documento)
                    documentoAEliminar = null
                }) {
                    Text(stringResource(R.string.accion_eliminar), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { documentoAEliminar = null }) { Text(stringResource(R.string.accion_cancelar)) }
            },
        )
    }
}

@Composable
private fun MenuPerfil(
    nombre: String,
    correo: String,
    codigoPerfil: String,
    onEditProfile: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
) {
    var abierto by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { abierto = true }, modifier = Modifier.size(48.dp)) {
            Avatar(nombre, tamano = 36)
        }
        DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).widthIn(min = 220.dp, max = 300.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(nombre, tamano = 40)
                Column(Modifier.padding(start = 12.dp)) {
                    Text(nombre, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        correo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (codigoPerfil.isNotBlank()) {
                        Text(
                            stringResource(R.string.perfil_id, codigoPerfil),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_editar_perfil)) },
                leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                onClick = {
                    abierto = false
                    onEditProfile()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.accion_configuracion)) },
                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                onClick = {
                    abierto = false
                    onSettings()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_cerrar_sesion), color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                onClick = {
                    abierto = false
                    onLogout()
                },
            )
        }
    }
}

@Composable
private fun Avatar(nombre: String, tamano: Int) {
    val iniciales = nombre.split(' ').filter(String::isNotBlank).take(2).joinToString("") { it.first().uppercase() }
    Surface(
        modifier = Modifier.size(tamano.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        border = BorderStroke(2.dp, Esfocar.colores.dorado),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (iniciales.isBlank()) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = stringResource(R.string.menu_perfil_desc), tint = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    iniciales,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = if (tamano >= 40) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun TituloBloque(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
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
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        EncabezadoSeccion(
            antetitulo = stringResource(if (esProfesor) R.string.inicio_eyebrow_profesor else R.string.inicio_eyebrow_estudiante),
            titulo = stringResource(R.string.inicio_saludo, nombre.substringBefore(' ')),
            descripcion = stringResource(R.string.inicio_pregunta),
        )

        TarjetaDestacada(onClick = { irA(SeccionMovil.MATERIAL) })

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (esProfesor) {
                ResumenContador(estadoAula.alumnos.size, stringResource(R.string.contador_estudiantes), Icons.Outlined.School, Modifier.weight(1f))
            } else {
                ResumenContador(estadoAula.mensajes.size, stringResource(R.string.contador_mensajes), Icons.Outlined.ChatBubbleOutline, Modifier.weight(1f))
                ResumenContador(estadoAula.tareas.size, stringResource(R.string.contador_tareas), Icons.Outlined.Description, Modifier.weight(1f))
            }
            ResumenContador(documentos.size, stringResource(R.string.contador_pdf), Icons.Outlined.PictureAsPdf, Modifier.weight(1f))
        }

        TituloBloque(stringResource(R.string.accesos_rapidos))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AccesoRapido(
                titulo = stringResource(R.string.nav_aula),
                descripcion = stringResource(if (esProfesor) R.string.acceso_aula_desc_profesor else R.string.acceso_aula_desc_estudiante),
                icono = Icons.Outlined.School,
                onClick = { irA(SeccionMovil.AULA) },
            )
            AccesoRapido(
                titulo = stringResource(R.string.acceso_repasar),
                descripcion = stringResource(R.string.acceso_repasar_desc),
                icono = Icons.Outlined.Quiz,
                onClick = { irA(SeccionMovil.REPASO) },
            )
        }

        if (documentos.isNotEmpty()) {
            TituloBloque(stringResource(R.string.ultimos_documentos))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                documentos.take(2).forEach { documento ->
                    AccesoRapido(
                        titulo = documento.nombre,
                        descripcion = stringResource(R.string.documento_desc, nombreCategoria(documento.categoria)),
                        icono = Icons.Outlined.PictureAsPdf,
                        onClick = { alAbrirDocumento(documento) },
                    )
                }
            }
        }
    }
}

/** Tarjeta principal: degradado verde institucional cerrado por la franja dorada. */
@Composable
private fun TarjetaDestacada(onClick: () -> Unit) {
    val colores = Esfocar.colores
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(colores.heroInicio, colores.heroFin))),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(shape = RoundedCornerShape(50), color = colores.dorado) {
                    Text(
                        stringResource(R.string.hero_pill).uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = colores.sobreDorado,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(stringResource(R.string.hero_titulo), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(
                    stringResource(R.string.hero_desc),
                    color = Color.White.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(2.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = colores.heroInicio,
                    ),
                ) {
                    Text(stringResource(R.string.hero_cta), style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            FranjaDorada(alto = 6.dp)
        }
    }
}

@Composable
private fun TarjetaBase(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable () -> Unit) {
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val borde = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = MaterialTheme.shapes.large, colors = colores, border = borde) { content() }
    } else {
        Card(modifier = modifier, shape = MaterialTheme.shapes.large, colors = colores, border = borde) { content() }
    }
}

@Composable
private fun ResumenContador(
    valor: Int,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
) {
    TarjetaBase(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconoEnCaja(icono, tamano = 34.dp)
            Text(valor.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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
    TarjetaBase(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconoEnCaja(icono)
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(titulo, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
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
    TarjetaBase {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoEnCaja(Icons.Outlined.FolderOpen, tamano = 46.dp)
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(stringResource(R.string.biblioteca_titulo), style = MaterialTheme.typography.titleSmall)
                    Text(
                        pluralStringResource(R.plurals.biblioteca_conteo, documentos.size, documentos.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Button(
                onClick = onChoosePdf,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Outlined.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (cargando) R.string.material_procesando else R.string.material_cargar))
            }
            OutlinedButton(
                onClick = onScan,
                enabled = !cargando,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(Icons.Outlined.DocumentScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.material_escanear))
            }
            mensaje?.let { InlineMessage(it, mensajeEsError) }
        }
    }
}

@Composable
private fun DocumentoCard(
    documento: Documento,
    codigoPerfil: String,
    resumenExpandido: Boolean,
    alExpandir: () -> Unit,
    alCambiarCategoria: (String) -> Unit,
    alAbrir: () -> Unit,
    alEliminar: () -> Unit,
    alSubir: () -> Unit,
) {
    var menuCategoriaAbierto by remember(documento.id) { mutableStateOf(false) }
    TarjetaBase {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                IconoEnCaja(Icons.Outlined.PictureAsPdf)
                Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(documento.nombre, style = MaterialTheme.typography.titleSmall)
                    if (documento.propietarioRemoto != null && documento.propietarioRemoto != codigoPerfil) {
                        Text(
                            stringResource(R.string.documento_compartido_por, documento.propietarioRemoto),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Box {
                        TextButton(
                            onClick = { menuCategoriaAbierto = true },
                            modifier = Modifier.heightIn(min = 44.dp),
                        ) {
                            Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(stringResource(R.string.documento_carpeta, nombreCategoria(documento.categoria)))
                        }
                        DropdownMenu(
                            expanded = menuCategoriaAbierto,
                            onDismissRequest = { menuCategoriaAbierto = false },
                        ) {
                            CATEGORIAS.forEach { (clave, etiqueta) ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(etiqueta)) },
                                    onClick = {
                                        menuCategoriaAbierto = false
                                        alCambiarCategoria(clave)
                                    },
                                )
                            }
                        }
                    }
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.documento_resumen).uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        documento.resumen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (resumenExpandido) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = alExpandir, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text(stringResource(if (resumenExpandido) R.string.ver_menos else R.string.ver_resumen), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                OutlinedButton(
                    onClick = alAbrir,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.abrir_pdf), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
                if (documento.propietarioRemoto == null || documento.propietarioRemoto == codigoPerfil) {
                    IconButton(onClick = alEliminar, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Outlined.DeleteOutline,
                            contentDescription = stringResource(R.string.eliminar_documento_desc, documento.nombre),
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            if (documento.idRemoto == null &&
                (documento.propietarioRemoto == null || documento.propietarioRemoto == codigoPerfil)
            ) {
                OutlinedButton(
                    onClick = alSubir,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(stringResource(R.string.doc_subir_para_compartir))
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(icono: ImageVector, titulo: String, descripcion: String) {
    TarjetaBase {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                }
            }
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}
