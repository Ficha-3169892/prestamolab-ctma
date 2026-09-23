# Matriz de Trazabilidad Integrada – PréstamoLab CTMA

Esta matriz vincula los requisitos de la guía integradora con sus Historias de Usuario, Criterios de Aceptación, Riesgos, Código, Pruebas y Resultados.

| Requisito | Historia de Usuario | Criterio de Aceptación | Riesgo Asociado | Implementación (Código) | Caso de Prueba | Commit / PR | Resultado |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| **Catálogo de equipos** | **HU-01:** Ver catálogo | CA-01.1, CA-01.2, CA-01.3 | R01 (Pérdida de señal) | `CatalogoScreen.kt` | CP-01, `ConsultarCatalogoTest` | Commit `feat: catalogo` | **Pasó** |
| **Detalle de equipo** | **HU-02:** Ver detalle | CA-02.1, CA-02.2 | R01 (ID inexistente) | `EquipoDetalleScreen.kt` | `ConsultarDetalleTest` | Commit `feat: detalle` | **Pasó** |
| **Solicitar préstamo** | **HU-03:** Crear solicitud | CA-03.1, CA-03.2, CA-03.3 | R02 (Duplicación) | `SolicitarScreen.kt` | CP-02, `RegistrarSolicitudTest` | Commit `feat: solicitud` | **Pasó** |
| **Validar solicitud** | **HU-04:** Validar campos | CA-04.1 - CA-04.6 | R03 (Datos inválidos) | `SolicitarScreen.kt`, `PrestamoViewModel` | `PrestamoViewModelTest` | Commit `feat: validaciones` | **Pasó** |
| **Mis solicitudes** | **HU-05:** Préstamos activos | CA-07.1, CA-07.2 | R01 (Sin red) | `MisSolicitudesScreen.kt` | `PrestamoViewModelTest` | Commit `feat: solicitudes` | **Pasó** |
| **Detalle de préstamo** | **HU-06:** Ver préstamo | CA-08.1, CA-08.2 | R01 (Sin red) | `SolicitudDetalleScreen.kt` | `PrestamoViewModelTest` | Commit `feat: detalle_solicitud` | **Pasó** |
| **Devolución de equipo** | **HU-07:** Registrar devolución | CA-09.1, CA-09.2, CA-09.3 | R04 (Falta evidencia) | `DevolucionScreen.kt` | CP-04 | Commit `feat: devolucion` | **Pasó** |
| **Evidencia fotográfica** | **HU-08:** Foto cámara/galería | CA-09.1 | R04 (Permiso denegado) | `DevolucionScreen.kt` (TakePicture) | CP-04 | Commit `feat: foto_evidencia` | **Pasó** |
| **Capacidad física GPS** | **HU-09:** Coordenadas GPS | CA-09.1 | R05 (Sin GPS) | `LocationManager.kt` | CP-05 | Commit `feat: gps` | **Pasó** |
| **Autenticación Admin** | **HU-10:** CRUD seguro | CA-10.1, CA-10.2 | R06 (Acceso no autorizado) | `CatalogoScreen.kt`, `PrestamoViewModel` | CP-06, `AdminAuthValidatorTest` | Commit `feat: admin_login` | **Pasó** |
| **Limpieza de historial** | **HU-11:** Borrar devueltos | CA-11.1, CA-11.2 | R07 (Eliminación accidental) | `MisSolicitudesScreen.kt`, `SolicitudDao` | CP-07 | Commit `feat: borrar_historial` | **Pasó** |
