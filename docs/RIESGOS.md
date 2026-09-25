# Matriz de Riesgos - PrestamoLab CTMA (Semana 9)

## 1. Identificación y Mitigación de Riesgos

| ID | Categoría | Riesgo Identificado | Impacto | Probabilidad | Estrategia de Mitigación |
|---|---|---|---|---|---|
| **R-01** | Hardware & Cámara | Denegación de permisos de cámara por el usuario en tiempo de ejecución al adjuntar evidencia. | Alto | Media | Implementación de permisos con principio de mínimo privilegio: solicitar permiso únicamente al pulsar el botón de captura y permitir selección alternativa de imagen mediante la galería. |
| **R-02** | Autenticación & Sesión | Pérdida de conectividad con Supabase Auth durante la autenticación de usuarios. | Medio | Media | Almacenamiento seguro y persistente del token de sesión, ID de usuario y rol con DataStore Preferences, complementado con modo de degradación elegante (fallback offline). |
| **R-03** | Sincronización Cloud | Fallo de sincronización en tiempo real entre la base de datos Room y Supabase PostgREST. | Alto | Baja | Arquitectura Local-First donde Room actúa como la única fuente canónica de verdad para la UI, sincronizando las mutaciones hacia Supabase de forma asíncrona. |
| **R-04** | Seguridad & Secretos | Exposición de la URL o Anon API Key de Supabase en el repositorio de control de versiones. | Crítico | Baja | Configuración de `secrets.properties` fuera del control de versiones (`.gitignore`), inyectado dinámicamente mediante `BuildConfig`. |
| **R-05** | Integridad de Datos | Rotura de historial de solicitudes por eliminación física de equipos en el inventario admin. | Medio | Baja | Inclusión de llaves foráneas con CASCADE y soft delete en operaciones administrativas de inventario. |
