# Matriz de Trazabilidad – PréstamoLab CTMA

Esta matriz vincula los requisitos de la guía con la implementación y las pruebas.

| Requisito | Historia de Usuario | Implementación | Caso de Prueba |
| :--- | :--- | :--- | :--- |
| Catálogo de equipos | HU-01: Ver catálogo | `CatalogoScreen.kt` | CP-01 |
| Detalle de equipo | HU-02: Ver detalle | `EquipoDetalleScreen.kt` | - |
| Solicitar préstamo | HU-03: Crear solicitud | `SolicitarScreen.kt` | CP-02 |
| Préstamos activos | HU-04: Mis solicitudes | `MisSolicitudesScreen.kt` | - |
| Devoluciones | HU-05: Registrar devolución | `DevolucionScreen.kt` | CP-04 |
| Evidencia (Cámara) | HU-06: Adjuntar foto | `DevolucionScreen.kt` | CP-04 |
| Sin conexión | HU-07: Modo offline | `RoomPrestamoRepository.kt` | CP-01, CP-02 |
| Sincronización REST | HU-08: Sincronización | `SyncWorker.kt` | CP-03 |
| GPS (Capacidad Física) | HU-09: Ubicación entrega | `LocationManager.kt` | CP-05 |
