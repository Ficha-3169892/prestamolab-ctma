# Sprint Planning & Evolución de Incrementos - PréstamoLab CTMA

---

## Sprint 1: Funcionalidad Base y Préstamos Offline

### Sprint Goal 1
Permitir consultar equipos disponibles y registrar solicitudes de préstamo válidas con persistencia local y soporte para cancelación.

### PBIs Seleccionados
- **HU-01:** Consultar catálogo. — *Done*
- **HU-02:** Consultar detalle de equipo. — *Done*
- **HU-03:** Registrar solicitud. — *Done*
- **HU-04:** Validar campos de solicitud. — *Done*
- **HU-05:** Controlar disponibilidad de equipos. — *Done*
- **HU-06:** Evitar solicitudes duplicadas. — *Done*
- **HU-07:** Consultar mis solicitudes. — *Done*
- **HU-08:** Consultar detalle de solicitud. — *Done*
- **HU-09:** Cancelar solicitud. — *Done*

---

## Sprint 2: Capacidades de Dispositivo, Supabase, CRUD Admin y Calidad Final

### Sprint Goal 2
Evolucionar la app hacia una arquitectura resiliente *Offline-First* conectada a Supabase, agregando captura de evidencias fotográficas, GPS, gestión protegida de catálogo (CRUD), limpieza de historial y pruebas automatizadas (TDD y No Funcionales).

### PBIs Seleccionados
- **HU-07 (Devolución):** Registrar devolución de equipo con evidencia fotográfica (Cámara/Photo Picker). — *Done*
- **HU-09 (GPS):** Captura de coordenadas GPS en metadatos de entrega. — *Done*
- **HU-10:** Autenticación de Administrador para proteger el acceso al menú de gestión (CRUD). — *Done*
- **HU-11:** Limpieza de historial de préstamos devueltos y cancelados. — *Done*
- **Infraestructura Cloud:** Conexión remota con Supabase PostgREST API y almacenamiento en Supabase Storage. — *Done*
- **Preferencias Local:** Persistencia con `DataStore` para preferencias de usuario. — *Done*
- **Pruebas y Calidad:** Pruebas TDD (`AdminAuthValidatorTest`), pruebas No Funcionales y CI/CD con GitHub Actions. — *Done*

---

## Definition of Done (DoD)

Un elemento del Sprint se considera **Done** cuando:

1. El proyecto compila sin errores ni advertencias bloqueantes (`assembleDebug`).
2. Todos los criterios de aceptación de la Historia de Usuario están implementados y verificados.
3. El ViewModel expone `UiState` de solo lectura y las mutaciones se realizan exclusivamente a través de flujos controlados.
4. Las pruebas unitarias automatizadas (`testDebugUnitTest`) pasan al 100% (33/33 pruebas pasadas).
5. Se incluye evidencia de pruebas no funcionales de accesibilidad, seguridad y rendimiento.
6. Todos los secretos/claves están aislados en `local.properties` y no se exponen en Git.
7. La Matriz de Trazabilidad (`MATRIZ_TRAZABILIDAD.md`) vincula la HU con sus Criterios, Riesgos, Código y Resultado de Pruebas.
8. El pipeline de CI/CD en GitHub Actions ejecuta exitosamente Lint, Tests y Build.
9. Se han creado y publicado los Tags de Git correspondiente a cada hito de versión.

---

## Estado Final del Proyecto
El producto ha alcanzado su versión **v1.0.0-final**, cumpliendo con la totalidad de los requisitos funcionales, técnicos, no funcionales y de gestión exigidos por la guía integradora de aprendizaje.
