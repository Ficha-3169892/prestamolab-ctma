# Mi Formación CTMA — Instrucciones para Gemini

## Contexto del proyecto

Este proyecto corresponde a "Mi Formación CTMA" del programa ADSO del SENA.

Estamos implementando la Semana 6 de la guía:
"Persistencia local y fuente única de verdad".

El objetivo principal es evolucionar la aplicación para utilizar:

* Room para datos estructurados.
* DataStore para preferencias.
* Repository como única puerta de acceso a los datos.
* ViewModel como intermediario entre UI y Repository.
* Flow para observar cambios.
* Migraciones de Room sin pérdida de datos.
* Pruebas de DAO y migraciones.

## Regla principal de arquitectura

La arquitectura objetivo es:

Compose → ViewModel → Repository → Room/DataStore

La UI nunca debe acceder directamente a:

* DAO.
* FormacionDatabase.
* DataStore.
* SQLite.

El ViewModel tampoco debe acceder directamente al DAO.

El Repository será la única puerta de acceso de las capas superiores a los datos.

Room será la fuente única de verdad para actividades y competencias.

DataStore será utilizado exclusivamente para preferencias pequeñas del usuario.

No mantener listas maestras duplicadas en memoria si Room puede proporcionar el dato mediante Flow.

## Persistencia

Las actividades y competencias deben persistir mediante Room.

Las preferencias de filtro, orden o modo de visualización deben persistir mediante Preferences DataStore.

El texto temporal de formularios que todavía no se ha guardado debe permanecer como estado efímero de UI.

Los datos deben sobrevivir al cierre y reapertura de la aplicación.

La aplicación debe poder funcionar sin conexión para las operaciones locales.

## Room

Utilizar Room 3 estable según las dependencias compatibles con el proyecto.

No mezclar imports de Room antiguos y Room 3.

Verificar cuidadosamente los imports antes de modificar código.

Utilizar KSP para la generación de código.

Mantener minSdk 23 o superior.

Crear las entidades necesarias:

* ActividadEntity
* CompetenciaEntity

La relación entre Actividad y Competencia debe representar una relación 1:N cuando corresponda al dominio existente.

Utilizar claves primarias, claves foráneas e índices apropiados.

No colocar anotaciones de persistencia en los modelos de dominio si pueden mantenerse separados mediante mapeadores.

Crear mapeadores claros entre:

* Modelo de dominio → Entity
* Entity → Modelo de dominio

## DAO

El DAO debe proporcionar operaciones para:

* Crear actividades.
* Consultar actividades.
* Consultar por ID.
* Actualizar actividades.
* Eliminar actividades.
* Buscar actividades.
* Consultar la relación entre actividades y competencias.
* Exponer lecturas observables mediante Flow cuando corresponda.

Las operaciones de escritura deben utilizar suspend cuando corresponda.

Las consultas deben estar diseñadas para evitar accesos innecesarios a la base de datos.

## Database

Crear una única instancia de FormacionDatabase para toda la aplicación.

No crear una nueva instancia de Room desde una pantalla o ViewModel.

La creación de la base de datos debe permanecer centralizada dentro del mecanismo de inyección/contenedor de dependencias existente en el proyecto.

Respetar la arquitectura actual del proyecto en lugar de introducir una arquitectura diferente sin necesidad.

## Repository

Implementar RoomActividadRepository respetando el contrato existente del Repository.

La UI debe poder seguir utilizando el contrato del Repository sin conocer que internamente ahora existe Room.

No acoplar Compose, rutas ni pantallas a Room.

No acceder al DAO desde las pantallas.

No acceder al DAO directamente desde los ViewModels.

Toda operación de datos debe pasar por el Repository.

## DataStore

Implementar PreferenciasRepository utilizando Preferences DataStore.

Las preferencias deben exponerse mediante Flow.

No escribir preferencias directamente desde Compose.

No crear múltiples instancias innecesarias de DataStore.

Mantener DataStore separado de Room.

## Migraciones

La base de datos debe evolucionar mediante migraciones explícitas.

Implementar la migración de versión 1 a versión 2.

La migración 1 → 2 debe agregar:

completada

El valor inicial de la columna completada debe ser false.

La migración debe conservar los datos existentes.

NO utilizar destructive migration para resolver este cambio.

No borrar tablas ni datos existentes para evitar errores de migración.

Exportar y mantener los esquemas versionados en la carpeta correspondiente del proyecto.

## Cambios sobre el proyecto existente

Antes de modificar código:

1. Inspeccionar la arquitectura existente.
2. Identificar modelos, ViewModels, Repository, pantallas y navegación.
3. Reutilizar código existente siempre que sea correcto.
4. Evitar reescribir componentes completos sin necesidad.
5. Evitar modificar la UI si no es necesario.
6. No cambiar nombres públicos o contratos existentes sin justificarlo.
7. No eliminar funcionalidad existente que todavía sea necesaria.

No crear duplicados de clases, repositories, bases de datos o modelos.

Antes de crear una nueva clase, comprobar si ya existe una implementación equivalente.

## UI y Compose

La UI debe continuar recibiendo estado desde los ViewModels.

Las pantallas deben enviar eventos al ViewModel.

Las pantallas no deben ejecutar SQL.

Las pantallas no deben utilizar DAOs.

Las pantallas no deben crear instancias de Room.

Las pantallas no deben escribir directamente en DataStore.

La UI debe reaccionar automáticamente a cambios provenientes de Flow.

Evitar recargas manuales de listas cuando Room ya proporciona el flujo observable.

## Manejo de errores

Un ID inexistente no debe provocar un cierre inesperado de la aplicación.

La aplicación debe mostrar estados controlados cuando un registro no existe.

Los errores de persistencia deben tratarse de forma segura y comprensible.

No ocultar excepciones importantes.

No resolver errores simplemente eliminando código relacionado.

## Pruebas

Crear o mantener pruebas para DAO que cubran como mínimo:

* Insertar.
* Consultar.
* Actualizar.
* Eliminar.
* Buscar.
* Consultar relaciones.

Crear una prueba específica para la migración 1 → 2.

La prueba de migración debe comprobar que:

* Los datos existentes permanecen.
* La nueva columna existe.
* Los registros existentes reciben false como valor inicial.

Preferir pruebas reproducibles y aisladas.

## Compilación y verificación

Después de realizar cambios importantes:

1. Compilar el proyecto.
2. Ejecutar las pruebas correspondientes.
3. Revisar errores de Gradle.
4. Revisar errores de Kotlin.
5. Corregir los problemas encontrados.
6. Volver a ejecutar la verificación.

No declarar una tarea terminada si el proyecto no compila cuando la compilación sea relevante para la tarea.

## Control de cambios

Realizar cambios pequeños y relacionados.

No modificar archivos que no sean necesarios para la tarea actual.

Antes de eliminar código, comprobar si existe alguna referencia hacia él.

No modificar configuraciones globales del proyecto sin necesidad.

No añadir dependencias innecesarias.

No versionar:

* Bases de datos locales.
* Contraseñas.
* Tokens.
* Secretos.
* Archivos .env con credenciales reales.

## Git

Los commits deben ser descriptivos y seguir Conventional Commits cuando sea posible.

Ejemplos:

feat: add Room persistence

feat: add DataStore preferences

feat: add Room migration 1 to 2

test: add DAO tests

test: add migration test

docs: update README for persistence layer

No hacer commits gigantes que mezclen funcionalidades no relacionadas.

## README y documentación

Al finalizar la implementación, actualizar el README con:

* Arquitectura.
* Room.
* DataStore.
* Repository.
* Migración.
* Pruebas.
* Instrucciones de ejecución.
* Decisiones importantes.
* Limitaciones conocidas.

## Regla de trabajo con Gemini

Antes de implementar una tarea importante:

1. Analizar primero los archivos relacionados.
2. Explicar brevemente qué archivos se modificarán.
3. Implementar solamente lo necesario.
4. Ejecutar compilación o pruebas cuando corresponda.
5. Corregir errores encontrados.
6. Entregar un resumen de cambios.
7. Indicar qué quedó pendiente.

No asumir que una clase o archivo funciona sin inspeccionarlo.

No inventar archivos ni APIs que no existen.

Cuando exista una implementación previa en el proyecto, adaptarla antes de crear otra.

## Prioridad

La prioridad es:

1. Mantener el proyecto funcionando.
2. Cumplir los requisitos de Semana 6.
3. Mantener la arquitectura existente.
4. Evitar duplicación.
5. Evitar pérdida de datos.
6. Mantener cambios simples y verificables.
7. Documentar lo implementado.

## Criterios finales de aceptación

La implementación se considera completa cuando:

* Las actividades permanecen después de cerrar y abrir la aplicación.
* Crear, editar y eliminar actualiza la lista sin recarga manual.
* Un ID inexistente se maneja sin crash.
* La búsqueda y los filtros funcionan.
* Las preferencias sobreviven al reinicio.
* Existe relación Actividad → Competencia.
* Existe RoomActividadRepository.
* Existe PreferenciasRepository.
* Room es la fuente única de verdad.
* DataStore conserva preferencias.
* Existe migración 1 → 2.
* La migración conserva los datos.
* completada comienza en false.
* Las pruebas DAO funcionan.
* La prueba de migración funciona.
* Los schemas están versionados.
* El README está actualizado.

## Regla de seguridad

No realizar cambios destructivos sobre datos o estructura sin una migración explícita.

No eliminar datos existentes para hacer que una migración funcione.

No reemplazar una arquitectura funcional por otra solamente por preferencia personal.

Cuando haya varias soluciones válidas, escoger la que requiera menos cambios sobre el proyecto existente y mantenga mejor la compatibilidad.
