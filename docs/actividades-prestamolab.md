# PréstamoLab CTMA: Gestión de Préstamos en Ambientes de Formación

## Introducción y Definición del Product Goal

El presente documento recopila las actividades de planificación, análisis y diseño incremental para la aplicación móvil **PréstamoLab CTMA**, orientada a optimizar la gestión, consulta y trazabilidad de los préstamos de herramientas y equipos tecnológicos dentro del centro de formación.

### Product Goal (Objetivo del Producto)
Mejorar la visibilidad, consulta en tiempo real y el seguimiento del estado de los préstamos de equipos de formación mediante un incremento móvil funcional que reduzca los tiempos de gestión y evite el extravío de inventario.

### Actores del Sistema
* **Solicitante:** Aprendiz o instructor encargado de registrar solicitudes de préstamo y consultar el estado de sus reservas.
* **Gestor / Administrador:** Encargado de aprobar, rechazar o gestionar el inventario de equipos disponibles en el laboratorio.

## Historias de Usuario y Criterios de Aceptación

### Historia de Usuario 1: Consulta de Inventario de Equipos
* **Como** solicitante (aprendiz o instructor),
* **Quiero** consultar la lista de equipos y herramientas disponibles en tiempo real,
* **Para** verificar la disponibilidad antes de realizar una solicitud de préstamo.

#### Criterios de Aceptación:
1. La aplicación debe mostrar una lista clara con el nombre, categoría y estado actual de cada equipo (Disponible / Ocupado).
2. Al seleccionar un equipo, el usuario debe poder visualizar detalles técnicos básicos e imagen de referencia.
3. Si el sistema no logra conectar con el servidor o base de datos local, debe mostrar un mensaje de error descriptivo con opción de reintentar.

### Historia de Usuario 2: Registro de Solicitud de Préstamo
* **Como** solicitante,
* **Quiero** registrar una solicitud de préstamo seleccionando uno o varios equipos y especificando el tiempo requerido,
* **Para** apartar formalmente los implementos necesarios para mi ambiente de formación.

#### Criterios de Aceptación:
1. El formulario de solicitud debe validar campos obligatorios (equipo seleccionado, fecha de devolución estimada y motivo del préstamo).
2. El sistema debe impedir la creación de solicitudes si el equipo seleccionado ya se encuentra en estado no disponible.
3. Una vez enviada la solicitud, el sistema debe registrar el estado inicial como "Pendiente" y mostrar un comprobante visual en pantalla.

## Modelo de Dominio y Arquitectura (MVVM)

### Arquitectura de Software
La aplicación **PréstamoLab CTMA** se construye bajo el patrón arquitectónico **MVVM (Model-View-ViewModel)**, garantizando la separación de responsabilidades:
* **View (V):** Interfaces desarrolladas en Jetpack Compose encargadas exclusivamente de renderizar la UI y capturar la interacción del usuario.
* **ViewModel (VM):** Componentes que gestionan el estado de la interfaz, procesan la lógica de negocio y se comunican con las fuentes de datos mediante flujos reactivos (`StateFlow`).
* **Model (M):** Capas de datos, modelos de dominio y repositorios que encapsulan la lógica de persistencia y obtención de información.

### Modelo de Datos (Entidades Principales)
1. **Equipo:**
    * `id`: Identificador único (Int / String)
    * `nombre`: Nombre del elemento o herramienta
    * `categoria`: Tipo de equipo (Computo, Medición, Herramienta manual)
    * `estado`: Disponibilidad actual (DISPONIBLE, PRESTADO, MANTENIMIENTO)
    * `imagenUrl`: Referencia visual del elemento

2. **SolicitudPrestamo:**
    * `id`: Identificador de la solicitud
    * `equipoId`: ID del equipo asociado
    * `solicitante`: Nombre o documento del usuario
    * `fechaSolicitud`: Timestamp de creación
    * `fechaDevolucionEstimada`: Plazo estipulado
    * `estado`: Estado actual (PENDIENTE, APROBADA, RECHAZADA, FINALIZADA)

