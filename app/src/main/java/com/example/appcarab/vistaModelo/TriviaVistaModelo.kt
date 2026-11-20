package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.modelo.TriviaApiRepository
import com.example.appcarab.modelo.TriviaQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EstadoTrivia(
    val pregunta: TriviaQuestion? = null,
    val cargando: Boolean = false,
    val respuestaElegida: String? = null,
    val mensaje: String? = null,
)

class TriviaVistaModelo(private val repository: TriviaApiRepository) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoTrivia())
    val estado: StateFlow<EstadoTrivia> = _estado.asStateFlow()

    fun cargarPregunta() {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null, respuestaElegida = null)
            runCatching { repository.question() }
                .onSuccess { _estado.value = EstadoTrivia(pregunta = it) }
                .onFailure { _estado.value = EstadoTrivia(mensaje = it.message ?: "API externa no disponible. El contenido local sigue accesible.") }
        }
    }

    fun responder(respuesta: String) {
        val pregunta = _estado.value.pregunta ?: return
        _estado.value = _estado.value.copy(
            respuestaElegida = respuesta,
            mensaje = if (respuesta == pregunta.correctAnswer) "Respuesta correcta." else "No es correcto. Intenta otra pregunta.",
        )
    }
}
