# Resumen del Sprint

## Sprint Goal
Permitir la consulta de equipos disponibles y el registro de solicitudes de préstamo válidas, manteniendo la coherencia en la disponibilidad y demostrando la calidad mediante pruebas reproducibles.

## PBIs Seleccionados
Todos los elementos seleccionados están **Terminados**:
*   **HU-01:** Consultar catálogo.
*   **HU-02:** Consultar detalle.
*   **HU-03:** Registrar solicitud.
*   **HU-04:** Validar solicitud.
*   **HU-05:** Controlar disponibilidad.
*   **HU-06:** Evitar duplicados.
*   **HU-07:** Consultar mis solicitudes.
*   **HU-08:** Consultar detalle de solicitud.
*   **HU-09:** Cancelar solicitud.

## Sprint Backlog (Tareas)
Todas las tareas marcadas como **Terminadas**, incluyendo:
*   Creación de modelos (Equipo y SolicitudPrestamo) y repositorios.
*   Implementación de UI (Catálogo, Detalle, Formulario de Solicitud).
*   Implementación de validaciones (Destino, Propósito, Duración, Disponibilidad).
*   Gestión de estados (SOLICITADA/RESERVADO).
*   Manejo de navegación, IDs y estado de la UI (ViewModel/StateFlow).
*   Diseño y ejecución de pruebas, corrección de defectos y actualización de documentación.

## Impedimentos iniciales
*   No se identificaron impedimentos bloqueantes al inicio.
*   El desarrollo y las pruebas se realizaron con Android Studio y emulador Android.

## Definition of Done (DoD)
Un ítem se considera "Terminado" cuando:
*   El proyecto compila y corre en el entorno definido.
*   Los criterios de aceptación están implementados.
*   La UI no modifica fuentes de datos directamente; los ViewModels exponen `UiState`/`StateFlow` de solo lectura.
*   La navegación maneja identificadores e IDs inexistentes.
*   Los casos de prueba se ejecutan con resultados coincidentes con la ejecución real.
*   Defectos críticos/altos tienen una decisión explícita; las correcciones tienen confirmación y regresión.
*   Git y README están actualizados.
*   El incremento puede ser demostrado y explicado por cada integrante.

## Estado Final del Sprint
*   **Meta alcanzada:** Todas las funcionalidades fueron implementadas y verificadas.
*   **Correcciones:** Se identificó y corrigió un defecto menor de navegación (retorno desde la pantalla Mis Solicitudes).
*   **Resultado:** El incremento final permite el ciclo completo de consulta de equipos, registro/cancelación de solicitudes y mantenimiento de coherencia de estados.
