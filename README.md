# PréstamoLab CTMA - Prototipo Android

## 1. Propósito y Alcance
Prototipo educativo en Android Jetpack Compose para gestionar el préstamo de equipos de laboratorio (multímetros, kits de electrónica, etc.). Permite consultar catálogo, solicitar préstamos, validar reglas de negocio y hacer seguimiento del estado.

## 2. Arquitectura y Estructura
El proyecto sigue las pautas de arquitectura recomendadas por Android con separación clara de capas:
- **Model:** Clases de datos (`Equipo`, `SolicitudPrestamo`) y enumeraciones.
- **Data:** Contrato `PrestamoRepository` e implementación compartida `InMemoryPrestamoRepository`.
- **ViewModel:** `PrestamoViewModel` manejando el estado mediante `StateFlow` y `PrestamoUiState` de solo lectura.
- **UI:** Pantallas Compose desacopladas de la fuente de datos.

## 3. Instrucciones de Instalación y Ejecución
1. Clonar el repositorio: `git clone <URL_DEL_REPOSITORIO>`
2. Abrir el proyecto en Android Studio (versión compatible con JDK 21 / Compose).
3. Sincronizar Gradle y ejecutar en emulador o dispositivo físico con Android 8.0+ (API 26+).

## 4. Reglas de Negocio Implementadas
- **RN-01 / RN-06:** Solo equipos DISPONIBLES pueden solicitarse; pasan a RESERVADO al crearse la solicitud.
- **RN-03 / RN-04:** Validaciones de Propósito (10-180 caracteres) y Duración (1-8 horas).
- **RN-05:** Prevención de duplicados por doble pulsación.
- **RN-07:** Solo solicitudes en estado SOLICITADA pueden ser canceladas.

## 5. Pruebas y Decisiones de Calidad
- Suite de 18 casos de prueba ejecutada con resultado ACEPTABLE.
- Manejo de IDs inexistentes sin cierre abrupto.
- Cumplimiento de criterios de accesibilidad básicos (soporte a fuente 1.5× y texto indicativo en estados).
