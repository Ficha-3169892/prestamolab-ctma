# Pruebas inestables (flaky tests)

Guía, semana 9: *flaky tests: identificación, causas y manejo*.

Una prueba es inestable cuando falla o pasa con el mismo código. Criterio del equipo: una falla que desaparece al repetir la prueba **no se ignora**. Se registra aquí, se busca la causa y, si se encuentra, se corrige la prueba o el entorno; si no se encuentra, queda abierta.

| ID | Prueba | Síntoma | Causa | Manejo | Estado |
|---|---|---|---|---|---|
| FLK-01 | `SupabasePrestamosDataSourceTest` (varias) | `SocketTimeoutException: Read timed out` al ejecutar `testDebugUnitTest` junto con la compilación y lint. Se vio dos veces seguidas el 2026-09-25 (2 y 3 pruebas distintas); al repetir la clase sola, pasaba | El cliente de las pruebas esperaba solo **500 ms** por cualquier respuesta. Ese margen solo lo necesita TC-HU07-05, que comprueba el timeout con una respuesta demorada 2 s. Con la máquina cargada, respuestas normales de MockWebServer superaban los 500 ms | El cliente general espera 10 s y TC-HU07-05 crea su propio cliente de 500 ms. Lo mismo en `SupabaseUsuariosDataSourceTest`. Verificado con dos corridas forzadas de pruebas y lint (`--rerun`): 202 de 202 | Corregida |
| FLK-02 | Pruebas de UI en el teléfono | `KeyguardLocked` o "Failed to inject touch input" cuando la pantalla se bloquea a mitad de la suite | Entorno: el teléfono se bloquea por inactividad | Mantener la pantalla encendida durante toda la sesión (`adb shell svc power stayon usb`) y restaurar el ajuste solo al final | Controlada |
| FLK-03 | Pruebas de UI en el teléfono (Xiaomi, HyperOS) | La suite se queda esperando `MainActivity`; en logcat, "MIUILOG- Permission Denied Activity" | Entorno: MIUI bloquea abrir actividades en segundo plano. Desinstalar la app, como hace `connectedDebugAndroidTest`, borra el permiso. El 2026-09-25 también lo borró una reinstalación con `adb install -r` | Instalar con `adb install -r -t`, dar el permiso con `cmd appops set com.example.prestamolab 10021 allow` **después de cada instalación** y ejecutar con `am instrument` (`docs/PLAN_PRUEBAS.md`) | Controlada |
| FLK-04 | `EvidenciasUiTest`: diálogo del permiso de cámara | La prueba se omite si el teléfono ya tiene el permiso CAMERA (por ejemplo, después de una prueba manual) | Precondición del entorno. Revocar el permiso desde la propia prueba mata el proceso de pruebas | La prueba se omite con un *assumption* explícito, en vez de fallar o pasar en falso. Antes de la suite se ejecuta `adb shell pm revoke com.example.prestamolab android.permission.CAMERA` | Controlada |
| FLK-05 | 10 pruebas de UI: Actividades, CatalogoFiltros, Devolución y Evidencias | En la primera suite completa del 2026-09-25 (head `822817f`), 10 de 159 fallaron por nodos no encontrados o no visibles. Cada clase pasó sola, y la suite completa repetida dio 159 de 159 | **No determinada.** El teléfono estaba desbloqueado al revisar. Una hipótesis sin confirmar es la carga del sistema en la primera ejecución después de instalar | Registrada. Si se repite, guardar el logcat de esa corrida y revisar si las esperas de esas pruebas dependen del tiempo | Abierta |

## Prácticas que evitan pruebas inestables en este proyecto

- Las pruebas de ViewModel usan `MainDispatcherRule`, con `UnconfinedTestDispatcher`, y `runTest`: no dependen del reloj real.
- En las pruebas de Compose, las esperas avanzan el reloj de la prueba (`mainClock.advanceTimeBy`) o usan `waitUntil`. `Thread.sleep` no avanza ese reloj, así que no hay recomposición y los `LaunchedEffect` no se ejecutan.
- Las pruebas de red usan MockWebServer y nunca el Supabase real. La única excepción es `SupabaseRealE2ETest`, que solo se ejecuta si se pide (`-e e2e true`).
- Después de las pruebas instrumentadas se borran los datos locales de la app en el teléfono, para que la siguiente corrida empiece igual y los datos de prueba no se sincronicen con Supabase.
