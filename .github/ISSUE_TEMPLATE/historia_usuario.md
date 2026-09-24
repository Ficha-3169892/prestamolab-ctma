---
name: Historia de usuario
about: HU con criterios de aceptación y casos de prueba 1:1
title: "[HU-XX] "
labels: historia-usuario
---

**Prioridad:** Alta / Media / Baja · **Sprint:** N

## Historia de usuario

**Como** <rol>, **quiero** <funcionalidad>, **para** <beneficio>.

## Criterios de aceptación

- [ ] **CA-HUXX-01:** **Dado** que <contexto>, **cuando** <acción>, **entonces** <resultado verificable>.

## Casos de prueba

Cada caso verifica el criterio con el mismo número (trazabilidad 1:1).

| Caso | Criterio | Tipo | Pasos / datos | Resultado esperado | Prueba automatizada |
|---|---|---|---|---|---|
| TC-HUXX-01 | CA-HUXX-01 | Unitaria / Integración / UI | | | Pendiente |

## Definición de terminado

- [ ] Todos los criterios implementados y sus casos TC en verde.
- [ ] La UI consume solo `StateFlow` del ViewModel; el ViewModel usa solo el Repository.
- [ ] Defectos encontrados registrados como Issues `BUG-XX` con prueba de confirmación y regresión.
