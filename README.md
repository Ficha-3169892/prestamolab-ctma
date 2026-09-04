# PréstamoLab CTMA: Aplicación Móvil Educativa para Gestión de Préstamos

## 1. Descripción del Proyecto
PréstamoLab CTMA es un prototipo educativo desarrollado en Android que permite consultar un catálogo de recursos, registrar solicitudes de préstamo y realizar trazabilidad de estados durante la ejecución (SENA CTMA, 2026). El proyecto se enmarca dentro del programa de Análisis y Desarrollo de Software (ADSO) del Centro de Tecnología de Manufactura Avanzada (CTMA) en Medellín, integrando de manera simultánea prácticas del marco Scrum, arquitectura móvil orientada a componentes (MVVM) y diseño de pruebas de software.

## 2. Alcance Actual del Incremento
El incremento funcional actual implementa las siguientes características clave:
* Visualización de un catálogo de equipos con nombre, categoría y estado de disponibilidad actualizado de forma dinámica.
* Navegación estructurada por identificadores (`equipoId` y `solicitudId`) con manejo seguro de rutas y control de ID's inexistentes.
* Registro de solicitudes de préstamo asociadas a equipos disponibles mediante un repositorio simulado en memoria.
* Validación desacoplada de reglas de negocio (propósito, duración de horas y selección de recursos disponibles).
* Cambio automático de la disponibilidad del equipo al ser solicitado (transición de estados).
* Funcionalidad para cancelar solicitudes que se encuentren en estado inicial (`SOLICITADA`), revirtiendo el estado del equipo a disponible de forma síncrona.
* Gestión de estado observable mediante `ViewModel` y flujos de datos unidireccionales (UDF).

## 3. Arquitectura y Componentes
La aplicación implementa el patrón arquitectónico **Model-View-ViewModel (MVVM)** en conjunto con el patrón Repository para desacoplar completamente las reglas de negocio de la interfaz de usuario:
* **Capa UI (Jetpack Compose y Material 3):** Encargada de renderizar el estado observable y emitir eventos de usuario sin alterar directamente las fuentes de datos.
* **Capa ViewModel:** Coordina el comportamiento de la pantalla, procesa las validaciones lógicas y expone un estado de solo lectura.
* **Capa Repository:** Define la interfaz de acceso a datos (`PrestamoRepository`) y su implementación simulada en memoria (`InMemoryPrestamoRepository`) para garantizar consistencia durante la ejecución del incremento.

## 4. Instrucciones de Ejecución
1. Clonar el repositorio oficial del proyecto mediante Git.
2. Abrir el entorno de desarrollo oficial **Android Studio**.
3. Sincronizar el proyecto con las dependencias de Gradle especificadas en el archivo de configuración.
4. Conectar un dispositivo físico con la depuración USB habilitada o iniciar un emulador Android configurado con API nivel 26 o superior.
5. Ejecutar la aplicación seleccionando la configuración de compilación principal (`app`).

## 5. Uso Responsable de Inteligencia Artificial
| Herramienta | Propósito | Sugerencia Recibida | Verificación del Equipo | Decisión Adoptada |
| :--- | :--- | :--- | :--- | :--- |
| **Gemini / ChatGPT** | Apoyo en diseño de pruebas y casos de borde | Estructuración de límites (0, 1, 8, 9 horas; 9, 10, 180, 181 caracteres) | Verificación contra la Guía de Aprendizaje e integración en código | Aceptado e integrado en las validaciones y suite de pruebas |
| **Android Docs / IA** | Revisión de arquitectura UDF y Compose | Ejemplos de desacoplamiento de estado en ViewModel | Pruebas de compilación y comportamiento en emulador | Implementado usando StateFlow de solo lectura |

## 6. Referencias
* Android Developers. (s. f.). *Guide to app architecture; UI layer; ViewModel; State and Jetpack Compose; Navigation*. Recuperado de https://developer.android.com/
* SENA - Centro de Tecnología de Manufactura Avanzada [CTMA]. (2026). *Guía de Aprendizaje Integradora: Scrum, Desarrollo Móvil Android y Pruebas de Software - Caso PréstamoLab CTMA*. Medellín, Colombia.