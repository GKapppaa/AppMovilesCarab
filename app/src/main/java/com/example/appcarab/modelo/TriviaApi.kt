package com.example.appcarab.modelo

import android.text.Html
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class TriviaApiResponse(val response_code: Int, val results: List<TriviaApiQuestion>)
data class TriviaApiQuestion(
    val question: String,
    val correct_answer: String,
    val incorrect_answers: List<String>,
)

interface OpenTriviaService {
    @GET("api.php")
    suspend fun getQuestion(
        @Query("amount") amount: Int = 1,
        @Query("type") type: String = "multiple",
    ): TriviaApiResponse
}

class TriviaApiRepository(
    private val service: OpenTriviaService = Retrofit.Builder()
        .baseUrl("https://opentdb.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenTriviaService::class.java),
) {
    suspend fun question(): TriviaQuestion {
        val response = service.getQuestion()
        require(response.response_code == 0 && response.results.isNotEmpty()) { "La trivia externa no está disponible." }
        val result = response.results.first()
        val correct = decode(result.correct_answer)
        return TriviaQuestion(
            question = decode(result.question),
            correctAnswer = correct,
            answers = (result.incorrect_answers.map(::decode) + correct).shuffled(),
        )
    }

    private fun decode(value: String): String =
        Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY).toString()
}

data class TriviaQuestion(val question: String, val correctAnswer: String, val answers: List<String>)
