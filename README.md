# PréstamoLab CTMA - Aplicación Android (Semana 9)

Aplicación móvil Android desarrollada en Kotlin con Jetpack Compose para la gestión integral de préstamos de equipos de laboratorio en la CTMA. Implementa arquitectura **Local-First** utilizando **Room** como fuente canónica de verdad y sincronización remota con **Supabase** (Auth, PostgREST y Storage).

---

## 🚀 Requisitos y Tecnologías Usadas
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose con Material 3
- **Arquitectura:** MVVM (Unidirectional Data Flow) con Corrutinas y Flow
- **Base de Datos Local:** Room (Estrategia Local-First)
- **Backend / Cloud:** Supabase Kotlin SDK (Auth, PostgREST, Storage)
- **Persistencia de Sesión:** DataStore Preferences
- **Hardware Integrado:** Cámara Nativa / FileProvider
- **Pruebas:** JUnit 4, Compose UI Test Rule y GitHub Actions CI/CD

---

## ⚙️ Instrucciones de Configuración y Ejecución

### 1. Clonar e Iniciar el Proyecto
```bash
git clone <URL_DEL_REPOSITORIO>
```
Abre el proyecto en Android Studio (Ladybug / Jellyfish o superior) con JDK 17+.

### 2. Configuración de Secretos de Supabase
1. En la raíz del proyecto, copia el archivo `secrets.properties.example` y renómbralo a `secrets.properties`.
2. Completa tus credenciales de Supabase:
   ```properties
   SUPABASE_URL=https://tu-proyecto.supabase.co
   SUPABASE_ANON_KEY=tu-anon-key-aqui
   ```
*(Nota: `secrets.properties` está ignorado en `.gitignore` para no exponer credenciales).*

### 3. Configuración de la Base de Datos en Supabase
Ejecuta el script SQL ubicado en la guía de instalación de la Semana 9 para crear las tablas `perfiles`, `equipos`, `solicitudes` y la política RLS en el bucket de Storage `evidencias`.

---

## 🧪 Pruebas Unitarias e Instrumentadas

Para ejecutar las pruebas desde terminal:

```bash
# Pruebas Unitarias (28/28 Pasadas)
./gradlew testDebugUnitTest

# Construcción de APK Debug
./gradlew assembleDebug
```

---

## 🛡️ Roles de Usuario
- **Usuario (`'usuario'`):** Inicia sesión, consulta catálogo, solicita préstamos, visualiza sus solicitudes y adjunta evidencias fotográficas.
- **Administrador (`'admin'`):** Acceso al Panel de Administración para agregar, editar y eliminar equipos del inventario.
