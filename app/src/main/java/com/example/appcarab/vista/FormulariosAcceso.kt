package com.example.appcarab.vista

import com.example.appcarab.modelo.ValidadoresFormulario
import com.example.appcarab.ui.theme.EsfocarGreen
import com.example.appcarab.ui.theme.EsfocarMuted
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
internal fun FormularioSesion(
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
internal fun FormularioRegistro(
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
            label = { Text("Nombre y apellido (usa datos de prueba)") },
            singleLine = true,
            isError = nombreError,
            supportingText = if (nombreError) ({ ValidationHint("Usa 2 a 5 nombres; solo letras, tildes, espacios o guiones.") }) else null,
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
        if (passwordError) ValidationHint("Usa 8–64 caracteres e incluye mayúscula, minúscula, número y símbolo; sin espacios.")
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
internal fun FormularioPerfil(
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
            label = { Text("Nombre y apellido") },
            singleLine = true,
            isError = nombreError,
            supportingText = if (nombreError) ({ ValidationHint("Usa 2 a 5 nombres; solo letras, tildes, espacios o guiones.") }) else null,
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
internal fun PasswordField(value: String, onValueChange: (String) -> Unit, isError: Boolean, label: String) {
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
internal fun InlineMessage(message: String, isError: Boolean) {
    Text(
        message,
        modifier = Modifier.padding(top = 10.dp),
        color = if (isError) MaterialTheme.colorScheme.error else EsfocarGreen,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
internal fun ValidationHint(message: String) {
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
