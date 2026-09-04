# Artefactos y Gestión Ágil - Scrum (PréstamoLab CTMA)

## 1. Product Goal & Sprint Goal

* **Product Goal:** Mejorar la trazabilidad, consulta y control de préstamos de recursos de formación mediante una experiencia móvil confiable y accesible para aprendices e instructores del CTMA.
* **Sprint Goal:** Permitir consultar un equipo disponible y registrar una solicitud de préstamo válida, manteniendo la disponibilidad coherente y demostrando su calidad mediante pruebas reproducibles.

---

## 2. Product Backlog Ordenado (PBI)

| ID | Historia / Necesidad | Prioridad | Riesgo | Criterio de Aceptación Clave |
| :--- | :--- | :--- | :--- | :--- |
| **PB-01** | Consultar catálogo de equipos y disponibilidad | Alta | Alto | Muestra lista actualizada con estados `DISPONIBLE`, `RESERVADO` o `PRESTADO`. |
| **PB-02** | Consultar detalle de un equipo | Alta | Medio | Navega mediante `equipoId` y recupera el detalle correcto. |
| **PB-03** | Registrar solicitud de préstamo | Alta | Alto | Permite crear solicitud solo si el equipo está `DISPONIBLE`. |
| **PB-04** | Validar propósito, destino y duración | Alta | Alto | Ambiente obligatorio; Propósito (10-180 chars); Duración (1-8 hrs). |
| **PB-05** | Evitar solicitud sobre equipo no disponible | Alta | Alto | Rechaza intentos de solicitud en equipos `RESERVADO` o `PRESTADO`. |
| **PB-06** | Evitar duplicación por doble pulsación | Alta | Alto | Múltiples clics rápidos en Guardar generan **una sola** solicitud. |
| **PB-07** | Consultar "Mis solicitudes" | Media | Medio | Lista todas las solicitudes activas e históricas del usuario. |
| **PB-08** | Consultar detalle de solicitud | Media | Medio | Muestra información del ambiente, propósito, duración y estado actual. |
| **PB-09** | Cancelar solicitud en estado `SOLICITADA` | Media | Medio | Cambia estado a `CANCELADA` y libera el equipo a `DISPONIBLE`. |
| **PB-10** | Manejo de IDs inexistentes | Media | Medio | ID inválido muestra interfaz de error o estado recuperable sin `crash`. |

---

## 3. Definition of Done (DoD)
1. El proyecto compila sin errores en Android Studio.
2. Los criterios de aceptación de las historias están implementados.
3. La UI (Compose) no modifica directamente la fuente de datos.
4. ViewModel expone `UiState` / `StateFlow` de solo lectura.
5. La navegación transporta IDs y controla escenarios con IDs inexistentes.
6. Suite de pruebas ejecutada con datos sintéticos.
7. Los defectos de severidad Alta/Crítica han sido corregidos y verificados.
8. Se ejecutaron pruebas de confirmación y regresión tras cada corrección.
9. Repositorio Git limpio, actualizado y libre de credenciales.
10. El incremento fue demostrado en la Sprint Review.

---

## 4. Inspección y Adaptación (Review & Retrospective)

* **Sprint Review:** Se demostró la aplicación en emulador navegando desde catálogo hasta la creación y cancelación de préstamos. Se validó la corrección del bloqueo de doble pulsación (`BUG-03`).
* **Sprint Retrospective (Mejora Concreta):** Para el siguiente ciclo, se estructurarán las precondiciones y datos de prueba en la etapa de Sprint Planning para evitar casos bloqueados (`BLOCKED`) durante la ejecución del QA.