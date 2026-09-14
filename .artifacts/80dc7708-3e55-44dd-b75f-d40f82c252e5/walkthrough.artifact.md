# Walkthrough: Suite de Pruebas PrestamoLab

Se ha completado la implementación de la suite de pruebas unitarias e instrumentadas para la aplicación PrestamoLab, cubriendo los 18 Casos de Prueba (TCs) solicitados.

## Cambios Realizados

### Alineación de Lógica de Negocio
Para que los tests fueran consistentes con los requerimientos, se realizaron los siguientes ajustes:
- **Estados de Dominio:** Se estandarizaron los estados a `RESERVADO` (antes EN_PRESTAMO) y `SOLICITADA` (antes PENDING).
- **Validaciones:** Se implementaron las reglas de negocio para el propósito (10-180 caracteres) y duración (1-8 horas).
- **Prevención de Duplicados:** Se añadió el flag `guardando` en el `PrestamoUiState` para deshabilitar el botón y prevenir la doble pulsación (TC-13 / BUG-03).
- **Manejo de Errores UI:** Se actualizó `SolicitudScreen` para mostrar errores visuales y `PrestamoScreen` para manejar equipos inexistentes (TC-03).

### Pruebas Unitarias (`PrestamoViewModelTest.kt`)
Se implementaron 11 tests unitarios que validan:
- **Validaciones de Input:** Casos de borde para caracteres y horas (TC-04 a TC-11).
- **Lógica de Estados:** Verificación de transiciones al solicitar y cancelar (TC-12, TC-14, TC-15, TC-16).
- **Doble Pulsación:** Verificación de bloqueo lógico en el ViewModel (TC-13).

### Pruebas de UI (`PrestamoUiTest.kt`)
Se creó una suite de pruebas instrumentadas en Compose que cubre:
- **Flujo de Usuario:** Carga de catálogo, ver detalle y navegación (TC-01, TC-02, TC-17).
- **Formulario:** Ingreso de datos y guardado exitoso (TC-13).
- **Accesibilidad:** Verificación básica de semántica y layout (TC-18).

## Resultados de Verificación

### Tests Unitarios
Ejecutados con éxito:
```bash
:app:testDebugUnitTest -> 11 passed, 0 failed
```

### Tests Instrumentados
La suite está lista y configurada en `app/src/androidTest`.

> [!WARNING]
> Durante la ejecución automática, el dispositivo físico `6TKZ4P9T9LHI8PF6` reportó `INSTALL_FAILED_USER_RESTRICTED`.
> **Acción requerida:** Habilitar "Instalar vía USB" en las Opciones de Desarrollador del dispositivo para ejecutar los tests de UI.

## Archivos Clave
- [PrestamoViewModelTest.kt](file:///C:/Users/dmk/AndroidStudioProjects/prestamolab-ctma/app/src/test/java/com/example/prestamolab/ui/PrestamoViewModelTest.kt)
- [PrestamoUiTest.kt](file:///C:/Users/dmk/AndroidStudioProjects/prestamolab-ctma/app/src/androidTest/java/com/example/prestamolab/ui/PrestamoUiTest.kt)
- [PrestamoViewModel.kt](file:///C:/Users/dmk/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/ui/PrestamoViewModel.kt) (Lógica actualizada)
