# Riesgos técnicos — PréstamoLab CTMA

Hallazgos conocidos que se aceptan temporalmente y se verifican en las pruebas no funcionales de seguridad del **Sprint 9** (OWASP ZAP / MASVS).

| ID | Riesgo | Causa | Impacto | Estado | Tratamiento previsto |
|----|--------|-------|---------|--------|----------------------|
| R-01 | Contraseñas con SHA-256 sin salt | `users.password_hash` = SHA-256 calculado en la app (`HashUtils`) | Claves comunes como `123456` se revierten al instante con tablas precalculadas | Aceptado (Sprint 5–8) | bcrypt con `pgcrypto` dentro de una función RPC `SECURITY DEFINER`, o Supabase Auth |
| R-02 | El hash funciona como contraseña | La app compara `password_hash=eq.<hash>` directamente en PostgREST | Quien obtenga un hash puede iniciar sesión sin conocer la clave | Aceptado (Sprint 5–8) | Verificar la clave en el servidor (RPC) y no exponer la columna |
| R-03 | Tabla `users` legible con la anon key | `GRANT SELECT` a `anon` y política RLS de `SELECT` público (`docs/supabase/003_acceso_lectura_users.sql`); la anon key va dentro del APK | Cualquiera puede listar correos, documentos, roles y hashes | Aceptado (Sprint 5–8) | Quitar el `SELECT` público y exponer solo una función de login |
| R-04 | Sin sesión en el servidor | La sesión es local (DataStore) y no hay token firmado | Las operaciones remotas futuras no pueden verificar quién las hace | Abierto | Token de Supabase Auth o JWT emitido por la función de login |
| R-05 | Escritura abierta en equipments, loans y returns | `docs/supabase/004_sincronizacion.sql` concede INSERT/UPDATE a `anon` con políticas `using (true)` para la sincronización; `006_inventario.sql` agrega DELETE en `equipments` (HU-12) `007_actividades.sql` abre `activities` (HU-11) y `008_evidencias.sql` abre `evidences` y un bucket público de Storage (HU-08) | Cualquiera con la anon key puede crear o modificar préstamos de otro usuario, cambiar el estado de un equipo, borrar equipos sin préstamos, crear, editar y borrar actividades, o subir fotos al bucket; las fotos de evidencia se ven con solo conocer su URL | Aceptado (Sprint 6–8) | Políticas RLS por `auth.uid()` (requiere Supabase Auth) o escrituras mediante funciones RPC que validen al usuario |

## Cómo verificarlo en el Sprint 9

- **R-01 / R-02:** consultar `users?select=email,password_hash` con la anon key y buscar el hash en una tabla precalculada.
- **R-03:** repetir la consulta sin sesión en la app; debe fallar una vez aplicado el tratamiento.
- **R-04:** interceptar las peticiones con ZAP y comprobar que ninguna depende solo de datos del cliente.
- **R-05:** con la anon key, hacer upsert de un préstamo con el `user_id` de otro usuario; debe rechazarse.
