package com.example.appcarab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appcarab.modelo.AppDatabase
import com.example.appcarab.modelo.DocumentoRepository
import com.example.appcarab.modelo.UsuarioRepository
import com.example.appcarab.ui.theme.AppCarabTheme
import com.example.appcarab.vista.AccesoApp
import com.example.appcarab.vistaModelo.InicioSesionVistaModelo
import com.example.appcarab.vistaModelo.DocumentosVistaModelo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = UsuarioRepository(database.usuarioDao())
        val documentRepository = DocumentoRepository(applicationContext, database.documentoDao())
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

        setContent {
            AppCarabTheme {
                val viewModel: InicioSesionVistaModelo = viewModel(factory = factory)
                val documentsViewModel: DocumentosVistaModelo = viewModel(factory = documentsFactory)
                AccesoApp(viewModel, documentsViewModel)
            }
        }
    }
}
