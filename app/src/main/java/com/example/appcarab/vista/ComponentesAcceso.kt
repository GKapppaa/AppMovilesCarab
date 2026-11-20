package com.example.appcarab.vista

import androidx.compose.ui.res.painterResource
import com.example.appcarab.R
import com.example.appcarab.ui.theme.EsfocarGold
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun AuthPage(content: @Composable ColumnScope.() -> Unit) {
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
internal fun LogoInstitucional(modifier: Modifier = Modifier, compact: Boolean = false) {
    Surface(
        modifier = modifier.padding(start = if (compact) 0.dp else 18.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.88f),
    ) {
        Image(
            painter = painterResource(R.drawable.logo_esfocar),
            contentDescription = "Logo oficial de la Escuela de Formación de Carabineros de Chile",
            modifier = Modifier
                .padding(horizontal = if (compact) 7.dp else 9.dp, vertical = 5.dp)
                .width(if (compact) 156.dp else 202.dp)
                .height(if (compact) 34.dp else 44.dp),
        )
    }
}
