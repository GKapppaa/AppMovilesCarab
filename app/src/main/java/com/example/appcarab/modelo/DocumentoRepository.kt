package com.example.appcarab.modelo

import android.content.Context
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
) {
    fun uriTemporalEscaneo(usuarioId: Int): Uri {
        val carpeta = File(context.filesDir, "documentos/$usuarioId/escaneos").apply { mkdirs() }
        val archivo = File(carpeta, "captura-${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    suspend fun agregarEscaneo(usuarioId: Int, uri: Uri): Documento = withContext(Dispatchers.IO) {
        val archivoPdf = File(context.filesDir, "documentos/$usuarioId/${UUID.randomUUID()}.pdf")
        archivoPdf.parentFile?.mkdirs()
        try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: error("La cámara no entregó una imagen legible.")
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
            documento.copy(id = id)
        } catch (error: Exception) {
            archivoPdf.delete()
            throw error
        }
    }

    suspend fun agregarPdf(usuarioId: Int, uri: Uri): Documento = withContext(Dispatchers.IO) {
        val nombre = obtenerNombre(uri)
        val archivoInterno = "${UUID.randomUUID()}.pdf"
        val carpeta = File(context.filesDir, "documentos/$usuarioId").apply { mkdirs() }
        val destino = File(carpeta, archivoInterno)

        try {
            val entrada = context.contentResolver.openInputStream(uri)
                ?: error("No se pudo abrir el archivo seleccionado.")
            entrada.use { input -> destino.outputStream().use { output -> input.copyTo(output) } }

            val resumen = extraerResumen(destino)
            val documento = Documento(
                usuarioId = usuarioId,
                nombre = nombre,
                archivoInterno = archivoInterno,
                resumen = resumen,
            )
            val id = documentoDao.insertar(documento).toInt()
            documento.copy(id = id)
        } catch (error: Exception) {
            destino.delete()
            throw error
        }
    }

    suspend fun eliminar(documento: Documento) = withContext(Dispatchers.IO) {
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

    private fun extraerResumen(file: File): String {
        PDFBoxResourceLoader.init(context)
        PDDocument.load(file).use { pdf ->
            if (pdf.numberOfPages == 0) return "El archivo no contiene páginas para resumir."
            val extractor = PDFTextStripper().apply {
                startPage = 1
                endPage = minOf(pdf.numberOfPages, MAX_PAGES_TO_SCAN)
            }
            return ResumenLocal.crear(extractor.getText(pdf))
        }
    }

    private companion object {
        const val MAX_PAGES_TO_SCAN = 8
    }
}

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
