# Artefactos y gestión ágil - Scrum (PréstamoLab CTMA)

## 1. Product Goal

Mejorar la trazabilidad, consulta y control de préstamos de recursos de formación mediante una experiencia móvil
confiable, que funcione sin conexión y sea accesible para aprendices e instructores del CTMA.

## 2. Product Backlog de la Parte 2 (semanas 5 a 9)

Detalle de cada historia, con sus criterios Dado/cuando/entonces y sus casos de prueba 1:1, en
`docs/backlog/issues/` (Issues de GitHub) y trazabilidad completa en `docs/MATRIZ_TRAZABILIDAD.md`.

| Orden | Historia | Prioridad | Sprint | Estado |
|---|---|---|---|---|
| 1 | HU-10 Iniciar sesión y control de acceso por rol | Alta | 5 | Hecho |
| 2 | HU-05 Registrar devolución | Alta | 5 | Hecho |
| 3 | HU-13 Registrar geolocalización de las operaciones | Media | 5 | Hecho |
| 4 | HU-01 Consultar equipos disponibles | Alta | 6 | Hecho |
| 5 | HU-02 Consultar detalle de un equipo | Alta | 6 | Hecho |
| 6 | HU-06 Conservar datos localmente sin conexión | Alta | 6 | Hecho |
| 7 | HU-12 Gestionar inventario de equipos (instructor) | Media | 6 | Hecho |
| 8 | HU-11 Gestionar actividades formativas (instructor) | Media | 6 | Hecho |
| 9 | HU-03 Solicitar préstamo | Alta | 7 | Hecho |
| 10 | HU-04 Consultar mis préstamos activos | Alta | 7 | Hecho |
| 11 | HU-14 Revisar solicitudes de préstamo (instructor) | Alta | 7 | Hecho |
| 12 | HU-07 Sincronizar datos con servicio remoto | Media/Alta | 8 | Hecho |
| 13 | HU-08 Adjuntar evidencia fotográfica | Media | 8 | Hecho |
| 14 | HU-09 Recibir recordatorio de devolución | Media | 9 | Hecho |

**Avance:** 14 de 14 historias terminadas; 74 de 74 criterios con prueba automatizada.

## 3. Sprint Goals

| Sprint | Sprint Goal | Historias |
|---|---|---|
| 5 | Cada usuario entra con su rol y el estudiante puede devolver un equipo dejando su ubicación | HU-10, HU-05, HU-13 |
| 6 | El catálogo y el inventario se conservan en el teléfono sin conexión y el instructor los mantiene | HU-01, HU-02, HU-06, HU-11, HU-12 |
| 7 | El ciclo del préstamo se cierra: el estudiante solicita y el instructor aprueba o rechaza | HU-03, HU-04, HU-14 |
| 8 | Los datos del teléfono y de Supabase coinciden, con evidencia fotográfica del estado del equipo | HU-07, HU-08 |
| 9 | El estudiante recibe recordatorios y se verifica la seguridad de la solución | HU-09 y pruebas de seguridad (`docs/RIESGOS.md`) |

## 4. Incremento

Aplicación Android (Jetpack Compose, MVVM) con Room como fuente de verdad local, sesión en DataStore,
sincronización con Supabase mediante WorkManager, GPS, cámara y notificaciones. Historial de entregas en los
commits de la rama `andres-vargas`; cada commit indica las historias que cubre y el resultado de las pruebas.

## 5. Definition of Done (Parte 2)

1. El proyecto compila y el CI de GitHub Actions (`.github/workflows/android.yml`) está en verde.
2. Los criterios de aceptación de la historia están implementados y sus casos TC registrados en la matriz.
3. La UI (Compose) solo consume `StateFlow` del ViewModel; el ViewModel solo usa el Repository.
4. Room es la fuente de verdad que lee la UI; cada cambio de esquema tiene su migración probada con
   `MigrationTestHelper`.
5. Los cambios locales quedan PENDIENTES y se sincronizan con Supabase sin duplicarse (upsert por uuid).
6. Los permisos del dispositivo se piden en el momento de uso y negarlos no bloquea la app.
7. Suites unitaria e instrumentada en verde en el dispositivo de pruebas, con datos sintéticos.
8. Los defectos de severidad Alta o Crítica están corregidos, con prueba de confirmación y regresión
   (`DEFECTOS.md`).
9. Repositorio limpio y sin credenciales: la clave de Supabase vive en `local.properties`, fuera de git; los
   cambios de base de datos quedan versionados en `docs/supabase/`.
10. El incremento se demostró y quedó registrado en la revisión del sprint (sección 6).

## 6. Eventos: Sprint Review y Retrospectiva

**Cómo se redactaron estas actas.** No hubo reuniones formales registradas. Cada acta es una *revisión del
incremento* construida con evidencia verificable: los commits de la rama `andres-vargas`, las suites de pruebas
ejecutadas en el Xiaomi y las verificaciones contra el proyecto Supabase real. La implementación de la Parte 2 se
concentró entre el 23 y el 24 de septiembre de 2026 (fechas reales del historial de git), por eso cada acta indica
la fecha de su último commit. Las mejoras de las retrospectivas salen de defectos y situaciones reales del proyecto;
el equipo las **adoptó** el 25 de septiembre de 2026, y cada una indica dónde quedó aplicada.

### Sprint 5: acceso por rol y devolución con GPS

- **Fecha del incremento:** 24 de septiembre de 2026 (último commit `22342ae`).
- **Historias revisadas:** HU-10 (login y roles), HU-05 (devolución), HU-13 (GPS en la devolución).
- **Qué se demostró:** en el Xiaomi, contra Supabase: el instructor entra por correo a Gestión, el estudiante
  entra por documento al Catálogo, una clave incorrecta muestra "Usuario o contraseña incorrectos" y la sesión se
  conserva al reabrir la app. Con pruebas de UI en el teléfono: la devolución registra el estado del equipo y la
  ubicación del Fused Location Provider, pidiendo el permiso solo al tocar "Capturar ubicación actual".
- **Pruebas al cierre:** 58 unitarias y 44 instrumentadas en verde.
- **Resultado:** incremento aceptado tras corregir BUG-05 (sin el permiso INTERNET el login fallaba siempre) y
  BUG-04 (cancelar un préstamo entregado liberaba el equipo).
- **Adaptación del backlog:** login contra la tabla `users` sin Supabase Auth y contraseña en SHA-256 calculado en
  la app (decisión del equipo); los riesgos de esa decisión se registraron en `docs/RIESGOS.md` (R-01 a R-05).

**Retrospectiva**
- *Qué salió bien:* criterios Dado/cuando/entonces con un caso de prueba 1:1 desde el inicio del sprint.
- *Qué mejorar:* BUG-05 pasó todas las pruebas porque usan un servidor simulado.
- *Mejora adoptada:* toda historia que use la red se verifica también en el teléfono contra Supabase real antes de
  darla por terminada. *Aplicada:* pruebas manuales contra Supabase de HU-07, HU-08 y HU-14, y
  `SupabaseRealE2ETest` para HU-11 y HU-12 (`docs/PLAN_PRUEBAS.md`, secciones 7b y 8).

### Sprint 6: catálogo e inventario persistentes sin conexión

- **Fecha del incremento:** 24 de septiembre de 2026 (último commit `ca16808`).
- **Historias revisadas:** HU-01 (catálogo y filtros), HU-02 (detalle), HU-06 (Room sin conexión), HU-11
  (actividades), HU-12 (inventario).
- **Qué se demostró:** Room como fuente de verdad con migraciones probadas de la versión 1 a la 8; el catálogo y
  los préstamos siguen visibles al reiniciar sin red (CA-HU06-01); filtros del catálogo que se conservan al
  reabrir la app; el instructor mantiene inventario y actividades y el estudiante las ve en solo lectura.
- **Pruebas al cierre:** 177 unitarias y 153 instrumentadas en verde.
- **Resultado:** incremento aceptado. HU-11 y HU-12 quedan pendientes de una prueba manual contra Supabase real
  (sus pruebas automatizadas usan el servidor simulado).
- **Adaptación del backlog:** un equipo con préstamos cerrados tampoco se elimina, para conservar su historial.

**Retrospectiva**
- *Qué salió bien:* BUG-11 (la pestaña Catálogo llevaba a Mis Solicitudes) lo detectó una prueba de UI en el
  teléfono físico, no un usuario.
- *Qué mejorar:* los chips de filtro con desplazamiento horizontal rompieron 11 pruebas de UI y ocultaban
  categorías en pantallas pequeñas.
- *Mejora adoptada:* ejecutar la suite de UI completa en el teléfono real antes de cada commit que cambie una
  pantalla. *Aplicada:* cada commit posterior registra la suite instrumentada completa en verde en su mensaje.

### Sprint 7: ciclo completo del préstamo

- **Fecha del incremento:** 24 de septiembre de 2026 (último commit `ca16808`).
- **Historias revisadas:** HU-03 (solicitar), HU-04 (mis préstamos), HU-14 (aprobar o rechazar).
- **Qué se demostró:** reglas de revisión en el dominio con TDD (solo una solicitud SOLICITADA se revisa; rechazar
  exige motivo); lista de pendientes del instructor; tarjetas de Mis Solicitudes con equipo, ambiente, fechas y
  estado. En la prueba de punta a punta del Sprint 9, el ciclo solicitar → aprobar quedó verificado en Supabase
  real con solicitante y revisor correctos.
- **Pruebas al cierre:** 177 unitarias y 153 instrumentadas en verde.
- **Resultado:** incremento aceptado después de una auditoría de criterios que encontró funcionalidad faltante en
  HU-04 (CA-HU04-02), completada en `ca16808`.

**Retrospectiva**
- *Qué salió bien:* la matriz de trazabilidad permitió auditar criterio por criterio.
- *Qué mejorar:* se informó que las 14 historias estaban completas cuando la auditoría mostró 6 criterios sin la
  funcionalidad implementada (HU-01, HU-04, HU-06 y HU-13).
- *Mejora adoptada:* una historia es "Hecho" solo cuando todas sus filas de `docs/MATRIZ_TRAZABILIDAD.md` tienen
  prueba en verde, no cuando su pantalla principal funciona. *Aplicada:* el estado del backlog (sección 2) se
  actualizó con esa regla y la matriz se regenera con `generar_issues.py` en cada cambio (74 de 74).

### Sprint 8: sincronización y evidencia fotográfica

- **Fecha del incremento:** 24 de septiembre de 2026 (último commit `8f16c69`).
- **Historias revisadas:** HU-07 (sincronización con Supabase), HU-08 (evidencia fotográfica).
- **Qué se demostró:** en el Xiaomi contra Supabase: inicio de sesión del estudiante, recepción de sus préstamos
  y devolución con GPS real enviada al servidor, con todos los registros locales en SINCRONIZADO. El estudiante
  tomó una foto de evidencia desde Mis Solicitudes; la imagen (JPEG de 2,7 MB) quedó en el bucket `evidencias` y
  su fila en la tabla `evidences`.
- **Pruebas al cierre:** 165 unitarias y 141 instrumentadas en verde; primer CI de GitHub Actions en verde.
- **Resultado:** incremento aceptado tras corregir BUG-06 a BUG-10 y BUG-12, encontrados en la prueba manual
  contra el esquema real de Supabase.
- **Adaptación del backlog:** el estudiante envía solo el estado del equipo (PATCH) y el instructor el equipo
  completo; se agregó GPS a las evidencias.

**Retrospectiva**
- *Qué salió bien:* una sola prueba manual contra el servidor real encontró 6 defectos antes de la entrega.
- *Qué mejorar:* el código de sincronización asumió un esquema que no coincidía con el real (columnas NOT NULL
  como `equipments.title` y `loans.user_role`).
- *Mejora adoptada:* antes de escribir código contra una tabla, consultar sus columnas y restricciones reales en
  Supabase. *Aplicada:* se consultó el esquema real antes de HU-11 (`activities`), HU-08 (`evidences`) y HU-13
  (`loans.latitude`); por eso la precisión del GPS quedó solo en el teléfono, al no existir esa columna.

### Sprint 9: recordatorios y seguridad

- **Fecha del incremento:** 24 de septiembre de 2026 (último commit `ba85bb0`).
- **Historias y trabajo revisados:** HU-09 (recordatorio de devolución) y seguridad del servidor (R-01 a R-05).
- **Qué se demostró:** con pruebas instrumentadas en el teléfono, la programación del aviso 30 minutos antes de la
  hora límite (WorkManager real), la notificación publicada en el sistema y la apertura del préstamo al tocarla;
  `docs/seguridad/verificar_seguridad.py` superó 26 de 26 comprobaciones contra Supabase real actuando como un
  atacante con la anon key; prueba de punta a punta con la seguridad activa: el estudiante solicitó el Multímetro,
  el instructor lo aprobó y Supabase registró el préstamo PRESTADO con revisor y coordenadas GPS reales.
- **Pruebas al cierre:** 187 unitarias, 154 instrumentadas y 26 de seguridad en verde; CI en verde.
- **Resultado:** incremento aceptado tras corregir BUG-13 (propósito con salto de línea) y BUG-14 (fallas del
  script de seguridad, entre ellas políticas ajenas que permitieron a un estudiante borrar un equipo, restaurado
  de inmediato). Quedan como riesgos aceptados R-06 a R-09.
- **Adaptación del backlog:** la revisión con OWASP ZAP se hace en modo pasivo sobre el tráfico de la app, sin
  escanear activamente la infraestructura compartida de Supabase.

**Retrospectiva**
- *Qué salió bien:* verificar la seguridad atacando la base real encontró tres fallas que la lectura del script no
  mostraba.
- *Qué mejorar:* el script de reversión aparecía antes del de seguridad y casi se ejecutaron los dos; los datos que
  dejan las pruebas automáticas en el teléfono se habrían subido a Supabase al iniciar sesión; `005_reiniciar_demo.sql`
  dejó inconsistente un préstamo que no era de demostración (el equipo DISPONIBLE y su préstamo PRESTADO).
- *Mejoras adoptadas y aplicadas:* (1) los scripts de emergencia viven fuera de la secuencia numerada
  (`docs/supabase/emergencia/`); (2) después de cada ejecución de pruebas instrumentadas se limpian los datos locales
  de la app antes de usarla con el servidor real; (3) toda política RLS nueva se valida con
  `docs/seguridad/verificar_seguridad.py`; (4) los préstamos creados en pruebas contra el servidor real se cierran
  con su devolución antes de reiniciar la demo (así se cerró el del Multímetro).

## 7. Métricas del Sprint

Guía, semana 7, actividad 21: *registrar lead/cycle time básico de las HU y analizar cuellos de botella*. Los datos
salen del historial real de git de la rama (fecha de autor de cada commit que menciona la historia; los mismos
commits que enlaza `docs/MATRIZ_TRAZABILIDAD.md`). Calculado el 25 de septiembre de 2026.

- **Lead time:** desde que la historia entró al backlog (commit `6898c91`, 23-09 20:17) hasta su último commit.
- **Cycle time:** desde el primer hasta el último commit de la historia.

| Historia | Commits | Primer commit | Último commit | Cycle time | Lead time |
|---|---|---|---|---|---|
| HU-10 | 4 | 23-09 20:41 | 24-09 13:22 | 16,7 h | 17,1 h |
| HU-05 | 2 | 23-09 21:10 | 24-09 14:42 | 17,5 h | 18,4 h |
| HU-13 | 2 | 23-09 21:10 | 24-09 19:39 | 22,5 h | 23,4 h |
| HU-06 | 3 | 24-09 12:42 | 24-09 19:39 | 7,0 h | 23,4 h |
| HU-07 | 2 | 24-09 13:51 | 24-09 14:42 | 0,8 h | 18,4 h |
| HU-14 | 1 | 24-09 17:20 | 24-09 17:20 | < 1 commit | 21,1 h |
| HU-12 | 2 | 24-09 17:42 | 24-09 21:23 | 3,7 h | 25,1 h |
| HU-11 | 2 | 24-09 17:58 | 24-09 21:23 | 3,4 h | 25,1 h |
| HU-09 | 2 | 24-09 18:20 | 24-09 21:23 | 3,0 h | 25,1 h |
| HU-08 | 3 | 24-09 18:35 | 24-09 22:23 | 3,8 h | 26,1 h |
| HU-01 | 1 | 24-09 19:39 | 24-09 19:39 | < 1 commit | 23,4 h |
| HU-04 | 1 | 24-09 19:39 | 24-09 19:39 | < 1 commit | 23,4 h |
| HU-03 | 3 | 24-09 22:17 | 24-09 22:18 | < 1 commit (ciclo TDD) | 26,0 h |
| HU-02 | — | — | — | — | Base de la Parte 1, sin commit propio |

**Lectura de los datos**

- **Cuello de botella: la verificación contra el entorno real.** Las tres historias con mayor cycle time (HU-13,
  HU-05 y HU-10) son las primeras que se probaron en el teléfono contra Supabase. Ahí aparecieron BUG-05 a BUG-10:
  permiso de Internet, columnas NOT NULL del esquema real y el botón bajo la barra del sistema. El tiempo se fue en
  retrabajo, no en la implementación inicial. La mejora adoptada fue verificar toda historia que use la red en el
  teléfono contra Supabase real antes de darla por terminada (retrospectiva del Sprint 5, sección 6).
- **Una historia = uno a cuatro commits.** Cuando una historia cabe en un commit, git no registra cuándo empezó y
  el cycle time no se puede medir; figura como "< 1 commit". Para medirlo bien hace falta registrar el paso a "En
  progreso" en el tablero (GitHub Project) o trabajar cada historia en su propia rama `feature/hu-XX`.
- **Trabajo concentrado.** Toda la Parte 2 se implementó en unas 26 horas de calendario (23 y 24 de septiembre),
  así que los "sprints" 5 a 9 son agrupaciones del backlog y no iteraciones de una semana. El lead time casi igual en
  todas las historias refleja eso: todas entraron al backlog al mismo tiempo.

**Velocidad, burndown y burnup:** requieren estimar las historias en puntos (Planning Poker, actividad 17). Esa
estimación no se hizo, y no se reconstruye después para no presentar datos inventados. Queda pendiente para el equipo.

## 8. Parte 1 (histórico)

- **Sprint Goal:** permitir consultar un equipo disponible y registrar una solicitud de préstamo válida, manteniendo
  la disponibilidad coherente y demostrando su calidad con pruebas reproducibles.
- **Backlog:** PB-01 a PB-10 (catálogo, detalle, solicitud con validaciones, disponibilidad, doble pulsación, Mis
  solicitudes, cancelación e ids inexistentes). Su equivalencia con los casos actuales está en
  `docs/backlog/issues/README.md`.
- **Sprint Review:** se demostró en el emulador el flujo del catálogo a la creación y cancelación de préstamos, y
  la corrección de la doble pulsación (`BUG-03`).
- **Retrospectiva:** definir precondiciones y datos de prueba en el Sprint Planning para evitar casos bloqueados
  durante el QA.
