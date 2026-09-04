# Plan de Pruebas, Matriz de Riesgos y Suite QA (PréstamoLab CTMA)

## 1. Matriz de Riesgos de Calidad

| ID | Riesgo | Prob. | Impacto | Nivel | Cobertura / Estrategia |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **R-01** | Dos solicitudes reservan el mismo equipo simultáneamente | Alta | Alta | **Crítico** | `TC-12`, `TC-13` (Doble pulsación) |
| **R-02** | Formulario acepta valores fuera de rango o vacíos | Alta | Media | **Alto** | `TC-04` al `TC-11` (Equivalencia y Límites) |
| **R-03** | ID inexistente provoca cierre abrupto de la app (`crash`) | Media | Alta | **Alto** | `TC-03` (Navegación negativa) |
| **R-04** | Cancelación no sincroniza disponibilidad del equipo | Media | Alta | **Alto** | `TC-15`, `TC-16` (Transición de estados) |

---

## 2. Suite de Casos de Prueba (18 Casos)

| ID | Escenario de Prueba | Entrada / Precondición | Resultado Esperado | Técnica Aplicada |
| :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Cargar catálogo inicial | Inicio de la App | Muestra equipos con su disponibilidad correcta. | Caso de Uso |
| **TC-02** | Ver detalle de equipo válido | `equipoId = 1` | Carga el nombre y especificaciones de ese equipo. | Caso de Uso |
| **TC-03** | Ver detalle de `equipoId` inexistente | `equipoId = 999` | Muestra pantalla de error / recuperable sin crash. | Negativa |
| **TC-04** | Propósito con 9 caracteres | Texto de 9 chars | Error: "Propósito debe tener mínimo 10 caracteres". | Valor Límite |
| **TC-05** | Propósito con 10 caracteres | Texto de 10 chars | Formulario válido (pasa regla). | Valor Límite |
| **TC-06** | Propósito con 180 caracteres | Texto de 180 chars | Formulario válido (pasa regla). | Valor Límite |
| **TC-07** | Propósito con 181 caracteres | Texto de 181 chars | Error: "Máximo 180 caracteres". | Valor Límite |
| **TC-08** | Duración 0 horas | `duracion = 0` | Error: "Duración entre 1 y 8 horas". | Valor Límite |
| **TC-09** | Duración 1 hora | `duracion = 1` | Válido. | Valor Límite |
| **TC-10** | Duración 8 horas | `duracion = 8` | Válido. | Valor Límite |
| **TC-11** | Duración 9 horas | `duracion = 9` | Error: "Duración entre 1 y 8 horas". | Valor Límite |
| **TC-12** | Intentar solicitar equipo `RESERVADO` | Equipo `RESERVADO` | Botón deshabilitado o mensaje de rechazo. | Tabla Decisión |
| **TC-13** | Doble pulsación en "Guardar" | Clics rápidos | **Crea una sola solicitud**; equipo `RESERVADO`. | Riesgo / Estrés |
| **TC-14** | Crear solicitud válida completa | Datos válidos | Solicitud `SOLICITADA` y equipo pasa a `RESERVADO`. | Caso de Uso |
| **TC-15** | Cancelar solicitud `SOLICITADA` | Solicitud activa | Pasa a `CANCELADA` y el equipo vuelve a `DISPONIBLE`. | Transición Estado |
| **TC-16** | Re-cancelar solicitud `CANCELADA` | Solicitud cancelada | Opción deshabilitada / sin cambio de estado. | Transición Estado |
| **TC-17** | Navegación Back stack | Pulsar Atrás | Regresa a la pantalla anterior manteniendo el estado. | Navegación |
| **TC-18** | Fuente aumentada 1.5× | Ajuste accesibilidad | UI legible sin solapamientos ni botones truncados. | Accesibilidad |

---

## 3. Matriz de Trazabilidad

| Historia (PBI) | Criterio de Aceptación | Riesgo | Caso de Prueba | Resultado | Defecto Asociado |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **PB-01** Catálogo | Lista inicial dinámica | R-04 | `TC-01` | **PASS** | - |
| **PB-02 / PB-10** Detalle | Navegación e ID inexistente | R-03 | `TC-02`, `TC-03` | **PASS** | - |
| **PB-04** Validaciones | Formulario de 10-180 chars / 1-8 hrs | R-02 | `TC-04` a `TC-11` | **PASS** | - |
| **PB-05 / PB-06** Solicitud | Evitar duplicados y reservas dobles | R-01 | `TC-12`, `TC-13` | **PASS** | `BUG-03` (Corregido) |
| **PB-09** Cancelar | Transición y retorno a Disponible | R-04 | `TC-15`, `TC-16` | **PASS** | - |

---

## 4. Bitácora de Ejecución QA

| Ejecución ID | Caso | Build | Resultado | Evidencia Registrada | Observaciones |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **EX-001** | `TC-13` | 0.1.0 | **FAIL** | `evidencias/TC13_fail.mp4` | Generaba 2 solicitudes (`BUG-03`). |
| **EX-002** | `TC-13` | 0.1.1 | **PASS** | `evidencias/TC13_pass.png` | Corregido con guardado bloqueado. |
| **EX-003** | `TC-01` a `TC-12` | 0.1.1 | **PASS** | `evidencias/suite_base.png` | Validaciones y flujos correctos. |
| **EX-004** | `TC-14` a `TC-18` | 0.1.1 | **PASS** | `evidencias/transiciones.png` | Flujo de cancelación y accesibilidad OK. |