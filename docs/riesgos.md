# Matriz de Riesgos 
## Objetivo
Identificar los principales riesgos funcionales y técnicos del
incremento PréstamoLab CTMA y relacionarlos con estrategias de
prueba que permitan detectarlos.

## Matriz
| ID | Riesgo | Probabilidad | Impacto | Nivel | Cobertura |
|---|---|---|---|---|---|
| R-01 | Dos pulsaciones de Guardar generan solicitudes duplicadas. | Alta | Alto | Crítico | TC-13 |
| R-02 | Se permite solicitar un equipo RESERVADO o PRESTADO. | Alta | Alto | Crítico | TC-12 |
| R-03 | Se aceptan datos fuera de los límites establecidos. | Alta | Medio | Alto | TC-04 a TC-11 |
| R-04 | Un ID inexistente provoca cierre de la aplicación. | Media | Alto | Alto | TC-03 |
| R-05 | Catálogo y solicitudes muestran información inconsistente. | Media | Alto | Alto | TC-14, TC-15 |
| R-06 | Se permite cancelar solicitudes en estados no permitidos. | Media | Medio | Medio | TC-15, TC-16 |

## Priorización
Los riesgos R-01 y R-02 son críticos porque afectan directamente
la integridad de las reservas y la disponibilidad de los equipos.
R-03 y R-04 tienen prioridad alta porque pueden permitir datos
incorrectos o provocar una experiencia de usuario inestable.
R-05 es importante porque la aplicación debe mantener una única
representación coherente del estado de los recursos.
R-06 tiene prioridad media porque afecta las reglas de transición
de estados, aunque su impacto sobre el flujo principal es menor.
