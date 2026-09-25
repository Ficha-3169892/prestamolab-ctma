# Informe ejecutivo de calidad - PréstamoLab CTMA (Parte 2)

**Proyecto:** PréstamoLab CTMA
**Versión evaluada:** 1.0 (rama `andres-vargas`)
**Periodo:** semanas 5 a 9, septiembre de 2026
**Programa:** Análisis y Desarrollo de Software (ADSO), CTMA
**Dispositivo de prueba:** Xiaomi 25078RA3EL, Android 15 (HyperOS), contra el proyecto Supabase real

---

## 1. Alcance construido y probado

- **Incluido:** inicio de sesión contra la tabla `users` con SHA-256 y control por rol (INSTRUCTOR / ESTUDIANTE);
  catálogo y detalle; solicitud, aprobación o rechazo, cancelación y devolución de préstamos; devolución con GPS
  (Fused Location Provider); evidencia fotográfica con cámara, FileProvider y Supabase Storage; inventario de equipos
  y actividades formativas del instructor; recordatorio de devolución con WorkManager y notificaciones;
  persistencia local con Room (v8, migraciones probadas) y sesión en DataStore; filtros del catálogo conservados en
  DataStore; sincronización offline-first con Supabase; GPS en solicitudes, devoluciones y evidencias.
- **Fuera de alcance de esta versión:** Supabase Auth y políticas de seguridad por rol en el servidor (ver sección 5).

## 2. Métricas de ejecución de QA

| Métrica | Resultado |
|---|---|
| Historias del backlog | 14 |
| Historias terminadas | 14 de 14 |
| Criterios de aceptación y casos de prueba (1:1) | 74 |
| Criterios con prueba automatizada | 74 (100 %) |
| Pruebas unitarias | 177, todas en verde (local y en GitHub Actions) |
| Pruebas instrumentadas y de UI en el dispositivo | 153, todas en verde (TC-HU13-01 se ejecuta aparte) |
| Verificación manual contra Supabase real | Login, sincronización, devolución con GPS, revisión de una solicitud y evidencia con foto |
| Defectos registrados en la Parte 2 | 9 (BUG-04 a BUG-12), todos cerrados |
| Defectos Críticos o Altos abiertos | 0 |

## 3. Principales defectos y gestión de calidad

Detalle, causa raíz y pruebas de confirmación en `DEFECTOS.md`.

- **BUG-05 (Crítica):** el login contra Supabase fallaba siempre por falta del permiso INTERNET. Las pruebas no lo
  detectaron porque usan dobles del servidor; se encontró en la prueba manual en el dispositivo.
- **BUG-06 a BUG-09 (Altas):** diferencias entre el esquema local y el real de Supabase (columnas NOT NULL), pérdida
  silenciosa de cambios rechazados y cierre de la app por una devolución duplicada. Todas con prueba de confirmación
  automatizada.
- **Lección aprendida:** las pruebas con dobles no sustituyen la verificación contra el servidor real; desde entonces
  cada historia que toca Supabase se valida también con consultas de solo lectura al proyecto real.

## 4. Evaluación de la Definition of Done

| Criterio (ver `SCRUM.md`) | Estado |
|---|---|
| 1. Compila y CI en verde | Cumple |
| 2. Criterios implementados y registrados en la matriz | Cumple (74 de 74) |
| 3. UI → ViewModel (`StateFlow`) → Repository | Cumple |
| 4. Room como fuente de verdad, migraciones probadas | Cumple |
| 5. Sincronización sin duplicados | Cumple |
| 6. Permisos en el momento de uso, sin bloquear la app | Cumple |
| 7. Suites unitaria e instrumentada en verde | Cumple |
| 8. Defectos Altos o Críticos corregidos con confirmación y regresión | Cumple |
| 9. Repositorio sin credenciales, cambios de BD versionados | Cumple |
| 10. Demostración en la Sprint Review | Pendiente de registrar |

## 5. Riesgo residual

Detalle y plan de verificación en `docs/RIESGOS.md`:

- **Seguridad del servidor (R-01 a R-05):** la anon key va dentro del APK y las políticas RLS permiten leer y
  escribir a cualquiera que la tenga; el control por rol solo existe en la app. El SHA-256 sin sal permite
  autenticarse con el hash. Las fotos de evidencia son públicas con solo conocer su URL.
- **Pruebas de seguridad del Sprint 9 (OWASP ZAP / MASVS):** planificadas, no ejecutadas.
- **Dependencia del dispositivo:** las pruebas de UI se ejecutan en un solo teléfono; el CI solo corre las
  unitarias.

## 6. Recomendación y dictamen

**Dictamen: ACEPTABLE CON OBSERVACIONES.** La funcionalidad principal está completa, probada en el dispositivo y
verificada contra Supabase, sin defectos graves abiertos, y los 74 criterios tienen prueba automatizada. Antes de la
entrega final se recomienda: cerrar los riesgos de seguridad del servidor (RLS por rol y login mediante una función que
valide la contraseña), ejecutar la prueba con OWASP ZAP y registrar las Sprint Reviews.
