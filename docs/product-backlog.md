# Product Backlog 

## HU-01 — Consultar catálogo

**Historia de usuario**

Como solicitante, quiero consultar un catálogo de equipos
para conocer los recursos disponibles y su estado.

### Criterios de aceptación

- CA-01.1: Dado que existen equipos registrados, cuando el usuario
  abre el catálogo, entonces se muestran nombre, categoría y estado.
- CA-01.2: Los equipos disponibles muestran el estado DISPONIBLE.
- CA-01.3: Los estados no dependen únicamente del color.

---

## HU-02 — Consultar detalle

**Historia de usuario**

Como solicitante, quiero consultar el detalle de un equipo
para conocer su información antes de solicitarlo.

### Criterios de aceptación

- CA-02.1: Un equipo con ID válido muestra su información.
- CA-02.2: Un ID inexistente muestra un estado recuperable.

---

## HU-03 — Registrar solicitud

**Historia de usuario**

Como solicitante, quiero registrar una solicitud de préstamo
de un equipo disponible para utilizarlo temporalmente.

### Criterios de aceptación

- CA-03.1: Un equipo DISPONIBLE permite iniciar una solicitud.
- CA-03.2: Una solicitud válida crea una única solicitud SOLICITADA.
- CA-03.3: La doble pulsación de Guardar no genera duplicados.

---

## HU-04 — Validar solicitud

**Historia de usuario**

Como solicitante, quiero que los datos sean validados antes
de guardar para evitar información incorrecta.

### Criterios de aceptación

- CA-04.1: El destino es obligatorio.
- CA-04.2: Propósito de 9 caracteres es inválido.
- CA-04.3: Propósito entre 10 y 180 caracteres es válido.
- CA-04.4: Propósito de 181 caracteres es inválido.
- CA-04.5: Duración fuera de 1 a 8 horas es inválida.
- CA-04.6: Duración entre 1 y 8 horas es válida.

---

## HU-05 — Controlar disponibilidad

**Historia de usuario**

Como sistema, quiero impedir solicitudes sobre equipos
no disponibles para evitar reservas inconsistentes.

### Criterios de aceptación

- CA-05.1: Un equipo RESERVADO no puede solicitarse.
- CA-05.2: Un equipo PRESTADO no puede solicitarse.
- CA-05.3: Al crear una solicitud válida, el equipo pasa a RESERVADO.

---

## HU-06 — Evitar duplicados

**Historia de usuario**

Como solicitante, quiero que una doble pulsación sobre Guardar
no genere solicitudes duplicadas.

### Criterios de aceptación

- CA-06.1: Dos pulsaciones rápidas producen una sola solicitud.

---

## HU-07 — Consultar mis solicitudes

**Historia de usuario**

Como solicitante, quiero consultar mis solicitudes para conocer
su estado actual.

### Criterios de aceptación

- CA-07.1: Las solicitudes registradas aparecen en la lista.
- CA-07.2: Si no existen solicitudes, se muestra un estado vacío.

---

## HU-08 — Consultar detalle de solicitud

**Historia de usuario**

Como solicitante, quiero consultar el detalle de una solicitud
para conocer toda la información del préstamo.

### Criterios de aceptación
- CA-08.1: Un ID válido muestra los datos de la solicitud.
- CA-08.2: Un ID inexistente no provoca cierre de la aplicación.

---

## HU-09 — Cancelar solicitud
**Historia de usuario**

Como solicitante, quiero cancelar una solicitud SOLICITADA
para desistir del préstamo.

### Criterios de aceptación
- CA-09.1: SOLICITADA puede pasar a CANCELADA.
- CA-09.2: Una solicitud CANCELADA no puede cancelarse nuevamente.
- CA-09.3: Otros estados no permiten cancelación.
