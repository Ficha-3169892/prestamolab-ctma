# Ambientes dev, stage y prod

Guía, semana 9, actividad 34: *configurar variables/URLs por ambiente sin exponer secretos*.

## Cómo se elige el ambiente

El ambiente se elige al compilar, con la propiedad de Gradle `ambiente` o con la variable de entorno `PRESTAMOLAB_AMBIENTE`. Si no se indica, se usa **dev**.

```bash
gradlew assembleDebug                      # dev (valor por defecto)
gradlew assembleRelease -Pambiente=stage
gradlew assembleRelease -Pambiente=prod
```

Se usa una propiedad y no *product flavors* para que las tareas conserven su nombre (`testDebugUnitTest`, `assembleDebug`, `lintDebug`). Así el CI y los comandos de `docs/PLAN_PRUEBAS.md` no cambian.

## De dónde salen la URL y la clave

| Ambiente | URL | Clave anon | Estado actual |
|---|---|---|---|
| dev | `SUPABASE_URL_DEV`, o `SUPABASE_URL` | `SUPABASE_ANON_KEY_DEV`, o `SUPABASE_ANON_KEY` | Configurado: proyecto Supabase del curso |
| stage | `SUPABASE_URL_STAGE` | `SUPABASE_ANON_KEY_STAGE` | Sin proyecto propio todavía |
| prod | `SUPABASE_URL_PROD` | `SUPABASE_ANON_KEY_PROD` | Sin proyecto propio todavía |

Cada valor se busca primero en `local.properties`, que no se sube a git. Si no está ahí, se busca en una variable de entorno con el mismo nombre; en GitHub Actions, esa variable sale de los *secrets* del repositorio. dev acepta también las claves sin sufijo, las mismas que ya usaba el proyecto.

Gradle copia el ambiente elegido a `BuildConfig.AMBIENTE`, `BuildConfig.SUPABASE_URL` y `BuildConfig.SUPABASE_KEY` (`app/build.gradle.kts`).

## Reglas que Gradle hace cumplir

| Regla | Qué pasa si no se cumple |
|---|---|
| El ambiente es `dev`, `stage` o `prod` | El build falla: `Ambiente 'qa' no válido: use dev, stage o prod` |
| stage y prod tienen URL y clave | El build falla: `Faltan SUPABASE_URL_STAGE o SUPABASE_ANON_KEY_STAGE…`. Así nunca se compila una versión de stage o prod que apunte en silencio a otro ambiente |
| Toda URL configurada usa HTTPS | El build falla: `La URL de Supabase del ambiente stage debe usar HTTPS` |
| dev puede quedar sin credenciales | Compila con URL vacía. Es el caso del CI: las pruebas usan dobles y MockWebServer, nunca el Supabase real |

Verificado el 2026-09-25: los tres mensajes de error aparecen con los valores indicados. Con `SUPABASE_URL_STAGE` y `SUPABASE_ANON_KEY_STAGE` como variables de entorno, el `BuildConfig` generado contiene `AMBIENTE = "stage"` y la URL de stage. Sin la propiedad, contiene `AMBIENTE = "dev"`.

## Qué ve el usuario

En dev y stage, la pantalla de inicio de sesión muestra "Entorno: dev" o "Entorno: stage", para no confundir una versión de prueba con la real. En prod no se muestra nada. `AmbienteDespliegue` (`model/AmbienteDespliegue.kt`) decide la etiqueta, y un valor desconocido se trata como dev, nunca como prod (`AmbienteDespliegueTest`).

El nombre `AmbienteDespliegue` evita confundirlo con el *ambiente de formación* (aula o laboratorio) que se pide en la solicitud de préstamo.

## Secretos

- Ninguna URL ni clave está en git: `local.properties` y `privado/` están en `.gitignore`.
- La clave anon queda dentro del APK, como en cualquier app que llama a Supabase desde el cliente. Por eso la protección real está en el servidor: RLS por rol y sesión con token (`docs/supabase/009_seguridad.sql`, riesgo residual R-08 en `docs/RIESGOS.md`).
- La app no escribe en el log URLs, tokens ni datos personales (no hay llamadas a `android.util.Log` en `app/src/main`).

## Para crear stage o prod

1. Crear otro proyecto en Supabase y ejecutar en él los scripts `docs/supabase/001` a `009`.
2. Agregar en `local.properties` `SUPABASE_URL_STAGE=https://<proyecto>.supabase.co` y `SUPABASE_ANON_KEY_STAGE=<clave anon>`, o definirlos como *secrets* del repositorio en GitHub.
3. Compilar con `-Pambiente=stage`.
