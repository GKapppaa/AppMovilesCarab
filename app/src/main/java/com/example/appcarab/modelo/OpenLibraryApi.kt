package com.example.appcarab.modelo

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class LibroOpenLibrary(
    val key: String,
    val titulo: String,
    val autores: String,
    val anoPublicacion: Int?,
    val temas: List<String>,
    val coverId: Long?,
) {
    val urlPortada: String?
        get() = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }

    val urlWeb: String
        get() = "https://openlibrary.org$key"
}

data class OpenLibrarySearchResponse(
    val numFound: Int = 0,
    val docs: List<OpenLibraryDoc>? = null,
)

data class OpenLibraryDoc(
    val key: String? = null,
    val title: String? = null,
    @SerializedName("author_name") val authorName: List<String>? = null,
    @SerializedName("first_publish_year") val firstPublishYear: Int? = null,
    @SerializedName("cover_i") val coverI: Long? = null,
    val subject: List<String>? = null,
)

interface OpenLibraryService {
    @GET("search.json")
    suspend fun buscarLibros(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10,
    ): OpenLibrarySearchResponse
}

class OpenLibraryRepository(
    private val service: OpenLibraryService = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(8, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build(),
        )
        .baseUrl("https://openlibrary.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenLibraryService::class.java),
) {
    suspend fun buscarLibros(query: String): List<LibroOpenLibrary> = withContext(Dispatchers.IO) {
        val q = query.trim()
        if (q.isBlank()) return@withContext emptyList()
        val response = service.buscarLibros(q)
        response.docs?.mapNotNull { doc ->
            val key = doc.key ?: return@mapNotNull null
            val title = doc.title ?: return@mapNotNull null
            LibroOpenLibrary(
                key = key,
                titulo = title,
                autores = doc.authorName?.joinToString(", ").orEmpty().ifBlank { "Autor no especificado" },
                anoPublicacion = doc.firstPublishYear,
                temas = doc.subject?.take(3).orEmpty(),
                coverId = doc.coverI,
            )
        }.orEmpty()
    }
}
