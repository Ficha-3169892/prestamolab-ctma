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
| 3 | HU-13 Registrar geolocalización de las operaciones | Media | 5 | Parcial: falta GPS al solicitar (CA-HU13-03) |
| 4 | HU-01 Consultar equipos disponibles | Alta | 6 | Parcial: faltan filtros y mensaje sin resultados (CA-HU01-03 a 05) |
| 5 | HU-02 Consultar detalle de un equipo | Alta | 6 | Hecho |
| 6 | HU-06 Conservar datos localmente sin conexión | Alta | 6 | Parcial: falta consulta con `@Relation` (CA-HU06-05) |
| 7 | HU-12 Gestionar inventario de equipos (instructor) | Media | 6 | Hecho |
| 8 | HU-11 Gestionar actividades formativas (instructor) | Media | 6 | Hecho |
| 9 | HU-03 Solicitar préstamo | Alta | 7 | Hecho |
| 10 | HU-04 Consultar mis préstamos activos | Alta | 7 | Parcial: la tarjeta no muestra ambiente ni fecha límite (CA-HU04-02) |
| 11 | HU-14 Revisar solicitudes de préstamo (instructor) | Alta | 7 | Hecho |
| 12 | HU-07 Sincronizar datos con servicio remoto | Media/Alta | 8 | Hecho |
| 13 | HU-08 Adjuntar evidencia fotográfica | Media | 8 | Hecho |
| 14 | HU-09 Recibir recordatorio de devolución | Media | 9 | Hecho |

**Avance:** 10 de 14 historias terminadas y 4 parciales; 62 de 74 criterios con prueba automatizada.

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
10. El incremento se demostró en la Sprint Review.

## 6. Eventos: Sprint Review y Retrospectiva

Registro a completar por el equipo en cada sprint (no hay actas de la Parte 2 en el repositorio):

| Sprint | Fecha de la Review | Qué se demostró | Retroalimentación | Mejora acordada en la Retrospectiva |
|---|---|---|---|---|
| 5 | | | | |
| 6 | | | | |
| 7 | | | | |
| 8 | | | | |
| 9 | | | | |

## 7. Parte 1 (histórico)

- **Sprint Goal:** permitir consultar un equipo disponible y registrar una solicitud de préstamo válida, manteniendo
  la disponibilidad coherente y demostrando su calidad con pruebas reproducibles.
- **Backlog:** PB-01 a PB-10 (catálogo, detalle, solicitud con validaciones, disponibilidad, doble pulsación, Mis
  solicitudes, cancelación e ids inexistentes). Su equivalencia con los casos actuales está en
  `docs/backlog/issues/README.md`.
- **Sprint Review:** se demostró en el emulador el flujo del catálogo a la creación y cancelación de préstamos, y
  la corrección de la doble pulsación (`BUG-03`).
- **Retrospectiva:** definir precondiciones y datos de prueba en el Sprint Planning para evitar casos bloqueados
  durante el QA.
