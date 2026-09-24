"""
Genera los Issues de GitHub (Markdown) del backlog de PréstamoLab CTMA.

Cada criterio de aceptación CA-HUxx-nn tiene exactamente un caso de prueba
TC-HUxx-nn (trazabilidad 1:1). Editar los datos aquí y volver a ejecutar:

    python docs/backlog/generar_issues.py
"""
from pathlib import Path

SALIDA = Path(__file__).parent / "issues"

# Cada criterio: (dado, cuando, entonces, tipo_prueba, pasos_y_datos, prueba_automatizada)
# prueba_automatizada: referencia a la prueba existente o "" si está pendiente.
HISTORIAS = [
    {
        "id": "HU01", "titulo": "Consultar equipos disponibles", "prioridad": "Alta", "sprint": 6,
        "rol": "estudiante o instructor", "quiero": "ver el catálogo de equipos con su disponibilidad",
        "para": "saber qué puedo solicitar sin ir al almacén",
        "criterios": [
            ("existen equipos registrados", "abro la pantalla Catálogo",
             "veo cada equipo con nombre, categoría y estado (DISPONIBLE, RESERVADO o PRESTADO)",
             "UI", "Abrir la app con los datos semilla (4 equipos).",
             "PrestamoUiTest.TC01_CargarCatalogoInicial_MuestraEquipos"),
            ("un equipo no está DISPONIBLE", "veo el catálogo",
             "su estado se muestra con un color distinto al de los disponibles",
             "UI", "Semilla: Osciloscopio en RESERVADO. Verificar estilo del estado.", ""),
            ("hay equipos de varias categorías", "activo el filtro \"Solo disponibles\" o elijo una categoría",
             "la lista muestra solo los equipos que cumplen el filtro",
             "Unitaria", "ViewModel con repositorio falso; aplicar filtro categoría = Herramienta.", ""),
            ("apliqué un filtro", "cierro y vuelvo a abrir la app",
             "el filtro se conserva (DataStore)",
             "Instrumentada", "Aplicar filtro, recrear la actividad, verificar filtro activo.", ""),
            ("no hay equipos que cumplan el filtro", "veo el catálogo",
             "aparece el mensaje \"No hay equipos para mostrar\" en lugar de una lista vacía",
             "UI", "Filtro por categoría inexistente.", ""),
        ],
    },
    {
        "id": "HU02", "titulo": "Consultar detalle de un equipo", "prioridad": "Alta", "sprint": 6,
        "rol": "estudiante o instructor", "quiero": "ver el detalle de un equipo",
        "para": "confirmar sus características y estado antes de solicitarlo",
        "criterios": [
            ("estoy en el catálogo", "toco un equipo",
             "se abre el detalle con id, nombre, categoría y estado de ese equipo",
             "UI", "Tocar \"Multímetro Digital\"; esperar \"ID: 1\".",
             "PrestamoUiTest.TC02_VerDetalleEquipoValido, DetalleEquipo_VerificaDisponibilidadVisible; PrestamoViewModelTest.TC-02"),
            ("navego con un id que no existe", "se carga el detalle",
             "veo \"Equipo no encontrado\" y un botón para volver, sin cierre inesperado",
             "Unitaria", "seleccionarEquipoPorId(999).",
             "PrestamoViewModelTest.TC-03"),
            ("el equipo no está DISPONIBLE", "veo su detalle",
             "el botón \"Solicitar Préstamo\" está deshabilitado",
             "UI", "Crear una solicitud sobre el Multímetro y volver a su detalle.",
             "PrestamoUiTest.TC12_BotonSolicitarDeshabilitado_SiEquipoEstaReservado"),
            ("estoy viendo el detalle", "el estado del equipo cambia en la fuente de datos",
             "el detalle muestra el estado nuevo sin volver a abrirlo",
             "Unitaria", "Detalle del equipo 2 (RESERVADO); cancelar la solicitud #1.",
             "PrestamoViewModelTest.Detalle seleccionado se actualiza cuando cambia el estado del equipo"),
        ],
    },
    {
        "id": "HU03", "titulo": "Solicitar préstamo", "prioridad": "Alta", "sprint": 7,
        "rol": "estudiante", "quiero": "registrar una solicitud de préstamo de un equipo disponible",
        "para": "reservarlo para mi práctica",
        "criterios": [
            ("estoy en el formulario", "guardo con el ambiente vacío",
             "veo \"El ambiente o destino es obligatorio.\" y no se crea la solicitud",
             "Unitaria", "ambiente = \"\"; propósito válido; duración 2.",
             "PrestamoViewModelTest.Validacion Ambiente"),
            ("estoy en el formulario", "escribo un propósito de 9 o de 181 caracteres",
             "veo el error de longitud; con 10 y con 180 caracteres se acepta",
             "Unitaria", "Valores límite: 9, 10, 180 y 181 caracteres.",
             "PrestamoViewModelTest.TC-04..TC-07"),
            ("estoy en el formulario", "indico una duración de 0 o de 9 horas",
             "veo \"Duración entre 1 y 8 horas\"; con 1 y con 8 horas se acepta",
             "Unitaria", "Valores límite: 0, 1, 8 y 9.",
             "PrestamoViewModelTest.TC-08..TC-11"),
            ("el equipo está RESERVADO o PRESTADO", "intento solicitarlo",
             "la solicitud se rechaza y no se crea ningún registro",
             "Unitaria", "Equipo 2 (RESERVADO) contra el repositorio.",
             "PrestamoViewModelTest.TC-12; InMemoryPrestamoRepositoryTest.crear solicitud sobre equipo RESERVADO"),
            ("el formulario es válido", "pulso Guardar varias veces seguidas",
             "se crea una sola solicitud y el botón muestra \"Procesando...\"",
             "Unitaria", "Repositorio lento simulado con MockK; 3 pulsaciones.",
             "PrestamoViewModelTest.TC-13"),
            ("el formulario es válido", "guardo",
             "la solicitud queda SOLICITADA con ambiente, propósito y duración, y el equipo pasa a RESERVADO",
             "Unitaria + UI", "Lab 305, \"Practica de Redes\", 4 horas, equipo 3.",
             "PrestamoViewModelTest.TC-14; PrestamoUiTest.TC14_FlujoCompleto_CrearSolicitud"),
            ("el repositorio falla al guardar", "guardo",
             "veo el mensaje de error, el botón se habilita de nuevo y sigo en el formulario",
             "Unitaria", "MockK: crearSolicitud devuelve Result.failure.",
             "PrestamoViewModelTest.Fallo del repositorio muestra mensaje de error"),
            ("inicié sesión como INSTRUCTOR", "abro el detalle de un equipo",
             "no veo la opción de solicitar préstamo (acción exclusiva del estudiante)",
             "UI + Unitaria", "Sesión con rol INSTRUCTOR.",
             "LoginUiTest.TC_HU03_08_Instructor_NoVeSolicitarPrestamo; PrestamoViewModelTest.TC-HU03-08"),
        ],
    },
    {
        "id": "HU04", "titulo": "Consultar mis préstamos activos", "prioridad": "Alta", "sprint": 7,
        "rol": "estudiante", "quiero": "ver mis solicitudes y préstamos en curso",
        "para": "saber qué tengo pendiente de recoger o devolver",
        "criterios": [
            ("hay solicitudes de varios usuarios", "abro \"Mis Solicitudes\"",
             "veo solo las del usuario con sesión iniciada",
             "Unitaria", "Repositorio con solicitudes de 2 usuarios; sesión del usuario A.", ""),
            ("tengo solicitudes", "abro \"Mis Solicitudes\"",
             "cada una muestra equipo, fechas, ambiente y estado",
             "UI", "Solicitud semilla #1.", ""),
            ("tengo solicitudes CANCELADAS o DEVUELTAS", "abro \"Mis Solicitudes\"",
             "no aparecen en la lista de activas",
             "UI", "Cancelar la solicitud #1 y verificar la lista.",
             "PrestamoUiTest.TC15_CancelarSolicitud_ActualizaLista"),
            ("no tengo solicitudes activas", "abro \"Mis Solicitudes\"",
             "veo \"No tienes solicitudes activas.\"",
             "UI", "Usuario sin solicitudes.", ""),
            ("tengo una solicitud SOLICITADA", "la cancelo",
             "pasa a CANCELADA y el equipo vuelve a DISPONIBLE",
             "Unitaria", "Cancelar la solicitud #1 (equipo 2).",
             "PrestamoViewModelTest.TC-15"),
            ("una solicitud ya está CANCELADA", "intento cancelarla otra vez",
             "no se produce ningún cambio ni error",
             "Unitaria", "Cancelar dos veces la solicitud #1.",
             "PrestamoViewModelTest.TC-16; InMemoryPrestamoRepositoryTest.cancelar es idempotente"),
        ],
    },
    {
        "id": "HU05", "titulo": "Registrar devolución", "prioridad": "Alta", "sprint": 5,
        "rol": "estudiante", "quiero": "registrar la devolución de un equipo prestado",
        "para": "cerrar mi préstamo y liberar el equipo",
        "criterios": [
            ("tengo préstamos en distintos estados", "abro \"Mis Solicitudes\"",
             "veo \"Registrar devolución\" solo en los préstamos PRESTADO; las solicitudes SOLICITADAS muestran \"Cancelar Solicitud\"",
             "UI + Unitaria", "Semilla: #1 SOLICITADA y #2 PRESTADO.",
             "DevolucionUiTest.TC_HU05_01_SoloElPrestamoPrestadoOfreceRegistrarDevolucion; DevolucionViewModelTest.TC-HU05-01"),
            ("tengo un préstamo PRESTADO", "registro la devolución",
             "el préstamo pasa a DEVUELTO, el equipo a DISPONIBLE y el préstamo sale de mis activos",
             "Unitaria (TDD) + UI", "Transición PRESTADO → DEVUELTO del préstamo #2.",
             "DevolucionViewModelTest.TC-HU05-02; DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo"),
            ("estoy registrando la devolución", "elijo el estado del equipo (Bueno, Con novedad, Dañado) y escribo una observación",
             "se guarda un registro de devolución (`returns`) con esos datos y la fecha/hora",
             "Unitaria", "Repositorio en memoria; con Room se verifica ReturnEntity (Sprint 6).",
             "DevolucionViewModelTest.TC-HU05-03"),
            ("marco el equipo como Dañado", "guardo la devolución",
             "la observación es obligatoria (mínimo 10 caracteres)",
             "Unitaria + UI", "Estado Dañado con observación \"roto\" y luego \"Pantalla rota\".",
             "DevolucionViewModelTest.TC-HU05-04; DevolucionUiTest.TC_HU05_04_EquipoDanadoSinObservacion_MuestraError"),
            ("el préstamo ya está DEVUELTO", "intento devolverlo otra vez",
             "la operación se rechaza sin cambios",
             "Unitaria (TDD)", "Transición DEVUELTO → DEVUELTO.",
             "InMemoryPrestamoRepositoryTest.TC-HU05-05"),
        ],
    },
    {
        "id": "HU06", "titulo": "Conservar datos localmente sin conexión", "prioridad": "Alta", "sprint": 6,
        "rol": "estudiante o instructor", "quiero": "usar la app sin conexión a internet",
        "para": "consultar y registrar información aunque la red del CTMA falle",
        "criterios": [
            ("hay datos guardados", "reinicio la app en modo avión",
             "el catálogo y mis préstamos siguen visibles (Room)",
             "Instrumentada", "Cargar datos, activar modo avión, recrear el proceso.", ""),
            ("no hay conexión", "creo una solicitud",
             "se guarda localmente con estado de sincronización PENDIENTE",
             "Integración", "Room en memoria; remoto simulado sin red.", ""),
            ("la app está en uso", "cambian los datos en Room",
             "la UI se actualiza sola, porque solo lee de Room mediante Flow",
             "Integración", "Insertar en el DAO y observar el Flow con Turbine.", ""),
            ("existe una versión anterior de la base de datos", "actualizo la app",
             "la migración conserva los datos existentes",
             "Instrumentada", "MigrationTestHelper de v1 a v2.", ""),
            ("existen entidades relacionadas", "consulto un préstamo",
             "obtengo el préstamo con su equipo y sus evidencias en una sola consulta (@Relation)",
             "Integración", "DAO: LoanWithEquipment / LoanWithEvidences.", ""),
        ],
    },
    {
        "id": "HU07", "titulo": "Sincronizar datos con servicio remoto", "prioridad": "Media/Alta", "sprint": 8,
        "rol": "estudiante o instructor", "quiero": "que mis datos se sincronicen con Supabase",
        "para": "que el instructor y yo veamos la misma información desde cualquier dispositivo",
        "criterios": [
            ("hay registros PENDIENTES", "se recupera la conexión",
             "se envían a Supabase y quedan SINCRONIZADOS",
             "Integración", "MockWebServer responde 201; WorkManager de prueba.", ""),
            ("el servidor responde 200 con datos nuevos", "se sincroniza",
             "Room se actualiza y la UI muestra los datos remotos",
             "Integración", "MockWebServer 200 con JSON de equipos.", ""),
            ("el servidor responde 401", "se sincroniza",
             "se cierra la sesión y se lleva al usuario al login",
             "Integración", "MockWebServer 401.", ""),
            ("el servidor responde 404", "se sincroniza un recurso",
             "se muestra un mensaje y los datos locales se conservan",
             "Integración", "MockWebServer 404.", ""),
            ("el servidor responde 5xx o se agota el tiempo de espera", "se sincroniza",
             "se reintenta con espera exponencial y el registro sigue PENDIENTE, sin cierre inesperado",
             "Integración", "MockWebServer 500/503 y respuesta con retraso mayor al timeout.", ""),
        ],
    },
    {
        "id": "HU08", "titulo": "Adjuntar evidencia fotográfica", "prioridad": "Media", "sprint": 8,
        "rol": "estudiante", "quiero": "adjuntar fotos del equipo al recibirlo o devolverlo",
        "para": "dejar constancia de su estado",
        "criterios": [
            ("no he concedido el permiso de cámara", "toco \"Adjuntar evidencia\"",
             "la app pide el permiso en ese momento (no al abrirla)",
             "Instrumentada", "Permiso revocado con `adb shell pm revoke`.", ""),
            ("tomo una foto", "confirmo la captura",
             "la imagen se guarda mediante FileProvider y su URI queda asociada al préstamo en Room",
             "Integración", "TakePicture simulado; verificar EvidenceEntity.", ""),
            ("niego el permiso de cámara", "intento adjuntar",
             "veo un mensaje explicativo y la app sigue funcionando",
             "Instrumentada", "Negar el permiso.", ""),
            ("abro la cámara", "cancelo sin tomar foto",
             "no se crea ninguna evidencia",
             "Unitaria", "Resultado de TakePicture = false.", ""),
            ("hay evidencias sin subir", "se sincroniza",
             "las fotos se suben a Supabase Storage y se guarda su URL remota",
             "Integración", "MockWebServer para Storage.", ""),
        ],
    },
    {
        "id": "HU09", "titulo": "Recibir recordatorio de devolución", "prioridad": "Media", "sprint": 9,
        "rol": "estudiante", "quiero": "recibir un recordatorio antes de la hora de devolución",
        "para": "no entregar tarde el equipo",
        "criterios": [
            ("un préstamo pasa a PRESTADO", "se registra el cambio",
             "se programa un recordatorio 30 minutos antes de la fecha de fin",
             "Integración", "WorkManagerTestInitHelper; verificar el trabajo programado.", ""),
            ("llega la hora del recordatorio", "se muestra la notificación",
             "indica el equipo y la hora límite, y al tocarla abre el préstamo",
             "Instrumentada", "Adelantar el reloj de prueba de WorkManager.", ""),
            ("registré la devolución", "se guarda",
             "el recordatorio pendiente se cancela",
             "Integración", "Verificar que el trabajo quede CANCELLED.", ""),
            ("uso Android 13 o superior y no concedí POST_NOTIFICATIONS", "se debe programar un recordatorio",
             "la app pide el permiso; si lo niego, el préstamo se registra igual sin fallar",
             "Instrumentada", "API 33+, permiso negado.", ""),
        ],
    },
    {
        "id": "HU10", "titulo": "Iniciar sesión y control de acceso por rol", "prioridad": "Alta", "sprint": 5,
        "rol": "usuario del CTMA", "quiero": "iniciar sesión con mis credenciales",
        "para": "acceder solo a las funciones de mi rol (INSTRUCTOR o ESTUDIANTE)",
        "criterios": [
            ("tengo una cuenta válida", "ingreso correo y contraseña correctos",
             "se inicia la sesión, se carga mi rol y veo la pantalla de inicio de ese rol",
             "Unitaria", "AuthRepository falso que devuelve un usuario ESTUDIANTE.",
             "LoginViewModelTest.TC-HU10-01; LoginUiTest.TC_HU10_01_LoginValido_MuestraCatalogo"),
            ("estoy en el login", "ingreso credenciales incorrectas",
             "veo \"Correo o contraseña incorrectos\" y no avanzo",
             "Unitaria", "AuthRepository falso que devuelve error 400/401.",
             "LoginViewModelTest.TC-HU10-02; LoginUiTest.TC_HU10_02_LoginInvalido_MuestraErrorYSigueEnLogin"),
            ("estoy en el login", "dejo vacío el correo o la contraseña, o el correo no es válido",
             "veo el error del campo sin llamar al servidor",
             "Unitaria", "\"\", \"correo-sin-arroba\", contraseña vacía.",
             "LoginViewModelTest.TC-HU10-03; LoginUiTest.TC_HU10_03_CamposVacios_MuestranErrores"),
            ("inicié sesión", "cierro y vuelvo a abrir la app",
             "la sesión se mantiene (token y rol guardados en DataStore)",
             "Instrumentada", "Recrear el proceso tras el login.",
             "DataStoreSessionStoreTest.TC_HU10_04_SesionPersisteAlReabrirElAlmacenamiento"),
            ("inicié sesión", "cierro sesión",
             "se borran el token y el rol, y vuelvo al login",
             "Unitaria", "Verificar que DataStore quede vacío.",
             "SesionViewModelTest.TC-HU10-05; LoginUiTest.TC_HU10_05_CerrarSesion_VuelveAlLogin"),
            ("inicié sesión como ESTUDIANTE", "intento abrir una pantalla de gestión (inventario, actividades, revisión)",
             "no la veo en el menú y el acceso directo por ruta se bloquea",
             "UI", "Sesión ESTUDIANTE; navegar a la ruta de gestión.",
             "ControlAccesoTest.TC-HU10-06; LoginUiTest.TC_HU10_06_Estudiante_NoVeGestion"),
            ("inicié sesión como INSTRUCTOR", "abro el menú",
             "veo las pantallas de gestión de inventario, actividades y revisión de solicitudes",
             "UI", "Sesión INSTRUCTOR.",
             "ControlAccesoTest.TC-HU10-07; LoginUiTest.TC_HU10_07_Instructor_VeYAbreGestion"),
        ],
    },
    {
        "id": "HU11", "titulo": "Gestionar actividades formativas (instructor)", "prioridad": "Media", "sprint": 6,
        "rol": "instructor", "quiero": "crear, editar, listar y eliminar actividades formativas",
        "para": "organizar las prácticas que requieren equipos",
        "criterios": [
            ("soy INSTRUCTOR", "creo una actividad con título, descripción, ambiente y fecha",
             "la actividad queda guardada y aparece en la lista",
             "Integración", "DAO de actividades en memoria.", ""),
            ("estoy creando una actividad", "dejo el título vacío o elijo una fecha pasada",
             "veo el error del campo y no se guarda",
             "Unitaria", "Título \"\"; fecha de ayer.", ""),
            ("existe una actividad", "la edito y guardo",
             "los cambios se reflejan en la lista",
             "Integración", "Actualizar el título.", ""),
            ("existe una actividad", "la elimino y confirmo",
             "desaparece de la lista; si cancelo la confirmación, se conserva",
             "UI", "Diálogo de confirmación.", ""),
            ("soy ESTUDIANTE", "abro Actividades",
             "las veo en modo solo lectura, sin opciones de crear, editar ni eliminar",
             "UI", "Sesión ESTUDIANTE.", ""),
        ],
    },
    {
        "id": "HU12", "titulo": "Gestionar inventario de equipos (instructor)", "prioridad": "Media", "sprint": 6,
        "rol": "instructor", "quiero": "registrar y mantener el inventario de equipos",
        "para": "que el catálogo refleje los recursos reales del CTMA",
        "criterios": [
            ("soy INSTRUCTOR", "registro un equipo con nombre y categoría",
             "se guarda con estado DISPONIBLE y aparece en el catálogo",
             "Integración", "DAO de equipos en memoria.", ""),
            ("estoy registrando un equipo", "dejo vacío el nombre o la categoría",
             "veo el error del campo y no se guarda",
             "Unitaria", "Nombre \"\".", ""),
            ("existe un equipo", "edito sus datos",
             "los cambios se ven en el catálogo y en el detalle",
             "Integración", "Actualizar la categoría.", ""),
            ("el equipo tiene un préstamo activo (SOLICITADA o PRESTADO)", "intento eliminarlo",
             "la eliminación se rechaza con un mensaje explicativo",
             "Unitaria", "Equipo 2 con la solicitud #1.", ""),
            ("soy ESTUDIANTE", "abro el catálogo",
             "no veo opciones para crear, editar ni eliminar equipos",
             "UI", "Sesión ESTUDIANTE.", ""),
        ],
    },
    {
        "id": "HU13", "titulo": "Registrar geolocalización de las operaciones", "prioridad": "Media", "sprint": 5,
        "rol": "instructor", "quiero": "saber dónde se solicitó, devolvió o fotografió cada equipo",
        "para": "verificar que las operaciones ocurren dentro del CTMA",
        "criterios": [
            ("no he concedido el permiso de ubicación", "toco \"Capturar ubicación actual\" al registrar una devolución",
             "la app pide en ese momento ACCESS_FINE_LOCATION (junto con COARSE, como exige Android 12+), nunca al abrirse ni en segundo plano",
             "Manual", "Permisos revocados con `adb shell pm revoke`; verificar el diálogo del sistema (Compose Test no puede operarlo).", ""),
            ("concedí el permiso", "toco \"Capturar ubicación actual\"",
             "veo \"Ubicación capturada correctamente\", la latitud, la longitud y la precisión",
             "UI + Unitaria", "LocationProvider falso con (6.2518, -75.5636, ±12 m).",
             "DevolucionUiTest.TC_HU13_02_CapturarUbicacion_MuestraCoordenadasYMensaje; DevolucionViewModelTest.TC-HU13-02"),
            ("concedí el permiso de ubicación", "solicito un préstamo",
             "la solicitud guarda latitud, longitud y precisión",
             "Unitaria", "LocationProvider falso con (6.2518, -75.5636).", ""),
            ("capturé la ubicación", "confirmo la devolución",
             "la devolución guarda la latitud y la longitud capturadas",
             "Unitaria + UI", "LocationProvider falso.",
             "DevolucionViewModelTest.TC-HU13-04; DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo"),
            ("negué el permiso o el GPS está desactivado", "confirmo la devolución",
             "la devolución se registra sin coordenadas y se muestra un aviso",
             "Unitaria", "Permiso denegado; LocationProvider que devuelve UbicacionNoDisponibleException.",
             "DevolucionViewModelTest.TC-HU13-05"),
            ("hay registros con coordenadas", "se sincroniza",
             "latitud y longitud llegan a Supabase (`loans` y `returns`)",
             "Integración", "MockWebServer: verificar el cuerpo JSON enviado.", ""),
        ],
    },
    {
        "id": "HU14", "titulo": "Revisar solicitudes de préstamo (instructor)", "prioridad": "Alta", "sprint": 7,
        "rol": "instructor", "quiero": "aprobar o rechazar las solicitudes de los estudiantes",
        "para": "controlar la entrega de equipos",
        "criterios": [
            ("hay solicitudes SOLICITADAS", "abro \"Revisar solicitudes\"",
             "veo la lista con estudiante, equipo, ambiente, propósito y duración",
             "UI", "Sesión INSTRUCTOR con la solicitud #1.", ""),
            ("reviso una solicitud SOLICITADA", "la apruebo",
             "pasa a PRESTADO y el equipo queda PRESTADO",
             "Unitaria (TDD)", "Transición SOLICITADA → PRESTADO.", ""),
            ("reviso una solicitud SOLICITADA", "la rechazo indicando el motivo",
             "pasa a RECHAZADA, guarda el motivo y el equipo vuelve a DISPONIBLE",
             "Unitaria (TDD)", "Transición SOLICITADA → RECHAZADA.", ""),
            ("la solicitud ya fue CANCELADA o RECHAZADA", "intento aprobarla",
             "la operación se rechaza sin cambios",
             "Unitaria (TDD)", "Transición inválida.", ""),
        ],
    },
]


def etiquetas(h):
    prioridad = h["prioridad"].split("/")[0].lower()
    return f"`historia-usuario`, `prioridad:{prioridad}`, `sprint-{h['sprint']}`"


def render(h):
    hu = h["id"]
    lineas = [
        f"# [{hu[:2]}-{hu[2:]}] {h['titulo']}",
        "",
        f"**Etiquetas:** {etiquetas(h)}  ",
        f"**Prioridad:** {h['prioridad']} · **Sprint:** {h['sprint']}",
        "",
        "## Historia de usuario",
        "",
        f"**Como** {h['rol']}, **quiero** {h['quiero']}, **para** {h['para']}.",
        "",
        "## Criterios de aceptación",
        "",
    ]
    for n, (dado, cuando, entonces, *_ ) in enumerate(h["criterios"], 1):
        lineas.append(f"- [ ] **CA-{hu}-{n:02d}:** **Dado** que {dado}, **cuando** {cuando}, **entonces** {entonces}.")
    lineas += [
        "",
        "## Casos de prueba",
        "",
        "Cada caso verifica el criterio con el mismo número (trazabilidad 1:1).",
        "",
        "| Caso | Criterio | Tipo | Pasos / datos | Resultado esperado | Prueba automatizada |",
        "|---|---|---|---|---|---|",
    ]
    for n, (_, cuando, entonces, tipo, pasos, auto) in enumerate(h["criterios"], 1):
        auto_txt = f"`{auto}`" if auto else "Pendiente"
        lineas.append(
            f"| TC-{hu}-{n:02d} | CA-{hu}-{n:02d} | {tipo} | {pasos} Acción: {cuando}. | {entonces[0].upper() + entonces[1:]}. | {auto_txt} |"
        )
    lineas += [
        "",
        "## Definición de terminado",
        "",
        "- [ ] Todos los criterios implementados y sus casos TC en verde.",
        "- [ ] La UI consume solo `StateFlow` del ViewModel; el ViewModel usa solo el Repository.",
        "- [ ] Defectos encontrados registrados como Issues `BUG-XX` con prueba de confirmación y regresión.",
        "",
    ]
    return "\n".join(lineas)


def matriz():
    lineas = [
        "# Backlog y matriz de trazabilidad: PréstamoLab CTMA (Parte 2)",
        "",
        "Archivos generados por `generar_issues.py`; no editar a mano.",
        "Para crear cada Issue en GitHub: **New issue**, pegar el título (primera línea sin `# `) y el resto como descripción.",
        "",
        "## Historias",
        "",
        "| Issue | Historia | Prioridad | Sprint | Criterios | Automatizados |",
        "|---|---|---|---|---|---|",
    ]
    total = automat = 0
    for h in sorted(HISTORIAS, key=lambda h: (h["sprint"], h["id"])):
        c = len(h["criterios"])
        a = sum(1 for cr in h["criterios"] if cr[5])
        total += c
        automat += a
        lineas.append(f"| [{h['id'][:2]}-{h['id'][2:]}]({h['id']}.md) | {h['titulo']} | {h['prioridad']} | {h['sprint']} | {c} | {a} |")
    lineas += ["", f"**Total:** {total} criterios, {total} casos de prueba, {automat} ya automatizados.", "",
               "## Matriz HU → CA → TC → prueba", "",
               "| Historia | Criterio | Caso | Tipo | Prueba automatizada |", "|---|---|---|---|---|"]
    for h in HISTORIAS:
        for n, cr in enumerate(h["criterios"], 1):
            lineas.append(f"| {h['id']} | CA-{h['id']}-{n:02d} | TC-{h['id']}-{n:02d} | {cr[3]} | {('`' + cr[5] + '`') if cr[5] else 'Pendiente'} |")
    lineas += ["", "## Equivalencia con la numeración anterior (Parte 1)", "",
               "| Anterior | Nuevo |", "|---|---|",
               "| TC-01 | TC-HU01-01 |", "| TC-02 | TC-HU02-01 |", "| TC-03 | TC-HU02-02 |",
               "| TC-04 a TC-07 | TC-HU03-02 |", "| TC-08 a TC-11 | TC-HU03-03 |", "| TC-12 | TC-HU03-04 |",
               "| TC-13 (BUG-03) | TC-HU03-05 |", "| TC-14 | TC-HU03-06 |", "| TC-15 | TC-HU04-05 |",
               "| TC-16 | TC-HU04-06 |", ""]
    return "\n".join(lineas)


if __name__ == "__main__":
    SALIDA.mkdir(parents=True, exist_ok=True)
    for h in HISTORIAS:
        (SALIDA / f"{h['id']}.md").write_text(render(h), encoding="utf-8", newline="\n")
    (SALIDA / "README.md").write_text(matriz(), encoding="utf-8", newline="\n")
    print(f"{len(HISTORIAS)} historias generadas en {SALIDA}")
