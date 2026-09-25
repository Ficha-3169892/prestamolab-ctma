# Contrato de endpoints: PréstamoLab CTMA

Guía, semana 8, actividad 23: *definir contrato de endpoints para equipos, préstamos y devoluciones*.

La API es la de Supabase: **PostgREST** para las tablas, **RPC** para las funciones de sesión y **Storage** para las fotos. La app la consume con `SupabaseRestClient` (OkHttp) desde `SupabasePrestamosDataSource` y `SupabaseUsuariosDataSource`. El esquema y los permisos están en `docs/supabase/001` a `009`.

## Convenciones de todas las peticiones

| Elemento | Valor |
|---|---|
| Base | `https://<proyecto>.supabase.co`, distinta por ambiente (`docs/AMBIENTES.md`); solo HTTPS |
| Cabeceras | `apikey: <anon key>`, `Authorization: Bearer <anon key>`, `Accept: application/json` |
| Sesión | `x-sesion: <token>`, el que devuelve `iniciar_sesion`. Las políticas RLS lo leen con `usuario_actual()` y `rol_actual()` |
| Formato | JSON UTF-8. Fechas en ISO-8601 (`FechasSupabase.kt`) |
| Escritura idempotente | `POST` con `Prefer: resolution=merge-duplicates,return=minimal`, un *upsert* por clave primaria (`uuid` generado en la app): reenviar el mismo registro no lo duplica |
| Tiempo de espera | 10 s para conectar, leer y escribir; 60 s para subir fotos |

## Sesión (RPC)

| Operación | Método y ruta | Cuerpo | Respuesta |
|---|---|---|---|
| Iniciar sesión (HU-10) | `POST /rest/v1/rpc/iniciar_sesion` | `{"p_identificador": "<correo o documento>", "p_hash": "<SHA-256 hex>"}` | `[{"id","email","full_name","role","token"}]`, o `[]` si las credenciales no son válidas |
| Validar sesión | `POST /rest/v1/rpc/sesion_valida` | `{}` | `true` / `false`. Se consulta antes de cada sincronización: con la sesión vencida, RLS devolvería listas vacías y el catálogo local se borraría |
| Cerrar sesión | `POST /rest/v1/rpc/cerrar_sesion` | `{}` | Invalida el token en el servidor |

## Equipos (`equipments`)

| Operación | Método y ruta | Quién | Cuerpo / respuesta |
|---|---|---|---|
| Listar (HU-01, HU-07) | `GET /rest/v1/equipments?select=id,name,category,status&order=name` | Ambos roles | `[{"id","name","category","status"}]` |
| Crear o editar (HU-12) | `POST /rest/v1/equipments` (upsert) | Instructor | `{"id","name","title","category","status"}`. `title` es NOT NULL y se envía igual a `name` |
| Cambiar estado (HU-03, HU-05) | `PATCH /rest/v1/equipments?id=eq.<id>` | Ambos roles | `{"status"}`. Es PATCH y no upsert porque el upsert exige todas las columnas NOT NULL (BUG-06) |
| Eliminar (HU-12) | `DELETE /rest/v1/equipments?id=eq.<id>` | Instructor | La app solo lo permite si el equipo no tiene préstamos |

`status`: `DISPONIBLE`, `RESERVADO` o `PRESTADO`.

## Préstamos (`loans`)

| Operación | Método y ruta | Quién | Cuerpo / respuesta |
|---|---|---|---|
| Listar (HU-04, HU-14) | `GET /rest/v1/loans?select=id,user_id,equipment_id,status,request_date,return_date,environment,purpose,duration_hours,reviewed_by,rejection_reason,latitude,longitude,solicitante:users!loans_user_id_fkey(full_name)&order=request_date` y, para el estudiante, `&user_id=eq.<id>` | Estudiante: solo los suyos. Instructor: todos | El nombre del solicitante llega en la misma consulta por la llave foránea |
| Crear, cancelar, aprobar o rechazar (HU-03, HU-04, HU-14) | `POST /rest/v1/loans` (upsert) | Estudiante crea y cancela; instructor aprueba y rechaza (lo exige un trigger del servidor) | `{"id","user_id","user_role":"ESTUDIANTE","equipment_id","status","request_date","return_date","environment","purpose","duration_hours","reviewed_by","rejection_reason","latitude","longitude"}` |

`status`: `SOLICITADA`, `PRESTADO`, `DEVUELTO`, `RECHAZADA` o `CANCELADA`. `return_date` es la **fecha límite** de devolución; la entrega real queda en `returns.return_date`.

## Devoluciones (`returns`)

| Operación | Método y ruta | Quién | Cuerpo / respuesta |
|---|---|---|---|
| Listar (HU-05, HU-07) | `GET /rest/v1/returns?select=id,loan_id,equipment_condition,notes,return_date,latitude,longitude` y, para el estudiante, `,loans!inner(user_id)&loans.user_id=eq.<id>` | Estudiante: las de sus préstamos. Instructor: todas | — |
| Registrar (HU-05, HU-13) | `POST /rest/v1/returns` (upsert) | Estudiante | `{"id","loan_id","equipment_condition","notes","return_date","latitude","longitude"}`. Las coordenadas son `null` si no hubo permiso o GPS |

## Actividades (`activities`) y evidencias (`evidences`, Storage)

| Operación | Método y ruta | Quién |
|---|---|---|
| Listar actividades (HU-11) | `GET /rest/v1/activities?select=id,title,description,location,scheduled_at,instructor_id&order=scheduled_at` | Ambos roles |
| Crear o editar actividad | `POST /rest/v1/activities` (upsert) `{"id","title","description","location","scheduled_at","instructor_id"}` | Instructor |
| Eliminar actividad | `DELETE /rest/v1/activities?id=eq.<id>` | Instructor |
| Subir foto (HU-08) | `POST /storage/v1/object/evidencias/<prestamo>/<evidencia>.jpg`, `Content-Type: image/jpeg`, `x-upsert: true` | Estudiante |
| Registrar evidencia | `POST /rest/v1/evidences` (upsert) `{"id","loan_id","stage","photo_url","taken_at","latitude","longitude"}` | Estudiante |

La foto se sube primero y luego se registra su URL pública (`/storage/v1/object/public/evidencias/...`). Si falla por la red o por un error temporal del servidor, la evidencia sigue **Local** y WorkManager la reintenta. Si el servidor la rechaza (4xx), queda **Fallida**.

## Errores y cómo los maneja la app

| Respuesta | Significado | Qué hace la app | Prueba |
|---|---|---|---|
| 2xx | Éxito | El registro local pasa a `SINCRONIZADO` | TC-HU07-01 |
| 401 | Sesión inválida o vencida | Cierra la sesión y lleva al login | TC-HU07-03 |
| 404 | Tabla o recurso inexistente | Muestra un aviso y conserva los datos locales | TC-HU07-04 |
| 408, 429, 5xx, sin red, timeout | Error temporal | WorkManager reintenta con espera exponencial; el registro sigue `PENDIENTE` | TC-HU07-05 |
| Otros 4xx (p. ej. RLS o un trigger rechaza el cambio) | El servidor no acepta ese registro | Ese registro queda en `ERROR` sin bloquear los demás, y no se pisa con la versión remota (BUG-08) | `SincronizadorPrestamosTest` |

Todas estas respuestas se simulan con MockWebServer en `SupabasePrestamosDataSourceTest` y `SupabaseUsuariosDataSourceTest`. La prueba opcional `SupabaseRealE2ETest` recorre el contrato contra el Supabase real.
