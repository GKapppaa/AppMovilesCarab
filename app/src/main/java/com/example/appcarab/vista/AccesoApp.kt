package com.example.appcarab.vista

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.appcarab.vistaModelo.DocumentosVistaModelo
import com.example.appcarab.vistaModelo.InicioSesionVistaModelo
import com.example.appcarab.vistaModelo.PantallaAcceso

@Composable
fun AccesoApp(viewModel: InicioSesionVistaModelo, documentosViewModel: DocumentosVistaModelo) {
    val estado by viewModel.estado.collectAsState()
    val estadoDocumentos by documentosViewModel.estado.collectAsState()
    val context = LocalContext.current
    var uriEscaneo by rememberSaveable { mutableStateOf<String?>(null) }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val usuarioId = estado.usuarioId
        if (uri != null && usuarioId != null) documentosViewModel.agregar(usuarioId, uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardado ->
        val uri = uriEscaneo?.let(Uri::parse)
        val usuarioId = estado.usuarioId
        if (guardado && uri != null && usuarioId != null) documentosViewModel.agregarEscaneo(usuarioId, uri)
    }
    LaunchedEffect(estado.usuarioId) { estado.usuarioId?.let(documentosViewModel::observar) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (estado.pantalla) {
            PantallaAcceso.BIENVENIDA -> Bienvenida(
                onLogin = { viewModel.irA(PantallaAcceso.INICIAR_SESION) },
                onRegister = { viewModel.irA(PantallaAcceso.REGISTRO) },
            )
            PantallaAcceso.INICIAR_SESION -> FormularioSesion(
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.BIENVENIDA) },
                onRegister = { viewModel.irA(PantallaAcceso.REGISTRO) },
                onSubmit = viewModel::iniciarSesion,
            )
            PantallaAcceso.REGISTRO -> FormularioRegistro(
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.BIENVENIDA) },
                onLogin = { viewModel.irA(PantallaAcceso.INICIAR_SESION) },
                onSubmit = viewModel::registrar,
            )
            PantallaAcceso.EDITAR_PERFIL -> FormularioPerfil(
                nombreActual = estado.nombreUsuario,
                correoActual = estado.correoUsuario.orEmpty(),
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.INICIO) },
                onSubmit = viewModel::guardarPerfil,
            )
            PantallaAcceso.INICIO -> InicioEstudiante(
                nombre = estado.nombreUsuario,
                codigoPerfil = estado.codigoPerfil,
                correo = estado.correoUsuario.orEmpty(),
                rol = estado.rolUsuario,
                documentos = estadoDocumentos.documentos,
                documentosCargando = estadoDocumentos.cargando,
                documentosMensaje = estadoDocumentos.mensaje,
                documentosMensajeEsError = estadoDocumentos.mensajeEsError,
                onChoosePdf = { pdfPicker.launch(arrayOf("application/pdf")) },
                onScan = {
                    estado.usuarioId?.let { usuarioId ->
                        documentosViewModel.uriTemporalEscaneo(usuarioId).also {
                            uriEscaneo = it.toString()
                            cameraLauncher.launch(it)
                        }
                    }
                },
                onDeleteDocument = documentosViewModel::eliminar,
                onChangeCategory = documentosViewModel::cambiarCategoria,
                onOpenDocument = { documento ->
                    val uri = documentosViewModel.uriParaAbrir(documento)
                    if (uri == null) {
                        Toast.makeText(context, "No se encontró el archivo guardado.", Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        runCatching { context.startActivity(Intent.createChooser(intent, "Abrir PDF")) }
                            .onFailure { Toast.makeText(context, "No hay una aplicación disponible para abrir PDF.", Toast.LENGTH_LONG).show() }
                    }
                },
                onEditProfile = { viewModel.irA(PantallaAcceso.EDITAR_PERFIL) },
                onLogout = viewModel::cerrarSesion,
            )
        }
    }
}
