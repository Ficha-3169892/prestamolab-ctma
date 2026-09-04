# Informe Ejecutivo de Calidad - PréstamoLab CTMA

**Proyecto:** PréstamoLab CTMA  
**Build Evaluada:** 0.1.0  
**Fecha de Evaluación:** Septiembre de 2026  
**Programa:** Análisis y Desarrollo de Software (ADSO) - CTMA  
**Caso Integrador:** Aplicación móvil educativa para préstamos de equipos de laboratorio

---

## 1. Alcance Construido y Probado
Se diseñó, construyó y evaluó el incremento funcional correspondiente al MVP de PréstamoLab CTMA[cite: 2]:
* **Incluido en el incremento:** Consulta de catálogo con disponibilidad síncrona[cite: 2], navegación por ID[cite: 2], registro de solicitudes con validación estricta de reglas[cite: 2], lista "Mis solicitudes"[cite: 2], flujo de cancelación en estado `SOLICITADA`[cite: 2], control de doble guardado[cite: 2], manejo de IDs inexistentes[cite: 2] y soporte de accesibilidad con fuentes aumentadas[cite: 2].
* **Excluido del incremento:** Autenticación real de usuarios, integración con backend/API externa y persistencia en base de datos física (se utilizó un repositorio simulado en memoria)[cite: 2].

---

## 2. Métricas de Ejecución de QA

| Métrica | Resultado |
| :--- | :--- |
| **Historias de Usuario (PBIs) Seleccionadas** | 7[cite: 2] |
| **Historias de Usuario Cumplidas (Done)** | 7[cite: 2] |
| **Casos de Prueba Planificados** | 18[cite: 2] |
| **Casos de Prueba Ejecutados** | 18[cite: 2] |
| **Resultados PASS / FAIL / BLOCKED** | 18 PASS / 0 FAIL / 0 BLOCKED (Post-correcciones)[cite: 2] |
| **Defectos Críticos / Altos Abiertos** | 0[cite: 2] |

---

## 3. Principales Defectos y Gestión de Calidad

* **BUG-03 (Severidad Alta / Prioridad Alta):** La doble pulsación rápida en "Guardar" creaba dos solicitudes activas para el mismo equipo[cite: 2].
    * **Acción:** Se implementó bloqueo en la UI (`guardando = true`) e idempotencia en el ViewModel[cite: 2].
    * **Verificación:** Caso `TC-13` re-ejecutado con resultado **PASS**, seguido de pruebas de regresión en catálogo y cancelación (`TC-01`, `TC-14`, `TC-15`)[cite: 2].

---

## 4. Evaluacion de Definition of Done (DoD)

Se verificó el cumplimiento de los **10 criterios** de la Definition of Done del proyecto[cite: 2]:
1. [x] Compilación limpia en el ambiente configurado[cite: 2].
2. [x] Criterios de aceptación verificados y aprobados[cite: 2].
3. [x] UI Compose desacoplada de la fuente de datos directa[cite: 2].
4. [x] ViewModel expone `UiState` / `StateFlow` de solo lectura[cite: 2].
5. [x] Navegación por IDs con control de IDs inexistentes[cite: 2].
6. [x] Suite de pruebas ejecutada con datos sintéticos[cite: 2].
7. [x] Defectos de severidad Alta/Crítica corregidos[cite: 2].
8. [x] Pruebas de confirmación y regresión documentadas[cite: 2].
9. [x] Repositorio Git estructurado y limpio[cite: 2].
10. [x] Incremento demostrable y sustentable[cite: 2].

---

## 5. Riesgo Residual y Limites del Incremneto
* **Riesgo Residual:** Al ser un repositorio en memoria (`InMemoryRepository`), los datos se reinician al cerrar la app[cite: 2]. Las pruebas de concurrencia de red y persistencia real deberán cubrirse en el siguiente incremento[cite: 2].

---

## 6. Recomendacion y Dictamen
**Dictamen:** **DONE / ACEPTABLE**[cite: 2]  
El incremento cumple con el Sprint Goal, todas las reglas de negocio críticas están validadas y los defectos de alta prioridad fueron resueltos[cite: 2]. Se recomienda el pase a sustentación individual e integración final del paquete de entregables[cite: 2].