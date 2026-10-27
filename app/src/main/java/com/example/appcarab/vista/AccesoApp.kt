package com.example.appcarab.vista

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.appcarab.vistaModelo.InicioSesionVistaModelo
import com.example.appcarab.vistaModelo.PantallaAcceso
import com.example.appcarab.vistaModelo.DocumentosVistaModelo
import com.example.appcarab.R
import com.example.appcarab.modelo.Documento
import com.example.appcarab.modelo.ValidadoresFormulario
import com.example.appcarab.ui.theme.EsfocarGold
import com.example.appcarab.ui.theme.EsfocarGreen
import com.example.appcarab.ui.theme.EsfocarGreenDark
import com.example.appcarab.ui.theme.EsfocarMuted

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay


@Composable
fun AccesoApp(viewModel: InicioSesionVistaModelo, documentosViewModel: DocumentosVistaModelo) {
    val estado by viewModel.estado.collectAsState()
    val estadoDocumentos by documentosViewModel.estado.collectAsState()
    val context = LocalContext.current
    var uriEscaneo by rememberSaveable { mutableStateOf<String?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val usuarioId = estado.usuarioId
        if (uri != null && usuarioId != null) documentosViewModel.agregar(usuarioId, uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardado ->
        val uri = uriEscaneo?.let(Uri::parse)
        val usuarioId = estado.usuarioId
        if (guardado && uri != null && usuarioId != null) documentosViewModel.agregarEscaneo(usuarioId, uri)
    }
    LaunchedEffect(estado.usuarioId) { estado.usuarioId?.let(documentosViewModel::observar) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (estado.pantalla) {
            PantallaAcceso.BIENVENIDA -> Bienvenida(
                onLogin = { viewModel.irA(PantallaAcceso.INICIAR_SESION) },
                onRegister = { viewModel.irA(PantallaAcceso.REGISTRO) },
            )
            PantallaAcceso.INICIAR_SESION -> FormularioSesion(
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.BIENVENIDA) },
                onRegister = { viewModel.irA(PantallaAcceso.REGISTRO) },
                onSubmit = viewModel::iniciarSesion,
            )
            PantallaAcceso.REGISTRO -> FormularioRegistro(
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.BIENVENIDA) },
                onLogin = { viewModel.irA(PantallaAcceso.INICIAR_SESION) },
                onSubmit = viewModel::registrar,
            )
            PantallaAcceso.EDITAR_PERFIL -> FormularioPerfil(
                nombreActual = estado.nombreUsuario,
                correoActual = estado.correoUsuario.orEmpty(),
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.INICIO) },
                onSubmit = viewModel::guardarPerfil,
            )
            PantallaAcceso.INICIO -> InicioEstudiante(
                nombre = estado.nombreUsuario,
                codigoPerfil = estado.codigoPerfil,
                correo = estado.correoUsuario.orEmpty(),
                rol = estado.rolUsuario,
                documentos = estadoDocumentos.documentos,
                documentosCargando = estadoDocumentos.cargando,
                documentosMensaje = estadoDocumentos.mensaje,
                documentosMensajeEsError = estadoDocumentos.mensajeEsError,
                onChoosePdf = { pdfPicker.launch(arrayOf("application/pdf")) },
                onScan = {
                    estado.usuarioId?.let { usuarioId ->
                        documentosViewModel.uriTemporalEscaneo(usuarioId).also {
                            uriEscaneo = it.toString()
                            cameraLauncher.launch(it)
                        }
                    }
                },
                onDeleteDocument = documentosViewModel::eliminar,
                onChangeCategory = documentosViewModel::cambiarCategoria,
                onOpenDocument = { documento ->
                    val uri = documentosViewModel.uriParaAbrir(documento)
                    if (uri == null) {
                        Toast.makeText(context, "No se encontró el archivo guardado.", Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        runCatching { context.startActivity(Intent.createChooser(intent, "Abrir PDF")) }
                            .onFailure { Toast.makeText(context, "No hay una aplicación disponible para abrir PDF.", Toast.LENGTH_LONG).show() }
                    }
                },
                onEditProfile = { viewModel.irA(PantallaAcceso.EDITAR_PERFIL) },
                onLogout = viewModel::cerrarSesion,
            )
        }
    }
}

@Composable
private fun Bienvenida(onLogin: () -> Unit, onRegister: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        FondoFotograficoAnimado()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            LogoInstitucional()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xD908180F),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                shadowElevation = 12.dp,
            ) {
                Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "ESCUELA DE FORMACIÓN · CHILE",
                        color = EsfocarGold,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp,
                    )
                    Text(
                        "Tu formación,\na tu alcance.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 43.sp,
                    )
                    Text(
                        "Repasa, practica y fortalece tu preparación desde cualquier lugar.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 25.sp,
                    )
                    Box(
                        Modifier
                            .padding(top = 4.dp)
                            .width(54.dp)
                            .height(4.dp)
                            .background(EsfocarGold, RoundedCornerShape(50)),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EsfocarGreenDark),
                ) {
                    Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onRegister,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                ) {
                    Text("Crear cuenta", fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "MATERIAL DE APOYO PARA TU FORMACIÓN",
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    color = Color.White.copy(alpha = 0.74f),
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                )
            }
        }
    }
}

@Composable
private fun FondoFotograficoAnimado() {
    val fotografias = remember {
        listOf(
            R.drawable.fondo_esfocar_01,
            R.drawable.fondo_esfocar_02,
            R.drawable.fondo_esfocar_03,
            R.drawable.fondo_esfocar_04,
            R.drawable.fondo_esfocar_05,
        )
    }
    val movimientoHabilitado = remember { ValueAnimator.areAnimatorsEnabled() }
    var indice by remember { mutableIntStateOf(0) }

    LaunchedEffect(movimientoHabilitado) {
        if (movimientoHabilitado) {
            while (true) {
                delay(6_200)
                indice = (indice + 1) % fotografias.size
            }
        }
    }

    val escala = if (movimientoHabilitado) {
        rememberInfiniteTransition(label = "movimiento fotográfico")
            .animateFloat(
                initialValue = 1.03f,
                targetValue = 1.10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 9_000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "zoom sutil",
            ).value
    } else {
        1.04f
    }

    Box(Modifier.fillMaxSize()) {
        Crossfade(
            targetState = if (movimientoHabilitado) indice else 0,
            animationSpec = tween(durationMillis = 1_500, easing = LinearEasing),
            label = "transición automática de fotografías",
        ) { fotografia ->
            Image(
                painter = painterResource(fotografias[fotografia]),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                },
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color(0x990D1A12),
                        0.42f to Color(0x99101D14),
                        1f to Color(0xE6001008),
                    ),
                ),
        )
    }
}

@Composable
private fun FormularioSesion(
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var enviado by rememberSaveable { mutableStateOf(false) }
    val emailError = enviado && !ValidadoresFormulario.correoValido(email)
    val passwordError = enviado && password.isBlank()

    AuthPage {
        TextButton(onClick = onBack) { Text("‹  Volver", color = EsfocarGreen) }
        LogoInstitucional()
        Spacer(Modifier.height(30.dp))
        Text("Bienvenido", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Ingresa con tu cuenta de estudiante.", color = EsfocarMuted)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ ValidationHint("Ingresa un correo válido.") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(password, { password = it }, passwordError, "Contraseña")
        if (passwordError) ValidationHint("Ingresa tu contraseña.")
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                enviado = true
                if (ValidadoresFormulario.correoValido(email) && password.isNotBlank()) onSubmit(email, password)
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text(if (cargando) "Verificando…" else "Ingresar") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Aún no tienes cuenta?", color = EsfocarMuted)
            TextButton(onClick = onRegister) { Text("Regístrate", color = EsfocarGreen, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun FormularioRegistro(
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSubmit: (String, String, String, String, String) -> Unit,
) {
    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var rol by rememberSaveable { mutableStateOf("ESTUDIANTE") }
    var codigoProfesor by rememberSaveable { mutableStateOf("") }
    var enviado by rememberSaveable { mutableStateOf(false) }
    val nombreError = enviado && !ValidadoresFormulario.nombreValido(nombre)
    val emailError = enviado && !ValidadoresFormulario.correoValido(email)
    val passwordError = enviado && !ValidadoresFormulario.contrasenaValida(password)
    val confirmationError = enviado && !ValidadoresFormulario.contrasenasCoinciden(password, confirmation)

    AuthPage {
        TextButton(onClick = onBack) { Text("‹  Volver", color = EsfocarGreen) }
        LogoInstitucional()
        Spacer(Modifier.height(26.dp))
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Crea tu perfil de formación.", color = EsfocarMuted)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de perfil (usa un dato de prueba)") },
            singleLine = true,
            isError = nombreError,
            supportingText = if (nombreError) ({ ValidationHint("Ingresa al menos 3 caracteres.") }) else null,
        )
        Spacer(Modifier.height(16.dp))
        Text("Tipo de perfil", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = rol == "ESTUDIANTE",
                onClick = { rol = "ESTUDIANTE" },
                label = { Text("Estudiante") },
            )
            FilterChip(
                selected = rol == "PROFESOR",
                onClick = { rol = "PROFESOR" },
                label = { Text("Profesor") },
            )
        }
        if (rol == "PROFESOR") {
            OutlinedTextField(
                value = codigoProfesor,
                onValueChange = { codigoProfesor = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Código de invitación docente") },
                singleLine = true,
                isError = enviado && !ValidadoresFormulario.codigoProfesorValido(codigoProfesor),
                supportingText = if (enviado && !ValidadoresFormulario.codigoProfesorValido(codigoProfesor)) {
                    ({ ValidationHint("Ingresa el código de invitación entregado por el equipo docente.") })
                } else null,
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ ValidationHint("Ingresa un correo válido.") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(10.dp))
        PasswordField(password, { password = it }, passwordError, "Contraseña")
        if (passwordError) ValidationHint("Usa 8 caracteres como mínimo, con letras y números.")
        Spacer(Modifier.height(10.dp))
        PasswordField(confirmation, { confirmation = it }, confirmationError, "Confirmar contraseña")
        if (confirmationError) ValidationHint("Las contraseñas no coinciden.")
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                enviado = true
                if (ValidadoresFormulario.nombreValido(nombre) && ValidadoresFormulario.correoValido(email) &&
                    ValidadoresFormulario.contrasenaValida(password) &&
                    ValidadoresFormulario.contrasenasCoinciden(password, confirmation)
                ) {
                    onSubmit(nombre, email, password, rol, codigoProfesor)
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text(if (cargando) "Creando cuenta…" else "Crear cuenta") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes una cuenta?", color = EsfocarMuted)
            TextButton(onClick = onLogin) { Text("Inicia sesión", color = EsfocarGreen, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun FormularioPerfil(
    nombreActual: String,
    correoActual: String,
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var nombre by rememberSaveable(nombreActual) { mutableStateOf(nombreActual) }
    var email by rememberSaveable(correoActual) { mutableStateOf(correoActual) }
    var enviado by rememberSaveable { mutableStateOf(false) }
    val nombreError = enviado && !ValidadoresFormulario.nombreValido(nombre)
    val emailError = enviado && !ValidadoresFormulario.correoValido(email)

    AuthPage {
        TextButton(onClick = onBack) { Text("‹  Volver", color = EsfocarGreen) }
        LogoInstitucional()
        Spacer(Modifier.height(24.dp))
        Text("Editar perfil", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Actualiza tu nombre de perfil y correo.", color = EsfocarMuted)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nombre de perfil") },
            singleLine = true,
            isError = nombreError,
            supportingText = if (nombreError) ({ ValidationHint("Ingresa al menos 3 caracteres.") }) else null,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ ValidationHint("Ingresa un correo válido.") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                enviado = true
                if (ValidadoresFormulario.nombreValido(nombre) && ValidadoresFormulario.correoValido(email)) onSubmit(nombre, email)
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(if (cargando) "Guardando…" else "Guardar cambios") }
    }
}

@Composable
private fun InicioEstudiante(
    nombre: String,
    codigoPerfil: String,
    correo: String,
    rol: String,
    documentos: List<Documento>,
    documentosCargando: Boolean,
    documentosMensaje: String?,
    documentosMensajeEsError: Boolean,
    onChoosePdf: () -> Unit,
    onScan: () -> Unit,
    onDeleteDocument: (Documento) -> Unit,
    onChangeCategory: (Int, String) -> Unit,
    onOpenDocument: (Documento) -> Unit,
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

@Composable
private fun AuthPage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
        content = {
            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(5.dp).background(EsfocarGold, RoundedCornerShape(50)))
            Spacer(Modifier.height(20.dp))
            content()
            Spacer(Modifier.height(24.dp))
        },
    )
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, isError: Boolean, label: String) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(if (visible) "Ocultar" else "Mostrar", color = EsfocarGreen)
            }
        },
    )
}

@Composable
private fun InlineMessage(message: String, isError: Boolean) {
    Text(
        message,
        modifier = Modifier.padding(top = 10.dp),
        color = if (isError) MaterialTheme.colorScheme.error else EsfocarGreen,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun ValidationHint(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error de validación",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun LogoInstitucional() {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 10.dp,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_esfocar),
            contentDescription = "Logo oficial de la Escuela de Formación de Carabineros de Chile",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp).width(224.dp).height(52.dp),
        )
    }
}
