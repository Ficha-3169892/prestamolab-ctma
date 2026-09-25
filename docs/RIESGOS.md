# Riesgos técnicos — PréstamoLab CTMA

Dos grupos: los **riesgos de calidad por historia** (R-10 a R-23), que orientan qué probar en cada HU, y los
**riesgos de seguridad** (R-01 a R-09), tratados en el Sprint 9. La matriz `docs/MATRIZ_TRAZABILIDAD.md` enlaza
cada historia con sus riesgos, casos de prueba, commits y defectos.

## Riesgos de calidad por historia

Probabilidad e impacto: A (alto), M (medio), B (bajo). Es una valoración cualitativa hecha al documentar los riesgos (2026-09-25) a partir de los defectos encontrados y de lo crítico de cada flujo; el equipo puede ajustarla. La prioridad
sale de combinar ambos: A·A y A·M se prueban primero y con más casos. "Se materializó" indica los defectos reales en
los que el riesgo ocurrió (`DEFECTOS.md`).

| ID | Historia | Riesgo | Prob. | Impacto | Prioridad | Mitigación: casos de prueba | Se materializó |
|----|----------|--------|-------|---------|-----------|-----------------------------|----------------|
| R-10 | HU-01 | El catálogo muestra un estado desactualizado y el estudiante intenta pedir un equipo que ya no está disponible | M | M | Media | TC-HU01-01, TC-HU01-02, TC-HU02-04 (el estado llega por Flow desde Room) | — |
| R-11 | HU-02 | Un id de equipo inexistente (enlace viejo, registro borrado) cierra la app | M | A | Alta | TC-HU02-02 | — |
| R-12 | HU-03 | Se crean solicitudes duplicadas o con datos fuera de los límites (propósito, duración, ambiente) | A | A | Alta | TC-HU03-01 a TC-HU03-05 (valores límite, doble pulsación), ciclo TDD de `ReglasSolicitud` | BUG-03, BUG-13 |
| R-13 | HU-04 | Mis préstamos muestra datos de otro usuario o datos incompletos | M | A | Alta | TC-HU04-01, TC-HU04-02 | BUG-12 |
| R-14 | HU-05 | Se registra una devolución sobre un préstamo no activo, dos veces, o no se puede confirmar | A | A | Alta | TC-HU05-01, TC-HU05-02, TC-HU05-05, recorrido `RegresionFlujoCriticoUiTest` | BUG-04, BUG-09, BUG-10 |
| R-15 | HU-06 | Se pierden datos locales al reiniciar la app o al migrar la base de Room | M | A | Alta | TC-HU06-01 a TC-HU06-04 (`MigracionTest`, `PersistenciaSinConexionTest`) | — |
| R-16 | HU-07 | La sincronización pisa cambios locales o falla por diferencias con el esquema remoto | A | A | Alta | TC-HU07-01 a TC-HU07-05 (MockWebServer: 401, 404, 5xx, timeout) | BUG-06, BUG-07, BUG-08 |
| R-17 | HU-08 | Una foto se pierde o queda sin subir sin que el usuario lo sepa | M | M | Media | TC-HU08-02, TC-HU08-05, estados Local/Subiendo/Sincronizada/Fallida (`EstadoEvidenciaTest`) | — |
| R-18 | HU-09 | El recordatorio no llega, o llega para un préstamo ya devuelto | M | M | Media | TC-HU09-01, TC-HU09-03, TC-HU09-04 | — |
| R-19 | HU-10 | Un rol accede a funciones del otro, o nadie puede iniciar sesión | M | A | Alta | TC-HU10-01 a TC-HU10-07 y R-01 a R-05 (`verificar_seguridad.py`) | BUG-05, BUG-14 |
| R-20 | HU-11 | Se guardan actividades inválidas, o el estudiante puede modificarlas | B | M | Baja | TC-HU11-02, TC-HU11-05 | — |
| R-21 | HU-12 | Eliminar un equipo con préstamos deja el historial inconsistente | M | A | Alta | TC-HU12-04, TC-HU12-05 | — |
| R-22 | HU-13 | Sin GPS o sin permiso, la devolución se bloquea; o la ubicación se captura sin que el usuario lo decida | M | M | Media | TC-HU13-01, TC-HU13-05 (`docs/CAPACIDAD_GPS.md`) | — |
| R-23 | HU-14 | Se aprueba una solicitud ya cancelada, o se rechaza sin motivo | M | M | Media | TC-HU14-03, TC-HU14-04 | — |

## Riesgos de seguridad

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
