# Matriz de Riesgos - PréstamoLab CTMA

## Objetivo
Identificar los principales riesgos funcionales y técnicos del incremento PréstamoLab CTMA y relacionarlos con estrategias de prueba y mitigación.

## Matriz de Riesgos
| ID | Riesgo | Probabilidad | Impacto | Nivel | Cobertura / Mitigación |
|---|---|---|---|---|---|
| **R-01** | Dos pulsaciones de Guardar generan solicitudes duplicadas. | Alta | Alto | Crítico | Validación de estado `guardando` en `SolicitarScreen`. |
| **R-02** | Se permite solicitar un equipo RESERVADO o PRESTADO. | Alta | Alto | Crítico | Validación de disponibilidad previa en `InMemory` / `Room`. |
| **R-03** | Se aceptan datos fuera de los límites establecidos. | Alta | Medio | Alto | Validación de longitud y rango en `SolicitarScreen`. |
| **R-04** | Un ID inexistente provoca cierre de la aplicación. | Media | Alto | Alto | Control de nulos en `EquipoDetalleScreen` y `SolicitudDetalleScreen`. |
| **R-05** | Catálogo y solicitudes muestran información inconsistente. | Media | Alto | Alto | Fuente única de verdad local mediante Room y StateFlow. |
| **R-06** | Se permite cancelar solicitudes en estados no permitidos. | Media | Medio | Medio | Restricción de cancelación únicamente para estado `SOLICITADA`. |
| **R-07** | Acceso no autorizado a la modificación del catálogo (CRUD). | Media | Alto | Alto | Diálogo de inicio de sesión de Administrador (`HU-10`). |
| **R-08** | Pérdida de conexión durante sincronización con Supabase. | Alta | Medio | Alto | Estrategia *Offline-First* con Room, `NetworkMonitor` y `SyncWorker`. |

## Priorización
- **Críticos (R-01, R-02):** Afectan directamente la integridad de las reservas y la disponibilidad de equipos.
- **Altos (R-03, R-04, R-05, R-07, R-08):** Afectan la estabilidad, seguridad del CRUD y resiliencia ante cortes de red.
- **Medios (R-06):** Reglas de transición de estado en operaciones de cancelación.
