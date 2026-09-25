# PréstamoLab CTMA

Aplicación móvil Android desarrollada en **Jetpack Compose** bajo arquitectura **MVVM (Unidirectional Data Flow)**, persistencia Local-First con **Room** y **DataStore**, y sincronización en la nube con **Supabase** (Auth, Postgrest y Storage).

## Características Principales
- **Arquitectura Local-First:** Room actúa como fuente canónica de verdad sincronizada con Supabase.
- **Autenticación y Roles:** Control de acceso basado en roles (Usuario / Administrador).
- **Gestión de Inventario (Admin):** CRUD completo de equipos.
- **Préstamos y Solicitudes:** Registro, control de disponibilidad y cancelación de préstamos.
- **Capacidades Físicas (Semana 9):** Integración de evidencia fotográfica con `FileProvider` y geolocalización GPS con permisos de mínimo privilegio.

## Configuración y Ejecución
1. Clona el repositorio.
2. Copia el archivo `secrets.properties.example` a `secrets.properties` en la raíz del proyecto.
3. Completa tus credenciales de Supabase en `secrets.properties`:
   ```properties
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_KEY=tu-supabase-anon-key
   ```
4. Sincroniza el proyecto con Gradle y ejecuta la aplicación en tu emulador o dispositivo físico.
