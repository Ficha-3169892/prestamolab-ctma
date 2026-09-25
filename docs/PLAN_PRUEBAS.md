# Plan de pruebas: PréstamoLab CTMA (Parte 2, semanas 5 a 9)

## 1. Objetivo y alcance

Verificar que cada criterio de aceptación de las 14 historias del backlog (`docs/backlog/issues/`) se cumple, y que
la app es confiable sin conexión, al sincronizar con Supabase y al usar las capacidades del dispositivo (GPS,
cámara, notificaciones).

- **Dentro del alcance:** lógica de dominio, ViewModels, repositorios sobre Room, migraciones, sincronización con
  Supabase (PostgREST y Storage), WorkManager, permisos en tiempo de ejecución y flujos de UI en Jetpack Compose.
- **Fuera del alcance de este plan:** pruebas de carga y de seguridad del servidor; la prueba no funcional de
  seguridad (OWASP ZAP / MASVS) se planifica en `docs/RIESGOS.md` para el Sprint 9.

La trazabilidad criterio → caso → prueba está en [`MATRIZ_TRAZABILIDAD.md`](MATRIZ_TRAZABILIDAD.md): cada
CA-HUxx-nn tiene exactamente un caso TC-HUxx-nn.

## 2. Niveles de prueba

| Nivel | Qué verifica | Herramientas | Dónde |
|---|---|---|---|
| Unitarias de dominio (TDD) | Reglas puras: revisión de solicitudes, inventario, actividades, recordatorios | JUnit 4 | `app/src/test/.../model/` |
| Unitarias de ViewModel | Validaciones, estados de la UI, control por rol, eventos de navegación | JUnit, Turbine, MockK, `MainDispatcherRule`, repositorios en memoria | `app/src/test/.../ui/` |
| Contrato HTTP | Nombres de columna, filtros, métodos y cabeceras que se envían a Supabase | MockWebServer | `SupabasePrestamosDataSourceTest` |
| Integración con Room | Repositorios, transacciones y consultas sobre una base en memoria con datos semilla | Room in-memory, `DatosSemilla` | `app/src/androidTest/.../data/` |
| Migraciones | Cada migración 1→2 … 5→6 conserva los datos y deja el esquema exportado exacto | `MigrationTestHelper` | `MigracionTest` |
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
| Unitarias (`testDebugUnitTest`) | 160 | En verde |
| Instrumentadas y de UI (Xiaomi) | 137 | En verde |
| Criterios automatizados | 62 de 74 (83 %) | Ver matriz |

## 7. Pruebas manuales

| Caso | Pasos | Estado |
|---|---|---|
| Foto real de evidencia (HU-08) | Estudiante → Mis Solicitudes → préstamo PRESTADO → Evidencias → tomar foto → sincronizar; comprobar la fila en `evidences` y que la URL pública abre la imagen | Aprobado (foto JPEG de 2,7 MB en el bucket `evidencias`) |
| Diálogo de ubicación (TC-HU13-01) | Sin permiso de ubicación, tocar "Capturar ubicación actual" en la devolución: el diálogo aparece en ese momento | Pendiente de registrar |
| Diálogo de notificaciones (HU-09) | `adb shell pm revoke com.example.prestamolab android.permission.POST_NOTIFICATIONS`; entrar como estudiante con un préstamo PRESTADO: aparece el diálogo; negarlo no bloquea nada | Pendiente de registrar |
| Aprobar / rechazar, inventario y actividades contra Supabase real (HU-14, HU-12, HU-11) | Hacer la acción como instructor, sincronizar y consultar la tabla en Supabase | HU-14 verificada; HU-12 y HU-11 pendientes |

## 8. Criterios pendientes

De los 12 criterios sin prueba automatizada, 5 requieren también implementar la funcionalidad:

| Criterio | Qué falta |
|---|---|
| CA-HU01-02 | Prueba del color distinto para equipos no disponibles (ya implementado en el catálogo) |
| CA-HU01-03 | **Funcionalidad:** filtro "Solo disponibles" y por categoría, y su prueba |
| CA-HU01-04 | **Funcionalidad:** conservar el filtro en DataStore, y su prueba |
| CA-HU01-05 | **Funcionalidad:** mensaje "No hay equipos para mostrar", y su prueba |
| CA-HU04-01, 02, 04 | Pruebas de Mis Solicitudes: solo las del usuario, campos visibles, mensaje sin solicitudes (ya implementado) |
| CA-HU06-01 | Prueba de reinicio en modo avión (Room ya es la fuente de verdad) |
| CA-HU06-05 | **Funcionalidad:** consulta con `@Relation` (préstamo con su equipo y evidencias), y su prueba |
| CA-HU13-01 | Registro de la prueba manual del diálogo de ubicación |
| CA-HU13-03 | **Funcionalidad:** guardar latitud, longitud y precisión al solicitar un préstamo |
| CA-HU13-06 | Prueba de que las coordenadas llegan a Supabase (`returns` ya las envía; `loans` depende de CA-HU13-03) |

## 9. Riesgos del plan

- Las pruebas de UI dependen del teléfono físico; el CI solo ejecuta las unitarias.
- La cámara y los diálogos del sistema varían por fabricante: se cubren con pruebas manuales registradas aquí.
- Los riesgos de seguridad aceptados (R-01 a R-05) se verifican aparte, según `docs/RIESGOS.md`.
