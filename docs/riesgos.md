# Matriz de Riesgos

**Objetivo:** Identificar los principales riesgos funcionales y técnicos del incremento de PréstamoLab CTMA y relacionarlos con estrategias de prueba que permitan su detección.

## Matriz de Riesgos

| ID | Riesgo | Probabilidad | Impacto | Nivel | Cobertura |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **R-01** | Doble pulsación sobre Guardar genera solicitudes duplicadas. | Alta | Alto | Crítico | TC-13 |
| **R-02** | Permite solicitar equipos que ya se encuentran RESERVADOS o PRESTADOS. | Alta | Alto | Crítico | TC-12 |
| **R-03** | Acepta datos fuera de los límites establecidos. | Alta | Medio | Alto | TC-04 a TC-11 |
| **R-04** | Un ID inexistente provoca el cierre de la aplicación. | Media | Alto | Alto | TC-03 |
| **R-05** | El catálogo y las solicitudes muestran información inconsistente. | Media | Alto | Alto | TC-14, TC-15 |
| **R-06** | Permite cancelar solicitudes en estados donde no está permitido. | Media | Medio | Medio | TC-15, TC-16 |

---

## Priorización

*   **Críticos (R-01, R-02):** Son críticos porque afectan directamente la integridad de las reservas y la disponibilidad de los equipos.
*   **Prioridad Alta (R-03, R-04):** Pueden permitir ingresos de datos incorrectos o provocar una experiencia de usuario inestable.
*   **Importante (R-05):** La aplicación debe mantener una representación única y coherente del estado de los recursos.
*   **Prioridad Media (R-06):** Afecta las reglas de transición de estados, aunque su impacto en el flujo principal es menor que otros.
