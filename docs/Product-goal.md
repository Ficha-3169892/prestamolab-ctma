# PréstamoLab CTMA

## Problema

En los ambientes de formación existen equipos, herramientas y recursos compartidos que pueden necesitarse temporalmente para realizar prácticas. Cuando el control de estos recursos se realiza de forma manual, puede ser difícil conocer cuáles están disponibles, cuáles están reservados o prestados y qué solicitudes se encuentran activas.

## Usuarios

### Solicitante demo

Aprendiz o instructor que consulta los equipos disponibles y registra una solicitud de préstamo.

### Gestor simulado

Rol conceptual utilizado para representar cambios de estado de las solicitudes durante las actividades de prueba. No requiere autenticación real.

### Instructor

Facilita los datos del laboratorio, observa el proceso y valida las evidencias generadas por la aplicación.

## Necesidades

* Consultar los equipos disponibles.
* Conocer la disponibilidad de cada equipo.
* Consultar el detalle de un equipo.
* Registrar una solicitud de préstamo.
* Seleccionar el ambiente o destino.
* Registrar el propósito del préstamo.
* Registrar la duración estimada.
* Validar los datos antes de guardar.
* Consultar las solicitudes realizadas.
* Consultar el detalle de una solicitud.
* Cancelar una solicitud que se encuentre en estado SOLICITADA.
* Evitar solicitudes duplicadas.
* Mantener coherencia entre las solicitudes y la disponibilidad de los equipos.
* Controlar identificadores inexistentes sin cerrar la aplicación.

## Restricciones

* Aplicación móvil Android.
* Desarrollo con Kotlin y Jetpack Compose.
* Datos sintéticos y simulados.
* Uso de un repositorio en memoria.
* No se utilizarán datos personales reales.
* No se implementará autenticación real en este incremento.
* No se implementarán procedimientos institucionales reales.
* No se implementará un inventario físico real de equipos o herramientas.

## Valor esperado

La aplicación permitirá consultar de manera sencilla los equipos y herramientas disponibles, registrar solicitudes de préstamo y realizar seguimiento de su estado, manteniendo una relación coherente entre las solicitudes y la disponibilidad de los equipos.

## Product Goal

Mejorar la consulta y trazabilidad de los préstamos de equipos y herramientas de formación mediante una aplicación móvil que permita consultar el catálogo, conocer la disponibilidad, registrar solicitudes y visualizar su estado de forma clara y organizada.