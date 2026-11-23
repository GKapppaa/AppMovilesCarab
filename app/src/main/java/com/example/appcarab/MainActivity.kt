package com.example.appcarab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcarab.modelo.AppDatabase
import com.example.appcarab.modelo.ApiRestRepository
import com.example.appcarab.modelo.ApiDocumentosRepository
import com.example.appcarab.modelo.SincronizacionPendiente
import com.example.appcarab.modelo.AulaRepository
import com.example.appcarab.modelo.DocumentoRepository
import com.example.appcarab.modelo.UsuarioRepository
import com.example.appcarab.modelo.TriviaPdfRepository
import com.example.appcarab.modelo.OpenLibraryRepository
import com.example.appcarab.modelo.PreferenciasRepository
import com.example.appcarab.modelo.TemaApp
import com.example.appcarab.modelo.TextosAndroid
import com.example.appcarab.modelo.IdiomaAplicacion
import com.example.appcarab.ui.theme.AppCarabTheme
import com.example.appcarab.vista.AccesoApp
import com.example.appcarab.vistaModelo.InicioSesionVistaModelo
import com.example.appcarab.vistaModelo.DocumentosVistaModelo
import com.example.appcarab.vistaModelo.AulaVistaModelo
import com.example.appcarab.vistaModelo.TriviaVistaModelo
import com.example.appcarab.vistaModelo.SesionRemotaVistaModelo
import com.example.appcarab.vistaModelo.OpenLibraryVistaModelo
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        IdiomaAplicacion.asegurarEspanolPredeterminado(applicationContext)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val textos = TextosAndroid(applicationContext)
        val preferenciasRepository = PreferenciasRepository(applicationContext)
        val apiRepository = ApiRestRepository()
        val apiDocumentosRepository = ApiDocumentosRepository()
        val sincronizacion = SincronizacionPendiente(
            applicationContext,
            database.pendienteSincronizacionDao(),
            apiRepository,
        )
        lifecycleScope.launch { sincronizacion.programarSiHayPendientes() }
        val repository = UsuarioRepository(database.usuarioDao(), sincronizacion)
        val documentRepository = DocumentoRepository(
            applicationContext,
            database.documentoDao(),
            database.aulaDao(),
            textos,
            apiDocumentosRepository,
        )
        val aulaRepository = AulaRepository(
            database.aulaDao(),
            database.usuarioDao(),
            sincronizacion,
            textos,
            database.documentoDao(),
            apiDocumentosRepository,
        )
        val triviaRepository = TriviaPdfRepository(applicationContext, database.documentoDao())
        val openLibraryRepository = OpenLibraryRepository()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(InicioSesionVistaModelo::class.java)) {
                    return InicioSesionVistaModelo(repository, textos) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val documentsFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DocumentosVistaModelo::class.java)) {
                    return DocumentosVistaModelo(documentRepository, database.documentoDao(), textos) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val aulaFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AulaVistaModelo::class.java)) {
                    return AulaVistaModelo(aulaRepository, database.aulaDao(), textos) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val triviaFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TriviaVistaModelo::class.java)) {
                    return TriviaVistaModelo(triviaRepository, textos) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val sesionRemotaFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SesionRemotaVistaModelo::class.java)) {
                    return SesionRemotaVistaModelo(apiDocumentosRepository, textos) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val openLibraryFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(OpenLibraryVistaModelo::class.java)) {
                    return OpenLibraryVistaModelo(openLibraryRepository, textos) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }

        setContent {
            val preferencias by preferenciasRepository.preferencias.collectAsState()
            val oscuro = when (preferencias.tema) {
                TemaApp.SISTEMA -> isSystemInDarkTheme()
                TemaApp.CLARO -> false
                TemaApp.OSCURO -> true
            }
            AppCarabTheme(darkTheme = oscuro, escalaTexto = preferencias.tamanoTexto.escala) {
                val viewModel: InicioSesionVistaModelo = viewModel(factory = factory)
                val documentsViewModel: DocumentosVistaModelo = viewModel(factory = documentsFactory)
                val aulaViewModel: AulaVistaModelo = viewModel(factory = aulaFactory)
                val triviaViewModel: TriviaVistaModelo = viewModel(factory = triviaFactory)
                val sesionRemotaViewModel: SesionRemotaVistaModelo = viewModel(factory = sesionRemotaFactory)
                val openLibraryViewModel: OpenLibraryVistaModelo = viewModel(factory = openLibraryFactory)
                AccesoApp(
                    viewModel = viewModel,
                    documentosViewModel = documentsViewModel,
                    aulaViewModel = aulaViewModel,
                    triviaViewModel = triviaViewModel,
                    sesionRemotaViewModel = sesionRemotaViewModel,
                    openLibraryViewModel = openLibraryViewModel,
                    preferencias = preferencias,
                    temaOscuro = oscuro,
                    preferenciasRepository = preferenciasRepository,
                )
            }
        }
    }
}
