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
| **R-07** | Pérdida de sesión o token expirado en Supabase afecta la sincronización Local-First. | Media | Alto | Alto | TC-17 |
| **R-08** | Fallo en la subida de evidencia fotográfica al Bucket de Supabase Storage. | Media | Medio | Alto | TC-18 |
| **R-09** | Acceso no autorizado a rutas de administrador por elevación de privilegios. | Baja | Crítico | Crítico | TC-19 |

---

## Priorización

*   **Críticos (R-01, R-02, R-09):** Afectan la seguridad de roles (admin/usuario), la integridad de reservas y la disponibilidad.
*   **Prioridad Alta (R-03, R-04, R-07, R-08):** Involucran sincronización en la nube, autenticación y manejo de hardware (cámara/GPS).
*   **Importante (R-05):** Mantenimiento de coherencia de estado local-first.
*   **Prioridad Media (R-06):** Reglas de transición de estados.
