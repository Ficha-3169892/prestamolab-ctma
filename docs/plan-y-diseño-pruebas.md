# **Plan, Suite de Pruebas y Trazabilidad**

## **1. Plan y Suite de Pruebas (18 Casos de Prueba - Punto 11.3)**

| ID        | Escenario                 | Resultado Esperado                                                   | Técnica       |
|-----------|---------------------------|----------------------------------------------------------------------|---------------|
| **TC-01** | Catálogo con datos        | Equipos visibles con disponibilidad clara (texto + indicador).       | Caso de uso   |
| **TC-02** | EquipoId válido           | Detalle corresponde exactamente al equipo seleccionado.              | Caso de uso   |
| **TC-03** | EquipoId inexistente      | Estado recuperable sin cierre abrupto.                               | Negativa      |
| **TC-04** | Propósito 9 caracteres    | No guarda y muestra mensaje de error en el campo.                    | Límite        |
| **TC-05** | Propósito 10 caracteres   | Formulario válido, permite avanzar al guardado.                      | Límite        |
| **TC-06** | Propósito 180 caracteres  | Formulario válido, permite avanzar al guardado.                      | Límite        |
| **TC-07** | Propósito 181 caracteres  | No guarda y bloquea el envío.                                        | Límite        |
| **TC-08** | Duración 0 horas          | No guarda y notifica que el mínimo es 1 hora.                        | Límite        |
| **TC-09** | Duración 1 hora           | Valor válido, permite guardar solicitud.                             | Límite        |
| **TC-10** | Duración 8 horas          | Valor válido, permite guardar solicitud.                             | Límite        |
| **TC-11** | Duración 9 horas          | No guarda y notifica que supera el límite.                           | Límite        |
| **TC-12** | Equipo no disponible      | Intento de solicitud es rechazado por el sistema.                    | Decisión      |
| **TC-13** | Doble pulsación Guardar   | Genera únicamente una solicitud en el repositorio.                   | Riesgo        |
| **TC-14** | Crear solicitud válida    | Solicitud queda SOLICITADA y equipo pasa a RESERVADO.                | Caso de uso   |
| **TC-15** | Cancelar SOLICITADA       | Pasa a CANCELADA y el equipo vuelve a estar DISPONIBLE.              | Transición    |
| **TC-16** | Cancelar CANCELADA        | Opción no disponible o sin efecto sobre el estado.                   | Transición    |
| **TC-17** | Volver desde formulario   | Mantiene la integridad de la pila de navegación (back stack).        | Navegación    |
| **TC-18** | Fuente 1.5× y texto largo | La interfaz ajusta sus componentes sin ocultar acciones principales. | Accesibilidad |

---

## **2. Matriz de Trazabilidad (Punto 11.4)**

| Historia  | Criterio          | Riesgo     | Caso de Prueba      |
|-----------|-------------------|------------|---------------------|
| **HU-01** | CA-01.1 / CA-01.2 | R-04       | TC-01, TC-18        |
| **HU-02** | CA-02.1 / CA-02.2 | R-03       | TC-02, TC-03        |
| **HU-03** | CA-03.1 / CA-03.2 | R-01       | TC-13, TC-14        |
| **HU-04** | CA-04.1 a CA-04.3 | R-02       | TC-04 al TC-11      |
| **HU-05** | CA-05.1 / CA-05.2 | R-04       | TC-14, TC-17        |
| **HU-06** | CA-06.1 / CA-06.2 | R-01, R-04 | TC-12, TC-15, TC-16 |

---

## **3. Bitácora de Ejecución Simulada (Punto 12.1)**

| Ejecución  | Caso          | Build | Resultado | Observación                                                   |
|------------|---------------|-------|-----------|---------------------------------------------------------------|
| **EX-001** | TC-01 a TC-12 | 0.1.0 | **PASS**  | Validaciones y listados operando según especificación.        |
| **EX-002** | TC-13         | 0.1.0 | **FAIL**  | Genera dos registros en rápida sucesión (vinculado a BUG-03). |
| **EX-003** | TC-14 a TC-18 | 0.1.0 | **PASS**  | Flujos de cambio de estado y UI correctos.                    |
| **EX-004** | TC-13         | 0.1.1 | **PASS**  | Re-ejecución tras aplicar bandera `guardando` en ViewModel.   |

---

## **4. Reporte de Defecto (BUG-03 - Punto 12.2)**

* **ID:** BUG-03
* **Título:** Doble pulsación en el botón "Guardar" crea dos solicitudes duplicadas para el mismo
  equipo.
* **Build:** 0.1.0
* **Precondición:** Formulario diligenciado con datos válidos y equipo DISPONIBLE.
* **Pasos:** Pulsar rápidamente dos veces el botón de "Guardar".
* **Esperado:** Se procesa únicamente una solicitud y el botón se inhabilita durante la operación.
* **Obtenido:** Se enviaron dos solicitudes y el repositorio almacenó ambas.
* **Severidad / Prioridad:** Alta / Alta.
* **Solución aplicada:** Bloqueo con `val guardando` en `PrestamoViewModel`.

---

## **5. Informe Ejecutivo de Calidad (Punto 15)**

```text
PréstamoLab CTMA · build 0.1.1
PBIs seleccionados: 10       PBIs Done: 10
Casos planificados: 18      Ejecutados: 18
PASS: 18   FAIL: 0   BLOCKED: 0
Defectos abiertos: 0
DoD: 10 de 10 criterios cumplidos
Decisión sugerida: ACEPTABLE
```
