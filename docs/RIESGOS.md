# Riesgos técnicos — PréstamoLab CTMA

Los riesgos R-01 a R-05 se aceptaron durante los Sprints 5 a 8 y se trataron en el **Sprint 9** con
`docs/supabase/009_seguridad.sql`. La verificación es reproducible con `docs/seguridad/verificar_seguridad.py`
(resultados en `docs/seguridad/README.md`).

| ID | Riesgo | Causa original | Tratamiento aplicado | Estado |
|----|--------|----------------|----------------------|--------|
| R-01 | Contraseñas con SHA-256 sin sal | `users.password_hash` = SHA-256 calculado en la app | La app sigue enviando SHA-256; el servidor guarda **bcrypt(SHA-256)** con sal (`pgcrypto`) y lo verifica en `iniciar_sesion()`, con 1 s de demora por intento fallido | Mitigado y verificado |
| R-02 | El hash funcionaba como contraseña | La app filtraba `password_hash=eq.<hash>` en PostgREST | Se eliminó `password_hash`; la clave solo se verifica dentro de la función | Mitigado y verificado |
| R-03 | Tabla `users` legible con la anon key | `GRANT SELECT` y política pública (`003`) | Sin permiso de lectura; solo `id` y `full_name`, y solo con sesión | Mitigado y verificado |
| R-04 | Sin sesión en el servidor | Sesión solo local (DataStore) | Token de sesión de 7 días emitido por `iniciar_sesion()`, en la cabecera `x-sesion`; `cerrar_sesion()` lo invalida; la tabla `sesiones` no es legible | Mitigado y verificado |
| R-05 | Escritura abierta en todas las tablas | Políticas `using (true)` de `004` a `008` y políticas creadas a mano en el panel | RLS por rol en las 6 tablas, eliminación de toda política ajena al script y triggers con las reglas del negocio (un estudiante no se aprueba, no revisa, no renombra equipos) | Mitigado y verificado |

## Riesgos residuales

| ID | Riesgo | Causa | Impacto | Estado | Tratamiento posible |
|----|--------|-------|---------|--------|---------------------|
| R-06 | Subida abierta al bucket `evidencias` | Storage no recibe la cabecera `x-sesion`, así que sus políticas no pueden exigir sesión (`008`) | Cualquiera con la anon key puede subir JPEG de hasta 15 MB; las fotos se ven con solo conocer su URL (difícil de adivinar: incluye dos uuid) | Aceptado | Supabase Auth, o subir mediante una Edge Function que valide el token |
| R-07 | El estudiante puede cambiar el estado de cualquier equipo | Al reservar o devolver, la app envía el estado del equipo; el trigger solo impide cambiar nombre y categoría | Un estudiante con la anon key podría marcar un equipo como DISPONIBLE o PRESTADO por fuera de la app | Aceptado | Calcular el estado del equipo en el servidor con un trigger sobre `loans` |
| R-08 | La anon key va dentro del APK | Diseño de Supabase: la clave es pública por naturaleza | Sin sesión válida ya no da acceso a datos (R-03 a R-05); sí permite intentar logins | Aceptado | Limitar la tasa de peticiones por IP (Supabase Pro o un proxy) |
| R-09 | Sin bloqueo tras varios intentos fallidos | `iniciar_sesion()` solo agrega 1 s por intento | Un ataque de diccionario es lento pero posible contra claves débiles como `123456` | Aceptado (datos de demostración) | Contador de intentos por usuario y política de contraseñas |

## Cómo verificarlo

```
python docs/seguridad/verificar_seguridad.py
```

El script actúa como un atacante que extrajo la anon key del APK y comprueba 26 puntos de R-01 a R-05. Los
ataques de escritura apuntan a datos de demostración y deben ser rechazados; el de borrado usa un equipo de
prueba que el script crea y elimina.
