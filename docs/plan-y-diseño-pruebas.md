# Plan y Diseño de Pruebas: PréstamoLab CTMA

## 1. Introducción
El presente documento define la estrategia de pruebas para validar el correcto funcionamiento del incremento móvil de **PréstamoLab CTMA**, asegurando la estabilidad de la lógica de negocio en los ViewModels y la integridad de los datos en las solicitudes de préstamo.

## 2. Tipos de Pruebas Implementadas
* **Pruebas Unitarias (Unit Tests):** Validación de los casos de uso, transformaciones de estado en el `PrestamoViewModel` y reglas de negocio del dominio utilizando JUnit y bibliotecas de aserciones.
* **Pruebas de Componentes / UI (UI Tests):** Verificación de la correcta renderización de los elementos en Jetpack Compose y la respuesta ante interacciones táctiles en formularios.

## 3. Casos de Prueba Clave
1. **CP-01: Validación de campos vacíos en el formulario de préstamo**
    * *Descripción:* Intentar registrar una solicitud sin seleccionar equipo o sin fecha de devolución.
    * *Resultado esperado:* El sistema debe bloquear el envío y mostrar un mensaje de error en los campos requeridos.
2. **CP-02: Cambio de estado exitoso al crear solicitud**
    * *Descripción:* Registrar una solicitud con datos válidos para un equipo disponible.
    * *Resultado esperado:* La solicitud se agrega a la lista con estado "Pendiente" y el equipo cambia su disponibilidad.