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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            LogoInstitucional()

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF062817),
                shape = RoundedCornerShape(24.dp),
            ) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.width(46.dp).height(4.dp).background(EsfocarGold, RoundedCornerShape(50)))
                    Text(
                        "Tu formación,\na tu alcance.",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                    )
                    Text(
                        "Estudia, repasa y organiza tu material de formación desde un solo lugar.",
                        color = Color(0xFFE6EEE8),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                    )
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = onRegister,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsfocarGold,
                            contentColor = Color(0xFF17251B),
                        ),
                    ) {
                        Text("Comenzar   →", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.58f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    ) {
                        Text("Ya tengo cuenta", fontWeight = FontWeight.SemiBold)
                    }
                }
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
