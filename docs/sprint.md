# Sprint Planning

## Sprint Goal

Permitir consultar un equipo disponible y registrar una solicitud
de préstamo válida, manteniendo la disponibilidad coherente y
demostrando su calidad mediante pruebas reproducibles.

## PBIs seleccionados

- HU-01 — Consultar catálogo. — Done
- HU-02 — Consultar detalle. — Done
- HU-03 — Registrar solicitud. — Done
- HU-04 — Validar solicitud. — Done
- HU-05 — Controlar disponibilidad. — Done
- HU-06 — Evitar duplicados. — Done
- HU-07 — Consultar mis solicitudes. — Done
- HU-08 — Consultar detalle de solicitud. — Done
- HU-09 — Cancelar solicitud. — Done

## Sprint Backlog

1. Crear modelos Equipo y SolicitudPrestamo. — Done
2. Crear estados y categorías. — Done
3. Implementar InMemoryPrestamoRepository. — Done
4. Implementar catálogo de equipos. — Done
5. Implementar detalle de equipo. — Done
6. Implementar navegación mediante equipoId. — Done
7. Implementar formulario de solicitud. — Done
8. Implementar validación de destino. — Done
9. Implementar validación de propósito. — Done
10. Implementar validación de duración. — Done
11. Validar disponibilidad del equipo. — Done
12. Crear solicitud en estado SOLICITADA. — Done
13. Cambiar equipo a RESERVADO. — Done
14. Evitar doble pulsación de Guardar. — Done
15. Implementar Mis Solicitudes. — Done
16. Implementar detalle de solicitud. — Done
17. Implementar cancelación. — Done
18. Manejar IDs inexistentes. — Done
19. Diseñar y ejecutar pruebas. — Done
20. Corregir defectos encontrados. — Done
21. Actualizar documentación. — Done

## Impedimentos iniciales

No se identifican impedimentos bloqueantes al inicio del Sprint.
El desarrollo y las pruebas se realizaron utilizando Android Studio
y un emulador Android.

## Definition of Done

Un elemento del Sprint se considera Done cuando:

1. El proyecto compila y puede ejecutarse en el ambiente definido.
2. Los criterios de aceptación seleccionados están implementados.
3. La UI no modifica directamente la fuente de datos.
4. El ViewModel expone UiState/StateFlow de solo lectura.
5. La navegación transporta identificadores y controla IDs inexistentes.
6. Los casos de prueba acordados fueron ejecutados y sus resultados
   corresponden con ejecuciones reales.
7. Los defectos críticos y altos tienen una decisión explícita.
8. Las correcciones relevantes tienen confirmación y regresión.
9. Git y README están actualizados.
10. El incremento puede demostrarse y cada integrante puede explicar
    las decisiones técnicas y de calidad relacionadas con su trabajo.

## Estado final del Sprint

El Sprint Goal fue alcanzado.

Las funcionalidades seleccionadas fueron implementadas y verificadas
mediante pruebas funcionales. Durante las pruebas se identificó y
corrigió un defecto menor de navegación relacionado con el regreso
desde la pantalla de Mis Solicitudes.

El incremento final permite consultar equipos, consultar detalles,
registrar solicitudes válidas, controlar la disponibilidad, evitar
duplicados, consultar solicitudes, cancelar solicitudes permitidas y
mantener coherencia entre el estado de las solicitudes y los equipos.
