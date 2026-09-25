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
- **Seguridad en el servidor (Sprint 9):** contraseñas con bcrypt, sesiones con token, RLS por rol y reglas del
  negocio en triggers (`docs/supabase/009_seguridad.sql`), verificadas con 26 ataques contra la base real.

## 2. Métricas de ejecución de QA

| Métrica | Resultado |
|---|---|
| Historias del backlog | 14 |
| Historias terminadas | 14 de 14 |
| Criterios de aceptación y casos de prueba (1:1) | 74 |
| Criterios con prueba automatizada | 74 (100 %) |
| Pruebas unitarias | 187, todas en verde (local y en GitHub Actions) |
| Pruebas instrumentadas y de UI en el dispositivo | 154, todas en verde (TC-HU13-01 se ejecuta aparte) |
| Pruebas de seguridad contra Supabase real | 26 de 26 superadas (`docs/seguridad/`) |
| Verificación manual contra Supabase real | Login, sincronización, devolución con GPS, evidencia con foto y el ciclo completo solicitar → aprobar con la seguridad activa |
| Defectos registrados en la Parte 2 | 11 (BUG-04 a BUG-14), todos cerrados |
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
| 10. Demostración registrada en la revisión del sprint | Cumple (actas por evidencia en `SCRUM.md`, sección 6) |

## 5. Riesgo residual

Los riesgos R-01 a R-05 (contraseñas, lectura de usuarios, sesión y escritura abierta) quedaron **mitigados y
verificados** con `docs/supabase/009_seguridad.sql`. Quedan aceptados, con su tratamiento posible en
`docs/RIESGOS.md`:

- **R-06:** la subida al bucket de evidencias sigue abierta, porque Storage no recibe el token de sesión.
- **R-07:** un estudiante con la anon key podría cambiar el estado de un equipo por fuera de la app.
- **R-08 / R-09:** la anon key va dentro del APK y el login no bloquea tras varios intentos (solo los demora).
- **OWASP ZAP:** no se escaneó activamente la infraestructura compartida de Supabase; el procedimiento en modo
  pasivo está en `docs/seguridad/README.md`.
- **Dependencia del dispositivo:** las pruebas de UI se ejecutan en un solo teléfono; el CI solo corre las
  unitarias.

## 6. Recomendación y dictamen

**Dictamen: DONE / ACEPTABLE.** Las 14 historias están completas, los 74 criterios tienen prueba automatizada,
la seguridad del servidor está verificada contra la base real y no hay defectos graves abiertos. Pendientes antes
de la entrega: que el equipo confirme las mejoras propuestas en las retrospectivas (`SCRUM.md`) y, si la guía lo
exige, la revisión pasiva con OWASP ZAP.
