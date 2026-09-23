# Plan de Pruebas – PréstamoLab CTMA

Este documento detalla la estrategia de validación funcional y no funcional de la aplicación.

---

## Estrategia de Pruebas

Se sigue el patrón de la pirámide de pruebas:
1. **Pruebas Unitarias (JUnit 4 + TDD):** Validación de mappers, validadores de negocio, ViewModels y repositorios en memoria.
2. **Pruebas de Integración (MockWebServer + Retrofit):** Validación de las respuestas HTTP y parseo de DTOs JSON.
3. **Pruebas Instrumentadas (Room/DAOs):** Validación de la base de datos sqlite en memoria con AndroidX Test.
4. **Pruebas E2E (Compose UI Test):** Validación de los flujos de interfaz de usuario.

---

## Casos de Prueba Funcionales

| ID | Descripción | Resultado Esperado | Estado |
| :--- | :--- | :--- | :---: |
| **CP-01** | Listar catálogo en modo avión | Se muestran los datos cacheados en Room localmente. | **Pasó** |
| **CP-02** | Solicitar equipo sin conexión | La solicitud se guarda en Room con estadoSincronizacion 'LOCAL'. | **Pasó** |
| **CP-03** | Sincronización automática | WorkManager sube solicitudes pendientes al reconectar a internet. | **Pasó** |
| **CP-04** | Registro de devolución con foto | Se actualiza el estado a DEVUELTA y se adjunta la URI de evidencia. | **Pasó** |
| **CP-05** | Denegación de GPS | La devolución se registra con foto sin coordenadas GPS. | **Pasó** |
| **CP-06** | Autenticación Admin para CRUD | Solicita correo y contraseña (`admin@gmail.com` / `admin123`) antes de acceder al menú de gestión. | **Pasó** |
| **CP-07** | Borrar historial finalizado | Elimina solicitudes devueltas/canceladas manteniendo intactas las activas. | **Pasó** |

---

## Pruebas No Funcionales

### 1. Pruebas de Accesibilidad
- **Tamaños de objetivo táctil (Touch Targets):** Todos los botones e íconos interactivos tienen un tamaño mínimo de **48x48 dp** según guías de Material Design 3.
- **Lectores de Pantalla (TalkBack):** Todos los componentes interactivos (`IconButton`, `AsyncImage`, etc.) cuentan con atributos `contentDescription` explicativos.
- **Contraste de Color:** Aplicación del sistema de diseño Material 3 asegurando contraste alto en texto y badges de estado.

### 2. Pruebas de Seguridad
- **Protección de Credenciales:** Aislamiento de claves de API mediante `local.properties` e inyección segura en `BuildConfig`.
- **Canal Seguro (HTTPS):** Comunicaciones cifradas SSL/TLS obligatorias con Supabase PostgREST.
- **Principio de Mínimo Privilegio:** Permisos de Cámara y Ubicación solicitados exclusivamente bajo demanda explícita del usuario.

### 3. Pruebas de Rendimiento y Resiliencia
- **Timeouts de Red:** Tiempos de espera configurados a 15 segundos en `OkHttpClient` para evitar bloqueos por latencia de red.
- **Memoria y Cold Start:** Inicio de aplicación fluido aprovechando inyección perezosa (`by lazy`) en `PrestamoApplication`.

---

## Herramientas Utilizadas
- **JUnit 4** & **Kotlinx Coroutines Test**
- **MockWebServer** (OkHttp)
- **Compose UI Test** (`createComposeRule`)
- **Android Profiler** (Uso de CPU, Memoria y Red)
