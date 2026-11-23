package com.example.appcarab.vista

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.example.appcarab.R
import com.example.appcarab.modelo.PreferenciasApp
import com.example.appcarab.modelo.PreferenciasRepository
import com.example.appcarab.vistaModelo.DocumentosVistaModelo
import com.example.appcarab.vistaModelo.InicioSesionVistaModelo
import com.example.appcarab.vistaModelo.PantallaAcceso
import com.example.appcarab.vistaModelo.AulaVistaModelo
import com.example.appcarab.vistaModelo.EstadoAula
import com.example.appcarab.vistaModelo.TriviaVistaModelo
import com.example.appcarab.vistaModelo.SesionRemotaVistaModelo

@Composable
fun AccesoApp(
    viewModel: InicioSesionVistaModelo,
    documentosViewModel: DocumentosVistaModelo,
    aulaViewModel: AulaVistaModelo,
    triviaViewModel: TriviaVistaModelo,
    sesionRemotaViewModel: SesionRemotaVistaModelo,
    preferencias: PreferenciasApp,
    temaOscuro: Boolean,
    preferenciasRepository: PreferenciasRepository,
) {
    val estado by viewModel.estado.collectAsState()
    val estadoDocumentos by documentosViewModel.estado.collectAsState()
    val estadoAula by aulaViewModel.estado.collectAsState()
    val estadoTrivia by triviaViewModel.estado.collectAsState()
    val estadoSesionRemota by sesionRemotaViewModel.estado.collectAsState()
    val context = LocalContext.current
    var uriEscaneo by rememberSaveable { mutableStateOf<String?>(null) }
    var configuracionAbierta by rememberSaveable { mutableStateOf(false) }
    val textoArchivoNoEncontrado = stringResource(R.string.archivo_no_encontrado)
    val textoAbrirPdf = stringResource(R.string.abrir_pdf_selector)
    val textoSinAppPdf = stringResource(R.string.sin_app_pdf)

    // Íconos de la barra de estado legibles sobre la foto de bienvenida y en modo oscuro.
    val view = LocalView.current
    val iconosClaros = temaOscuro || (estado.pantalla == PantallaAcceso.BIENVENIDA && !configuracionAbierta)
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !iconosClaros
                isAppearanceLightNavigationBars = !temaOscuro
            }
        }
    }
    BackHandler(enabled = configuracionAbierta) { configuracionAbierta = false }

    val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val usuarioId = estado.usuarioId
        if (uri != null && usuarioId != null) {
            documentosViewModel.agregar(usuarioId, estado.codigoPerfil, estadoSesionRemota.token, uri)
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardado ->
        val uri = uriEscaneo?.let(Uri::parse)
        val usuarioId = estado.usuarioId
        if (guardado && uri != null && usuarioId != null) {
            documentosViewModel.agregarEscaneo(
                usuarioId,
                estado.codigoPerfil,
                estadoSesionRemota.token,
                uri,
            )
        }
    }
    LaunchedEffect(estado.usuarioId, estado.codigoPerfil, estadoSesionRemota.token) {
        val usuarioId = estado.usuarioId
        if (usuarioId != null) {
            documentosViewModel.observar(usuarioId, estado.codigoPerfil, estadoSesionRemota.token)
            aulaViewModel.observar(usuarioId, estado.rolUsuario, estadoSesionRemota.token)
        }
    }
    LaunchedEffect(estado.usuarioId, estado.codigoPerfil, estado.rolUsuario) {
        if (estado.usuarioId != null && estado.codigoPerfil.isNotBlank()) {
            sesionRemotaViewModel.abrir(estado.codigoPerfil, estado.rolUsuario)
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (configuracionAbierta) {
            PantallaConfiguracion(
                preferencias = preferencias,
                onTema = preferenciasRepository::cambiarTema,
                onTamanoTexto = preferenciasRepository::cambiarTamanoTexto,
                onReducirMovimiento = preferenciasRepository::cambiarReducirMovimiento,
                onRestablecer = preferenciasRepository::restablecer,
                onBack = { configuracionAbierta = false },
            )
            return@Surface
        }
        when (estado.pantalla) {
            PantallaAcceso.BIENVENIDA -> Bienvenida(
                reducirMovimiento = preferencias.reducirMovimiento,
                onLogin = { viewModel.irA(PantallaAcceso.INICIAR_SESION) },
                onRegister = { viewModel.irA(PantallaAcceso.REGISTRO) },
                onSettings = { configuracionAbierta = true },
            )
            PantallaAcceso.INICIAR_SESION -> FormularioSesion(
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.BIENVENIDA) },
                onRegister = { viewModel.irA(PantallaAcceso.REGISTRO) },
                onSettings = { configuracionAbierta = true },
                onSubmit = viewModel::iniciarSesion,
            )
            PantallaAcceso.REGISTRO -> FormularioRegistro(
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.BIENVENIDA) },
                onLogin = { viewModel.irA(PantallaAcceso.INICIAR_SESION) },
                onSettings = { configuracionAbierta = true },
                onSubmit = viewModel::registrar,
            )
            PantallaAcceso.EDITAR_PERFIL -> FormularioPerfil(
                nombreActual = estado.nombreUsuario,
                correoActual = estado.correoUsuario.orEmpty(),
                mensaje = estado.mensaje,
                cargando = estado.cargando,
                onBack = { viewModel.irA(PantallaAcceso.INICIO) },
                onSettings = { configuracionAbierta = true },
                onSubmit = viewModel::guardarPerfil,
            )
            PantallaAcceso.INICIO -> InicioEstudiante(
                nombre = estado.nombreUsuario,
                codigoPerfil = estado.codigoPerfil,
                correo = estado.correoUsuario.orEmpty(),
                rol = estado.rolUsuario,
                estadoAula = estadoAula,
                estadoTrivia = estadoTrivia,
                sesionRemotaError = estadoSesionRemota.error,
                onRetrySesionRemota = {
                    sesionRemotaViewModel.abrir(estado.codigoPerfil, estado.rolUsuario, reintentar = true)
                },
                onLoadTrivia = { triviaViewModel.cargarPregunta(estado.usuarioId) },
                onAnswerTrivia = triviaViewModel::responder,
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
                onDeleteDocument = { documento ->
                    documentosViewModel.eliminar(documento, estado.codigoPerfil, estadoSesionRemota.token)
                },
                onUploadDocument = { documento ->
                    documentosViewModel.subirAlServidor(documento, estado.codigoPerfil, estadoSesionRemota.token)
                },
                onChangeCategory = documentosViewModel::cambiarCategoria,
                onOpenDocument = { documento ->
                    val uri = documentosViewModel.uriParaAbrir(documento)
                    if (uri == null) {
                        Toast.makeText(context, textoArchivoNoEncontrado, Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        runCatching { context.startActivity(Intent.createChooser(intent, textoAbrirPdf)) }
                            .onFailure { Toast.makeText(context, textoSinAppPdf, Toast.LENGTH_LONG).show() }
                    }
                },
                onAddStudent = { codigo ->
                    estado.usuarioId?.let {
                        aulaViewModel.agregarAlumno(it, codigo, estadoSesionRemota.token)
                    }
                },
                onSendMessage = { codigo, texto -> estado.usuarioId?.let { aulaViewModel.enviarMensaje(it, codigo, texto) } },
                onShareMaterial = { codigo, documentoId ->
                    estado.usuarioId?.let {
                        aulaViewModel.compartirMaterial(it, codigo, documentoId, estadoSesionRemota.token)
                    }
                },
                onAssignTask = { codigo, titulo, descripcion -> estado.usuarioId?.let { aulaViewModel.asignarTarea(it, codigo, titulo, descripcion) } },
                onEditProfile = { viewModel.irA(PantallaAcceso.EDITAR_PERFIL) },
                onSettings = { configuracionAbierta = true },
                onLogout = {
                    sesionRemotaViewModel.cerrar()
                    viewModel.cerrarSesion()
                },
            )
        }
    }
}
