package com.example.appcarab.modelo

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TemaApp { SISTEMA, CLARO, OSCURO }

enum class TamanoTexto(val escala: Float) {
    ESTANDAR(1f),
    GRANDE(1.15f),
    MUY_GRANDE(1.3f),
}

/** Idiomas con traducción completa de la interfaz. [etiqueta] vacía = seguir al sistema. */
enum class IdiomaApp(val etiqueta: String) {
    SISTEMA(""),
    ESPANOL("es"),
    INGLES("en"),
}

data class PreferenciasApp(
    val tema: TemaApp = TemaApp.SISTEMA,
    val tamanoTexto: TamanoTexto = TamanoTexto.ESTANDAR,
    val reducirMovimiento: Boolean = false,
)

/** Preferencias visuales guardadas en el dispositivo; no se sincronizan con el backend. */
class PreferenciasRepository(context: Context) {
    private val prefs = context.getSharedPreferences("preferencias_app", Context.MODE_PRIVATE)
    private val _preferencias = MutableStateFlow(leer())
    val preferencias: StateFlow<PreferenciasApp> = _preferencias.asStateFlow()

    fun cambiarTema(tema: TemaApp) = guardar(_preferencias.value.copy(tema = tema))
    fun cambiarTamanoTexto(tamano: TamanoTexto) = guardar(_preferencias.value.copy(tamanoTexto = tamano))
    fun cambiarReducirMovimiento(activo: Boolean) = guardar(_preferencias.value.copy(reducirMovimiento = activo))
    fun restablecer() = guardar(PreferenciasApp())

    private fun leer() = PreferenciasApp(
        tema = enumOrDefault(prefs.getString(CLAVE_TEMA, null), TemaApp.SISTEMA),
        tamanoTexto = enumOrDefault(prefs.getString(CLAVE_TEXTO, null), TamanoTexto.ESTANDAR),
        reducirMovimiento = prefs.getBoolean(CLAVE_MOVIMIENTO, false),
    )

    private fun guardar(nuevas: PreferenciasApp) {
        prefs.edit {
            putString(CLAVE_TEMA, nuevas.tema.name)
            putString(CLAVE_TEXTO, nuevas.tamanoTexto.name)
            putBoolean(CLAVE_MOVIMIENTO, nuevas.reducirMovimiento)
        }
        _preferencias.value = nuevas
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(valor: String?, defecto: T): T =
        valor?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: defecto

    private companion object {
        const val CLAVE_TEMA = "tema"
        const val CLAVE_TEXTO = "tamano_texto"
        const val CLAVE_MOVIMIENTO = "reducir_movimiento"
    }
}

/**
 * Idioma por aplicación de Android 13+ (minSdk 33). El sistema lo persiste, lo muestra en
 * Ajustes › Apps › Idioma y recrea la actividad al cambiarlo.
 */
object IdiomaAplicacion {
    fun actual(context: Context): IdiomaApp {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) return IdiomaApp.SISTEMA
        return IdiomaApp.entries.firstOrNull { it.etiqueta == locales[0].language } ?: IdiomaApp.SISTEMA
    }

    fun aplicar(context: Context, idioma: IdiomaApp) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            if (idioma == IdiomaApp.SISTEMA) LocaleList.getEmptyLocaleList() else LocaleList.forLanguageTags(idioma.etiqueta)
    }
}

/** Acceso a textos traducidos desde ViewModels y repositorios, que viven fuera de Compose. */
fun interface Textos {
    fun get(@StringRes id: Int, vararg args: Any): String
}

class TextosAndroid(private val context: Context) : Textos {
    override fun get(id: Int, vararg args: Any): String = contextoLocalizado().getString(id, *args)

    private fun contextoLocalizado(): Context {
        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (locales.isEmpty) return context
        val config = Configuration(context.resources.configuration).apply { setLocales(locales) }
        return context.createConfigurationContext(config)
    }
}
