package com.example.appcarab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcarab.modelo.AppDatabase
import com.example.appcarab.modelo.ApiRestRepository
import com.example.appcarab.modelo.AulaRepository
import com.example.appcarab.modelo.DocumentoRepository
import com.example.appcarab.modelo.UsuarioRepository
import com.example.appcarab.modelo.TriviaApiRepository
import com.example.appcarab.ui.theme.AppCarabTheme
import com.example.appcarab.vista.AccesoApp
import com.example.appcarab.vistaModelo.InicioSesionVistaModelo
import com.example.appcarab.vistaModelo.DocumentosVistaModelo
import com.example.appcarab.vistaModelo.AulaVistaModelo
import com.example.appcarab.vistaModelo.TriviaVistaModelo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val apiRepository = ApiRestRepository()
        val repository = UsuarioRepository(database.usuarioDao(), apiRepository)
        val documentRepository = DocumentoRepository(applicationContext, database.documentoDao())
        val aulaRepository = AulaRepository(database.aulaDao(), database.usuarioDao(), apiRepository)
        val triviaRepository = TriviaApiRepository()
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(InicioSesionVistaModelo::class.java)) {
                    return InicioSesionVistaModelo(repository) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val documentsFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(DocumentosVistaModelo::class.java)) {
                    return DocumentosVistaModelo(documentRepository, database.documentoDao()) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val aulaFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AulaVistaModelo::class.java)) {
                    return AulaVistaModelo(aulaRepository, database.aulaDao()) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }
        val triviaFactory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TriviaVistaModelo::class.java)) {
                    return TriviaVistaModelo(triviaRepository) as T
                }
                throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
            }
        }

        setContent {
            AppCarabTheme {
                val viewModel: InicioSesionVistaModelo = viewModel(factory = factory)
                val documentsViewModel: DocumentosVistaModelo = viewModel(factory = documentsFactory)
                val aulaViewModel: AulaVistaModelo = viewModel(factory = aulaFactory)
                val triviaViewModel: TriviaVistaModelo = viewModel(factory = triviaFactory)
                AccesoApp(viewModel, documentsViewModel, aulaViewModel, triviaViewModel)
            }
        }
    }
}
