package com.example.appcarab.modelo

import com.example.appcarab.BuildConfig
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

data class AbrirSesionRequest(val codigoPerfil: String, val rol: String)
data class SesionRemota(val token: String, val codigoPerfil: String, val rol: String, val expiraEn: String)
data class DocumentoRemoto(
    val id: String,
    val propietario: String,
    val nombre: String,
    val resumen: String,
    val categoria: String,
    val bytes: Long,
    val creadoEn: String,
)
data class CompartirDocumentoRequest(val codigoAlumno: String)
data class VincularAlumnoRequest(val codigoAlumno: String)
data class AlumnoVinculadoRemoto(val codigoPerfil: String, val nombre: String)
data class ComparticionRemota(
    val id: String,
    val documentoId: String,
    val profesor: String,
    val alumno: String,
    val compartidoEn: String,
)

interface ApiDocumentosRemotos {
    @POST("sesiones")
    suspend fun abrirSesion(@Body body: AbrirSesionRequest): SesionRemota

    @DELETE("sesiones/actual")
    suspend fun cerrarSesion(@Header("Authorization") authorization: String)

    @POST("aula/alumnos")
    suspend fun vincularAlumno(
        @Header("Authorization") authorization: String,
        @Body body: VincularAlumnoRequest,
    ): AlumnoVinculadoRemoto

    @GET("aula/alumnos")
    suspend fun listarAlumnos(@Header("Authorization") authorization: String): List<AlumnoVinculadoRemoto>

    @Multipart
    @POST("documentos")
    suspend fun subirPdf(
        @Header("Authorization") authorization: String,
        @Part archivo: MultipartBody.Part,
        @Part("resumen") resumen: okhttp3.RequestBody,
        @Part("categoria") categoria: okhttp3.RequestBody,
    ): DocumentoRemoto

    @GET("documentos/mios")
    suspend fun listarPropios(@Header("Authorization") authorization: String): List<DocumentoRemoto>

    @GET("documentos/compartidos")
    suspend fun listarCompartidos(@Header("Authorization") authorization: String): List<DocumentoRemoto>

    @POST("documentos/{id}/compartir")
    suspend fun compartir(
        @Header("Authorization") authorization: String,
        @Path("id") documentoId: String,
        @Body body: CompartirDocumentoRequest,
    ): ComparticionRemota

    @Streaming
    @GET("documentos/{id}/contenido")
    suspend fun descargar(
        @Header("Authorization") authorization: String,
        @Path("id") documentoId: String,
    ): ResponseBody

    @DELETE("documentos/{id}")
    suspend fun eliminar(
        @Header("Authorization") authorization: String,
        @Path("id") documentoId: String,
    )
}

class ApiDocumentosRepository(
    private val api: ApiDocumentosRemotos = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .connectTimeout(4, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build(),
        )
        .baseUrl(BuildConfig.API_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiDocumentosRemotos::class.java),
) {
    suspend fun abrirSesion(codigoPerfil: String, rol: String): SesionRemota =
        api.abrirSesion(AbrirSesionRequest(codigoPerfil, rol))

    suspend fun cerrarSesion(token: String) = api.cerrarSesion(bearer(token))

    suspend fun vincularAlumno(token: String, codigoAlumno: String): AlumnoVinculadoRemoto =
        api.vincularAlumno(bearer(token), VincularAlumnoRequest(codigoAlumno))

    suspend fun listarAlumnos(token: String) = api.listarAlumnos(bearer(token))

    suspend fun subirPdf(token: String, archivo: File, resumen: String, categoria: String): DocumentoRemoto {
        val parte = MultipartBody.Part.createFormData(
            "archivo",
            archivo.name,
            archivo.asRequestBody("application/pdf".toMediaType()),
        )
        return api.subirPdf(
            bearer(token),
            parte,
            resumen.toRequestBody("text/plain".toMediaType()),
            categoria.toRequestBody("text/plain".toMediaType()),
        )
    }

    suspend fun listarPropios(token: String) = api.listarPropios(bearer(token))

    suspend fun listarCompartidos(token: String) = api.listarCompartidos(bearer(token))

    suspend fun compartir(token: String, documentoId: String, codigoAlumno: String) =
        api.compartir(bearer(token), documentoId, CompartirDocumentoRequest(codigoAlumno))

    suspend fun descargar(token: String, documentoId: String): ByteArray =
        api.descargar(bearer(token), documentoId).use { it.bytes() }

    suspend fun eliminar(token: String, documentoId: String) =
        api.eliminar(bearer(token), documentoId)

    private fun bearer(token: String) = "Bearer $token"
}
