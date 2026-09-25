# Resumen del Sprint - Versión v0.3.0 (Semana 6)

## Sprint Goal (Semana 6)
Reemplazar el almacenamiento temporal en memoria por persistencia local robusta con Room (`EquipoEntity`, `SolicitudEntity`, DAOs) y DataStore (`UserPreferencesRepository`), estableciendo la base de datos local como la fuente canónica de verdad.

## PBIs Seleccionados (Versión v0.3.0)
*   **HU-01:** Consultar catálogo (desde Room).
*   **HU-02:** Consultar detalle (desde Room).
*   **HU-03:** Registrar solicitud (persistencia en Room).
*   **HU-09:** Cancelar solicitud (persistencia y actualización de estado en Room).

## Sprint Backlog (Tareas - Semana 6)
*   [x] Configuración de KSP y dependencias de Room y DataStore.
*   [x] Creación de entidades (`EquipoEntity`, `SolicitudEntity`) y mapeadores de dominio.
*   [x] Creación de DAOs (`EquipoDao`, `SolicitudDao`) y base de datos local (`PrestamoDatabase`) con precarga de datos iniciales.
*   [x] Implementación de DataStore para preferencias de usuario (`UserPreferencesRepository`).
*   [x] Implementación de `RoomPrestamoRepository` y conexión con el `ViewModel` mediante factory.
*   [x] Actualización de la Definition of Done (DoD) para incluir persistencia local.

## Definition of Done (DoD) - Actualizada v0.3.0
*   El proyecto compila con Room y KSP sin errores.
*   Los datos de equipos y solicitudes persisten en la base de datos local Room.
*   Las preferencias de usuario se gestionan mediante DataStore.
*   Las pruebas unitarias y de integración pasan correctamente.
