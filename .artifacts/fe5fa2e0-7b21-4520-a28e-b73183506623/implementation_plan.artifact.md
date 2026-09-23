# Plan de Implementación – PréstamoLab CTMA

Este plan detalla la evolución del proyecto actual para cumplir con la guía integradora, priorizando la estabilidad y la arquitectura por capas.

## User Review Required

> [!IMPORTANT]
> Se añadirán múltiples dependencias al proyecto (Room, Retrofit, OkHttp, DataStore, CameraX, WorkManager). Asegúrate de tener conexión a internet para la sincronización de Gradle.

> [!WARNING]
> La implementación de la estrategia "Local-first" cambiará la forma en que los datos fluyen en la aplicación. El repositorio actual `InMemoryPrestamoRepository` será reemplazado por uno que coordina Room y Retrofit.

## Open Questions

- ¿Qué capacidad física adicional prefieres implementar? He propuesto **GPS** para registrar la ubicación donde se solicita el equipo o se realiza la entrega, pero también podría ser Biometría para autorizar el préstamo.

## Proposed Changes

---

### [Component Name] Infraestructura y Dependencias

Se actualizará el archivo `build.gradle.kts` para incluir todas las librerías necesarias.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/build.gradle.kts)
- Añadir plugins de KSP y Room.
- Añadir dependencias de Room, Retrofit, OkHttp, Serialization, DataStore, CameraX, WorkManager y MockWebServer.

---

### [Component Name] Capa de Datos (Data Layer)

Creación de la estructura para persistencia local y remota.

#### [NEW] [Entities & DAOs](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/data/local)
- `EquipoEntity`, `SolicitudEntity`.
- `PrestamoDatabase`, `EquipoDao`, `SolicitudDao`.

#### [NEW] [DTOs & API Service](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/data/remote)
- `EquipoDto`, `SolicitudDto`.
- `PrestamoApiService`.

#### [NEW] [Mappers](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/data/mapper)
- Transformadores entre Entity ↔ DTO ↔ Domain Model.

---

### [Component Name] Repositorio y Sincronización

Refactorización para cumplir con la arquitectura local-first.

#### [MODIFY] [PrestamoRepository.kt](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/repository/PrestamoRepository.kt)
- Convertir métodos a `suspend` y retornar `Flow`.

#### [NEW] [OfflineFirstPrestamoRepository.kt](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/repository/OfflineFirstPrestamoRepository.kt)
- Implementación real que usa Room como fuente canónica y Retrofit para sincronización.

---

### [Component Name] ViewModel y UI

Adaptación a estados asíncronos y nuevas funcionalidades.

#### [MODIFY] [PrestamoUiState.kt](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/viewmodel/PrestamoUiState.kt)
- Incluir estados: `Loading`, `Error`, `Content`.

#### [MODIFY] [PrestamoViewModel.kt](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/viewmodel/PrestamoViewModel.kt)
- Usar `viewModelScope` y manejar flujos de datos reactivos.

#### [NEW] [DevolucionScreen.kt](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/ui/devolucion/DevolucionScreen.kt)
- Registro de devoluciones con captura de evidencia.

---

### [Component Name] Capacidades del Dispositivo y Seguridad

#### [NEW] [CameraManager/PhotoPicker](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/util)
- Utilidades para adjuntar fotos como evidencia (URI).

#### [NEW] [LocationManager](file:///C:/Users/Joel/AndroidStudioProjects/prestamolab-ctma/app/src/main/java/com/example/prestamolab/util)
- Implementación de GPS para metadatos de la solicitud.

---

## Verification Plan

### Automated Tests
- `gradlew test`: Ejecución de tests unitarios (Mappers, DAOs con Room en memoria).
- `gradlew connectedAndroidTest`: Tests instrumentados en dispositivo/emulador.
- Pruebas de integración con `MockWebServer`.

### Manual Verification
- Flujo completo: Catálogo → Detalle → Solicitud (con GPS) → Listado → Devolución (con Foto) → Sincronización.
- Comprobar que los datos sobreviven al cierre de la app (Persistence).
- Modo avión: Comprobar que la app sigue funcionando con datos locales.
