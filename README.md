# PréstamoLab CTMA: Aplicación Móvil Educativa para Gestión de Préstamos

## 1. Descripción del Proyecto
PréstamoLab CTMA es un prototipo educativo desarrollado en Android que permite consultar un catálogo de recursos, registrar solicitudes de préstamo y realizar trazabilidad de estados durante la ejecución (SENA CTMA, 2026). El proyecto se enmarca dentro del programa de Análisis y Desarrollo de Software (ADSO) del Centro de Tecnología de Manufactura Avanzada (CTMA) en Medellín, integrando de manera simultánea prácticas del marco Scrum, arquitectura móvil orientada a componentes (MVVM) y diseño de pruebas de software.

## 2. Alcance Actual del Incremento (Parte 2)
* **Acceso por rol:** inicio de sesión con correo o documento contra la tabla `users` de Supabase (contraseña en SHA-256); el instructor entra a Gestión y el estudiante al Catálogo.
* **Estudiante:** catálogo y detalle de equipos, solicitud de préstamo, Mis Solicitudes con cancelación, devolución con estado del equipo y ubicación GPS, evidencias fotográficas al recibir y al devolver, recordatorio 30 minutos antes de la hora límite y consulta de actividades formativas.
* **Instructor:** aprobar o rechazar solicitudes (con motivo), inventario de equipos y actividades formativas.
* **Sin conexión:** Room es la fuente de verdad; los cambios se sincronizan con Supabase (y las fotos con Supabase Storage) mediante WorkManager.
* Backlog, criterios y trazabilidad: `docs/backlog/issues/`, `docs/MATRIZ_TRAZABILIDAD.md` y `docs/PLAN_PRUEBAS.md`.

## 3. Arquitectura y Componentes
La aplicación implementa el patrón arquitectónico **Model-View-ViewModel (MVVM)** en conjunto con el patrón Repository para desacoplar completamente las reglas de negocio de la interfaz de usuario:
* **Capa UI (Jetpack Compose y Material 3):** Encargada de renderizar el estado observable y emitir eventos de usuario sin alterar directamente las fuentes de datos.
* **Capa ViewModel:** Coordina el comportamiento de la pantalla, procesa las validaciones lógicas y expone un estado de solo lectura.
* **Capa Repository:** Define la interfaz de acceso a datos (`PrestamoRepository`) implementada sobre Room (`RoomPrestamoRepository`), cuyas tablas `equipments`, `loans` y `returns` replican las de Supabase. Room es la única fuente que lee la UI; `SincronizadorPrestamos`, ejecutado por WorkManager, envía a Supabase los registros PENDIENTES y recibe los remotos (reintento exponencial ante 5xx o sin red, cierre de sesión ante 401). El inicio de sesión (`UsuariosAuthRepository`) valida correo o documento y contraseña contra la tabla `users` de Supabase y guarda la sesión en DataStore.

## 4. Instrucciones de Ejecución
1. Clonar el repositorio oficial del proyecto mediante Git.
2. Abrir el entorno de desarrollo oficial **Android Studio**.
3. Agregar a `local.properties` (no se sube a git) las credenciales del proyecto Supabase:
   `SUPABASE_URL=https://<proyecto>.supabase.co` y `SUPABASE_ANON_KEY=<clave anon>`.
   Ese es el ambiente **dev**, el que se compila por defecto. Para stage o prod se agregan `SUPABASE_URL_STAGE`, `SUPABASE_ANON_KEY_STAGE`, etc., y se compila con `-Pambiente=stage` o `-Pambiente=prod`: ver `docs/AMBIENTES.md`.
4. Preparar la base de datos ejecutando en orden los scripts de `docs/supabase/` (001 a 009) en el SQL Editor de Supabase. El 009 activa la seguridad por rol: sin él, esta versión de la app no puede iniciar sesión.
5. Sincronizar el proyecto con Gradle y ejecutar la configuración `app` en un dispositivo o emulador con Android 7.0 (API 24) o superior.
6. Pruebas: `gradlew testDebugUnitTest` (unitarias; también las ejecuta GitHub Actions en cada push). Las instrumentadas se ejecutan en el dispositivo como indica `docs/PLAN_PRUEBAS.md`. Las pruebas inestables detectadas y su manejo están en `docs/PRUEBAS_INESTABLES.md`.

## 5. Uso Responsable de Inteligencia Artificial
| Herramienta | Propósito | Sugerencia Recibida | Verificación del Equipo | Decisión Adoptada |
| :--- | :--- | :--- | :--- | :--- |
| **Gemini / ChatGPT** | Apoyo en diseño de pruebas y casos de borde | Estructuración de límites (0, 1, 8, 9 horas; 9, 10, 180, 181 caracteres) | Verificación contra la Guía de Aprendizaje e integración en código | Aceptado e integrado en las validaciones y suite de pruebas |
| **Android Docs / IA** | Revisión de arquitectura UDF y Compose | Ejemplos de desacoplamiento de estado en ViewModel | Pruebas de compilación y comportamiento en emulador | Implementado usando StateFlow de solo lectura |

## 6. Referencias
* Android Developers. (s. f.). *Guide to app architecture; UI layer; ViewModel; State and Jetpack Compose; Navigation*. Recuperado de https://developer.android.com/
* SENA - Centro de Tecnología de Manufactura Avanzada [CTMA]. (2026). *Guía de Aprendizaje Integradora: Scrum, Desarrollo Móvil Android y Pruebas de Software - Caso PréstamoLab CTMA*. Medellín, Colombia.