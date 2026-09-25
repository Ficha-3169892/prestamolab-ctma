# Registro de Defectos (Bug Log) – PréstamoLab CTMA

## BUG-01 — Mis solicitudes no tiene botón para volver

**Build:** 0.1.0

**Título:** La pantalla Mis solicitudes no presenta una opción visible para regresar.

**Precondición:** El usuario se encuentra en la pantalla "Mis solicitudes".

**Pasos para reproducir:**
1. Abrir la aplicación.
2. Entrar a "Mis solicitudes".
3. Intentar regresar a la pantalla anterior.

**Resultado esperado:**
La pantalla debe proporcionar una opción clara para regresar a la pantalla anterior.

**Resultado obtenido:**
No existe un botón "Volver" visible dentro de la pantalla.

**Severidad:** Media | **Prioridad:** Media

**Estado:** Cerrado / Corregido

**Resolución:** Se agregó un `TopAppBar` con el ícono `Icons.AutoMirrored.Filled.ArrowBack` en `MisSolicitudesScreen.kt`.

**Tipo:** Usabilidad / Navegación

---

## BUG-02 — Vista previa de evidencia fotográfica en devolución no actualizaba tras tomar la foto

**Build:** 0.2.0

**Título:** Al tomar la foto con la cámara en Registrar Devolución, el recuadro de evidencia permanecía vacío.

**Precondición:** El usuario se encuentra en la pantalla "Registrar Devolución".

**Pasos para reproducir:**
1. Abrir la aplicación e ingresar al detalle de una solicitud prestada.
2. Presionar el botón "Devolver".
3. Presionar el botón "Cámara", tomar la foto y aceptar.

**Resultado esperado:**
El recuadro superior debe mostrar la foto recién capturada mediante `AsyncImage`.

**Resultado obtenido:**
La URI de la foto se asignaba de forma prematura antes de lanzar el intent de la cámara, causando que Coil intentara cargar un archivo de 0 bytes y mostrara un marco gris vacío.

**Severidad:** Alta | **Prioridad:** Alta

**Estado:** Cerrado / Corregido

**Resolución:** Se separó la URI de la cámara en una variable temporal (`tempCameraUri`) y solo se asigna a `fotoUri` cuando la respuesta del Launcher confirma éxito (`success == true`).

**Tipo:** Funcional / UI
