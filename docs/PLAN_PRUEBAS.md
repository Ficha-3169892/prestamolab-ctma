# Plan y Reporte de Ejecución de Pruebas - PrestamoLab CTMA

## 1. Estrategia de Pruebas (Semana 9)

El plan de pruebas abarca tres niveles de validación:
1. **Pruebas Unitarias (`JUnit 4`):** Validación de lógica de negocio, validadores de solicitudes, ViewModel y control de permisos por rol (Admin vs Usuario).
2. **Pruebas Instrumentadas UI (`Compose Test Rule`):** Recorrido End-to-End (E2E) simulando el flujo de Login, visualización de catálogo y solicitud de equipos.
3. **Integración Contínua (`GitHub Actions`):** Pipeline automatizado ejecutando Lint, Unit Tests y Gradle Build.

---

## 2. Resumen de Ejecución de Pruebas Unitarias

- **Total de pruebas unitarias ejecutadas:** 28
- **Pruebas pasadas:** 28
- **Pruebas fallidas:** 0
- **Estado:** 🟢 VERDE (100% Exitoso)

### Módulos Probados:
- `ValidacionesSolicitudTest`: Validaciones de límites para ambiente, propósito (10-180 caracteres) y duración (1-8 horas).
- `InMemoryPrestamoRepositoryTest`: Operaciones del repositorio reactivo, estado de solicitudes y reserva de equipos.
- `PrestamoViewModelTest`: Flujo de presentación de UI, llamadas a guardado/cancelación y manejo de errores.
- `RolePermissionsTest`: Validación de restricciones de rol; asegurando que operaciones admin (crear/eliminar equipo) son denegadas a usuarios normales y permitidas a administradores.

---

## 3. Pruebas Instrumentadas E2E

- **Clase:** `LoginAndNavigationTest`
- **Flujo:** Login con credenciales -> Verificación de navegación al Catálogo de Equipos.
- **Herramientas:** Compose UI Test rule (`androidx.compose.ui.test`).
