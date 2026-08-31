# PréstamoLab
Aplicación móvil Android para consultar equipos y herramientas de
formación, registrar solicitudes de préstamo y consultar su estado.

## Descripción
PréstamoLab busca mejorar la trazabilidad y consulta de préstamos de
equipos y herramientas mediante una aplicación móvil sencilla.

La aplicación permite consultar la disponibilidad de los equipos,
registrar solicitudes de préstamo válidas, consultar las solicitudes
realizadas y cancelar aquellas que todavía se encuentran en estado
`SOLICITADA`.

Los datos utilizados son sintéticos y se almacenan temporalmente
mediante un repositorio en memoria.

## Objetivo
Permitir que un solicitante pueda:

- Consultar el catálogo de equipos.
- Conocer el estado y disponibilidad de cada equipo.
- Consultar el detalle de un equipo.
- Registrar una solicitud de préstamo.
- Validar los datos antes de registrar una solicitud.
- Consultar sus solicitudes.
- Consultar el detalle de una solicitud.
- Cancelar solicitudes que estén en estado `SOLICITADA`.
- Mantener coherencia entre el estado de las solicitudes y la
  disponibilidad de los equipos.
- Evitar solicitudes duplicadas.

## Tecnologías
- Kotlin
- Android
- Jetpack Compose
- Material 3
- Navigation Compose
- ViewModel
- StateFlow
- Repository Pattern
- Repositorio en memoria

## Arquitectura
El proyecto utiliza una separación por responsabilidades:

```text
UI
 ↓
ViewModel
 ↓
Repository
 ↓
Datos en memoria