package com.example.appcarab.modelo

import com.example.appcarab.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

data class ApiHealth(val status: String, val service: String)
data class RegistroApi(val id: Long, val tipo: String, val clave: String, val contenido: String)
data class RegistroApiRequest(val clave: String, val contenido: String)
data class ResultadoSync(val sincronizado: Boolean, val mensaje: String)

interface EsfocarApi {
    @GET("health")
    suspend fun health(): ApiHealth

    @POST("{tipo}")
    suspend fun crear(@Path("tipo") tipo: String, @Body body: RegistroApiRequest): RegistroApi

    @GET("{tipo}/{clave}")
    suspend fun obtener(@Path("tipo") tipo: String, @Path("clave") clave: String): RegistroApi

    @PUT("{tipo}/{clave}")
    suspend fun actualizar(
        @Path("tipo") tipo: String,
        @Path("clave") clave: String,
        @Body body: ActualizarRegistroRequest,
    ): RegistroApi

    @DELETE("{tipo}/{clave}")
    suspend fun eliminar(@Path("tipo") tipo: String, @Path("clave") clave: String)
}

data class ActualizarRegistroRequest(val contenido: String)

class ApiRestRepository(
    private val api: EsfocarApi = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .writeTimeout(3, TimeUnit.SECONDS)
                .callTimeout(4, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build(),
        )
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(EsfocarApi::class.java),
) {
    suspend fun health(): Result<ApiHealth> = runCatching { api.health() }

    /** Sincronización best-effort: Room sigue siendo la fuente local si el backend no está disponible. */
    suspend fun sincronizar(tipo: String, clave: String, contenido: String): ResultadoSync {
        return try {
            try {
                api.crear(tipo, RegistroApiRequest(clave, contenido))
            } catch (error: HttpException) {
                if (error.code() != 409) throw error
                api.actualizar(tipo, clave, ActualizarRegistroRequest(contenido))
            }
            val verificado = api.obtener(tipo, clave)
            ResultadoSync(verificado.contenido == contenido, "Sincronizado con el servicio remoto.")
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            ResultadoSync(false, "Guardado localmente; se sincronizará al volver la conexión.")
        }
    }
}
