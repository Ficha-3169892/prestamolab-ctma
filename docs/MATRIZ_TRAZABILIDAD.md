# Matriz de trazabilidad: PréstamoLab CTMA (Parte 2)

Generada por `docs/backlog/generar_issues.py`; no editar a mano. Cada criterio de aceptación
CA-HUxx-nn tiene exactamente un caso de prueba TC-HUxx-nn (trazabilidad 1:1), y cada caso apunta a
la prueba automatizada que lo verifica (`Clase.metodo`) o queda como pendiente.

**Cobertura:** 74 de 74 criterios automatizados (100 %).

## Resumen por historia

| Historia | Título | Sprint | Criterios | Automatizados | Pendientes |
|---|---|---|---|---|---|
| HU-05 | Registrar devolución | 5 | 5 | 5 | 0 |
| HU-10 | Iniciar sesión y control de acceso por rol | 5 | 7 | 7 | 0 |
| HU-13 | Registrar geolocalización de las operaciones | 5 | 6 | 6 | 0 |
| HU-01 | Consultar equipos disponibles | 6 | 5 | 5 | 0 |
| HU-02 | Consultar detalle de un equipo | 6 | 4 | 4 | 0 |
| HU-06 | Conservar datos localmente sin conexión | 6 | 5 | 5 | 0 |
| HU-11 | Gestionar actividades formativas (instructor) | 6 | 5 | 5 | 0 |
| HU-12 | Gestionar inventario de equipos (instructor) | 6 | 5 | 5 | 0 |
| HU-03 | Solicitar préstamo | 7 | 8 | 8 | 0 |
| HU-04 | Consultar mis préstamos activos | 7 | 6 | 6 | 0 |
| HU-14 | Revisar solicitudes de préstamo (instructor) | 7 | 4 | 4 | 0 |
| HU-07 | Sincronizar datos con servicio remoto | 8 | 5 | 5 | 0 |
| HU-08 | Adjuntar evidencia fotográfica | 8 | 5 | 5 | 0 |
| HU-09 | Recibir recordatorio de devolución | 9 | 4 | 4 | 0 |

## HU-01: Consultar equipos disponibles

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU01-01 | Dado que existen equipos registrados, cuando abro la pantalla Catálogo, entonces veo cada equipo con nombre, categoría y estado (DISPONIBLE, RESERVADO o PRESTADO). | TC-HU01-01 | UI | Automatizada | `PrestamoUiTest.TC01_CargarCatalogoInicial_MuestraEquipos` |
| CA-HU01-02 | Dado que un equipo no está DISPONIBLE, cuando veo el catálogo, entonces su estado se muestra con un color distinto al de los disponibles. | TC-HU01-02 | UI | Automatizada | `ColorEstadoTest.TC_HU01_02_LosNoDisponiblesTienenUnColorDistintoAlDeLosDisponibles` |
| CA-HU01-03 | Dado que hay equipos de varias categorías, cuando activo el filtro "Solo disponibles" o elijo una categoría, entonces la lista muestra solo los equipos que cumplen el filtro. | TC-HU01-03 | Unitaria | Automatizada | `FiltroCatalogoTest.TC_HU01_03_SoloDisponibles`<br>`FiltroCatalogoTest.TC_HU01_03_PorCategoria`<br>`PrestamoViewModelTest.TC-HU01-03`<br>`CatalogoFiltrosUiTest.TC_HU01_03_SoloDisponiblesYCategoria_FiltranLaLista` |
| CA-HU01-04 | Dado que apliqué un filtro, cuando cierro y vuelvo a abrir la app, entonces el filtro se conserva (DataStore). | TC-HU01-04 | Instrumentada | Automatizada | `CatalogoFiltrosUiTest.TC_HU01_04_ElFiltroSeConservaAlCerrarYVolverAAbrirLaApp`<br>`PrestamoViewModelTest.TC-HU01-04` |
| CA-HU01-05 | Dado que no hay equipos que cumplan el filtro, cuando veo el catálogo, entonces aparece el mensaje "No hay equipos para mostrar" en lugar de una lista vacía. | TC-HU01-05 | UI | Automatizada | `CatalogoFiltrosUiTest.TC_HU01_05_SinResultados_MuestraElMensajeYSePuedenQuitarLosFiltros`<br>`PrestamoViewModelTest.TC-HU01-05` |

## HU-02: Consultar detalle de un equipo

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU02-01 | Dado que estoy en el catálogo, cuando toco un equipo, entonces se abre el detalle con id, nombre, categoría y estado de ese equipo. | TC-HU02-01 | UI | Automatizada | `PrestamoUiTest.TC02_VerDetalleEquipoValido, DetalleEquipo_VerificaDisponibilidadVisible`<br>`PrestamoViewModelTest.TC-02` |
| CA-HU02-02 | Dado que navego con un id que no existe, cuando se carga el detalle, entonces veo "Equipo no encontrado" y un botón para volver, sin cierre inesperado. | TC-HU02-02 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-03` |
| CA-HU02-03 | Dado que el equipo no está DISPONIBLE, cuando veo su detalle, entonces el botón "Solicitar Préstamo" está deshabilitado. | TC-HU02-03 | UI | Automatizada | `PrestamoUiTest.TC12_BotonSolicitarDeshabilitado_SiEquipoEstaReservado` |
| CA-HU02-04 | Dado que estoy viendo el detalle, cuando el estado del equipo cambia en la fuente de datos, entonces el detalle muestra el estado nuevo sin volver a abrirlo. | TC-HU02-04 | Unitaria | Automatizada | `PrestamoViewModelTest.Detalle seleccionado se actualiza cuando cambia el estado del equipo` |

## HU-03: Solicitar préstamo

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU03-01 | Dado que estoy en el formulario, cuando guardo con el ambiente vacío, entonces veo "El ambiente o destino es obligatorio." y no se crea la solicitud. | TC-HU03-01 | Unitaria | Automatizada | `PrestamoViewModelTest.Validacion Ambiente` |
| CA-HU03-02 | Dado que estoy en el formulario, cuando escribo un propósito de 9 o de 181 caracteres, entonces veo el error de longitud; con 10 y con 180 caracteres se acepta. | TC-HU03-02 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-04..TC-07` |
| CA-HU03-03 | Dado que estoy en el formulario, cuando indico una duración de 0 o de 9 horas, entonces veo "Duración entre 1 y 8 horas"; con 1 y con 8 horas se acepta. | TC-HU03-03 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-08..TC-11` |
| CA-HU03-04 | Dado que el equipo está RESERVADO o PRESTADO, cuando intento solicitarlo, entonces la solicitud se rechaza y no se crea ningún registro. | TC-HU03-04 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-12`<br>`RoomPrestamoRepositoryTest.CrearSolicitudSobreEquipoReservado_FallaSinCrearRegistros` |
| CA-HU03-05 | Dado que el formulario es válido, cuando pulso Guardar varias veces seguidas, entonces se crea una sola solicitud y el botón muestra "Procesando...". | TC-HU03-05 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-13` |
| CA-HU03-06 | Dado que el formulario es válido, cuando guardo, entonces la solicitud queda SOLICITADA con ambiente, propósito y duración, y el equipo pasa a RESERVADO. | TC-HU03-06 | Unitaria + UI | Automatizada | `PrestamoViewModelTest.TC-14`<br>`PrestamoUiTest.TC14_FlujoCompleto_CrearSolicitud` |
| CA-HU03-07 | Dado que el repositorio falla al guardar, cuando guardo, entonces veo el mensaje de error, el botón se habilita de nuevo y sigo en el formulario. | TC-HU03-07 | Unitaria | Automatizada | `PrestamoViewModelTest.Fallo del repositorio muestra mensaje de error` |
| CA-HU03-08 | Dado que inicié sesión como INSTRUCTOR, cuando abro el detalle de un equipo, entonces no veo la opción de solicitar préstamo (acción exclusiva del estudiante). | TC-HU03-08 | UI + Unitaria | Automatizada | `LoginUiTest.TC_HU03_08_Instructor_NoVeSolicitarPrestamo`<br>`PrestamoViewModelTest.TC-HU03-08` |

## HU-04: Consultar mis préstamos activos

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU04-01 | Dado que hay solicitudes de varios usuarios, cuando abro "Mis Solicitudes", entonces veo solo las del usuario con sesión iniciada. | TC-HU04-01 | Unitaria | Automatizada | `PrestamoViewModelTest.Mis Solicitudes del estudiante solo muestra las suyas y el instructor ve todas` |
| CA-HU04-02 | Dado que tengo solicitudes, cuando abro "Mis Solicitudes", entonces cada una muestra equipo, fechas, ambiente y estado. | TC-HU04-02 | UI | Automatizada | `CatalogoFiltrosUiTest.TC_HU04_02_CadaSolicitudMuestraEquipoFechasAmbienteYEstado` |
| CA-HU04-03 | Dado que tengo solicitudes CANCELADAS o DEVUELTAS, cuando abro "Mis Solicitudes", entonces no aparecen en la lista de activas. | TC-HU04-03 | UI | Automatizada | `PrestamoUiTest.TC15_CancelarSolicitud_ActualizaLista` |
| CA-HU04-04 | Dado que no tengo solicitudes activas, cuando abro "Mis Solicitudes", entonces veo "No tienes solicitudes activas.". | TC-HU04-04 | UI | Automatizada | `CatalogoFiltrosUiTest.TC_HU04_04_SinSolicitudesActivas_MuestraElMensaje` |
| CA-HU04-05 | Dado que tengo una solicitud SOLICITADA, cuando la cancelo, entonces pasa a CANCELADA y el equipo vuelve a DISPONIBLE. | TC-HU04-05 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-15` |
| CA-HU04-06 | Dado que una solicitud ya está CANCELADA, cuando intento cancelarla otra vez, entonces no se produce ningún cambio ni error. | TC-HU04-06 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-16`<br>`RoomPrestamoRepositoryTest.Cancelar_EsIdempotenteYLiberaElEquipo` |

## HU-05: Registrar devolución

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU05-01 | Dado que tengo préstamos en distintos estados, cuando abro "Mis Solicitudes", entonces veo "Registrar devolución" solo en los préstamos PRESTADO; las solicitudes SOLICITADAS muestran "Cancelar Solicitud". | TC-HU05-01 | UI + Unitaria | Automatizada | `DevolucionUiTest.TC_HU05_01_SoloElPrestamoPrestadoOfreceRegistrarDevolucion`<br>`DevolucionViewModelTest.TC-HU05-01` |
| CA-HU05-02 | Dado que tengo un préstamo PRESTADO, cuando registro la devolución, entonces el préstamo pasa a DEVUELTO, el equipo a DISPONIBLE y el préstamo sale de mis activos. | TC-HU05-02 | Unitaria (TDD) + UI | Automatizada | `DevolucionViewModelTest.TC-HU05-02`<br>`DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo` |
| CA-HU05-03 | Dado que estoy registrando la devolución, cuando elijo el estado del equipo (Bueno, Con novedad, Dañado) y escribo una observación, entonces se guarda un registro de devolución (`returns`) con esos datos y la fecha/hora. | TC-HU05-03 | Unitaria + Integración | Automatizada | `DevolucionViewModelTest.TC-HU05-03`<br>`RoomPrestamoRepositoryTest.TC_HU05_03_Devolucion_GuardaReturnEntityConCondicionFechaYUbicacion` |
| CA-HU05-04 | Dado que marco el equipo como Dañado, cuando guardo la devolución, entonces la observación es obligatoria (mínimo 10 caracteres). | TC-HU05-04 | Unitaria + UI | Automatizada | `DevolucionViewModelTest.TC-HU05-04`<br>`DevolucionUiTest.TC_HU05_04_EquipoDanadoSinObservacion_MuestraError` |
| CA-HU05-05 | Dado que el préstamo ya está DEVUELTO, cuando intento devolverlo otra vez, entonces la operación se rechaza sin cambios. | TC-HU05-05 | Unitaria (TDD) | Automatizada | `RoomPrestamoRepositoryTest.TC_HU05_05_DevolverUnPrestamoYaDevuelto_SeRechazaSinCambios` |

## HU-06: Conservar datos localmente sin conexión

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU06-01 | Dado que hay datos guardados, cuando reinicio la app en modo avión, entonces el catálogo y mis préstamos siguen visibles (Room). | TC-HU06-01 | Instrumentada | Automatizada | `PersistenciaSinConexionTest.TC_HU06_01_TrasReiniciarSinRedElCatalogoYMisPrestamosSiguenVisibles` |
| CA-HU06-02 | Dado que no hay conexión, cuando creo una solicitud, entonces se guarda localmente con estado de sincronización PENDIENTE. | TC-HU06-02 | Integración | Automatizada | `RoomPrestamoRepositoryTest.TC_HU06_02_SolicitudNueva_QuedaPendienteConUuidYPideSincronizar` |
| CA-HU06-03 | Dado que la app está en uso, cuando cambian los datos en Room, entonces la UI se actualiza sola, porque solo lee de Room mediante Flow. | TC-HU06-03 | Integración | Automatizada | `RoomPrestamoRepositoryTest.TC_HU06_03_ElFlujoDeEquiposEmiteLosCambiosDeRoom` |
| CA-HU06-04 | Dado que existe una versión anterior de la base de datos, cuando actualizo la app, entonces la migración conserva los datos existentes. | TC-HU06-04 | Instrumentada | Automatizada | `MigracionTest.TC_HU06_04_MigracionDeV1AV2ConservaLosDatos` |
| CA-HU06-05 | Dado que existen entidades relacionadas, cuando consulto un préstamo, entonces obtengo el préstamo con su equipo y sus evidencias en una sola consulta (@Relation). | TC-HU06-05 | Integración | Automatizada | `RoomPrestamoRepositoryTest.TC_HU06_05_ElPrestamoLlegaConSuEquipoYSusEvidenciasEnUnaSolaConsulta` |

## HU-07: Sincronizar datos con servicio remoto

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU07-01 | Dado que hay registros PENDIENTES, cuando se recupera la conexión, entonces se envían a Supabase y quedan SINCRONIZADOS. | TC-HU07-01 | Integración | Automatizada | `SincronizadorPrestamosTest.TC_HU07_01_PendientesSeEnvianYQuedanSincronizados`<br>`SupabasePrestamosDataSourceTest.TC-HU07-01` |
| CA-HU07-02 | Dado que el servidor responde 200 con datos nuevos, cuando se sincroniza, entonces Room se actualiza y la UI muestra los datos remotos. | TC-HU07-02 | Integración | Automatizada | `SincronizadorPrestamosTest.TC_HU07_02_DatosRemotosNuevosActualizanRoom`<br>`SupabasePrestamosDataSourceTest.TC-HU07-02` |
| CA-HU07-03 | Dado que el servidor responde 401, cuando se sincroniza, entonces se cierra la sesión y se lleva al usuario al login. | TC-HU07-03 | Integración | Automatizada | `CoordinadorSincronizacionTest.TC-HU07-03`<br>`SincronizacionWorkerTest.TC_HU07_03_Respuesta401_CierraLaSesionYFalla`<br>`SupabasePrestamosDataSourceTest.TC-HU07-03` |
| CA-HU07-04 | Dado que el servidor responde 404, cuando se sincroniza un recurso, entonces se muestra un mensaje y los datos locales se conservan. | TC-HU07-04 | Integración | Automatizada | `CoordinadorSincronizacionTest.TC-HU07-04`<br>`SincronizadorPrestamosTest.TC_HU07_04_Respuesta404ConservaLosDatosLocales`<br>`PrestamoViewModelTest.TC-HU07-04` |
| CA-HU07-05 | Dado que el servidor responde 5xx o se agota el tiempo de espera, cuando se sincroniza, entonces se reintenta con espera exponencial y el registro sigue PENDIENTE, sin cierre inesperado. | TC-HU07-05 | Integración | Automatizada | `SincronizacionWorkerTest.TC_HU07_05_Error5xx_PideReintentarConEsperaExponencial`<br>`SincronizadorPrestamosTest.TC_HU07_05_Error5xxOTiempoAgotadoDejaLosRegistrosPendientes`<br>`SupabasePrestamosDataSourceTest.TC-HU07-05` |

## HU-08: Adjuntar evidencia fotográfica

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU08-01 | Dado que no he concedido el permiso de cámara, cuando toco "Adjuntar evidencia", entonces la app pide el permiso en ese momento (no al abrirla). | TC-HU08-01 | Instrumentada | Automatizada | `EvidenciasUiTest.TC_HU08_01_Y_03_ElPermisoSePideAlAdjuntar_YNegarloMuestraUnMensaje` |
| CA-HU08-02 | Dado que tomo una foto, cuando confirmo la captura, entonces la imagen se guarda mediante FileProvider y su URI queda asociada al préstamo en Room. | TC-HU08-02 | Integración | Automatizada | `EvidenciasDatosTest.TC_HU08_02_LaUriQuedaAsociadaAlPrestamoEnRoom`<br>`EvidenciasViewModelTest.TC_HU08_02_AlConfirmarLaFoto_SuUriQuedaAsociadaAlPrestamo` |
| CA-HU08-03 | Dado que niego el permiso de cámara, cuando intento adjuntar, entonces veo un mensaje explicativo y la app sigue funcionando. | TC-HU08-03 | Instrumentada | Automatizada | `EvidenciasUiTest.TC_HU08_01_Y_03_ElPermisoSePideAlAdjuntar_YNegarloMuestraUnMensaje`<br>`EvidenciasViewModelTest.TC_HU08_03_PermisoNegado_MuestraUnMensajeExplicativo` |
| CA-HU08-04 | Dado que abro la cámara, cuando cancelo sin tomar foto, entonces no se crea ninguna evidencia. | TC-HU08-04 | Unitaria | Automatizada | `EvidenciasViewModelTest.TC_HU08_04_CancelarLaCamara_NoCreaEvidenciaYBorraElArchivo` |
| CA-HU08-05 | Dado que hay evidencias sin subir, cuando se sincroniza, entonces las fotos se suben a Supabase Storage y se guarda su URL remota. | TC-HU08-05 | Integración | Automatizada | `SincronizadorPrestamosTest.TC_HU08_05_LaFotoSeSubeAStorageYSeGuardaSuUrlRemota`<br>`SupabasePrestamosDataSourceTest.TC-HU08-05` |

## HU-09: Recibir recordatorio de devolución

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU09-01 | Dado que un préstamo pasa a PRESTADO, cuando se registra el cambio, entonces se programa un recordatorio 30 minutos antes de la fecha de fin. | TC-HU09-01 | Integración | Automatizada | `WorkManagerRecordatoriosTest.TC_HU09_01_SeProgramaUnTrabajoParaLaHoraDelAviso`<br>`PlanRecordatoriosTest.TC_HU09_01_PrestamoPrestado_SeProgramaTreintaMinutosAntesDelFin`<br>`CoordinadorRecordatoriosTest.TC_HU09_01_AlAprobarseUnaSolicitudSeProgramaSuRecordatorio` |
| CA-HU09-02 | Dado que llega la hora del recordatorio, cuando se muestra la notificación, entonces indica el equipo y la hora límite, y al tocarla abre el préstamo. | TC-HU09-02 | Instrumentada | Automatizada | `RecordatorioWorkerTest.TC_HU09_02_AlLlegarLaHora_MuestraElEquipoYLaHoraLimite`<br>`AndroidNotificadorTest.TC_HU09_02_PublicaElAvisoConEquipoHoraYAccionDeApertura`<br>`RecordatorioUiTest.TC_HU09_02_TocarElRecordatorio_AbreElPrestamo` |
| CA-HU09-03 | Dado que registré la devolución, cuando se guarda, entonces el recordatorio pendiente se cancela. | TC-HU09-03 | Integración | Automatizada | `WorkManagerRecordatoriosTest.TC_HU09_03_ElPrestamoQueSaleDeLaListaQuedaCancelado`<br>`CoordinadorRecordatoriosTest.TC_HU09_03_AlDevolverSeCancelaElRecordatorio` |
| CA-HU09-04 | Dado que uso Android 13 o superior y no concedí POST_NOTIFICATIONS, cuando se debe programar un recordatorio, entonces la app pide el permiso; si lo niego, el préstamo se registra igual sin fallar. | TC-HU09-04 | Instrumentada | Automatizada | `RecordatorioWorkerTest.TC_HU09_04_SinPermiso_NoMuestraNadaYNoFalla`<br>`PermisoNotificacionesTest.TC_HU09_04_EnAndroid13SinPermiso_SePideAlTenerUnPrestamoEntregado`<br>`PermisoNotificacionesUiTest.TC_HU09_04_ConPrestamoEntregadoSePideElPermiso_YNegarloNoBloqueaLaApp (se ejecuta aparte, tras revocar el permiso)` |

## HU-10: Iniciar sesión y control de acceso por rol

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU10-01 | Dado que tengo una cuenta válida, cuando ingreso mi correo o documento y mi contraseña correctos, entonces se inicia la sesión, se carga mi rol y veo la pantalla de inicio de ese rol. | TC-HU10-01 | Unitaria | Automatizada | `LoginViewModelTest.TC-HU10-01`<br>`LoginUiTest.TC_HU10_01_LoginValido_MuestraCatalogo` |
| CA-HU10-02 | Dado que estoy en el login, cuando ingreso credenciales incorrectas, entonces veo "Usuario o contraseña incorrectos" y no avanzo. | TC-HU10-02 | Unitaria | Automatizada | `LoginViewModelTest.TC-HU10-02`<br>`LoginUiTest.TC_HU10_02_LoginInvalido_MuestraErrorYSigueEnLogin` |
| CA-HU10-03 | Dado que estoy en el login, cuando dejo vacío el correo/documento o la contraseña, o el formato no es válido, entonces veo el error del campo sin llamar al servidor. | TC-HU10-03 | Unitaria | Automatizada | `LoginViewModelTest.TC-HU10-03`<br>`LoginUiTest.TC_HU10_03_CamposVacios_MuestranErrores` |
| CA-HU10-04 | Dado que inicié sesión, cuando cierro y vuelvo a abrir la app, entonces la sesión se mantiene (user_id, email, role y full_name guardados en DataStore). | TC-HU10-04 | Instrumentada | Automatizada | `DataStoreSessionStoreTest.TC_HU10_04_SesionPersisteAlReabrirElAlmacenamiento` |
| CA-HU10-05 | Dado que inicié sesión, cuando cierro sesión, entonces se borran el usuario y el rol, y vuelvo al login. | TC-HU10-05 | Unitaria | Automatizada | `SesionViewModelTest.TC-HU10-05`<br>`LoginUiTest.TC_HU10_05_CerrarSesion_VuelveAlLogin` |
| CA-HU10-06 | Dado que inicié sesión como ESTUDIANTE, cuando intento abrir una pantalla de gestión (inventario, actividades, revisión), entonces no la veo en el menú y el acceso directo por ruta se bloquea. | TC-HU10-06 | UI | Automatizada | `ControlAccesoTest.TC-HU10-06`<br>`LoginUiTest.TC_HU10_06_Estudiante_NoVeGestion` |
| CA-HU10-07 | Dado que inicié sesión como INSTRUCTOR, cuando entro a la app, entonces inicio en Gestión y veo las pantallas de inventario, actividades y revisión de solicitudes. | TC-HU10-07 | UI | Automatizada | `ControlAccesoTest.TC-HU10-07`<br>`LoginUiTest.TC_HU10_07_Instructor_IniciaEnGestion` |

## HU-11: Gestionar actividades formativas (instructor)

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU11-01 | Dado que soy INSTRUCTOR, cuando creo una actividad con título, descripción, ambiente y fecha, entonces la actividad queda guardada y aparece en la lista. | TC-HU11-01 | Integración | Automatizada | `RoomActividadRepositoryTest.TC_HU11_01_CrearActividad_QuedaGuardadaYApareceEnLaLista`<br>`ActividadesViewModelTest.TC-HU11-01`<br>`ActividadesUiTest.TC_HU11_01_CrearActividad_ApareceEnLaLista` |
| CA-HU11-02 | Dado que estoy creando una actividad, cuando dejo el título vacío o elijo una fecha pasada, entonces veo el error del campo y no se guarda. | TC-HU11-02 | Unitaria | Automatizada | `ReglasActividadTest.TC_HU11_02_TituloVacio_MuestraErrorDelCampo`<br>`ReglasActividadTest.TC_HU11_02_FechaPasada_MuestraErrorDelCampo`<br>`ActividadesUiTest.TC_HU11_02_TituloVacioYFechaPasada_MuestranErroresYNoSeGuarda` |
| CA-HU11-03 | Dado que existe una actividad, cuando la edito y guardo, entonces los cambios se reflejan en la lista. | TC-HU11-03 | Integración | Automatizada | `RoomActividadRepositoryTest.TC_HU11_03_EditarActividad_ReflejaLosCambios`<br>`ActividadesUiTest.TC_HU11_03_EditarActividad_SeReflejaEnLaLista` |
| CA-HU11-04 | Dado que existe una actividad, cuando la elimino y confirmo, entonces desaparece de la lista; si cancelo la confirmación, se conserva. | TC-HU11-04 | UI | Automatizada | `ActividadesUiTest.TC_HU11_04_EliminarConConfirmacion_YCancelarLaConserva`<br>`RoomActividadRepositoryTest.TC_HU11_04_EliminarActividad_DesapareceYQuedaPendienteDeEnviar` |
| CA-HU11-05 | Dado que soy ESTUDIANTE, cuando abro Actividades, entonces las veo en modo solo lectura, sin opciones de crear, editar ni eliminar. | TC-HU11-05 | UI | Automatizada | `ActividadesUiTest.TC_HU11_05_ElEstudianteVeLasActividadesEnSoloLectura`<br>`ControlAccesoTest.TC-HU11-05`<br>`ActividadesViewModelTest.TC-HU11-05` |

## HU-12: Gestionar inventario de equipos (instructor)

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU12-01 | Dado que soy INSTRUCTOR, cuando registro un equipo con nombre y categoría, entonces se guarda con estado DISPONIBLE y aparece en el catálogo. | TC-HU12-01 | Integración | Automatizada | `RoomPrestamoRepositoryTest.TC_HU12_01_RegistrarEquipo_QuedaDisponibleEnElCatalogoYPendienteDeEnviar`<br>`InventarioViewModelTest.TC-HU12-01`<br>`InventarioUiTest.TC_HU12_01_RegistrarEquipo_ApareceDisponibleEnElCatalogo` |
| CA-HU12-02 | Dado que estoy registrando un equipo, cuando dejo vacío el nombre o la categoría, entonces veo el error del campo y no se guarda. | TC-HU12-02 | Unitaria | Automatizada | `ReglasInventarioTest.TC_HU12_02_NombreVacio_MuestraErrorDelCampo`<br>`InventarioViewModelTest.TC-HU12-02`<br>`InventarioUiTest.TC_HU12_02_CamposVacios_MuestranErroresYNoSeGuarda` |
| CA-HU12-03 | Dado que existe un equipo, cuando edito sus datos, entonces los cambios se ven en el catálogo y en el detalle. | TC-HU12-03 | Integración | Automatizada | `RoomPrestamoRepositoryTest.TC_HU12_03_EditarEquipo_CambiaCatalogoYDetalle`<br>`InventarioUiTest.TC_HU12_03_EditarEquipo_SeVeEnElCatalogoYEnElDetalle` |
| CA-HU12-04 | Dado que el equipo tiene un préstamo activo (SOLICITADA o PRESTADO), cuando intento eliminarlo, entonces la eliminación se rechaza con un mensaje explicativo. | TC-HU12-04 | Unitaria | Automatizada | `ReglasInventarioTest.TC_HU12_04_ConPrestamoActivo_LaEliminacionSeRechaza`<br>`RoomPrestamoRepositoryTest.TC_HU12_04_EliminarEquipoConPrestamoActivo_SeRechaza`<br>`InventarioUiTest.TC_HU12_04_EliminarEquipoConPrestamoActivo_MuestraElMotivo` |
| CA-HU12-05 | Dado que soy ESTUDIANTE, cuando abro el catálogo, entonces no veo opciones para crear, editar ni eliminar equipos. | TC-HU12-05 | UI | Automatizada | `InventarioUiTest.TC_HU12_05_ElEstudianteNoVeOpcionesDeInventario`<br>`ControlAccesoTest.TC-HU12-05` |

## HU-13: Registrar geolocalización de las operaciones

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU13-01 | Dado que no he concedido el permiso de ubicación, cuando toco "Capturar ubicación actual" al registrar una devolución, entonces la app pide en ese momento ACCESS_FINE_LOCATION (junto con COARSE, como exige Android 12+), nunca al abrirse ni en segundo plano. | TC-HU13-01 | Instrumentada | Automatizada | `PermisoUbicacionUiTest.TC_HU13_01_ElPermisoSePideAlCapturarLaUbicacion_YNegarloNoBloqueaLaDevolucion (se ejecuta aparte, tras revocar la ubicación)` |
| CA-HU13-02 | Dado que concedí el permiso, cuando toco "Capturar ubicación actual", entonces veo "Ubicación capturada correctamente", la latitud, la longitud y la precisión. | TC-HU13-02 | UI + Unitaria | Automatizada | `DevolucionUiTest.TC_HU13_02_CapturarUbicacion_MuestraCoordenadasYMensaje`<br>`DevolucionViewModelTest.TC-HU13-02` |
| CA-HU13-03 | Dado que concedí el permiso de ubicación, cuando solicito un préstamo, entonces la solicitud guarda latitud, longitud y precisión. | TC-HU13-03 | Unitaria | Automatizada | `PrestamoViewModelTest.TC-HU13-03`<br>`RoomPrestamoRepositoryTest.TC_HU13_03_LaSolicitudGuardaLatitudLongitudYPrecision` |
| CA-HU13-04 | Dado que capturé la ubicación, cuando confirmo la devolución, entonces la devolución guarda la latitud y la longitud capturadas. | TC-HU13-04 | Unitaria + UI | Automatizada | `DevolucionViewModelTest.TC-HU13-04`<br>`DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo` |
| CA-HU13-05 | Dado que negué el permiso o el GPS está desactivado, cuando confirmo la devolución, entonces la devolución se registra sin coordenadas y se muestra un aviso. | TC-HU13-05 | Unitaria | Automatizada | `DevolucionViewModelTest.TC-HU13-05` |
| CA-HU13-06 | Dado que hay registros con coordenadas, cuando se sincroniza, entonces latitud y longitud llegan a Supabase (`loans` y `returns`). | TC-HU13-06 | Integración | Automatizada | `SincronizadorPrestamosTest.TC_HU13_06_LasCoordenadasDePrestamosYDevolucionesLleganASupabase`<br>`SupabasePrestamosDataSourceTest.TC-HU13-06` |

## HU-14: Revisar solicitudes de préstamo (instructor)

| Criterio | Dado / cuando / entonces | Caso | Tipo | Estado | Prueba |
|---|---|---|---|---|---|
| CA-HU14-01 | Dado que hay solicitudes SOLICITADAS, cuando abro "Revisar solicitudes", entonces veo la lista con estudiante, equipo, ambiente, propósito y duración. | TC-HU14-01 | UI | Automatizada | `RevisarSolicitudesUiTest.TC_HU14_01_ListaMuestraEstudianteEquipoAmbientePropositoYDuracion`<br>`ControlAccesoTest.TC-HU14-01` |
| CA-HU14-02 | Dado que reviso una solicitud SOLICITADA, cuando la apruebo, entonces pasa a PRESTADO y el equipo queda PRESTADO. | TC-HU14-02 | Unitaria (TDD) | Automatizada | `RevisionSolicitudTest.TC_HU14_02_AprobarSolicitada_PasaAPrestadoYEntregaElEquipo`<br>`RoomPrestamoRepositoryTest.TC_HU14_02_AprobarSolicitud_EntregaElEquipoYQuedaPendienteDeEnviar`<br>`RevisarSolicitudesUiTest.TC_HU14_02_AprobarQuitaLaSolicitudYElEquipoQuedaPrestado` |
| CA-HU14-03 | Dado que reviso una solicitud SOLICITADA, cuando la rechazo indicando el motivo, entonces pasa a RECHAZADA, guarda el motivo y el equipo vuelve a DISPONIBLE. | TC-HU14-03 | Unitaria (TDD) | Automatizada | `RevisionSolicitudTest.TC_HU14_03_RechazarSolicitada_GuardaElMotivoYLiberaElEquipo`<br>`RoomPrestamoRepositoryTest.TC_HU14_03_RechazarSolicitud_GuardaElMotivoYLiberaElEquipo`<br>`RevisarSolicitudesUiTest.TC_HU14_03_RechazarExigeMotivoYLiberaElEquipo` |
| CA-HU14-04 | Dado que la solicitud ya fue CANCELADA o RECHAZADA, cuando intento aprobarla, entonces la operación se rechaza sin cambios. | TC-HU14-04 | Unitaria (TDD) | Automatizada | `RevisionSolicitudTest.TC_HU14_04_AprobarCanceladaORechazada_SeRechazaSinCambios`<br>`RoomPrestamoRepositoryTest.TC_HU14_04_AprobarUnaSolicitudCancelada_SeRechazaSinCambios` |
