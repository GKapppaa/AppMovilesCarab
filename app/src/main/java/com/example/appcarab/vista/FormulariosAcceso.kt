package com.example.appcarab.vista

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appcarab.R
import com.example.appcarab.modelo.ValidadoresFormulario
import com.example.appcarab.ui.theme.Esfocar

@Composable
private fun TituloFormulario(titulo: String, subtitulo: String) {
    LogoInstitucional()
    Spacer(Modifier.height(28.dp))
    Text(titulo, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
    Spacer(Modifier.height(6.dp))
    Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun LineaAlternativa(pregunta: String, accion: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(pregunta, color = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = onClick, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(accion, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun FormularioSesion(
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onRegister: () -> Unit,
    onSettings: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var enviado by rememberSaveable { mutableStateOf(false) }
    val emailError = enviado && !ValidadoresFormulario.correoValido(email)
    val passwordError = enviado && password.isBlank()
    val enviar = {
        enviado = true
        if (ValidadoresFormulario.correoValido(email) && password.isNotBlank()) onSubmit(email, password)
    }

    AuthPage(onBack = onBack, onSettings = onSettings) {
        TituloFormulario(stringResource(R.string.sesion_titulo), stringResource(R.string.sesion_subtitulo))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_correo)) },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ ValidationHint(stringResource(R.string.error_correo)) }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(password, { password = it }, passwordError, stringResource(R.string.campo_contrasena))
        if (passwordError) ValidationHint(stringResource(R.string.error_contrasena_vacia))
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = enviar,
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                stringResource(if (cargando) R.string.sesion_verificando else R.string.sesion_ingresar),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Spacer(Modifier.height(4.dp))
        LineaAlternativa(stringResource(R.string.sesion_sin_cuenta), stringResource(R.string.sesion_registrate), onRegister)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun FormularioRegistro(
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onSettings: () -> Unit,
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

    AuthPage(onBack = onBack, onSettings = onSettings) {
        TituloFormulario(stringResource(R.string.crear_cuenta), stringResource(R.string.registro_subtitulo))
        Text(
            stringResource(R.string.registro_tipo_perfil),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                Triple("ESTUDIANTE", R.string.rol_estudiante, Icons.Outlined.School),
                Triple("PROFESOR", R.string.rol_profesor, Icons.Outlined.Badge),
            ).forEach { (valor, etiqueta, icono) ->
                FilterChip(
                    selected = rol == valor,
                    onClick = { rol = valor },
                    label = { Text(stringResource(etiqueta)) },
                    leadingIcon = { Icon(icono, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.heightIn(min = 40.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (rol == "PROFESOR") {
            OutlinedTextField(
                value = codigoProfesor,
                onValueChange = { codigoProfesor = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.campo_codigo_docente)) },
                singleLine = true,
                isError = enviado && !ValidadoresFormulario.codigoProfesorValido(codigoProfesor),
                supportingText = if (enviado && !ValidadoresFormulario.codigoProfesorValido(codigoProfesor)) {
                    ({ ValidationHint(stringResource(R.string.error_codigo_docente)) })
                } else null,
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(10.dp))
        }
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_nombre_prueba)) },
            singleLine = true,
            isError = nombreError,
            supportingText = if (nombreError) ({ ValidationHint(stringResource(R.string.error_nombre)) }) else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_correo)) },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ ValidationHint(stringResource(R.string.error_correo)) }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.height(10.dp))
        PasswordField(password, { password = it }, passwordError, stringResource(R.string.campo_contrasena))
        if (passwordError) ValidationHint(stringResource(R.string.error_contrasena_reglas))
        Spacer(Modifier.height(10.dp))
        PasswordField(confirmation, { confirmation = it }, confirmationError, stringResource(R.string.campo_confirmar_contrasena))
        if (confirmationError) ValidationHint(stringResource(R.string.error_contrasenas_distintas))
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(24.dp))
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
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                stringResource(if (cargando) R.string.registro_creando else R.string.crear_cuenta),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Spacer(Modifier.height(4.dp))
        LineaAlternativa(stringResource(R.string.registro_con_cuenta), stringResource(R.string.registro_inicia_sesion), onLogin)
    }
}

@Composable
internal fun FormularioPerfil(
    nombreActual: String,
    correoActual: String,
    mensaje: String?,
    cargando: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var nombre by rememberSaveable(nombreActual) { mutableStateOf(nombreActual) }
    var email by rememberSaveable(correoActual) { mutableStateOf(correoActual) }
    var enviado by rememberSaveable { mutableStateOf(false) }
    val nombreError = enviado && !ValidadoresFormulario.nombreValido(nombre)
    val emailError = enviado && !ValidadoresFormulario.correoValido(email)

    AuthPage(onBack = onBack, onSettings = onSettings) {
        TituloFormulario(stringResource(R.string.perfil_titulo), stringResource(R.string.perfil_subtitulo))
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_nombre)) },
            singleLine = true,
            isError = nombreError,
            supportingText = if (nombreError) ({ ValidationHint(stringResource(R.string.error_nombre)) }) else null,
            shape = MaterialTheme.shapes.medium,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trimStart() },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.campo_correo)) },
            singleLine = true,
            isError = emailError,
            supportingText = if (emailError) ({ ValidationHint(stringResource(R.string.error_correo)) }) else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = MaterialTheme.shapes.medium,
        )
        mensaje?.let { InlineMessage(it, true) }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                enviado = true
                if (ValidadoresFormulario.nombreValido(nombre) && ValidadoresFormulario.correoValido(email)) onSubmit(nombre, email)
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                stringResource(if (cargando) R.string.perfil_guardando else R.string.perfil_guardar),
                style = MaterialTheme.typography.titleSmall,
            )
        }
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
        shape = MaterialTheme.shapes.medium,
        trailingIcon = {
            TextButton(onClick = { visible = !visible }) {
                Text(
                    stringResource(if (visible) R.string.contrasena_ocultar else R.string.contrasena_mostrar),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
    )
}

/** Aviso en línea con fondo tenue: verde para confirmaciones y rojo para errores. */
@Composable
internal fun InlineMessage(message: String, isError: Boolean) {
    val fondo = if (isError) MaterialTheme.colorScheme.errorContainer else Esfocar.colores.exitoContenedor
    val texto = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = MaterialTheme.shapes.small,
        color = fondo,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                if (isError) Icons.Outlined.ErrorOutline else Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = texto,
                modifier = Modifier.size(20.dp),
            )
            Text(message, color = texto, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun ValidationHint(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = stringResource(R.string.validacion_error_desc),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}
