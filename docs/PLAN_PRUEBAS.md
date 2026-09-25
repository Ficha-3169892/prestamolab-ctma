# Plan de pruebas: PréstamoLab CTMA (Parte 2, semanas 5 a 9)

## 1. Objetivo y alcance

Verificar que cada criterio de aceptación de las 14 historias del backlog (`docs/backlog/issues/`) se cumple, y que
la app es confiable sin conexión, al sincronizar con Supabase y al usar las capacidades del dispositivo (GPS,
cámara, notificaciones).

- **Dentro del alcance:** lógica de dominio, ViewModels, repositorios sobre Room, migraciones, sincronización con
  Supabase (PostgREST y Storage), WorkManager, permisos en tiempo de ejecución y flujos de UI en Jetpack Compose.
- **Fuera del alcance de este plan:** pruebas de carga. Las pruebas de seguridad del servidor (Sprint 9) están en
  `docs/seguridad/`, con su resultado en `docs/RIESGOS.md`.

La trazabilidad criterio → caso → prueba está en [`MATRIZ_TRAZABILIDAD.md`](MATRIZ_TRAZABILIDAD.md): cada
CA-HUxx-nn tiene exactamente un caso TC-HUxx-nn.

## 2. Niveles de prueba

| Nivel | Qué verifica | Herramientas | Dónde |
|---|---|---|---|
| Unitarias de dominio (TDD) | Reglas puras: revisión de solicitudes, inventario, actividades, recordatorios | JUnit 4 | `app/src/test/.../model/` |
| Unitarias de ViewModel | Validaciones, estados de la UI, control por rol, eventos de navegación | JUnit, Turbine, MockK, `MainDispatcherRule`, repositorios en memoria | `app/src/test/.../ui/` |
| Contrato HTTP | Nombres de columna, filtros, métodos y cabeceras que se envían a Supabase | MockWebServer | `SupabasePrestamosDataSourceTest` |
| Integración con Room | Repositorios, transacciones y consultas sobre una base en memoria con datos semilla | Room in-memory, `DatosSemilla` | `app/src/androidTest/.../data/` |
| Migraciones | Cada migración 1→2 … 7→8 conserva los datos y deja el esquema exportado exacto | `MigrationTestHelper` | `MigracionTest` |
| Sincronización | Envío de pendientes, recepción, conflictos, errores 401/404/4xx/5xx, fotos a Storage | Room in-memory, `FakePrestamosRemoteDataSource` | `SincronizadorPrestamosTest` |
| WorkManager | Reintento exponencial, programación y cancelación de recordatorios | `TestListenableWorkerBuilder`, WorkManager real con retrasos largos | `SincronizacionWorkerTest`, `WorkManagerRecordatoriosTest` |
| UI de extremo a extremo en el dispositivo | Flujos completos por rol sobre la app real | Compose UI Test, `PrestamoLabTestRunner` | `app/src/androidTest/.../ui/` |
| Manual contra Supabase real | Lo que depende de otra app o del servidor real (cámara del fabricante, diálogo del sistema) | Teléfono + consultas de solo lectura a Supabase | Sección 7 |

## 3. Entorno

- **Dispositivo:** Xiaomi 25078RA3EL, Android 15 (HyperOS). Gradle `connectedDebugAndroidTest` no funciona en este
  teléfono (MIUI bloquea abrir la actividad); las pruebas instrumentadas se ejecutan así:
  ```
  gradlew assembleDebug assembleDebugAndroidTest
  adb install -r -t app/build/outputs/apk/debug/app-debug.apk
  adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
  adb shell cmd appops set com.example.prestamolab 10021 allow
  adb shell am instrument -w -r com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner
  ```
- **Runner propio** (`PrestamoLabTestRunner`): reemplaza Supabase, WorkManager y las notificaciones por dobles antes de
  crear la app, y concede `POST_NOTIFICATIONS` para que el diálogo del sistema no tape la UI. El permiso de cámara
  nunca se concede en las pruebas: revocar un permiso concedido mata el proceso de prueba.
- **Integración continua:** `.github/workflows/android.yml` ejecuta las pruebas unitarias y compila la app y las
  pruebas instrumentadas en cada push y pull request (sin credenciales de Supabase).

## 4. Datos de prueba

- **Instrumentadas:** `DatosSemilla` reinicia Room antes de cada prueba: 5 equipos (Osciloscopio RESERVADO, Kit
  Arduino PRESTADO), la solicitud #1 SOLICITADA, el préstamo #2 PRESTADO y una actividad en 2030. Los usuarios
  vienen de `FakeUsuariosDataSource` (estudiante e instructor, contraseña de prueba `123456`).
- **Unitarias:** `InMemoryPrestamoRepository` e `InMemoryActividadRepository` aplican las mismas reglas de dominio
  que Room; los relojes se inyectan para que las fechas sean deterministas.
- **Supabase real (demo):** `docs/supabase/005_reiniciar_demo.sql` devuelve los préstamos y equipos al estado inicial.

## 5. Criterios de entrada y salida

- **Entrada:** la historia tiene sus criterios en formato Dado/cuando/entonces y la app compila.
- **Salida de una historia:** todos sus casos TC en verde (automatizados o manuales registrados), sin defectos de
  severidad Alta o Crítica abiertos, y la matriz regenerada con `python docs/backlog/generar_issues.py`.
- **Regresión:** antes de cada commit se ejecutan las suites unitaria e instrumentada completas.

## 6. Resultados actuales

| Suite | Pruebas | Resultado |
|---|---|---|
| Unitarias (`testDebugUnitTest`) | 187 | En verde (también en el CI) |
| Instrumentadas y de UI (Xiaomi) | 156 | 153 en verde en la suite completa; las 3 que requieren condiciones especiales (TC-HU13-01, TC-HU09-04 y la de punta a punta contra Supabase real) en verde ejecutadas aparte (secciones 7 y 7b) |
| Seguridad del servidor (`docs/seguridad/verificar_seguridad.py`) | 26 | En verde contra Supabase real |
| Criterios automatizados | 74 de 74 (100 %) | Ver matriz |

## 7. Pruebas que dependen de permisos no concedidos

Dos pruebas verifican el diálogo real del sistema y necesitan el permiso **sin conceder**; si está concedido se
omiten (`assumeTrue`). Revocar un permiso mata el proceso de prueba, así que se revoca antes desde adb:

| Caso | Preparación | Ejecución |
|---|---|---|
| TC-HU08-01 y 03 (cámara) | `adb shell pm revoke com.example.prestamolab android.permission.CAMERA` | Corre en la suite completa (ninguna otra prueba concede la cámara) |
| TC-HU13-01 (ubicación) | `adb shell pm revoke com.example.prestamolab android.permission.ACCESS_FINE_LOCATION` y lo mismo con `ACCESS_COARSE_LOCATION` | Aparte, porque `DevolucionUiTest` concede la ubicación durante la suite: `adb shell am instrument -w -r -e class com.example.prestamolab.ui.PermisoUbicacionUiTest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner` |
| TC-HU09-04 (notificaciones) | `adb shell pm revoke com.example.prestamolab android.permission.POST_NOTIFICATIONS`; al terminar, `pm grant` | Aparte y con `-e concederNotificaciones false`, porque el runner concede el permiso para el resto de la suite: `adb shell am instrument -w -r -e concederNotificaciones false -e class com.example.prestamolab.ui.PermisoNotificacionesUiTest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner`. Aprobada el 24 de septiembre de 2026: el logcat registra la solicitud `REQUEST_PERMISSIONS` al iniciar sesión el estudiante con un préstamo entregado |

## 7b. Prueba de punta a punta contra Supabase real

`SupabaseRealE2ETest` usa las clases reales de la app (login por RPC, Room, repositorios y sincronización) contra
el servidor real, sin UI. Como instructor crea, edita y elimina un equipo (HU-12) y crea y elimina una actividad
(HU-11); como estudiante recibe la actividad y el servidor le rechaza crear otra (RLS de `009`). Elimina todo lo
que crea, aunque falle. Solo corre si se pide:

```
adb shell am instrument -w -r -e e2e true -e class com.example.prestamolab.e2e.SupabaseRealE2ETest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner
```

Resultado del 24 de septiembre de 2026: **aprobada**, y el servidor quedó sin restos de la prueba.

## 8. Pruebas manuales: bitácora PASS / FAIL / BLOCKED

Estados: **PASS** (el resultado coincide con el esperado), **FAIL** (no coincide: se abre un BUG) y **BLOCKED**
(no se pudo ejecutar por una condición externa). Todas las ejecuciones son en el Xiaomi 25078RA3EL (Android 15)
contra el proyecto Supabase real, salvo que se indique otra cosa.

### 8.1 Ejecuciones registradas (Sprints 5 a 9)

Cada fila sale de una evidencia del repositorio: el defecto que se abrió (`DEFECTOS.md`) o el resultado registrado
en este plan. Un FAIL se vuelve a ejecutar después de la corrección (confirmación).

| Id | Caso manual | Historia | Fecha | Resultado | Evidencia | Confirmación |
|---|---|---|---|---|---|---|
| M-01 | Iniciar sesión como estudiante e instructor contra la tabla `users` real | HU-10 | 2026-09-24 | FAIL | BUG-05: siempre "No se pudo iniciar sesión" | PASS con ambos roles tras `22342ae` |
| M-02 | Sincronizar el cambio de estado de un equipo | HU-07 | 2026-09-24 | FAIL | BUG-06: error 23502, `equipments.title` NOT NULL | PASS tras `2795f7a` |
| M-03 | Sincronizar una solicitud de préstamo | HU-07 | 2026-09-24 | FAIL | BUG-07: `loans.user_role` NOT NULL | PASS tras `2795f7a` |
| M-04 | Un cambio que el servidor rechaza se conserva y se avisa | HU-07 | 2026-09-24 | FAIL | BUG-08: el cambio se perdía sin aviso | PASS tras `2795f7a` |
| M-05 | Devolver un préstamo ya devuelto desde otro dispositivo | HU-05 | 2026-09-24 | FAIL | BUG-09: la app se cerraba | PASS tras `2795f7a` (mensaje "Este préstamo ya tiene una devolución registrada") |
| M-06 | Confirmar la devolución en el teléfono | HU-05 | 2026-09-24 | FAIL | BUG-10: el botón quedaba bajo la barra del sistema | PASS tras `2795f7a` |
| M-07 | Mis Solicitudes muestra el nombre del equipo | HU-04 | 2026-09-24 | FAIL | BUG-12: mostraba el id local | PASS tras `2795f7a` |
| M-08 | Devolución con GPS real sincronizada: el préstamo queda DEVUELTO, el equipo DISPONIBLE y todos los registros SINCRONIZADO | HU-05, HU-07, HU-13 | 2026-09-24 | PASS | Filas de `loans` y `returns` en Supabase con coordenadas reales | — |
| M-09 | Foto real de evidencia: tomar la foto, sincronizar y abrir la URL pública | HU-08 | 2026-09-24 | PASS | Foto JPEG de 2,7 MB en el bucket `evidencias` y fila en `evidences` | — |
| M-10 | Punta a punta con seguridad (`009`): el estudiante solicita el Multímetro, el instructor aprueba y el estudiante lo ve PRESTADO | HU-03, HU-14, HU-13 | 2026-09-24 | PASS | Préstamo PRESTADO con solicitante, revisor y coordenadas GPS reales. Observación: encontró BUG-13 (severidad Baja, propósito con salto de línea) | BUG-13 confirmado con `PrestamoViewModelTest` |

**Resumen 8.1:** 10 casos; 7 FAIL que abrieron un defecto y pasaron en la confirmación, y 3 PASS. Ninguno BLOCKED.

### 8.2 Regresión manual de la versión de entrega (por ejecutar)

Con la versión del tag de entrega instalada, después de ejecutar `docs/supabase/005_reiniciar_demo.sql`. Quien
ejecuta anota la fecha y el resultado. No se llena por anticipado: la guía prohíbe reportar resultados que no se
ejecutaron.

| Id | Caso manual | Pasos | Resultado esperado | Fecha | Resultado |
|---|---|---|---|---|---|
| RM-01 | Inicio de sesión por rol | Entrar como estudiante; salir; entrar como instructor | Estudiante en Catálogo; instructor en Gestión; "Entorno: dev" visible en el login | | |
| RM-02 | Credenciales inválidas | Entrar con una contraseña incorrecta | "Usuario o contraseña incorrectos"; no avanza | | |
| RM-03 | Catálogo y filtro | Activar "Solo disponibles"; cerrar y abrir la app | Solo equipos DISPONIBLE; el filtro se conserva | | |
| RM-04 | Solicitud con valores límite | Propósito de 9 caracteres; luego duración 9 h; luego datos válidos | Los dos errores y, con datos válidos, la solicitud SOLICITADA en Mis Solicitudes | | |
| RM-05 | Aprobación | Instructor → Revisar solicitudes → Aprobar | La solicitud sale de la lista; el equipo queda PRESTADO | | |
| RM-06 | Devolución con GPS | Estudiante → Mis Solicitudes → Registrar devolución → Capturar ubicación → Confirmar | "Ubicación capturada correctamente"; el préstamo sale de los activos; el equipo vuelve a DISPONIBLE | | |
| RM-07 | Evidencia con cámara | En un préstamo PRESTADO → Evidencias → tomar foto | La app pide la cámara al tocar; la foto aparece como Local y luego Sincronizada | | |
| RM-08 | Sin conexión | Modo avión → solicitar un equipo → quitar el modo avión | La solicitud se guarda y se ve sin red; al volver la red se sincroniza | | |
| RM-09 | Supabase refleja el flujo | Consultar `loans` y `returns` en el panel | Los registros de RM-04 a RM-06 con estado y coordenadas | | |
| RM-10 | Cerrar sesión | Tocar "Salir" | Vuelve al login; al abrir la app no entra sin credenciales | | |

## 9. Riesgos del plan

- Las pruebas de UI dependen del teléfono físico; el CI solo ejecuta las unitarias.
- La cámara y los diálogos del sistema varían por fabricante: los diálogos se verificaron en el Xiaomi (sección 7)
  y la captura real de la foto es manual (sección 8).
- Las pruebas instrumentadas usan dobles de Supabase: los cambios del servidor (`docs/supabase/`) se verifican
  aparte contra la base real, con `docs/seguridad/verificar_seguridad.py` y la prueba de punta a punta.
