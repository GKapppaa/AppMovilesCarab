package com.example.appcarab.vista

import android.animation.ValueAnimator
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appcarab.R
import com.example.appcarab.ui.theme.EsfocarGold
import com.example.appcarab.ui.theme.EsfocarGreenDark
import kotlinx.coroutines.delay

@Composable
internal fun Bienvenida(onLogin: () -> Unit, onRegister: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        FondoFotograficoAnimado()

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.5f),
                        0.3f to Color.Transparent,
                        0.6f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                LogoInstitucional()
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xCC0A1A12), // Verde oscuro traslúcido
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Tu formación,\n")
                            withStyle(style = SpanStyle(color = EsfocarGold)) {
                                append("a tu alcance.")
                            }
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 38.sp
                    )

                    Text(
                        text = "Estudia, repasa y organiza tu material de formación desde un solo lugar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = onLogin,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EsfocarGreenDark,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Iniciar sesión",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.graphicsLayer { rotationZ = 0f }
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, EsfocarGold.copy(alpha = 0.8f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = EsfocarGold
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Crear cuenta",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
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
                initialValue = 1.02f,
                targetValue = 1.08f,
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
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = escala
                        scaleY = escala
                    },
            )
        }
    }
}