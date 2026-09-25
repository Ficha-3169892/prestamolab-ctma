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

## Revisión con OWASP ZAP (25 de septiembre de 2026)

**Resultado: 0 alertas de riesgo alto, 0 de riesgo medio, 2 bajas y 4 informativas, ninguna atribuible a la app.**
Informe completo (con la URL del proyecto reemplazada por `<proyecto>`): [`zap/informe-zap.md`](zap/informe-zap.md).

### Cómo se hizo

- **Modo pasivo:** ZAP 2.17.0 (paquete multiplataforma, checksum verificado) como proxy local, sin arañas ni
  escaneo activo: la API es la infraestructura compartida de Supabase y atacarla con un escáner automático puede
  violar sus términos de uso. ZAP analiza las peticiones y respuestas reales.
- **Tráfico real de la app:** `SupabaseRealE2ETest` con `-e zapProxy 127.0.0.1:8090 -e zapCa <certificado de ZAP>`
  (login por RPC, sincronización, inventario, actividades e intento bloqueado del estudiante) desde el Xiaomi, con
  `adb reverse tcp:8090 tcp:8090`. Solo el tráfico de esa prueba pasó por ZAP: no se cambió la red del teléfono ni
  se instaló ningún certificado en él. **56 mensajes.**
- **Tráfico de ataque:** `verificar_seguridad.py` por el mismo proxy (26 de 26 superadas). **32 mensajes.**

### Hallazgos y decisión

| Alerta | Riesgo | Veces | Análisis | Decisión |
|---|---|---|---|---|
| Cookie con `SameSite=None` (`__cf_bm`) | Bajo | 26 | La pone Cloudflare (protección anti bots delante de Supabase); la app no usa cookies | No aplica |
| Divulgación de marcas de tiempo Unix | Bajo | 77 | Vencimiento de esa cookie y `created_at` de los datos; no revela información sensible | No aplica |
| Cookie con dominio amplio (`supabase.co`) | Informativo | 26 | Misma cookie de Cloudflare | No aplica |
| Respuesta de gestión de sesión | Informativo | 27 | El `token` de `iniciar_sesion()` (R-04): esperado, viaja en el cuerpo por HTTPS y nunca en la URL | Correcto |
| Revisar `Cache-Control` | Informativo | 15 | Las respuestas de la API no prohíben la caché; la app no guarda caché HTTP | Aceptado |
| Información sensible en la URL (`user_id`) | Informativo | 2 | La sincronización del estudiante filtra por `user_id=eq.<uuid>`; un uuid no es secreto y desde `009` el servidor ya limita la consulta a los préstamos propios | Aceptado |

Además se comprobó en el tráfico capturado que la contraseña nunca viaja en claro: solo su SHA-256, en el cuerpo de
`iniciar_sesion`, y el token de sesión en la cabecera `x-sesion`.

### Cómo repetirla

1. Descargar ZAP (paquete multiplataforma; requiere Java 17 o superior) e iniciarlo como proxy local:
   `java -jar zap-2.17.0.jar -daemon -host 127.0.0.1 -port 8090 -config api.disablekey=true`
2. `adb reverse tcp:8090 tcp:8090` y obtener el certificado raíz:
   `curl -o zap-ca.pem http://127.0.0.1:8090/OTHER/core/other/rootcert/`
3. Ejecutar la prueba por el proxy:
   `adb shell am instrument -w -r -e e2e true -e zapProxy 127.0.0.1:8090 -e zapCa $(base64 -w0 zap-ca.pem) -e class com.example.prestamolab.e2e.SupabaseRealE2ETest com.example.prestamolab.test/com.example.prestamolab.PrestamoLabTestRunner`
4. Ver las alertas en `http://127.0.0.1:8090/JSON/alert/view/alertsSummary/` y generar el informe con la API de
   reportes. Al terminar: `adb reverse --remove tcp:8090`.
