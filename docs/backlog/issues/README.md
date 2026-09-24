# Backlog y matriz de trazabilidad: PréstamoLab CTMA (Parte 2)

Archivos generados por `generar_issues.py`; no editar a mano.
Para crear cada Issue en GitHub: **New issue**, pegar el título (primera línea sin `# `) y el resto como descripción.

## Historias

| Issue | Historia | Prioridad | Sprint | Criterios | Automatizados |
|---|---|---|---|---|---|
| [HU-05](HU05.md) | Registrar devolución | Alta | 5 | 5 | 5 |
| [HU-10](HU10.md) | Iniciar sesión y control de acceso por rol | Alta | 5 | 7 | 7 |
| [HU-13](HU13.md) | Registrar geolocalización de las operaciones | Media | 5 | 6 | 3 |
| [HU-01](HU01.md) | Consultar equipos disponibles | Alta | 6 | 5 | 1 |
| [HU-02](HU02.md) | Consultar detalle de un equipo | Alta | 6 | 4 | 4 |
| [HU-06](HU06.md) | Conservar datos localmente sin conexión | Alta | 6 | 5 | 3 |
| [HU-11](HU11.md) | Gestionar actividades formativas (instructor) | Media | 6 | 5 | 5 |
| [HU-12](HU12.md) | Gestionar inventario de equipos (instructor) | Media | 6 | 5 | 5 |
| [HU-03](HU03.md) | Solicitar préstamo | Alta | 7 | 8 | 8 |
| [HU-04](HU04.md) | Consultar mis préstamos activos | Alta | 7 | 6 | 3 |
| [HU-14](HU14.md) | Revisar solicitudes de préstamo (instructor) | Alta | 7 | 4 | 4 |
| [HU-07](HU07.md) | Sincronizar datos con servicio remoto | Media/Alta | 8 | 5 | 5 |
| [HU-08](HU08.md) | Adjuntar evidencia fotográfica | Media | 8 | 5 | 0 |
| [HU-09](HU09.md) | Recibir recordatorio de devolución | Media | 9 | 4 | 0 |

**Total:** 74 criterios, 74 casos de prueba, 53 ya automatizados.

## Matriz HU → CA → TC → prueba

| Historia | Criterio | Caso | Tipo | Prueba automatizada |
|---|---|---|---|---|
| HU01 | CA-HU01-01 | TC-HU01-01 | UI | `PrestamoUiTest.TC01_CargarCatalogoInicial_MuestraEquipos` |
| HU01 | CA-HU01-02 | TC-HU01-02 | UI | Pendiente |
| HU01 | CA-HU01-03 | TC-HU01-03 | Unitaria | Pendiente |
| HU01 | CA-HU01-04 | TC-HU01-04 | Instrumentada | Pendiente |
| HU01 | CA-HU01-05 | TC-HU01-05 | UI | Pendiente |
| HU02 | CA-HU02-01 | TC-HU02-01 | UI | `PrestamoUiTest.TC02_VerDetalleEquipoValido, DetalleEquipo_VerificaDisponibilidadVisible; PrestamoViewModelTest.TC-02` |
| HU02 | CA-HU02-02 | TC-HU02-02 | Unitaria | `PrestamoViewModelTest.TC-03` |
| HU02 | CA-HU02-03 | TC-HU02-03 | UI | `PrestamoUiTest.TC12_BotonSolicitarDeshabilitado_SiEquipoEstaReservado` |
| HU02 | CA-HU02-04 | TC-HU02-04 | Unitaria | `PrestamoViewModelTest.Detalle seleccionado se actualiza cuando cambia el estado del equipo` |
| HU03 | CA-HU03-01 | TC-HU03-01 | Unitaria | `PrestamoViewModelTest.Validacion Ambiente` |
| HU03 | CA-HU03-02 | TC-HU03-02 | Unitaria | `PrestamoViewModelTest.TC-04..TC-07` |
| HU03 | CA-HU03-03 | TC-HU03-03 | Unitaria | `PrestamoViewModelTest.TC-08..TC-11` |
| HU03 | CA-HU03-04 | TC-HU03-04 | Unitaria | `PrestamoViewModelTest.TC-12; RoomPrestamoRepositoryTest.CrearSolicitudSobreEquipoReservado_FallaSinCrearRegistros` |
| HU03 | CA-HU03-05 | TC-HU03-05 | Unitaria | `PrestamoViewModelTest.TC-13` |
| HU03 | CA-HU03-06 | TC-HU03-06 | Unitaria + UI | `PrestamoViewModelTest.TC-14; PrestamoUiTest.TC14_FlujoCompleto_CrearSolicitud` |
| HU03 | CA-HU03-07 | TC-HU03-07 | Unitaria | `PrestamoViewModelTest.Fallo del repositorio muestra mensaje de error` |
| HU03 | CA-HU03-08 | TC-HU03-08 | UI + Unitaria | `LoginUiTest.TC_HU03_08_Instructor_NoVeSolicitarPrestamo; PrestamoViewModelTest.TC-HU03-08` |
| HU04 | CA-HU04-01 | TC-HU04-01 | Unitaria | Pendiente |
| HU04 | CA-HU04-02 | TC-HU04-02 | UI | Pendiente |
| HU04 | CA-HU04-03 | TC-HU04-03 | UI | `PrestamoUiTest.TC15_CancelarSolicitud_ActualizaLista` |
| HU04 | CA-HU04-04 | TC-HU04-04 | UI | Pendiente |
| HU04 | CA-HU04-05 | TC-HU04-05 | Unitaria | `PrestamoViewModelTest.TC-15` |
| HU04 | CA-HU04-06 | TC-HU04-06 | Unitaria | `PrestamoViewModelTest.TC-16; RoomPrestamoRepositoryTest.Cancelar_EsIdempotenteYLiberaElEquipo` |
| HU05 | CA-HU05-01 | TC-HU05-01 | UI + Unitaria | `DevolucionUiTest.TC_HU05_01_SoloElPrestamoPrestadoOfreceRegistrarDevolucion; DevolucionViewModelTest.TC-HU05-01` |
| HU05 | CA-HU05-02 | TC-HU05-02 | Unitaria (TDD) + UI | `DevolucionViewModelTest.TC-HU05-02; DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo` |
| HU05 | CA-HU05-03 | TC-HU05-03 | Unitaria + Integración | `DevolucionViewModelTest.TC-HU05-03; RoomPrestamoRepositoryTest.TC_HU05_03_Devolucion_GuardaReturnEntityConCondicionFechaYUbicacion` |
| HU05 | CA-HU05-04 | TC-HU05-04 | Unitaria + UI | `DevolucionViewModelTest.TC-HU05-04; DevolucionUiTest.TC_HU05_04_EquipoDanadoSinObservacion_MuestraError` |
| HU05 | CA-HU05-05 | TC-HU05-05 | Unitaria (TDD) | `RoomPrestamoRepositoryTest.TC_HU05_05_DevolverUnPrestamoYaDevuelto_SeRechazaSinCambios` |
| HU06 | CA-HU06-01 | TC-HU06-01 | Instrumentada | Pendiente |
| HU06 | CA-HU06-02 | TC-HU06-02 | Integración | `RoomPrestamoRepositoryTest.TC_HU06_02_SolicitudNueva_QuedaPendienteConUuidYPideSincronizar` |
| HU06 | CA-HU06-03 | TC-HU06-03 | Integración | `RoomPrestamoRepositoryTest.TC_HU06_03_ElFlujoDeEquiposEmiteLosCambiosDeRoom` |
| HU06 | CA-HU06-04 | TC-HU06-04 | Instrumentada | `MigracionTest.TC_HU06_04_MigracionDeV1AV2ConservaLosDatos` |
| HU06 | CA-HU06-05 | TC-HU06-05 | Integración | Pendiente |
| HU07 | CA-HU07-01 | TC-HU07-01 | Integración | `SincronizadorPrestamosTest.TC_HU07_01_PendientesSeEnvianYQuedanSincronizados; SupabasePrestamosDataSourceTest.TC-HU07-01` |
| HU07 | CA-HU07-02 | TC-HU07-02 | Integración | `SincronizadorPrestamosTest.TC_HU07_02_DatosRemotosNuevosActualizanRoom; SupabasePrestamosDataSourceTest.TC-HU07-02` |
| HU07 | CA-HU07-03 | TC-HU07-03 | Integración | `CoordinadorSincronizacionTest.TC-HU07-03; SincronizacionWorkerTest.TC_HU07_03_Respuesta401_CierraLaSesionYFalla; SupabasePrestamosDataSourceTest.TC-HU07-03` |
| HU07 | CA-HU07-04 | TC-HU07-04 | Integración | `CoordinadorSincronizacionTest.TC-HU07-04; SincronizadorPrestamosTest.TC_HU07_04_Respuesta404ConservaLosDatosLocales; PrestamoViewModelTest.TC-HU07-04` |
| HU07 | CA-HU07-05 | TC-HU07-05 | Integración | `SincronizacionWorkerTest.TC_HU07_05_Error5xx_PideReintentarConEsperaExponencial; SincronizadorPrestamosTest.TC_HU07_05_Error5xxOTiempoAgotadoDejaLosRegistrosPendientes; SupabasePrestamosDataSourceTest.TC-HU07-05` |
| HU08 | CA-HU08-01 | TC-HU08-01 | Instrumentada | Pendiente |
| HU08 | CA-HU08-02 | TC-HU08-02 | Integración | Pendiente |
| HU08 | CA-HU08-03 | TC-HU08-03 | Instrumentada | Pendiente |
| HU08 | CA-HU08-04 | TC-HU08-04 | Unitaria | Pendiente |
| HU08 | CA-HU08-05 | TC-HU08-05 | Integración | Pendiente |
| HU09 | CA-HU09-01 | TC-HU09-01 | Integración | Pendiente |
| HU09 | CA-HU09-02 | TC-HU09-02 | Instrumentada | Pendiente |
| HU09 | CA-HU09-03 | TC-HU09-03 | Integración | Pendiente |
| HU09 | CA-HU09-04 | TC-HU09-04 | Instrumentada | Pendiente |
| HU10 | CA-HU10-01 | TC-HU10-01 | Unitaria | `LoginViewModelTest.TC-HU10-01; LoginUiTest.TC_HU10_01_LoginValido_MuestraCatalogo` |
| HU10 | CA-HU10-02 | TC-HU10-02 | Unitaria | `LoginViewModelTest.TC-HU10-02; LoginUiTest.TC_HU10_02_LoginInvalido_MuestraErrorYSigueEnLogin` |
| HU10 | CA-HU10-03 | TC-HU10-03 | Unitaria | `LoginViewModelTest.TC-HU10-03; LoginUiTest.TC_HU10_03_CamposVacios_MuestranErrores` |
| HU10 | CA-HU10-04 | TC-HU10-04 | Instrumentada | `DataStoreSessionStoreTest.TC_HU10_04_SesionPersisteAlReabrirElAlmacenamiento` |
| HU10 | CA-HU10-05 | TC-HU10-05 | Unitaria | `SesionViewModelTest.TC-HU10-05; LoginUiTest.TC_HU10_05_CerrarSesion_VuelveAlLogin` |
| HU10 | CA-HU10-06 | TC-HU10-06 | UI | `ControlAccesoTest.TC-HU10-06; LoginUiTest.TC_HU10_06_Estudiante_NoVeGestion` |
| HU10 | CA-HU10-07 | TC-HU10-07 | UI | `ControlAccesoTest.TC-HU10-07; LoginUiTest.TC_HU10_07_Instructor_IniciaEnGestion` |
| HU11 | CA-HU11-01 | TC-HU11-01 | Integración | `RoomActividadRepositoryTest.TC_HU11_01_CrearActividad_QuedaGuardadaYApareceEnLaLista; ActividadesViewModelTest.TC-HU11-01; ActividadesUiTest.TC_HU11_01_CrearActividad_ApareceEnLaLista` |
| HU11 | CA-HU11-02 | TC-HU11-02 | Unitaria | `ReglasActividadTest.TC_HU11_02_TituloVacio_MuestraErrorDelCampo; ReglasActividadTest.TC_HU11_02_FechaPasada_MuestraErrorDelCampo; ActividadesUiTest.TC_HU11_02_TituloVacioYFechaPasada_MuestranErroresYNoSeGuarda` |
| HU11 | CA-HU11-03 | TC-HU11-03 | Integración | `RoomActividadRepositoryTest.TC_HU11_03_EditarActividad_ReflejaLosCambios; ActividadesUiTest.TC_HU11_03_EditarActividad_SeReflejaEnLaLista` |
| HU11 | CA-HU11-04 | TC-HU11-04 | UI | `ActividadesUiTest.TC_HU11_04_EliminarConConfirmacion_YCancelarLaConserva; RoomActividadRepositoryTest.TC_HU11_04_EliminarActividad_DesapareceYQuedaPendienteDeEnviar` |
| HU11 | CA-HU11-05 | TC-HU11-05 | UI | `ActividadesUiTest.TC_HU11_05_ElEstudianteVeLasActividadesEnSoloLectura; ControlAccesoTest.TC-HU11-05; ActividadesViewModelTest.TC-HU11-05` |
| HU12 | CA-HU12-01 | TC-HU12-01 | Integración | `RoomPrestamoRepositoryTest.TC_HU12_01_RegistrarEquipo_QuedaDisponibleEnElCatalogoYPendienteDeEnviar; InventarioViewModelTest.TC-HU12-01; InventarioUiTest.TC_HU12_01_RegistrarEquipo_ApareceDisponibleEnElCatalogo` |
| HU12 | CA-HU12-02 | TC-HU12-02 | Unitaria | `ReglasInventarioTest.TC_HU12_02_NombreVacio_MuestraErrorDelCampo; InventarioViewModelTest.TC-HU12-02; InventarioUiTest.TC_HU12_02_CamposVacios_MuestranErroresYNoSeGuarda` |
| HU12 | CA-HU12-03 | TC-HU12-03 | Integración | `RoomPrestamoRepositoryTest.TC_HU12_03_EditarEquipo_CambiaCatalogoYDetalle; InventarioUiTest.TC_HU12_03_EditarEquipo_SeVeEnElCatalogoYEnElDetalle` |
| HU12 | CA-HU12-04 | TC-HU12-04 | Unitaria | `ReglasInventarioTest.TC_HU12_04_ConPrestamoActivo_LaEliminacionSeRechaza; RoomPrestamoRepositoryTest.TC_HU12_04_EliminarEquipoConPrestamoActivo_SeRechaza; InventarioUiTest.TC_HU12_04_EliminarEquipoConPrestamoActivo_MuestraElMotivo` |
| HU12 | CA-HU12-05 | TC-HU12-05 | UI | `InventarioUiTest.TC_HU12_05_ElEstudianteNoVeOpcionesDeInventario; ControlAccesoTest.TC-HU12-05` |
| HU13 | CA-HU13-01 | TC-HU13-01 | Manual | Pendiente |
| HU13 | CA-HU13-02 | TC-HU13-02 | UI + Unitaria | `DevolucionUiTest.TC_HU13_02_CapturarUbicacion_MuestraCoordenadasYMensaje; DevolucionViewModelTest.TC-HU13-02` |
| HU13 | CA-HU13-03 | TC-HU13-03 | Unitaria | Pendiente |
| HU13 | CA-HU13-04 | TC-HU13-04 | Unitaria + UI | `DevolucionViewModelTest.TC-HU13-04; DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo` |
| HU13 | CA-HU13-05 | TC-HU13-05 | Unitaria | `DevolucionViewModelTest.TC-HU13-05` |
| HU13 | CA-HU13-06 | TC-HU13-06 | Integración | Pendiente |
| HU14 | CA-HU14-01 | TC-HU14-01 | UI | `RevisarSolicitudesUiTest.TC_HU14_01_ListaMuestraEstudianteEquipoAmbientePropositoYDuracion; ControlAccesoTest.TC-HU14-01` |
| HU14 | CA-HU14-02 | TC-HU14-02 | Unitaria (TDD) | `RevisionSolicitudTest.TC_HU14_02_AprobarSolicitada_PasaAPrestadoYEntregaElEquipo; RoomPrestamoRepositoryTest.TC_HU14_02_AprobarSolicitud_EntregaElEquipoYQuedaPendienteDeEnviar; RevisarSolicitudesUiTest.TC_HU14_02_AprobarQuitaLaSolicitudYElEquipoQuedaPrestado` |
| HU14 | CA-HU14-03 | TC-HU14-03 | Unitaria (TDD) | `RevisionSolicitudTest.TC_HU14_03_RechazarSolicitada_GuardaElMotivoYLiberaElEquipo; RoomPrestamoRepositoryTest.TC_HU14_03_RechazarSolicitud_GuardaElMotivoYLiberaElEquipo; RevisarSolicitudesUiTest.TC_HU14_03_RechazarExigeMotivoYLiberaElEquipo` |
| HU14 | CA-HU14-04 | TC-HU14-04 | Unitaria (TDD) | `RevisionSolicitudTest.TC_HU14_04_AprobarCanceladaORechazada_SeRechazaSinCambios; RoomPrestamoRepositoryTest.TC_HU14_04_AprobarUnaSolicitudCancelada_SeRechazaSinCambios` |

## Equivalencia con la numeración anterior (Parte 1)

| Anterior | Nuevo |
|---|---|
| TC-01 | TC-HU01-01 |
| TC-02 | TC-HU02-01 |
| TC-03 | TC-HU02-02 |
| TC-04 a TC-07 | TC-HU03-02 |
| TC-08 a TC-11 | TC-HU03-03 |
| TC-12 | TC-HU03-04 |
| TC-13 (BUG-03) | TC-HU03-05 |
| TC-14 | TC-HU03-06 |
| TC-15 | TC-HU04-05 |
| TC-16 | TC-HU04-06 |
