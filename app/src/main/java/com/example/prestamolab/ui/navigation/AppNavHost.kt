package com.example.prestamolab.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.ui.EventoPrestamo
import com.example.prestamolab.ui.PrestamoViewModel
import com.example.prestamolab.ui.auth.LoginScreen
import com.example.prestamolab.ui.auth.LoginViewModel
import com.example.prestamolab.ui.catalogo.CatalogScreen
import com.example.prestamolab.ui.devolucion.DevolucionRoute
import com.example.prestamolab.ui.devolucion.DevolucionViewModel
import com.example.prestamolab.ui.equipo.DetalleEquipoScreen
import com.example.prestamolab.ui.equipo.EstadoDetalleEquipo
import com.example.prestamolab.ui.gestion.GestionScreen
import com.example.prestamolab.ui.sesion.EstadoSesion
import com.example.prestamolab.ui.sesion.SesionViewModel
import com.example.prestamolab.ui.solicitud.SolicitudScreen
import com.example.prestamolab.ui.solicitudes.MisSolicitudesScreen

/** Raíz de la app: login mientras no haya sesión; área autenticada según el rol. */
@Composable
fun AppNavHost() {
    val sesionViewModel: SesionViewModel = viewModel(factory = SesionViewModel.Factory)
    val estado by sesionViewModel.estado.collectAsState()

    when (val sesion = estado) {
        EstadoSesion.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        EstadoSesion.SinSesion -> {
            val loginViewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
            val uiState by loginViewModel.uiState.collectAsState()
            LoginScreen(
                uiState = uiState,
                onIdentificadorChange = loginViewModel::onIdentificadorChanged,
                onContrasenaChange = loginViewModel::onContrasenaChanged,
                onIngresarClick = loginViewModel::iniciarSesion
            )
        }

        // La clave descarta la navegación del usuario anterior al cambiar de cuenta
        is EstadoSesion.Autenticado -> key(sesion.usuario.id) {
            AreaAutenticada(usuario = sesion.usuario, onCerrarSesion = sesionViewModel::cerrarSesion)
        }
    }
}

private val AZUL = Color(0xFF1E6091)

/** Rutas que se muestran a pantalla completa, sin las barras comunes. */
private val RUTAS_SIN_MARCO = setOf(Rutas.DEVOLUCION)

private data class DestinoPrincipal(val etiqueta: String, val icono: String)

private val DESTINOS = mapOf(
    Rutas.CATALOGO to DestinoPrincipal("Catálogo", "📦"),
    Rutas.MIS_SOLICITUDES to DestinoPrincipal("Mis Solicitudes", "📋"),
    Rutas.GESTION to DestinoPrincipal("Gestión", "🛠")
)

/** El detalle y el formulario pertenecen a la pestaña Catálogo. */
private fun destinoPrincipalDe(ruta: String?): String? = when (ruta) {
    Rutas.DETALLE_EQUIPO, Rutas.SOLICITUD -> Rutas.CATALOGO
    else -> ruta
}

/** Cambia de pestaña conservando el estado de cada una, como recomienda Navigation. */
private fun NavHostController.irADestinoPrincipal(ruta: String) {
    // La pestaña de inicio se alcanza regresando a ella: con restoreState, Navigation 2.7
    // restauraría la pila guardada de otra pestaña (p. ej. Mis Solicitudes tras solicitar)
    val inicio = graph.findStartDestination()
    if (ruta == inicio.route && popBackStack(inicio.id, inclusive = false)) return
    navigate(ruta) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaAutenticada(usuario: Usuario, onCerrarSesion: () -> Unit) {
    val navController = rememberNavController()
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    // Compartido por catálogo, detalle, formulario y Mis Solicitudes
    val prestamoViewModel: PrestamoViewModel = viewModel(
        key = "prestamos-${usuario.id}",
        factory = PrestamoViewModel.factory(usuario)
    )
    val uiState by prestamoViewModel.uiState.collectAsState()
    val puedeSolicitar = ControlAcceso.puedeSolicitarPrestamo(usuario.rol)

    LaunchedEffect(prestamoViewModel) {
        prestamoViewModel.eventos.collect { evento ->
            when (evento) {
                // Tras solicitar se va a Mis Solicitudes; Atrás vuelve al catálogo, no al formulario
                is EventoPrestamo.SolicitudRegistrada -> navController.navigate(Rutas.MIS_SOLICITUDES) {
                    popUpTo(Rutas.CATALOGO)
                    launchSingleTop = true
                }
            }
        }
    }

    val conMarco = rutaActual !in RUTAS_SIN_MARCO
    Scaffold(
        topBar = {
            if (conMarco) TopAppBar(
                title = { Text("PréstamoLab CTMA", fontWeight = FontWeight.Bold, fontFamily = FontFamily.SansSerif) },
                actions = {
                    TextButton(onClick = onCerrarSesion) {
                        Text("Salir", color = Color.White, fontFamily = FontFamily.SansSerif)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AZUL, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            if (conMarco) NavigationBar(containerColor = Color(0xFFE8F1F5)) {
                val seleccionado = destinoPrincipalDe(rutaActual)
                ControlAcceso.destinosPrincipales(usuario.rol).forEach { ruta ->
                    val destino = DESTINOS.getValue(ruta)
                    NavigationBarItem(
                        selected = seleccionado == ruta,
                        onClick = { navController.irADestinoPrincipal(ruta) },
                        label = { Text(destino.etiqueta, fontFamily = FontFamily.SansSerif) },
                        icon = { Text(destino.icono) }
                    )
                }
            }
        }
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                // Evita que las pantallas con safeDrawingPadding sumen otra vez las barras del sistema
                .consumeWindowInsets(relleno)
        ) {
            uiState.avisoSincronizacion?.let { aviso ->
                if (conMarco) Surface(color = Color(0xFFFFF4E5), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(aviso, modifier = Modifier.weight(1f), fontFamily = FontFamily.SansSerif)
                        TextButton(onClick = prestamoViewModel::descartarAvisoSincronizacion) { Text("Cerrar") }
                    }
                }
            }

            NavHost(
                navController = navController,
                startDestination = ControlAcceso.rutaInicio(usuario.rol),
                modifier = Modifier.weight(1f)
            ) {
                composable(Rutas.CATALOGO) {
                    RutaProtegida(Rutas.CATALOGO, usuario, onVolver = onCerrarSesion) {
                        CatalogScreen(
                            equipos = uiState.equipos,
                            onEquipoClick = { equipoId ->
                                uiState.equipos.find { it.id == equipoId }?.let(prestamoViewModel::seleccionarEquipoParaDetalle)
                                navController.navigate(Rutas.detalleEquipo(equipoId))
                            },
                            onNavigateToSolicitudes = if (puedeSolicitar) {
                                { navController.irADestinoPrincipal(Rutas.MIS_SOLICITUDES) }
                            } else null
                        )
                    }
                }
                composable(
                    route = Rutas.DETALLE_EQUIPO,
                    arguments = listOf(navArgument(Rutas.ARG_EQUIPO_ID) { type = NavType.IntType })
                ) { entrada ->
                    val equipoId = entrada.arguments?.getInt(Rutas.ARG_EQUIPO_ID) ?: -1
                    RutaProtegida(Rutas.DETALLE_EQUIPO, usuario, onVolver = { navController.popBackStack() }) {
                        LaunchedEffect(equipoId) { prestamoViewModel.seleccionarEquipoPorId(equipoId) }
                        val equipo = uiState.equipoSeleccionado?.takeIf { it.id == equipoId }
                        DetalleEquipoScreen(
                            estado = when {
                                equipo != null -> EstadoDetalleEquipo.Encontrado(equipo)
                                uiState.equipoNoEncontrado == equipoId -> EstadoDetalleEquipo.NoEncontrado
                                else -> EstadoDetalleEquipo.Cargando
                            },
                            puedeSolicitar = puedeSolicitar,
                            onAtrasClick = { navController.popBackStack() },
                            onSolicitarClick = { navController.navigate(Rutas.solicitud(equipoId)) }
                        )
                    }
                }
                composable(
                    route = Rutas.SOLICITUD,
                    arguments = listOf(navArgument(Rutas.ARG_EQUIPO_ID) { type = NavType.IntType })
                ) { entrada ->
                    val equipoId = entrada.arguments?.getInt(Rutas.ARG_EQUIPO_ID) ?: -1
                    RutaProtegida(Rutas.SOLICITUD, usuario, onVolver = { navController.popBackStack() }) {
                        LaunchedEffect(equipoId) {
                            prestamoViewModel.seleccionarEquipoPorId(equipoId)
                            prestamoViewModel.limpiarMensajeError()
                        }
                        SolicitudScreen(
                            uiState = uiState,
                            onAmbienteChange = prestamoViewModel::onAmbienteChanged,
                            onPropositoChange = prestamoViewModel::onPropositoChanged,
                            onDuracionChange = prestamoViewModel::onDuracionChanged,
                            onGuardarClick = { prestamoViewModel.guardarSolicitud() },
                            onAtrasClick = { navController.popBackStack() }
                        )
                    }
                }
                composable(Rutas.MIS_SOLICITUDES) {
                    RutaProtegida(Rutas.MIS_SOLICITUDES, usuario, onVolver = { navController.popBackStack() }) {
                        MisSolicitudesScreen(
                            solicitudes = uiState.solicitudes,
                            equipos = uiState.equipos,
                            onCancelarClick = prestamoViewModel::cancelarSolicitud,
                            onRegistrarDevolucionClick = { id -> navController.navigate(Rutas.devolucion(id)) }
                        )
                    }
                }
                composable(
                    route = Rutas.DEVOLUCION,
                    arguments = listOf(navArgument(Rutas.ARG_SOLICITUD_ID) { type = NavType.IntType })
                ) { entrada ->
                    val solicitudId = entrada.arguments?.getInt(Rutas.ARG_SOLICITUD_ID) ?: -1
                    RutaProtegida(Rutas.DEVOLUCION, usuario, onVolver = { navController.popBackStack() }) {
                        val viewModel: DevolucionViewModel = viewModel(factory = DevolucionViewModel.factory(solicitudId))
                        DevolucionRoute(viewModel = viewModel, onVolver = { navController.popBackStack() })
                    }
                }
                composable(Rutas.GESTION) {
                    RutaProtegida(Rutas.GESTION, usuario, onVolver = { navController.popBackStack() }) {
                        GestionScreen()
                    }
                }
            }
        }
    }
}

/** Bloquea el contenido si el rol no tiene permiso, aunque se llegue a la ruta directamente. */
@Composable
private fun RutaProtegida(
    ruta: String,
    usuario: Usuario,
    onVolver: () -> Unit,
    contenido: @Composable () -> Unit
) {
    if (ControlAcceso.puedeAcceder(ruta, usuario.rol)) {
        contenido()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Acceso denegado", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Tu rol (${usuario.rol.name}) no tiene permiso para esta sección.")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onVolver) { Text("Volver") }
        }
    }
}
