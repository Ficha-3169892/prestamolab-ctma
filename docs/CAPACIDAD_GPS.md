# Capacidad física adicional: geolocalización (GPS)

Guía, semana 9, actividad 31: *documentar propósito, API utilizada, permisos requeridos, manejo de errores, privacidad y pruebas realizadas*. Historia: **HU-13 · Registrar geolocalización de las operaciones** (`docs/backlog/issues/HU13.md`). Riesgo asociado: R-22.

## Propósito y justificación

Los equipos del CTMA se prestan y se devuelven dentro de la sede. Registrar dónde se hizo la solicitud, la devolución y cada foto de evidencia le da al instructor trazabilidad cuando hay un reclamo: por ejemplo, si un equipo se devolvió dañado o si se entregó fuera de la sede. Se eligió el GPS y no otra capacidad (Bluetooth, sensores, biometría) porque es la que aporta ese dato al flujo del préstamo sin hardware adicional.

## Dónde se usa

| Operación | Cuándo se lee la ubicación | Si no hay permiso |
|---|---|---|
| Registrar devolución (HU-05) | Cuando el estudiante toca "Capturar ubicación actual" | Se pide el permiso en ese momento. Si lo niega, la devolución se registra sin coordenadas y con un aviso (CA-HU13-05) |
| Solicitar préstamo (HU-03) | Después de guardar la solicitud | No se pide: la ubicación solo se agrega si el permiso ya estaba concedido (CA-HU13-03) |
| Foto de evidencia (HU-08) | Después de tomar la foto | No se pide: igual que en la solicitud |

## API utilizada

- **Fused Location Provider** de Google Play Services (`com.google.android.gms:play-services-location`), método `getCurrentLocation` (`data/location/LocationProvider.kt`).
- Lectura **única** con `PRIORITY_HIGH_ACCURACY`: espera hasta 15 s y acepta una posición de hasta 10 s de antigüedad. No se registran actualizaciones continuas.
- La lectura es una función `suspend` que se cancela si el usuario sale de la pantalla (`suspendCancellableCoroutine` + `CancellationTokenSource`).
- Detrás de la interfaz `LocationProvider`, lo que permite reemplazarla por una ubicación fija en las pruebas.

## Permisos requeridos

| Permiso | Por qué |
|---|---|
| `ACCESS_FINE_LOCATION` | Precisión suficiente para distinguir la sede |
| `ACCESS_COARSE_LOCATION` | Android 12 o superior exige pedirlo junto con el fino. El usuario puede conceder solo la aproximada, y la app la acepta |

**Mínimo privilegio:** no se declara `ACCESS_BACKGROUND_LOCATION`. El permiso se pide solo al tocar el botón de la devolución, nunca al abrir la app (CA-HU13-01), y negarlo no bloquea ninguna operación.

## Manejo de errores

| Situación | Qué ve el usuario | Qué se guarda |
|---|---|---|
| Permiso negado | "Permiso de ubicación denegado. La devolución se registrará sin coordenadas." | La devolución, sin latitud ni longitud |
| GPS apagado o sin señal (la API devuelve `null`) | "No se pudo obtener la ubicación. Verifica que la ubicación del dispositivo esté activada." | La operación, sin coordenadas |
| Error de Play Services | El mensaje del error | La operación, sin coordenadas |
| El usuario sale de la pantalla mientras se lee | Nada | La lectura se cancela; no queda trabajo pendiente |

En la solicitud y en la evidencia, un fallo del GPS no se muestra: el registro ya quedó guardado y simplemente no lleva coordenadas.

## Privacidad

- Una lectura por operación y solo cuando el usuario actúa. No hay seguimiento ni ubicación en segundo plano.
- Se guardan latitud y longitud en `loans`, `returns` y `evidences`. La precisión en metros solo se guarda en el teléfono y no se envía a Supabase.
- Con la seguridad por rol (`009_seguridad.sql`), un estudiante solo puede leer las coordenadas de sus propios préstamos; el instructor ve todas.
- La app no escribe coordenadas en el log (no usa `android.util.Log`).

## Pruebas realizadas

| Caso | Tipo | Prueba |
|---|---|---|
| TC-HU13-01: el permiso se pide al tocar y negarlo no bloquea | Instrumentada, con el diálogo real del sistema | `PermisoUbicacionUiTest` (se ejecuta aparte, tras revocar el permiso; `docs/PLAN_PRUEBAS.md`, sección 7) |
| TC-HU13-02: se muestran las coordenadas y la precisión | UI + unitaria | `DevolucionUiTest`, `DevolucionViewModelTest` |
| TC-HU13-03: la solicitud guarda la ubicación | Unitaria + Room | `PrestamoViewModelTest`, `RoomPrestamoRepositoryTest` |
| TC-HU13-04: la devolución guarda la ubicación | Unitaria + UI | `DevolucionViewModelTest`, `DevolucionUiTest`, `RegresionFlujoCriticoUiTest` |
| TC-HU13-05: sin permiso o sin GPS se registra sin coordenadas | Unitaria | `DevolucionViewModelTest` |
| TC-HU13-06: las coordenadas llegan a Supabase | Integración | `SincronizadorPrestamosTest`, `SupabasePrestamosDataSourceTest` |
| GPS real en el dispositivo | Manual en el Xiaomi (Android 15) contra Supabase | 2026-09-24: la devolución sincronizada llegó con coordenadas reales. En la prueba de punta a punta con seguridad, la solicitud del Multímetro quedó en `loans` con coordenadas reales (6.2744, -75.5534) (`docs/PLAN_PRUEBAS.md`, sección 8) |

En las pruebas automatizadas se usa una ubicación fija (`FakeLocationProvider` y un proveedor falso en las de UI), para que el resultado no dependa del GPS ni del lugar donde se ejecuta la suite.
