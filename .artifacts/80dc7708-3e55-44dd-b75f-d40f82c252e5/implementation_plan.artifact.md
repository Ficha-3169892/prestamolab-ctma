# Plan de Implementación: Expansión de Suite de Pruebas (18 + 18)

Este plan detalla la expansión de las pruebas unitarias e instrumentadas para alcanzar un total de 18 casos de prueba robustos en cada suite, asegurando la cobertura total del ciclo de vida de la aplicación PrestamoLab.

## User Review Required

> [!IMPORTANT]
> Se mantendrá la coherencia con los estados `RESERVADO`, `SOLICITADA` y `CANCELADA` definidos en la fase anterior. No se requieren cambios adicionales en la lógica de negocio, solo en la cobertura de pruebas.

## Proposed Changes

### 1. Suite de Pruebas Unitarias (18 Tests)
Se actualizará `PrestamoViewModelTest.kt` para incluir:
- **TC-01 a TC-03 (Lógica):** Carga inicial, selección de equipo válido e inexistente.
- **TC-04 a TC-07 (Propósito):** Límites de 9, 10, 180 y 181 caracteres.
- **TC-08 a TC-11 (Duración):** Límites de 0, 1, 8 y 9 horas.
- **TC-12 a TC-16 (Estados):** Bloqueo de reservados, doble pulsación, flujo de solicitud, cancelación y re-cancelación.
- **Tests Adicionales:** Navegación entre secciones, limpieza de errores al escribir y validación de campo "Ambiente" obligatorio.

### 2. Suite de Pruebas Instrumentadas (18 Tests)
Se actualizará `PrestamoUiTest.kt` para incluir:
- **Flujos de Navegación:** Acceso a cada pantalla y gestión del Backstack (TC-01, TC-02, TC-03, TC-17).
- **Validaciones UI:** Comprobación de que cada error de validación (propósito, duración, ambiente) muestra su mensaje correspondiente en la interfaz.
- **Ciclo de Vida de Solicitud:** Crear, visualizar en "Mis Solicitudes" y cancelar desde la UI.
- **Robustez y Accesibilidad:** Doble pulsación (TC-13) y semántica de componentes (TC-18).

## Verification Plan

### Automated Tests
- Ejecutar `./gradlew test` (Esperado: 18 passed).
- Ejecutar `./gradlew connectedAndroidTest` (Esperado: 18 passed).
