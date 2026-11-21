package com.example.appcarab.vistaModelo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appcarab.R
import com.example.appcarab.modelo.Textos
import com.example.appcarab.modelo.TriviaPdfRepository
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

class TriviaVistaModelo(
    private val repository: TriviaPdfRepository,
    private val textos: Textos,
) : ViewModel() {
    private val _estado = MutableStateFlow(EstadoTrivia())
    val estado: StateFlow<EstadoTrivia> = _estado.asStateFlow()

    fun cargarPregunta(usuarioId: Int?) {
        viewModelScope.launch {
            _estado.value = _estado.value.copy(cargando = true, mensaje = null, respuestaElegida = null)
            runCatching {
                requireNotNull(usuarioId) { textos.get(R.string.trivia_requiere_sesion) }
                repository.question(usuarioId)
            }
                .onSuccess { _estado.value = EstadoTrivia(pregunta = it) }
                .onFailure { _estado.value = EstadoTrivia(mensaje = it.message ?: textos.get(R.string.trivia_error_generar)) }
        }
    }

    fun responder(respuesta: String) {
        val pregunta = _estado.value.pregunta ?: return
        _estado.value = _estado.value.copy(
            respuestaElegida = respuesta,
            mensaje = if (respuesta == pregunta.correctAnswer) textos.get(R.string.trivia_correcta)
            else textos.get(R.string.trivia_incorrecta, pregunta.correctAnswer),
        )
    }
}
