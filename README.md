# PréstamoLab CTMA

Aplicación móvil Android para la gestión de préstamos de equipos, adaptada según la guía integradora de Scrum, Desarrollo Móvil y Pruebas.

## Descripción
PréstamoLab permite a los aprendices y facilitadores gestionar el préstamo de herramientas y equipos de formación. La aplicación implementa una estrategia **Local-first**, asegurando que los datos estén disponibles incluso sin conexión a internet, sincronizándose automáticamente con una API REST cuando la conectividad se restablece.

## Características Principales
- **Catálogo Offline:** Consulta de equipos persistida en Room.
- **Gestión de Préstamos:** Creación, consulta y cancelación de solicitudes.
- **Registro de Devoluciones:** Proceso formal de retorno con evidencia fotográfica.
- **Capacidades de Dispositivo:** 
    - **Cámara/Photo Picker:** Captura de evidencia para devoluciones.
    - **GPS:** Registro de ubicación como metadato de la operación.
- **Sincronización:** Uso de WorkManager para asegurar la integridad de los datos con el servidor.

## Tecnologías y Arquitectura
El proyecto sigue una arquitectura **Clean MVVM** con las siguientes capas:
- **UI:** Jetpack Compose (Material 3) y Navigation Compose.
- **ViewModel:** Gestión de estado reactivo (StateFlow/UiState) y eventos (SharedFlow).
- **Data Layer:**
    - **Room:** Fuente local canónica.
    - **Retrofit & OkHttp:** Sincronización remota JSON.
    - **WorkManager:** Sincronización en segundo plano.
    - **Mappers:** Separación estricta entre DTOs, Entities y modelos de Dominio.

## Pruebas y Calidad
- **Unit Tests:** JUnit y Coroutines Test para lógica de negocio y mappers.
- **Integration Tests:** MockWebServer para validación de red.
- **CI/CD:** Pipeline de GitHub Actions para compilación, lint y tests unitarios automáticos.

## Documentación Técnica
- [Gestión de Riesgos](docs/riesgos.md)
- [Plan de Pruebas](docs/PLAN_PRUEBAS.md)
- [Matriz de Trazabilidad](docs/MATRIZ_TRAZABILIDAD.md)
- [Registro de Defectos](docs/defectos.md)

---
*Proyecto adaptado siguiendo los lineamientos de la guía integradora PréstamoLab CTMA.*
