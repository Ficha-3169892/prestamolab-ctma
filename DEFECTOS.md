# Registro de Defectos, Confirmación y Regresión (PréstamoLab CTMA)

## Reporte de Defecto: BUG-03

* **ID Defecto:** `BUG-03`
* **Título:** Doble pulsación rápida en el botón "Guardar" crea solicitudes duplicadas para el mismo equipo.
* **Build Detectada:** 0.1.0
* **Severidad:** **Alta** (Rompe la regla de disponibilidad del dominio).
* **Prioridad:** **Alta** (Afecta directamente el Sprint Goal y la Definition of Done).
* **Caso de Prueba Origen:** `TC-13`

### Pasos para Reproducir
1. Abrir la aplicación y seleccionar un equipo en estado `DISPONIBLE`.
2. Presionar el botón "Solicitar".
3. Llenar el formulario con datos válidos (Ambiente "Lab 302", Propósito "Práctica de Sensores", Duración "2").
4. Pulsar el botón **"Guardar"** dos o tres veces muy rápidamente.

### Resultado Obtenido
Se registraban dos instancias distintas de `SolicitudPrestamo` asociadas al mismo `equipoId`, provocando inconsistencia de disponibilidad.

### Resultado Esperado
Se debe procesar únicamente la primera solicitud, deshabilitar la interfaz durante la petición y registrar un solo elemento.

---

## Solución Aplicada y Prueba de Confirmación

* **Solución Técnica:** Se añadió la propiedad `guardando: Boolean = false` dentro del `PrestamoUiState`. Al hacer clic en Guardar, el estado pasa inmediatamente a `guardando = true`, lo que deshabilita el botón en la UI Compose e ignora clics subsecuentes en el ViewModel.
* **Prueba de Confirmación (`TC-13`):** Se repitió el escenario pulsando repetidamente el botón. **Resultado: PASS** (Solo se generó 1 solicitud).

---

## Pruebas de Regresión Ejecutadas

Tras corregir el `BUG-03`, se ejecutaron las siguientes pruebas para descartar impactos colaterales:

1. **`TC-14` (Creación normal):** Guardar una solicitud de forma normal. -> **PASS**
2. **`TC-01` (Actualización de catálogo):** Verificar que el equipo cambie a `RESERVADO` en la lista general. -> **PASS**
3. **`TC-15` (Cancelación):** Cancelar la solicitud creada para asegurar que el estado vuelva a `DISPONIBLE`. -> **PASS**