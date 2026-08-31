# Sprint Planning 
## Sprint Goal
Permitir consultar un equipo disponible y registrar una solicitud
de préstamo válida, manteniendo la disponibilidad coherente y
demostrando su calidad mediante pruebas reproducibles.

## PBIs seleccionados
- HU-01 — Consultar catálogo.
- HU-02 — Consultar detalle.
- HU-03 — Registrar solicitud.
- HU-04 — Validar solicitud.
- HU-05 — Controlar disponibilidad.
- HU-06 — Evitar duplicados.
- HU-07 — Consultar mis solicitudes.
- HU-09 — Cancelar solicitud.

## Sprint Backlog
1. Crear modelos Equipo y SolicitudPrestamo.
2. Crear estados y categorías.
3. Implementar InMemoryPrestamoRepository.
4. Implementar catálogo de equipos.
5. Implementar detalle de equipo.
6. Implementar navegación mediante equipoId.
7. Implementar formulario de solicitud.
8. Implementar validación de destino.
9. Implementar validación de propósito.
10. Implementar validación de duración.
11. Validar disponibilidad del equipo.
12. Crear solicitud en estado SOLICITADA.
13. Cambiar equipo a RESERVADO.
14. Evitar doble pulsación de Guardar.
15. Implementar Mis Solicitudes.
16. Implementar detalle de solicitud.
17. Implementar cancelación.
18. Manejar IDs inexistentes.
19. Diseñar y ejecutar pruebas.
20. Corregir defectos encontrados.
21. Actualizar documentación.

## Impedimentos iniciales
No se identifican impedimentos bloqueantes al inicio del Sprint.
El desarrollo y las pruebas se realizarán utilizando Android Studio
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
