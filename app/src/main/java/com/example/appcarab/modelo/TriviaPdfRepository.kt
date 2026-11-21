package com.example.appcarab.modelo

import android.content.Context
import com.example.appcarab.R
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class TriviaQuestion(
    val question: String,
    val correctAnswer: String,
    val answers: List<String>,
    val sourceDocument: String,
)

class TriviaPdfRepository(
    private val context: Context,
    private val documentoDao: DocumentoDao,
    private val textos: Textos = TextosAndroid(context),
) {
    suspend fun question(usuarioId: Int): TriviaQuestion = withContext(Dispatchers.IO) {
        val documentos = documentoDao.documentosDeUsuario(usuarioId)
        val textosPdf = documentos.mapNotNull { documento ->
            if (documento.categoria == "Escaneos") return@mapNotNull null
            val texto = documento.textoExtraido.ifBlank {
                val archivo = File(
                    context.filesDir,
                    "documentos/${documento.usuarioId}/${documento.archivoInterno}",
                )
                if (archivo.isFile) runCatching { ExtractorTextoPdf.extraer(context, archivo) }.getOrDefault("")
                else ""
            }
            if (texto.isNotBlank() && documento.textoExtraido.isBlank()) {
                documentoDao.actualizarTextoExtraido(documento.id, texto)
            }
            texto.takeIf(String::isNotBlank)?.let { documento to it }
        }

        require(textosPdf.isNotEmpty()) { textos.get(R.string.trivia_sin_texto) }
        val (documento, texto) = textosPdf.random()
        GeneradorTriviaPdf.generar(documento.nombre, texto)
    }
}

object GeneradorTriviaPdf {
    private val palabrasIgnoradas = setOf(
        "acerca", "además", "ahora", "algunas", "algunos", "ambos", "ante", "bajo",
        "cada", "casi", "como", "con", "contra", "cual", "cuando", "desde", "donde",
        "durante", "ellos", "ellas", "entre", "esta", "estas", "este", "estos",
        "fueron", "hacia", "hasta", "hay", "hecho", "incluso", "junto", "luego",
        "mediante", "mientras", "misma", "mismo", "muchos", "nuestra", "nuestro",
        "otros", "para", "parte", "pero", "puede", "pueden", "porque", "según",
        "siendo", "sobre", "también", "tanto", "tiene", "tienen", "todos", "través",
        "través", "unos", "unas", "usted", "varios",
    )
    private val palabraRegex = Regex("[\\p{L}]{5,}")
    private val oracionRegex = Regex("(?<=[.!?;])\\s+|\\n+")

    fun generar(nombreDocumento: String, texto: String): TriviaQuestion {
        val textoLimpio = texto.replace(Regex("\\s+"), " ").trim()
        val oraciones = textoLimpio.split(oracionRegex)
            .map(String::trim)
            .filter { it.length in 55..360 && palabraRegex.findAll(it).count() >= 8 }
        require(oraciones.isNotEmpty()) {
            "No encontré oraciones suficientes en «$nombreDocumento». Prueba con un PDF más extenso."
        }

        val frecuencia = palabraRegex.findAll(textoLimpio)
            .map { it.value.lowercase(Locale.ROOT) }
            .filterNot(palabrasIgnoradas::contains)
            .groupingBy { it }
            .eachCount()
        val candidatos = oraciones.mapNotNull { oracion ->
            val terminos = palabraRegex.findAll(oracion)
                .map { it.value }
                .filter { termino ->
                    val minuscula = termino.lowercase(Locale.ROOT)
                    termino.length in 5..18 &&
                        minuscula !in palabrasIgnoradas &&
                        (frecuencia[minuscula] ?: 0) > 0
                }
                .distinctBy { it.lowercase(Locale.ROOT) }
            val respuesta = terminos.maxByOrNull { termino ->
                val frecuenciaTermino = frecuencia[termino.lowercase(Locale.ROOT)] ?: 0
                val nombrePropio = termino.firstOrNull()?.isUpperCase() == true
                frecuenciaTermino + termino.length / 10 + if (nombrePropio) 2 else 0
            } ?: return@mapNotNull null
            oracion to respuesta
        }
        require(candidatos.isNotEmpty()) {
            "No encontré conceptos claros en «$nombreDocumento». Prueba con otro PDF con más texto."
        }

        val (oracion, respuesta) = candidatos.random()
        val distractores = frecuencia.keys
            .filter { it != respuesta.lowercase(Locale.ROOT) && it.length in 5..18 }
            .shuffled()
            .take(3)
            .map { it.replaceFirstChar { first -> first.uppercase() } }
        require(distractores.size == 3) {
            "El PDF «$nombreDocumento» necesita más conceptos distintos para generar alternativas."
        }

        val marcador = Regex(
            "(?<![\\p{L}])${Regex.escape(respuesta)}(?![\\p{L}])",
            RegexOption.IGNORE_CASE,
        )
        val oracionConEspacio = marcador.replaceFirst(oracion, "_____")
        return TriviaQuestion(
            question = "Según «$nombreDocumento», ¿qué palabra completa la idea?\n\n$oracionConEspacio",
            correctAnswer = respuesta,
            answers = (distractores + respuesta).shuffled(),
            sourceDocument = nombreDocumento,
        )
    }
}
