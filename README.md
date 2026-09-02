PréstamoLab CTMA: Aplicación Móvil Educativa para Gestión de Préstamos

1. Descripción del Proyecto
   PréstamoLab CTMA es un prototipo educativo desarrollado en Android que permite consultar un catálogo de recursos, registrar solicitudes de préstamo y realizar trazabilidad de estados durante la ejecución[cite: 1]. El proyecto se enmarca dentro del programa de Análisis y Desarrollo de Software (ADSO) del CTMA en Medellín[cite: 1], integrando prácticas de Scrum, arquitectura móvil orientada a componentes y diseño de pruebas de software[cite: 1].

2. Alcance Actual del Incremento
   El incremento funcional actual implementa las siguientes características:
* Visualización de un catálogo de equipos con nombre, categoría y estado de disponibilidad actualizado dinámicamente[cite: 1].
* Registro de solicitudes de préstamo asociadas a los equipos disponibles en el repositorio simulado[cite: 1].
* Cambio automático de la disponibilidad del equipo al ser solicitado (transición de estados)[cite: 1].
* Funcionalidad para cancelar solicitudes que se encuentren en estado inicial (`SOLICITADA` o `PENDIENTE`), lo cual revierte el estado del equipo a disponible[cite: 1].
* Gestión de estado observable mediante `ViewModel`, `UiState` y flujos de datos unidireccionales[cite: 1].

3. Arquitectura y Componentes
   La aplicación implementa el patrón arquitectónico Model-View-ViewModel (MVVM) en conjunto con el patrón Repository para desacoplar las reglas de negocio de la interfaz de usuario[cite: 1]:
* Capa UI: Desarrollada en Jetpack Compose y Material 3, encargada de renderizar el estado observable y emitir eventos de usuario[cite: 1].
* Capa ViewModel: Coordina el comportamiento de la pantalla y expone un `UiState` inmutable de solo lectura mediante `StateFlow`[cite: 1].
* Capa Repository: Define la interfaz de acceso a datos (`PrestamoRepository`) y su implementación en memoria (`InMemoryPrestamoRepository`) para compartir datos simulados de forma consistente durante la ejecución[cite: 1].

4. Instrucciones de Ejecución
1. Clonar el repositorio oficial del proyecto[cite: 1].
2. Abrir el entorno de desarrollo Android Studio[cite: 1].
3. Sincronizar el proyecto con las dependencias de Gradle.
4. Conectar un dispositivo físico con depuración USB habilitada (o iniciar un emulador con API nivel 26 o superior)[cite: 1].
5. Ejecutar la aplicación seleccionando la configuración de compilación principal[cite: 1].

5. Referencias
   Android Developers. (s. f.). *Guide to app architecture; UI layer; ViewModel; State and Jetpack Compose*[cite: 1].
   SENA CTMA. (2026). *Guía de Aprendizaje Integradora: Scrum, Desarrollo Móvil Android y Pruebas de Software - Caso PréstamoLab CTMA*[cite: 1].