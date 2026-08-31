# Product Backlog y Secuencia de Actividades

## **Product Backlog Inicial**

| ID        | Historia / Necesidad                             | Prioridad | Riesgo | Criterio de Ordenación / Dependencia                    |
|-----------|--------------------------------------------------|-----------|--------|---------------------------------------------------------|
| **PB-01** | Consultar catálogo de equipos y disponibilidad.  | Alta      | Alto   | Base de la app; sin catálogo no hay navegación.         |
| **PB-02** | Consultar detalle de un equipo.                  | Alta      | Medio  | Depende de PB-01; se requiere para abrir el formulario. |
| **PB-03** | Registrar solicitud de préstamo.                 | Alta      | Alto   | Funcionalidad principal del sistema.                    |
| **PB-04** | Validar propósito, destino y duración.           | Alta      | Alto   | Vinculado a PB-03; asegura integridad de datos.         |
| **PB-05** | Evitar solicitud sobre equipo no disponible.     | Alta      | Alto   | Regla de negocio central vinculada a PB-01 y PB-03.     |
| **PB-06** | Evitar duplicación por doble pulsación.          | Alta      | Alto   | Control de concurrencia y UI en el guardado.            |
| **PB-07** | Consultar mis solicitudes.                       | Media     | Medio  | Visualización de solicitudes registradas.               |
| **PB-08** | Consultar detalle de solicitud.                  | Media     | Medio  | Depende de PB-07.                                       |
| **PB-09** | Cancelar solicitud SOLICITADA.                   | Media     | Medio  | Modificación de estado sobre solicitudes existentes.    |
| **PB-10** | Manejar IDs inexistentes y estados vacíos.       | Media     | Medio  | Robustez y manejo de errores de navegación.             |
| **PB-11** | Mantener interfaz usable con texto aumentado.    | Media     | Medio  | Requisito de accesibilidad visual.                      |
| **PB-12** | Documentar arquitectura, pruebas y limitaciones. | Media     | Medio  | Cierre de entrega y calidad.                            |

## Historias de Usuario, Criterios de Aceptación y Matriz de Riesgos**

### **Historias de Usuario y Criterios de Aceptación (Punto 9.2)**

* **HU-01 (Catalogar):** Como usuario, quiero ver la lista de equipos con su disponibilidad para
  saber qué puedo solicitar.
    * **CA-01.1:** Muestra nombre, categoría y estado (DISPONIBLE, RESERVADO, PRESTADO).
    * **CA-01.2:** Los estados no dependen únicamente del color; incluyen texto descriptivo.

* **HU-02 (Detalle Equipo):** Como usuario, quiero seleccionar un equipo para ver su información
  detallada mediante su ID.
    * **CA-02.1:** Transporta únicamente `equipoId` mediante la navegación.
    * **CA-02.2:** Muestra la opción de solicitar solo si el estado es DISPONIBLE.

* **HU-03 (Registrar Solicitud):** Como usuario, quiero solicitar un equipo disponible ingresando
  destino, propósito y duración.
    * **CA-03.1:** Dado un equipo DISPONIBLE y datos válidos, al pulsar Guardar se crea la solicitud
      en
      estado SOLICITADA y el equipo cambia a RESERVADO.
    * **CA-03.2:** Una sola acción de guardado genera exactamente una solicitud.

* **HU-04 (Validar Datos):** Como usuario, quiero que el sistema me alerte si los datos ingresados
  no son válidos antes de guardar.
    * **CA-04.1:** El destino es obligatorio.
    * **CA-04.2:** El propósito debe tener entre 10 y 180 caracteres.
    * **CA-04.3:** La duración debe ser entre 1 y 8 horas.

* **HU-05 (Consultar Solicitudes):** Como usuario, quiero ver el listado "Mis solicitudes" para
  hacer seguimiento.
    * **CA-05.1:** Muestra las solicitudes registradas con su estado actual.
    * **CA-05.2:** Permite ingresar al detalle de una solicitud mediante `solicitudId`.

* **HU-06 (Cancelar Solicitud):** Como usuario, quiero cancelar una solicitud que esté en estado
  SOLICITADA.
    * **CA-06.1:** Una solicitud en estado SOLICITADA pasa a CANCELADA y actualiza la disponibilidad
      del
      equipo.
    * **CA-06.2:** Solicitudes en otros estados no muestran la opción de cancelar.

---

### **Matriz de Riesgos y Cobertura (Punto 9.3)**

| ID       | Riesgo                                                               | Probabilidad | Impacto | Nivel   | Cobertura / Estrategia                                                    |
|----------|----------------------------------------------------------------------|--------------|---------|---------|---------------------------------------------------------------------------|
| **R-01** | Dos solicitudes activas reservan el mismo equipo.                    | Alta         | Alta    | Crítico | TC-12, TC-13 (Verificación de disponibilidad y prevención de duplicados). |
| **R-02** | Datos fuera de rango son aceptados en la solicitud.                  | Alta         | Media   | Alto    | TC-04 a TC-11 (Clases de equivalencia y valores límite en formulario).    |
| **R-03** | ID inexistente en navegación provoca un cierre abrupto.              | Media        | Alta    | Alto    | TC-03 (Prueba de navegación negativa con estado recuperable).             |
| **R-04** | El catálogo no refleja el cambio de estado tras guardar o cancelar.  | Media        | Alta    | Alto    | TC-14, TC-15 (Pruebas de consistencia de datos e integración).            |
| **R-05** | Botones o campos desaparecen o se solapan con tamaño de fuente 1.5×. | Media        | Media   | Medio   | TC-18 (Prueba de accesibilidad UI con escalado de texto).                 |

---

## **Sprint Planning, Sprint Goal y Definition of Done**

### **Plan del Sprint y Sprint Goal (Punto 9.4)**

* **Sprint Goal:** Permite consultar un equipo disponible y registrar una solicitud de préstamo
  válida, manteniendo la disponibilidad coherente y demostrando su calidad mediante pruebas
  reproducibles.

* **Selección de PBIs para el Sprint:** PB-01, PB-02, PB-03, PB-04, PB-05, PB-06, PB-07, PB-08,
  PB-09 y PB-10.

---

### **Definition of Done (DoD) Mínima (Punto 9.5)**

1. El proyecto compila sin errores y ejecuta en el emulador o dispositivo.
2. Los criterios de aceptación de los PBIs seleccionados están implementados.
3. La UI (Compose) no modifica directamente las listas del repositorio.
4. El ViewModel expone el estado mediante `UiState` y `StateFlow` de solo lectura.
5. La navegación transporta únicamente IDs (`equipoId`, `solicitudId`) y maneja IDs inexistentes sin
   cierres abruptos.
6. Se ejecutaron las pruebas planificadas y sus resultados son reales con evidencia trazable.
7. No existen defectos abiertos de severidad Alta o Crítica.
8. Las correcciones cuentan con pruebas de confirmación y regresión.
9. El repositorio Git está actualizado y el `README.md` incluye instrucciones claras.
10. El incremento puede demostrarse y cada integrante explica su contribución.

---
