package com.example.prestamolab.model

/**
 * Ambiente de despliegue con el que se compiló la app: dev, stage o prod (docs/AMBIENTES.md).
 * No confundir con el ambiente de formación (aula o laboratorio) de la solicitud.
 * Gradle valida el nombre y las credenciales; aquí solo se decide qué ve el usuario.
 */
enum class AmbienteDespliegue {
    DEV, STAGE, PROD;

    /** Etiqueta visible en el inicio de sesión; prod no muestra ninguna. */
    val etiqueta: String? get() = if (this == PROD) null else "Entorno: ${name.lowercase()}"

    companion object {
        /** Traduce BuildConfig.AMBIENTE; un valor desconocido se trata como dev, nunca como prod. */
        fun desde(nombre: String): AmbienteDespliegue =
            entries.firstOrNull { it.name.equals(nombre.trim(), ignoreCase = true) } ?: DEV
    }
}
