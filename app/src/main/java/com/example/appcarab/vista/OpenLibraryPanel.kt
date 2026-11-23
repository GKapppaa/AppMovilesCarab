package com.example.appcarab.vista

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.appcarab.R
import com.example.appcarab.modelo.LibroOpenLibrary
import com.example.appcarab.vistaModelo.EstadoOpenLibrary

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun OpenLibraryPanel(
    estado: EstadoOpenLibrary,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onOpenLink: (String) -> Unit,
) {
    val sugerencias = listOf(
        stringResource(R.string.openlibrary_chip_derecho),
        stringResource(R.string.openlibrary_chip_seguridad),
        stringResource(R.string.openlibrary_chip_criminologia),
        stringResource(R.string.openlibrary_chip_constitucion),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoEnCaja(Icons.AutoMirrored.Outlined.LibraryBooks)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(
                        stringResource(R.string.openlibrary_titulo),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Text(
                stringResource(R.string.openlibrary_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = estado.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(R.string.openlibrary_campo_buscar)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(estado.query) }),
                    trailingIcon = {
                        IconButton(onClick = { onSearch(estado.query) }) {
                            Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.openlibrary_btn_buscar))
                        }
                    },
                )
                Button(
                    onClick = { onSearch(estado.query) },
                    enabled = !estado.cargando && estado.query.isNotBlank(),
                    modifier = Modifier.height(56.dp),
                ) {
                    Text(stringResource(R.string.openlibrary_btn_buscar))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.openlibrary_sugerencias),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    sugerencias.forEach { sugerencia ->
                        AssistChip(
                            onClick = {
                                onQueryChange(sugerencia)
                                onSearch(sugerencia)
                            },
                            label = { Text(sugerencia) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            ),
                        )
                    }
                }
            }

            if (estado.cargando) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.openlibrary_cargando),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            estado.mensajeError?.let { error ->
                InlineMessage(message = error, isError = true)
            }

            if (estado.busquedaRealizada && estado.libros.isEmpty() && !estado.cargando && estado.mensajeError == null) {
                Text(
                    stringResource(R.string.openlibrary_sin_resultados, estado.query),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }

            if (estado.libros.isNotEmpty() && !estado.cargando) {
                Text(
                    stringResource(R.string.openlibrary_resultados_titulo, estado.libros.size),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )

                estado.libros.forEach { libro ->
                    TarjetaLibro(libro = libro, onOpenLink = onOpenLink)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaLibro(
    libro: LibroOpenLibrary,
    onOpenLink: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                libro.titulo,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                libro.autores,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            libro.anoPublicacion?.let { ano ->
                Text(
                    stringResource(R.string.openlibrary_publicado, ano),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (libro.temas.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    libro.temas.forEach { tema ->
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text(tema, style = MaterialTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { onOpenLink(libro.urlWeb) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = MaterialTheme.shapes.small,
            ) {
                Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.openlibrary_ver_detalle))
            }
        }
    }
}
