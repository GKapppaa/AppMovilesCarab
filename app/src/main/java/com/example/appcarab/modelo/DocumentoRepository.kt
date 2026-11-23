package com.example.appcarab.modelo

import android.content.Context
import com.example.appcarab.R
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentoRepository(
    private val context: Context,
    private val documentoDao: DocumentoDao,
    private val aulaDao: AulaDao,
    private val textos: Textos = TextosAndroid(context),
    private val documentosRemotos: ApiDocumentosRepository,
) {
    fun uriTemporalEscaneo(usuarioId: Int): Uri {
        val carpeta = File(context.filesDir, "documentos/$usuarioId/escaneos").apply { mkdirs() }
        val archivo = File(carpeta, "captura-${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    suspend fun agregarEscaneo(
        usuarioId: Int,
        codigoPerfil: String,
        token: String?,
        uri: Uri,
    ): Documento = withContext(Dispatchers.IO) {
        val archivoPdf = File(context.filesDir, "documentos/$usuarioId/${UUID.randomUUID()}.pdf")
        archivoPdf.parentFile?.mkdirs()
        try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: error(textos.get(R.string.doc_error_camara))
            val pdf = PdfDocument()
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val ratio = minOf(555f / bitmap.width, 802f / bitmap.height)
            val width = (bitmap.width * ratio).toInt()
            val height = (bitmap.height * ratio).toInt()
            val left = (595 - width) / 2
            val top = (842 - height) / 2
            page.canvas.drawBitmap(bitmap, null, Rect(left, top, left + width, top + height), null)
            pdf.finishPage(page)
            archivoPdf.outputStream().use(pdf::writeTo)
            pdf.close()
            bitmap.recycle()
            context.contentResolver.delete(uri, null, null)
            val documento = Documento(
                usuarioId = usuarioId,
                nombre = "Escaneo-${System.currentTimeMillis()}.pdf",
                archivoInterno = archivoPdf.name,
                resumen = "Escaneo guardado como PDF. El texto de una imagen requiere OCR para generar un resumen.",
                categoria = "Escaneos",
            )
            val id = documentoDao.insertar(documento).toInt()
            subirSiHaySesion(documento.copy(id = id), codigoPerfil, token)
        } catch (error: Exception) {
            archivoPdf.delete()
            throw error
        }
    }

    suspend fun agregarPdf(
        usuarioId: Int,
        codigoPerfil: String,
        token: String?,
        uri: Uri,
    ): Documento = withContext(Dispatchers.IO) {
        val nombre = obtenerNombre(uri)
        val archivoInterno = "${UUID.randomUUID()}.pdf"
        val carpeta = File(context.filesDir, "documentos/$usuarioId").apply { mkdirs() }
        val destino = File(carpeta, archivoInterno)

        try {
            val entrada = context.contentResolver.openInputStream(uri)
                ?: error(textos.get(R.string.doc_error_abrir_archivo))
            entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }

            val texto = ExtractorTextoPdf.extraer(context, destino)
            val resumen = if (texto.isBlank()) {
                "No se detectó texto seleccionable. Si el PDF es escaneado, necesitaría OCR para resumirlo."
            } else {
                ResumenLocal.crear(texto)
            }
            val documento = Documento(
                usuarioId = usuarioId,
                nombre = nombre,
                archivoInterno = archivoInterno,
                resumen = resumen,
                textoExtraido = texto,
            )
            val id = documentoDao.insertar(documento).toInt()
            subirSiHaySesion(documento.copy(id = id), codigoPerfil, token)
        } catch (error: Exception) {
            destino.delete()
            throw error
        }
    }

    suspend fun importarCompartidos(usuarioId: Int, token: String): Int = withContext(Dispatchers.IO) {
        var importados = 0
        documentosRemotos.listarCompartidos(token).forEach { remoto ->
            if (documentoDao.buscarPorIdRemoto(remoto.id) != null) return@forEach
            val bytes = documentosRemotos.descargar(token, remoto.id)
            require(bytes.size <= MAX_DOCUMENTO_BYTES) { textos.get(R.string.doc_error_demasiado_grande) }
            require(bytes.size >= 5 && bytes.copyOfRange(0, 5).decodeToString() == "%PDF-") {
                textos.get(R.string.doc_error_pdf_invalido)
            }
            val archivoInterno = "${UUID.randomUUID()}.pdf"
            val carpeta = File(context.filesDir, "documentos/$usuarioId").apply { mkdirs() }
            val destino = File(carpeta, archivoInterno)
            destino.writeBytes(bytes)
            try {
                val texto = ExtractorTextoPdf.extraer(context, destino)
                val documentoId = documentoDao.insertar(
                    Documento(
                        usuarioId = usuarioId,
                        nombre = remoto.nombre,
                        archivoInterno = archivoInterno,
                        resumen = remoto.resumen.ifBlank { resumenDesdeTexto(texto) },
                        textoExtraido = texto,
                        categoria = remoto.categoria,
                        idRemoto = remoto.id,
                        propietarioRemoto = remoto.propietario,
                    ),
                )
                aulaDao.compartirMaterial(
                    MaterialCompartido(
                        profesorId = ID_PROFESOR_REMOTO,
                        alumnoId = usuarioId,
                        documentoId = documentoId.toInt(),
                    ),
                )
                importados++
            } catch (error: Exception) {
                destino.delete()
                throw error
            }
        }
        importados
    }

    suspend fun subirExistente(
        documento: Documento,
        codigoPerfil: String,
        token: String,
    ): Documento = withContext(Dispatchers.IO) {
        if (documento.idRemoto != null) return@withContext documento
        val archivo = File(context.filesDir, "documentos/${documento.usuarioId}/${documento.archivoInterno}")
        require(archivo.isFile) { textos.get(R.string.doc_error_abrir_archivo) }
        val remoto = documentosRemotos.subirPdf(token, archivo, documento.resumen, documento.categoria)
        check(remoto.propietario == codigoPerfil) { textos.get(R.string.doc_error_sesion_propietario) }
        documentoDao.vincularRemoto(documento.id, remoto.id, remoto.propietario)
        documento.copy(idRemoto = remoto.id, propietarioRemoto = remoto.propietario)
    }

    suspend fun eliminar(documento: Documento, codigoPerfil: String, token: String?) = withContext(Dispatchers.IO) {
        if (token != null && documento.idRemoto != null && documento.propietarioRemoto == codigoPerfil) {
            documentosRemotos.eliminar(token, documento.idRemoto)
        }
        documentoDao.eliminar(documento)
        File(context.filesDir, "documentos/${documento.usuarioId}/${documento.archivoInterno}").delete()
    }

    fun uriParaAbrir(documento: Documento): Uri? {
        val file = File(context.filesDir, "documentos/${documento.usuarioId}/${documento.archivoInterno}")
        if (!file.exists()) return null
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun obtenerNombre(uri: Uri): String {
        val nombre = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else null
        }
        return nombre?.takeIf { it.endsWith(".pdf", ignoreCase = true) } ?: "Material de estudio.pdf"
    }

    private suspend fun subirSiHaySesion(
        documento: Documento,
        codigoPerfil: String,
        token: String?,
    ): Documento {
        if (token.isNullOrBlank()) return documento
        return try {
            val archivo = File(context.filesDir, "documentos/${documento.usuarioId}/${documento.archivoInterno}")
            val remoto = documentosRemotos.subirPdf(token, archivo, documento.resumen, documento.categoria)
            check(remoto.propietario == codigoPerfil) { textos.get(R.string.doc_error_sesion_propietario) }
            documentoDao.vincularRemoto(documento.id, remoto.id, remoto.propietario)
            documento.copy(idRemoto = remoto.id, propietarioRemoto = remoto.propietario)
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (_: Exception) {
            documento
        }
    }

    private fun resumenDesdeTexto(texto: String): String =
        if (texto.isBlank()) textos.get(R.string.doc_pdf_sin_texto)
        else ResumenLocal.crear(texto)

    private companion object {
        const val ID_PROFESOR_REMOTO = -1
    }
}

internal object ExtractorTextoPdf {
    private const val MAX_PAGES_TO_SCAN = 20
    private const val MAX_CHARS = 40_000

    fun extraer(context: Context, file: File): String {
        PDFBoxResourceLoader.init(context)
        PDDocument.load(file).use { pdf ->
            if (pdf.numberOfPages == 0) return ""
            val extractor = PDFTextStripper().apply {
                startPage = 1
                endPage = minOf(pdf.numberOfPages, MAX_PAGES_TO_SCAN)
            }
            return extractor.getText(pdf)
                .replace("\r", "\n")
                .replace(Regex("[\\t ]+"), " ")
                .replace(Regex("\\n+"), ". ")
                .trim()
                .take(MAX_CHARS)
        }
    }
}

private const val MAX_DOCUMENTO_BYTES = 20L * 1024 * 1024

object ResumenLocal {
    fun crear(texto: String): String {
        val limpio = texto.replace(Regex("\\s+"), " ").trim()
        if (limpio.isBlank()) {
            return "No se detectó texto seleccionable. Si el PDF es escaneado, necesitaría OCR para resumirlo."
        }

        val oraciones = limpio
            .split(Regex("(?<=[.!?])\\s+"))
            .map(String::trim)
            .filter { it.length >= 35 }
            .take(MAX_SENTENCES)

        val resumen = oraciones.joinToString(" ").ifBlank { limpio.take(MAX_CHARS) }
        return if (resumen.length > MAX_CHARS) resumen.take(MAX_CHARS).substringBeforeLast(' ') + "…" else resumen
    }

    private const val MAX_SENTENCES = 4
    private const val MAX_CHARS = 700
}
