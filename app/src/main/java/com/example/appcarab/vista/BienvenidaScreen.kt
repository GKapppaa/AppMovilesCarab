package com.example.appcarab.vista

import android.animation.ValueAnimator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.example.appcarab.R
import com.example.appcarab.ui.theme.EsfocarGold
import com.example.appcarab.ui.theme.EsfocarGreenDark
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
internal fun Bienvenida(onLogin: () -> Unit, onRegister: () -> Unit) {
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
