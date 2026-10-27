package com.example.appcarab.vista

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
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
import com.example.appcarab.R
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
fun AccesoApp(viewModel: InicioSesionVistaModelo) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current
    var pdfName by rememberSaveable { mutableStateOf<String?>(null) }
    var pdfSize by rememberSaveable { mutableStateOf<Long?>(null) }
    var pdfError by rememberSaveable { mutableStateOf<String?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val mimeType = context.contentResolver.getType(uri)
        val details = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                (if (nameIndex >= 0) cursor.getString(nameIndex) else null) to
                    (if (sizeIndex >= 0) cursor.getLong(sizeIndex) else null)
            } else null
        }
        val name = details?.first.orEmpty()
        if (mimeType == "application/pdf" || name.endsWith(".pdf", ignoreCase = true)) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            pdfName = name.ifBlank { "Documento PDF" }
            pdfSize = details?.second
            pdfError = null
        } else {
            pdfError = "Selecciona un archivo PDF válido."
        }
    }

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
            PantallaAcceso.INICIO -> InicioEstudiante(
                correo = estado.correoUsuario.orEmpty(),
                pdfName = pdfName,
                pdfSize = pdfSize,
                pdfError = pdfError,
                onChoosePdf = { pdfPicker.launch(arrayOf("application/pdf")) },
                onLogout = {
                    pdfName = null
                    pdfSize = null
                    pdfError = null
                    viewModel.irA(PantallaAcceso.BIENVENIDA)
                },
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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            LogoInstitucional()

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    color = Color.White.copy(alpha = 0.9f),
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
    val emailError = enviado && !emailValido(email)
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
            supportingText = if (emailError) ({ Text("Ingresa un correo válido.") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(password, { password = it }, passwordError, "Contraseña")
        if (passwordError) Text("Ingresa tu contraseña.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                enviado = true
                if (emailValido(email) && password.isNotBlank()) onSubmit(email, password)
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text(if (cargando) "Verificando…" else "Ingresar") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Aún no tienes cuenta?", color = EsfocarMuted)
            TextButton(onClick = onRegister) { Text("Regístrate", color = EsfocarGreen, fontWeight = FontWeight.SemiBold) }
        }
        FooterNote()
    }
}

@Composable
private fun FormularioRegistro(
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmation by rememberSaveable { mutableStateOf("") }
    var enviado by rememberSaveable { mutableStateOf(false) }
    val emailError = enviado && !emailValido(email)
    val passwordError = enviado && !passwordValida(password)
    val confirmationError = enviado && password != confirmation

    AuthPage {
        TextButton(onClick = onBack) { Text("‹  Volver", color = EsfocarGreen) }
        LogoInstitucional()
        Spacer(Modifier.height(26.dp))
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Registro de prueba para estudiantes en formación.", color = EsfocarMuted)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Correo electrónico") },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ Text("Ingresa un correo válido.") }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )
        Spacer(Modifier.height(10.dp))
        PasswordField(password, { password = it }, passwordError, "Contraseña")
        if (passwordError) Text("Usa al menos 8 caracteres, con letras y números.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        PasswordField(confirmation, { confirmation = it }, confirmationError, "Confirmar contraseña")
        if (confirmationError) Text("Las contraseñas no coinciden.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                enviado = true
                if (emailValido(email) && passwordValida(password) && password == confirmation) onSubmit(email, password)
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
        ) { Text(if (cargando) "Creando cuenta…" else "Crear cuenta") }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("¿Ya tienes una cuenta?", color = EsfocarMuted)
            TextButton(onClick = onLogin) { Text("Inicia sesión", color = EsfocarGreen, fontWeight = FontWeight.SemiBold) }
        }
        FooterNote()
    }
}

@Composable
private fun InicioEstudiante(
    correo: String,
    pdfName: String?,
    pdfSize: Long?,
    pdfError: String?,
    onChoosePdf: () -> Unit,
    onLogout: () -> Unit,
) {
    AuthPage {
        LogoInstitucional()
        Spacer(Modifier.height(28.dp))
        Text("Área de estudiante", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(correo, color = EsfocarMuted)
        Spacer(Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Material de estudio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Selecciona un PDF autorizado para consultarlo desde tu dispositivo.", color = EsfocarMuted)
                OutlinedButton(onClick = onChoosePdf, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                    Text(if (pdfName == null) "Seleccionar PDF" else "Cambiar PDF", color = EsfocarGreen)
                }
                pdfName?.let {
                    Text("Documento seleccionado: $it", style = MaterialTheme.typography.bodyMedium)
                    pdfSize?.let { size -> Text(formatSize(size), color = EsfocarMuted, style = MaterialTheme.typography.bodySmall) }
                    Text("El archivo permanece en este dispositivo; no se envía a un servidor.", color = EsfocarMuted, style = MaterialTheme.typography.bodySmall)
                }
                pdfError?.let { InlineMessage(it, true) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Próximamente", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("Aquí estarán los contenidos, trivias y autoevaluaciones disponibles para el MVP.", color = EsfocarMuted)
        Spacer(Modifier.height(28.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Cerrar sesión", color = EsfocarGreen)
        }
        FooterNote()
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
private fun FeatureLine(title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.padding(top = 6.dp).size(10.dp).background(EsfocarGold, RoundedCornerShape(50)))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(detail, color = EsfocarMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
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
private fun FooterNote() {
    Spacer(Modifier.height(30.dp))
    Text(
        "Prototipo académico · DSY1105",
        modifier = Modifier.fillMaxWidth(),
        color = EsfocarMuted,
        style = MaterialTheme.typography.labelSmall,
    )
}

private fun emailValido(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

private fun passwordValida(password: String): Boolean =
    password.length >= 8 && password.any(Char::isLetter) && password.any(Char::isDigit)

private fun formatSize(size: Long): String = when {
    size < 1024 -> "$size B"
    size < 1024 * 1024 -> "${size / 1024} KB"
    else -> "${size / (1024 * 1024)} MB"
}
@Composable
private fun LogoInstitucional() {
    Image(
        painter = painterResource(R.drawable.logo_esfocar),
        contentDescription = "Logo oficial de la Escuela de Formación de Carabineros de Chile",
        modifier = Modifier.fillMaxWidth().height(76.dp),
    )
}
