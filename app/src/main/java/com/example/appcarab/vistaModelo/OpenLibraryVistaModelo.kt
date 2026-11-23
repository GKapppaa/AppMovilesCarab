package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.R
import com.example.appcarab.modelo.LibroOpenLibrary
import com.example.appcarab.modelo.OpenLibraryRepository
import com.example.appcarab.modelo.Textos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoOpenLibrary(
    val query: String = "Ley Chile",
    val libros: List<LibroOpenLibrary> = emptyList(),
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val busquedaRealizada: Boolean = false,
)

class OpenLibraryVistaModelo(
    private val repository: OpenLibraryRepository = OpenLibraryRepository(),
    private val textos: Textos,
    private val coroutineScope: CoroutineScope? = null,
) : ViewModel() {
    private val scope: CoroutineScope get() = coroutineScope ?: viewModelScope
    private val _estado = MutableStateFlow(EstadoOpenLibrary())
    val estado: StateFlow<EstadoOpenLibrary> = _estado.asStateFlow()

    fun cambiarQuery(nuevaQuery: String) {
        _estado.value = _estado.value.copy(query = nuevaQuery)
    }

    fun buscar(query: String = _estado.value.query): Job {
        val q = query.trim()
        if (q.isBlank()) return scope.launch {}
        return scope.launch {
            _estado.value = _estado.value.copy(query = q, cargando = true, mensajeError = null)
            runCatching {
                repository.buscarLibros(q)
            }.onSuccess { libros ->
                _estado.value = _estado.value.copy(
                    libros = libros,
                    cargando = false,
                    busquedaRealizada = true,
                )
            }.onFailure { error ->
                _estado.value = _estado.value.copy(
                    cargando = false,
                    mensajeError = error.message ?: textos.get(R.string.openlibrary_error_buscar),
                    busquedaRealizada = true,
                )
            }
        }
    }
}
