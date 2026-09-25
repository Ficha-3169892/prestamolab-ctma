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
| Instrumentadas y de UI (Xiaomi) | 154 | 153 en verde en la suite completa; TC-HU13-01 en verde ejecutada aparte (ver 7) |
| Seguridad del servidor (`docs/seguridad/verificar_seguridad.py`) | 26 | En verde contra Supabase real |
| Criterios automatizados | 74 de 74 (100 %) | Ver matriz |

## 7. Pruebas que dependen de permisos no concedidos

Dos pruebas verifican el diálogo real del sistema y necesitan el permiso **sin conceder**; si está concedido se
omiten (`assumeTrue`). Revocar un permiso mata el proceso de prueba, así que se revoca antes desde adb:

| Caso | Preparación | Ejecución |
|---|---|---|
| TC-HU08-01 y 03 (cámara) | `adb shell pm revoke com.example.prestamolab android.permission.CAMERA` | Corre en la suite completa (ninguna otra prueba concede la cámara) |
| TC-HU13-01 (ubicación) | `adb shell pm revoke com.example.prestamolab android.permission.ACCESS_FINE_LOCATION` y lo mismo con `ACCESS_COARSE_LOCATION` | Aparte, porque `DevolucionUiTest` concede la ubicación durante la suite: `adb shell am instrument -w -r -e class com.example.prestamolab.ui.PermisoUbicacionUiTest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner` |
| TC-HU09-04 (notificaciones) | `adb shell pm revoke com.example.prestamolab android.permission.POST_NOTIFICATIONS`; al terminar, `pm grant` | Aparte y con `-e concederNotificaciones false`, porque el runner concede el permiso para el resto de la suite: `adb shell am instrument -w -r -e concederNotificaciones false -e class com.example.prestamolab.ui.PermisoNotificacionesUiTest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner`. **Pendiente de una ejecución completa:** en el intento del 24 de septiembre el diálogo apareció, pero el teléfono se bloqueó antes de terminar |

## 7b. Prueba de punta a punta contra Supabase real

`SupabaseRealE2ETest` usa las clases reales de la app (login por RPC, Room, repositorios y sincronización) contra
el servidor real, sin UI. Como instructor crea, edita y elimina un equipo (HU-12) y crea y elimina una actividad
(HU-11); como estudiante recibe la actividad y el servidor le rechaza crear otra (RLS de `009`). Elimina todo lo
que crea, aunque falle. Solo corre si se pide:

```
adb shell am instrument -w -r -e e2e true -e class com.example.prestamolab.e2e.SupabaseRealE2ETest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner
```

Resultado del 24 de septiembre de 2026: **aprobada**, y el servidor quedó sin restos de la prueba.

## 8. Pruebas manuales

| Caso | Pasos | Estado |
|---|---|---|
| Foto real de evidencia (HU-08) | Estudiante → Mis Solicitudes → préstamo PRESTADO → Evidencias → tomar foto → sincronizar; comprobar la fila en `evidences` y que la URL pública abre la imagen | Aprobado (foto JPEG de 2,7 MB en el bucket `evidencias`) |
| Diálogo de notificaciones (HU-09) | Automatizada con `PermisoNotificacionesUiTest` (sección 7) | Pendiente de una ejecución completa con el teléfono desbloqueado |
| Punta a punta con seguridad (HU-03, HU-14, HU-13, R-04) | Con `009` aplicado: estudiante inicia sesión y solicita el Multímetro; instructor lo aprueba; estudiante lo ve PRESTADO. Consultar `loans` en Supabase | Aprobado: préstamo PRESTADO con solicitante y revisor correctos, equipo PRESTADO y coordenadas GPS reales de la solicitud. Encontró BUG-13 |
| Inventario y actividades contra Supabase real (HU-12, HU-11) | Automatizada con `SupabaseRealE2ETest` (sección 7b) | Aprobado |

## 9. Riesgos del plan

- Las pruebas de UI dependen del teléfono físico; el CI solo ejecuta las unitarias.
- La cámara y los diálogos del sistema varían por fabricante: los diálogos se verificaron en el Xiaomi (sección 7)
  y la captura real de la foto es manual (sección 8).
- Las pruebas instrumentadas usan dobles de Supabase: los cambios del servidor (`docs/supabase/`) se verifican
  aparte contra la base real, con `docs/seguridad/verificar_seguridad.py` y la prueba de punta a punta.
