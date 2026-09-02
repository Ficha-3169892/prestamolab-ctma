
# Suite de Pruebas y Trazabilidad - PréstamoLab CTMA

## Casos de Prueba

| ID | Escenario | Resultado esperado | Técnica |
| --- | --- | --- | --- |
| TC-01 | Catálogo con datos | Equipos visibles con disponibilidad. | Caso de uso |
| TC-02 | EquipoId válido | Detalle corresponde al equipo seleccionado. | Caso de uso |
| TC-03 | EquipoId inexistente | Estado recuperable; sin cierre abrupto. | Negativa |
| TC-04 | Propósito 9 caracteres | No guarda; mensaje específico. | Límite |
| TC-05 | Propósito 10 caracteres | Puede guardar si demás datos son válidos. | Límite |
| TC-06 | Propósito 180 caracteres | Puede guardar. | Límite |
| TC-07 | Propósito 181 caracteres | No guarda. | Límite |
| TC-08 | Duración 0 horas | No guarda. | Límite |
| TC-09 | Duración 1 hora | Válida. | Límite |
| TC-10 | Duración 8 horas | Válida. | Límite |
| TC-11 | Duración 9 horas | No guarda. | Límite |
| TC-12 | Equipo no disponible | Solicitud rechazada. | Decisión |
| TC-13 | Doble pulsación Guardar | Una sola solicitud. | Riesgo |
| TC-14 | Crear solicitud válida | SOLICITADA + equipo RESERVADO. | Caso de uso |
| TC-15 | Cancelar SOLICITADA | CANCELADA y disponibilidad coherente. | Transición |
| TC-16 | Cancelar CANCELADA | Acción no disponible / sin cambio. | Transición |
| TC-17 | Volver desde detalle/formulario | Back stack correcto. | Navegación |
| TC-18 | Fuente 1.5× y texto largo | Contenido y acción esenciales utilizables. | Accesibilidad |

## Matriz de Trazabilidad

| Historia | Criterio | Riesgo | Caso | Estado |
| --- | --- | --- | --- | --- |
| HU-01 Consultar catálogo | CA-01.1 Lista equipos | R-04 Estado no reflejado | TC-01 | PASS |
| HU-02 Detalle equipo | CA-02.1 Datos equipo | R-03 ID inexistente | TC-02, TC-03 | PASS |
| HU-03 Solicitar préstamo | CA-03.1 Registro válido | R-01 Reserva doble | TC-14 | PASS |
| HU-03 Solicitar préstamo | CA-03.2 Una sola solicitud | R-01 Duplicación | TC-13 | PASS |
| HU-03 Solicitar préstamo | CA-03.3 Solo disponible | R-01 Reserva doble | TC-12 | PASS |
| HU-04 Validar campos | CA-04.1 Reglas propósito/duración | R-02 Datos fuera de rango | TC-04 a TC-11 | PASS |
| HU-09 Cancelar solicitud | CA-09.1 Estado SOLICITADA | R-06 Estado inconsistente | TC-15, TC-16 | PASS |

---

