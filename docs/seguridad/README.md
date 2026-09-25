# Pruebas de seguridad — Sprint 9

## Verificación del servidor (25 de septiembre de 2026)

`python docs/seguridad/verificar_seguridad.py` contra el proyecto Supabase real, con la anon key del APK y
`docs/supabase/009_seguridad.sql` aplicado: **26 de 26 comprobaciones superadas**.

| Riesgo | Comprobación | Resultado |
|---|---|---|
| R-03 | `users` completo sin sesión | 401 `permission denied` |
| R-03 | `users?select=id,full_name` sin sesión | `[]` |
| R-02 | `users?select=password_hash` | 400: la columna ya no existe |
| R-04 | `sesiones?select=*` | 401 `permission denied` |
| R-05 | `equipments`, `loans`, `returns`, `evidences`, `activities` sin sesión | `[]` en las cinco |
| R-01 | Clave incorrecta | `[]` después de 1,3 s |
| R-02 | SHA-256 del hash como contraseña | `[]` |
| R-04 | Token inventado en `sesion_valida()` | `false` |
| R-05 | Estudiante crea un préstamo a nombre del instructor | 401 `violates row-level security policy` |
| R-05 | Estudiante pasa su solicitud a PRESTADO | 400 `Transición no permitida para un estudiante` |
| R-05 | Estudiante escribe `reviewed_by` | 400 `Solo el instructor revisa solicitudes` |
| R-05 | Estudiante renombra un equipo | 400 `Solo el instructor edita el inventario` |
| R-05 | Estudiante crea una actividad | 401 `violates row-level security policy` |
| R-05 | Estudiante borra un equipo | El equipo sigue existiendo |
| R-05 | Sin sesión, `PATCH` de un préstamo | Ninguna fila modificada |
| R-04 | Token tras `cerrar_sesion()` | `false` |

### Hallazgos durante la verificación

La primera ejecución encontró tres fallas del script `009`, corregidas y verificadas:

1. `iniciar_sesion()` fallaba (42804): `users.email`, `full_name` y `role` son `varchar` y la función devolvía
   `text`. PostgreSQL lo acepta al crear la función y falla al ejecutarla.
2. RLS no estaba activo en `users`, así que su política no se aplicaba.
3. Había políticas permisivas creadas fuera de los scripts. Como las políticas se combinan con OR, una de ellas
   permitió a un estudiante **borrar un equipo** (se restauró de inmediato). El script ahora elimina toda
   política que no define.

Lección: las políticas RLS se verifican contra la base real, con ataques, no solo leyendo el script.

## OWASP ZAP

No se ejecutó un escaneo activo: la API es la de Supabase, una infraestructura compartida, y atacarla con un
escáner automático puede violar sus términos de uso. Las comprobaciones de arriba cubren de forma dirigida lo
que ZAP buscaría en esta API (control de acceso, exposición de datos y autenticación).

Para revisar el tráfico de la app en modo **pasivo** (sin atacar el servidor):

1. Instalar ZAP en el computador e iniciar el proxy (por defecto en el puerto 8080).
2. En el teléfono, en la misma red Wi-Fi: Ajustes → Wi-Fi → proxy manual → IP del computador, puerto 8080.
3. Instalar el certificado raíz de ZAP en el teléfono. En Android 7+ la app debe confiar en certificados de
   usuario solo en la versión de depuración (`network_security_config` con `debug-overrides`).
4. Usar la app (login, solicitar, aprobar, sincronizar) y revisar en ZAP → Alertas los hallazgos pasivos:
   cabeceras, datos sensibles en URL, cookies. Comprobar también que ninguna petición contiene la contraseña en
   claro: solo su SHA-256 en el cuerpo de `iniciar_sesion` y el token en `x-sesion`.
