# PréstamoLab CTMA - Product Goal & Alcance

## Problema
En los ambientes de formación existe dificultad para consultar y hacer seguimiento de la disponibilidad y las solicitudes de préstamo de equipos y herramientas, debido a que el proceso puede realizarse de manera manual y no siempre existe una visualización clara del estado de los recursos.

## Usuarios
### Solicitante (Aprendiz / Instructor)
Aprendiz o instructor que consulta equipos disponibles, solicita préstamos y registra devoluciones con evidencia fotográfica y coordenadas GPS.

### Administrador / Gestor de Catálogo
Usuario con credenciales de acceso que gestiona el inventario de equipos (crear, editar y eliminar equipos en el catálogo).

## Necesidades
- Consultar catálogo de equipos y herramientas y conocer su disponibilidad en tiempo real.
- Consultar el detalle completo de un equipo.
- Registrar solicitudes de préstamo validando restricciones de propósito y duración.
- Consultar el historial de solicitudes realizadas.
- Cancelar solicitudes que estén en estado SOLICITADA.
- Registrar devoluciones de equipos con evidencia fotográfica (Cámara/Galería) y posición GPS.
- Autenticación de Administrador para proteger las funciones de gestión de inventario (CRUD).
- Limpiar el historial de préstamos devueltos y cancelados.
- Funcionar en modo *Offline-First* con persistencia local en Room y sincronización remota en la nube.

## Restricciones y Arquitectura
- Aplicación móvil Android desarrollada con Kotlin y Jetpack Compose (Material 3).
- Arquitectura Clean MVVM con ViewModel, `StateFlow` y `SharedFlow`.
- Persistencia local mediante Room Database y almacenamiento de preferencias con `DataStore`.
- Conexión remota con **Supabase PostgREST API** y almacenamiento de archivos en Supabase Storage.
- Procesos asíncronos en segundo plano con `WorkManager` y `OkHttp` con timeouts de 15 segundos.
- Cifrado de credenciales aisladas mediante `local.properties` y `BuildConfig`.

## Valor Esperado
La aplicación permite consultar y gestionar de manera sencilla, segura y trazable las solicitudes de préstamo de equipos y herramientas, manteniendo una representación coherente de su disponibilidad tanto sin conexión a internet como en la nube.

## Product Goal
Mejorar la trazabilidad, disponibilidad y gestión de préstamos de equipos y herramientas de formación mediante una aplicación móvil nativa Android resiliente, con soporte *Offline-First*, sincronización remota en la nube y captura de evidencias físicas.
