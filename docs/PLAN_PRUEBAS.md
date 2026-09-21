# Plan de Pruebas – PréstamoLab CTMA

Este documento detalla la estrategia de validación de la aplicación.

## Estrategia de Pruebas
Se sigue el patrón de la pirámide de pruebas:
1. **Pruebas Unitarias (JUnit):** Validación de mappers, lógica de negocio en ViewModels y repositorios en memoria.
2. **Pruebas de Integración (MockWebServer):** Validación del cliente API y parseo de JSON.
3. **Pruebas Instrumentadas (Room/DAOs):** Validación de la persistencia local en base de datos real (en memoria para tests).
4. **Pruebas E2E (UI Automator / Compose Test):** Validación de los flujos principales.

## Casos de Prueba Principales
| ID | Descripción | Resultado Esperado |
| :--- | :--- | :--- |
| CP-01 | Listar catálogo en modo avión | Se muestran los datos cacheados en Room. |
| CP-02 | Solicitar equipo sin conexión | La solicitud se guarda localmente como 'LOCAL'. |
| CP-03 | Sincronización automática | WorkManager sube solicitudes pendientes al recuperar conexión. |
| CP-04 | Registro de devolución con foto | Se actualiza el estado y se guarda la URI de la evidencia. |
| CP-05 | Denegación de GPS | La solicitud se crea correctamente sin coordenadas. |

## Herramientas
- **JUnit 4**
- **MockWebServer** (OkHttp)
- **Kotlinx Coroutines Test**
- **Compose UI Test**
