package com.example.appcarab.vista

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
private fun Bienvenida(onLogin: () -> Unit, onRegister: () -> Unit) {
    AuthPage {
        Spacer(Modifier.height(18.dp))
        BrandLogo()
        Spacer(Modifier.height(38.dp))
        Text("ESFOCAR", color = EsfocarGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        Text("Tu formación,\na tu alcance.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, lineHeight = 39.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "Repasa contenidos, practica con actividades y revisa material de estudio desde tu celular.",
            style = MaterialTheme.typography.bodyLarge,
            color = EsfocarMuted,
        )
        Spacer(Modifier.height(28.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FeatureLine("Estudia a tu ritmo", "Accede al material académico desde la app.")
                FeatureLine("Practica y repasa", "Prepara tus evaluaciones con ejercicios.")
                FeatureLine("Continúa sin conexión", "Consulta el contenido disponible localmente.")
            }
        }
        Spacer(Modifier.height(28.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Iniciar sesión", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) {
            Text("Crear cuenta", color = EsfocarGreen, fontWeight = FontWeight.SemiBold)
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
private fun FooterNote() {
    Spacer(Modifier.height(30.dp))
    Text(
        "Prototipo académico · DSY1105",
        modifier = Modifier.fillMaxWidth(),
        color = EsfocarMuted,
        style = MaterialTheme.typography.labelSmall,
    )
}

@Composable
private fun LogoInstitucional() {
    Text("ESCUELA DE FORMACIÓN DE CARABINEROS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
}

