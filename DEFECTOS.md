# Registro de defectos, confirmación y regresión (PréstamoLab CTMA)

Formato de cada defecto: plantilla `.github/ISSUE_TEMPLATE/bug.md`. La numeración continúa la de la Parte 1
(BUG-03). Todos los defectos de la Parte 2 (BUG-04 a BUG-14) están **cerrados**; ninguno de severidad Alta o
Crítica queda abierto.

## Parte 2 (semanas 5 a 9)

| Id | Defecto | Severidad | Cómo se detectó | Corrección (commit) | Prueba de confirmación |
|---|---|---|---|---|---|
| BUG-04 | Cancelar un préstamo ya entregado (PRESTADO) liberaba el equipo | Alta | Desarrollo de HU-05 (devolución) | `de7f2f4` | `RoomPrestamoRepositoryTest.CancelarUnPrestamoPrestado_FallaYNoLiberaElEquipo` |
| BUG-05 | El login contra Supabase siempre fallaba con "No se pudo iniciar sesión" | Crítica | Prueba manual en el Xiaomi contra Supabase | `22342ae` | Manual en el dispositivo (las pruebas usan una tabla `users` simulada) |
| BUG-06 | Enviar el estado de un equipo fallaba con 23502 (`equipments.title` NOT NULL) | Alta | Prueba manual de sincronización contra Supabase | `2795f7a` | `SupabasePrestamosDataSourceTest`: "El estado del equipo se envia con PATCH…" y "El instructor guarda el equipo completo con title igual al nombre" |
| BUG-07 | Supabase rechazaba los préstamos por `loans.user_role` NOT NULL | Alta | Prueba manual de sincronización | `2795f7a` | `SupabasePrestamosDataSourceTest`: "TC-HU07-01 - Guardar un prestamo hace upsert…" |
| BUG-08 | Un cambio rechazado por el servidor (ERROR) se perdía sin aviso al recibir la versión remota | Alta | Prueba manual de sincronización contra Supabase | `2795f7a` | `SincronizadorPrestamosTest.UnRegistroEnErrorNoSePisaConLaVersionRemota` |
| BUG-09 | Registrar la devolución de un préstamo ya devuelto en otro dispositivo cerraba la app (índice único) | Alta | Prueba manual contra Supabase | `2795f7a` | `RoomPrestamoRepositoryTest.DevolucionYaRecibidaDeOtroDispositivo_SeRechazaSinCerrarLaApp` |
| BUG-10 | "Confirmar devolución" quedaba debajo de la barra de navegación del sistema y no se podía pulsar | Alta | Prueba manual en el Xiaomi (edge-to-edge) | `2795f7a` | `DevolucionUiTest.TC_HU05_02_DevolucionConUbicacion_CierraElPrestamo` |
| BUG-11 | Tras solicitar, tocar la pestaña Catálogo dejaba al usuario en Mis Solicitudes | Media | `PrestamoUiTest.TC12` falló en el Xiaomi | `3922d36` | `PrestamoUiTest.TC12_BotonSolicitarDeshabilitado_SiEquipoEstaReservado` y `Navegacion_BottomBar_CicloCompleto` |
| BUG-12 | "Mis Solicitudes" mostraba el id local del equipo en vez de su nombre | Baja | Prueba manual | `2795f7a` | `PrestamoUiTest.TC14_FlujoCompleto_CrearSolicitud` ("Equipo: Multímetro Digital") |
| BUG-13 | El propósito se guardaba con el salto de línea final del teclado, y ese salto contaba para el mínimo de 10 caracteres | Baja | Prueba de punta a punta contra Supabase (Sprint 9) | ver historial | `PrestamoViewModelTest`: "BUG-13 - El proposito se guarda sin espacios…" y "BUG-13 - Nueve letras y un salto de linea…" |
| BUG-14 | `009_seguridad.sql`: el login fallaba (42804, `varchar` frente a `text`), RLS no estaba activo en `users` y políticas creadas a mano anulaban las del script (un estudiante pudo borrar un equipo, restaurado de inmediato) | Crítica | `docs/seguridad/verificar_seguridad.py` contra Supabase | `fda65a1`, `edc7aeb` | `verificar_seguridad.py`: 26 de 26 comprobaciones |

### Detalle y causa raíz

- **BUG-04:** `cancelarSolicitud` no comprobaba el estado. **Solución:** solo se cancela una solicitud SOLICITADA;
  un préstamo entregado se cierra con la devolución.
- **BUG-05:** faltaba `android.permission.INTERNET` en el manifiesto. Las pruebas no lo detectaron porque el runner
  reemplaza Supabase por dobles. **Solución:** permiso agregado; verificado en el teléfono con ambos roles.
- **BUG-06:** el envío del estado era un upsert (`INSERT … ON CONFLICT`), que exige todas las columnas NOT NULL.
  **Solución:** el estudiante envía solo `status` con PATCH; el instructor envía el equipo completo con
  `title = name` (HU-12).
- **BUG-07:** `loans.user_role` es NOT NULL en Supabase y la app no lo enviaba. **Solución:** se envía
  `ESTUDIANTE`, único rol que solicita préstamos (CA-HU03-08).
- **BUG-08:** la recepción solo protegía los registros PENDIENTE. **Solución:** los registros en ERROR también se
  conservan y la app avisa cuántos cambios rechazó el servidor.
- **BUG-09:** la devolución remota ya estaba en Room y el índice único de `returns.loan_id` lanzaba una excepción.
  **Solución:** se rechaza con el mensaje "Este préstamo ya tiene una devolución registrada".
- **BUG-10:** la app dibuja de borde a borde y la pantalla no reservaba el espacio de las barras del sistema.
  **Solución:** `safeDrawingPadding` y `consumeWindowInsets` en las pantallas afectadas.
- **BUG-11:** con `saveState`/`restoreState`, Navigation 2.7 asociaba la pila guardada de Mis Solicitudes a la
  ruta de inicio y la restauraba. **Solución:** la pestaña de inicio se alcanza regresando a su entrada.
- **BUG-12:** la tarjeta mostraba `equipoId`, que no coincide entre dispositivos. **Solución:** se muestra el
  nombre del equipo.
- **BUG-13:** el ViewModel validaba y guardaba el propósito sin recortar (el ambiente sí se recortaba).
  **Solución:** el propósito se recorta antes de validar y de guardar.
- **BUG-14:** PostgreSQL valida los tipos de una función PL/pgSQL al ejecutarla, no al crearla; y las políticas
  RLS se combinan con OR, así que una política permisiva ajena anula las demás. **Solución:** conversiones
  explícitas a `text`, `enable row level security` en `users` y un bloque que elimina toda política que el script
  no define. **Lección:** la seguridad se verifica atacando la base real, no solo leyendo el script.

### Regresión

Después de cada corrección se ejecutaron las suites completas: unitaria (`testDebugUnitTest`) e instrumentada en el
Xiaomi. Resultado actual: **187 unitarias y 156 instrumentadas en verde**; el CI de GitHub Actions repite las
unitarias en cada push.

## Parte 1 (histórico)

### BUG-03: doble pulsación en "Guardar" creaba solicitudes duplicadas

- **Severidad / prioridad:** Alta / Alta (rompía la regla de disponibilidad). **Caso de origen:** TC-13.
- **Pasos:** seleccionar un equipo DISPONIBLE → "Solicitar" → datos válidos → pulsar "Guardar" dos o tres veces
  rápido. **Obtenido:** dos solicitudes para el mismo equipo. **Esperado:** una sola solicitud.
- **Solución:** `guardando = true` en el `UiState` deshabilita el botón y el ViewModel ignora los clics siguientes.
- **Confirmación:** TC-13 (hoy TC-HU03-05) en verde. **Regresión:** TC-14 (creación normal), TC-01 (catálogo en
  RESERVADO) y TC-15 (cancelación libera el equipo) en verde.
