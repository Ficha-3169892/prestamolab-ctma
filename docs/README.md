
# PréstamoLab CTMA - Prototipo Android

## Descripción

PréstamoLab CTMA es un prototipo educativo desarrollado en Kotlin con Jetpack Compose para simular el proceso de préstamo de equipos y herramientas de laboratorio en el centro CTMA. Permite consultar catálogo, verificar disponibilidad, solicitar préstamos y gestionar las solicitudes vigentes.

## Arquitectura y Estructura

El proyecto sigue las recomendaciones de la Guía de Arquitectura de Android, separando UI, Lógica de Negocio y Datos:

* `model/`: Definición de entidades (`Equipo`, `SolicitudPrestamo`) y enumeraciones (`Estados.kt`).
* `data/repository/`: Contrato del repositorio (`PrestamoRepository`) e implementación compartida en memoria (`InMemoryPrestamoRepository`).
* `viewmodel/`: Manejo de estado inmutable mediante `StateFlow` y `PrestamoUiState`.
* `ui/`: Pantallas desarrolladas en Jetpack Compose (`catalogo`, `equipo`, `solicitud`, `misprestamos`).
* `navigation/`: Gestión de rutas centralizada mediante Navigation Compose (`AppNavigation.kt`).
* `util/`: Validaciones desacopladas de las reglas de negocio (`Validaciones.kt`).

## Reglas de Negocio Implementadas

* **RN-01 / RN-06:** Solo pueden solicitarse equipos `DISPONIBLE`. Al registrar una solicitud válida, el equipo pasa a `RESERVADO`.
* **RN-02:** El ambiente o destino es obligatorio.
* **RN-03:** El propósito debe tener entre 10 y 180 caracteres.
* **RN-04:** La duración estimada debe ser entre 1 y 8 horas.
* **RN-05:** Control de doble pulsación en el botón de guardado.
* **RN-07:** Solo las solicitudes en estado `SOLICITADA` pueden cancelarse, devolviendo el equipo a `DISPONIBLE`.
* **RN-08:** Manejo seguro de identificadores inexistentes en la navegación sin cierres abruptos.

## Requisitos e Instalación

1. Android Studio Hedgehog (o superior).
2. SDK de Android 24 mínimo (Android 7.0+).
3. Clonar el repositorio y sincronizar Gradle.
4. Ejecutar en emulador o dispositivo físico.

## Pruebas y Evidencias

Consulte la documentación de pruebas en los siguientes archivos:

* `BACKLOG.md`: Historias de usuario y product backlog.
* `SPRINT.md`: Sprint Goal y Definition of Done (DoD).
* `PRUEBAS.md`: Suite de 18 casos de prueba y matriz de trazabilidad.

---

