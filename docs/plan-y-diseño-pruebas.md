# Plan, Suite de Pruebas y Trazabilidad

## 1. Plan y Suite de Pruebas (18 Casos de Prueba - Punto 11.3)

| ID        | Escenario                       | Resultado Esperado                                                    | Técnica       |
|-----------|---------------------------------|-----------------------------------------------------------------------|---------------|
| **TC-01** | Catálogo con datos              | Equipos visibles con disponibilidad clara mediante texto e indicador. | Caso de uso   |
| **TC-02** | EquipoId válido                 | El detalle corresponde al equipo seleccionado.                        | Caso de uso   |
| **TC-03** | EquipoId inexistente            | Estado recuperable sin cierre abrupto.                                | Negativa      |
| **TC-04** | Propósito de 9 caracteres       | No guarda y muestra mensaje de error.                                 | Límite        |
| **TC-05** | Propósito de 10 caracteres      | Formulario válido y permite guardar.                                  | Límite        |
| **TC-06** | Propósito de 180 caracteres     | Formulario válido y permite guardar.                                  | Límite        |
| **TC-07** | Propósito de 181 caracteres     | No guarda y muestra mensaje de error.                                 | Límite        |
| **TC-08** | Duración de 0 horas             | No guarda y notifica el valor mínimo permitido.                       | Límite        |
| **TC-09** | Duración de 1 hora              | Valor válido y permite guardar.                                       | Límite        |
| **TC-10** | Duración de 8 horas             | Valor válido y permite guardar.                                       | Límite        |
| **TC-11** | Duración de 9 horas             | No guarda y notifica que supera el límite.                            | Límite        |
| **TC-12** | Equipo no disponible            | La solicitud es rechazada.                                            | Decisión      |
| **TC-13** | Doble pulsación Guardar         | Se genera únicamente una solicitud.                                   | Riesgo        |
| **TC-14** | Crear solicitud válida          | Solicitud `SOLICITADA` y equipo `RESERVADO`.                          | Caso de uso   |
| **TC-15** | Cancelar solicitud `SOLICITADA` | Solicitud `CANCELADA` y equipo `DISPONIBLE`.                          | Transición    |
| **TC-16** | Cancelar solicitud `CANCELADA`  | La operación no modifica nuevamente el estado.                        | Transición    |
| **TC-17** | Volver desde formulario         | Se conserva correctamente la navegación anterior.                     | Navegación    |
| **TC-18** | Fuente 1.5× y texto largo       | Las acciones principales permanecen visibles y utilizables.           | Accesibilidad |

---

## 2. Matriz de Trazabilidad (Punto 11.4)

| Historia  | Criterio          | Riesgo     | Casos de Prueba     |
|-----------|-------------------|------------|---------------------|
| **HU-01** | CA-01.1 / CA-01.2 | R-04       | TC-01, TC-18        |
| **HU-02** | CA-02.1 / CA-02.2 | R-03       | TC-02, TC-03        |
| **HU-03** | CA-03.1 / CA-03.2 | R-01       | TC-13, TC-14        |
| **HU-04** | CA-04.1 a CA-04.3 | R-02       | TC-04 a TC-11       |
| **HU-05** | CA-05.1 / CA-05.2 | R-04       | TC-14, TC-17        |
| **HU-06** | CA-06.1 / CA-06.2 | R-01, R-04 | TC-12, TC-15, TC-16 |

---

## 3. Ejecución de las Pruebas

Los 18 casos fueron implementados y ejecutados mediante pruebas automatizadas, utilizando pruebas
unitarias para la lógica de negocio y pruebas instrumentadas para la interfaz y navegación.

La suite cubrió:

* Validaciones de datos y valores límite.
* Catálogo y detalle de equipos.
* Creación y cancelación de solicitudes.
* Disponibilidad de equipos.
* Prevención de solicitudes duplicadas.
* Navegación entre pantallas.
* Manejo de IDs inexistentes.
* Comportamiento de la interfaz con texto largo y aumento de tamaño.

---

## 4. Bitácora de Ejecución (Punto 12.1)

| Ejecución  | Casos         | Build | Resultado | Observación                                                             |
|------------|---------------|-------|-----------|-------------------------------------------------------------------------|
| **EX-001** | TC-01 a TC-12 | 0.1.0 | **PASS**  | Validaciones, catálogo y reglas de disponibilidad correctas.            |
| **EX-002** | TC-13         | 0.1.0 | **FAIL**  | Se detectó el problema de doble pulsación durante el guardado.          |
| **EX-003** | TC-14 a TC-18 | 0.1.0 | **PASS**  | Flujos de solicitud, cancelación, navegación y accesibilidad correctos. |
| **EX-004** | TC-13         | 0.1.1 | **PASS**  | Se confirmó la corrección de la duplicación.                            |
| **EX-005** | TC-01 a TC-18 | 0.1.1 | **PASS**  | Suite final ejecutada sin casos fallidos.                               |

---

## 5. Reporte de Defecto (BUG-03 - Punto 12.2)

**ID:** BUG-03
**Título:** Doble pulsación en "Guardar" podía generar solicitudes duplicadas.
**Build:** 0.1.0
**Precondición:** Formulario con datos válidos y equipo disponible.
**Resultado esperado:** Una sola solicitud y botón inhabilitado durante el guardado.
**Resultado obtenido:** El guardado podía procesar más de una acción.
**Severidad / Prioridad:** Alta / Alta.
**Solución:** Se añadió el control `guardando` en `PrestamoViewModel` para impedir operaciones
simultáneas y deshabilitar el botón durante el proceso.
**Resultado:** El defecto fue corregido y verificado mediante pruebas automatizadas.
**Estado:** **CERRADO**

---

## 6. Informe Ejecutivo de Calidad (Punto 15)

```text
PréstamoLab CTMA · build 0.1.1

PBIs seleccionados: 10
PBIs Done: 10

Casos planificados: 18
Casos ejecutados: 18

PASS: 18
FAIL: 0
BLOCKED: 0

Defectos abiertos: 0
Defectos corregidos: 1

DoD: 10 de 10 criterios cumplidos

Decisión sugerida: ACEPTABLE
```

### Resultado

Los 18 casos de prueba planificados fueron ejecutados. Se detectó y corrigió un defecto relacionado
con la doble pulsación del botón de guardado. Tras la corrección, la suite completa fue ejecutada
nuevamente con resultado satisfactorio.
