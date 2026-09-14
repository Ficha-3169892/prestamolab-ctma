# Especificación de Casos de Prueba

Este documento presenta los casos de prueba diseñados para validar el correcto funcionamiento de *
*PréstamoLab CTMA**, abarcando las pruebas unitarias (lógica del negocio) e instrumentadas (interfaz
de usuario) basadas en el Product Backlog.

## Pruebas Unitarias (Lógica / ViewModel)

| ID        | Componente / HU     | Descripción                                                                     | Criterio de Aceptación (CA) |
|:----------|:--------------------|:--------------------------------------------------------------------------------|:----------------------------|
| **TU-01** | `PrestamoViewModel` | Carga inicial correcta del catálogo de equipos sintéticos.                      | CA-01.1, CA-01.2            |
| **TU-02** | `PrestamoViewModel` | Búsqueda exitosa de un equipo por ID existente.                                 | CA-02.1                     |
| **TU-03** | `PrestamoViewModel` | Manejo de error controlado al buscar un equipo con ID no existente.             | CA-02.2                     |
| **TU-04** | `PrestamoViewModel` | Registro exitoso de una solicitud con todos los campos válidos.                 | CA-03.2, CA-04.3, CA-04.6   |
| **TU-05** | `PrestamoViewModel` | Rechazo de solicitud cuando el ambiente de destino está vacío.                  | CA-04.1                     |
| **TU-06** | `PrestamoViewModel` | Rechazo de solicitud cuando el propósito tiene menos de 10 caracteres.          | CA-04.2                     |
| **TU-07** | `PrestamoViewModel` | Rechazo de solicitud cuando el propósito tiene más de 180 caracteres.           | CA-04.4                     |
| **TU-08** | `PrestamoViewModel` | Rechazo de solicitud cuando la duración es menor a 1 hora (0 horas).            | CA-04.5                     |
| **TU-09** | `PrestamoViewModel` | Rechazo de solicitud cuando la duración es mayor a 8 horas (9 horas).           | CA-04.5                     |
| **TU-10** | `PrestamoViewModel` | Intento de registrar solicitud sobre un equipo con estado `RESERVADO`.          | CA-05.1                     |
| **TU-11** | `PrestamoViewModel` | Intento de registrar solicitud sobre un equipo con estado `PRESTADO`.           | CA-05.2                     |
| **TU-12** | `PrestamoViewModel` | Cambio de estado automático del equipo a `RESERVADO` al crear solicitud válida. | CA-05.3                     |
| **TU-13** | `PrestamoViewModel` | Control de mensajes de error de repositorio mapeados correctamente al UI State. | General                     |
| **TU-14** | `PrestamoViewModel` | Cancelación exitosa de una solicitud en estado `SOLICITADA`.                    | CA-09.1                     |
| **TU-15** | `PrestamoViewModel` | Rechazo de la cancelación si la solicitud ya está en estado `CANCELADA`.        | CA-09.2                     |

---

## Pruebas Instrumentadas (UI / Integración)

| ID        | Pantalla / Flujo     | Acción / Verificación                                                                       | Criterio de Aceptación (CA) |
|:----------|:---------------------|:--------------------------------------------------------------------------------------------|:----------------------------|
| **TI-01** | Catálogo             | Visualización correcta de títulos y tarjetas de equipos iniciales.                          | CA-01.1                     |
| **TI-02** | Catálogo             | Comprobación de que el estado se visualiza en texto legible sin depender solo de color.     | CA-01.3                     |
| **TI-03** | Navegación           | Navegación correcta hacia la pantalla de Detalle al pulsar un equipo.                       | CA-02.1                     |
| **TI-04** | Detalle              | Comprobación de visibilidad de datos del equipo (Nombre, Categoría, Estado).                | CA-02.1                     |
| **TI-05** | Detalle              | Visualización del botón de solicitud solo si el equipo está `DISPONIBLE`.                   | CA-03.1                     |
| **TI-06** | Detalle              | Ocultamiento del botón de solicitud si el equipo no está disponible.                        | CA-05.1                     |
| **TI-07** | Detalle / Navegación | Botón "Volver" regresa correctamente al Catálogo desde Detalles.                            | Usabilidad                  |
| **TI-08** | Solicitud            | Validación visual en vivo al no dejar ingresar más de 180 caracteres en propósito.          | CA-04.4                     |
| **TI-09** | Solicitud / UI       | Visualización del mensaje de error en pantalla al intentar enviar destino vacío.            | CA-04.1                     |
| **TI-10** | Solicitud / UI       | Visualización de error en pantalla con propósito inválido menor de 10 caracteres.           | CA-04.2                     |
| **TI-11** | Solicitud / UI       | Visualización de error en pantalla con duración inválida (fuera del rango 1-8).             | CA-04.5                     |
| **TI-12** | Flujo Completo       | Registro exitoso y redirección automática hacia la pantalla de Mis Préstamos.               | CA-07.1                     |
| **TI-13** | Mis Préstamos        | Visualización de estado vacío si el usuario no cuenta con préstamos creados.                | CA-07.2                     |
| **TI-14** | Mis Préstamos        | Cancelación interactiva de la solicitud y actualización visual de la tarjeta a `CANCELADA`. | CA-09.1                     |
| **TI-15** | Mis Préstamos        | El botón "Cancelar solicitud" desaparece una vez que la solicitud es cancelada.             | CA-09.3                     |
