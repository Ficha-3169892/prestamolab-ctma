package com.example.prestamolab.data.remote

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Convierte entre el formato local de Room ("yyyy-MM-dd HH:mm", hora del dispositivo)
 * y los timestamptz de Supabase (ISO-8601 con zona horaria).
 */
class FechasSupabase(private val zona: TimeZone = TimeZone.getDefault()) {

    fun aIso(local: String): String {
        val fecha = try {
            formato(LOCAL).parse(local)
        } catch (e: ParseException) {
            // Las semillas de la versión 1 solo tenían la fecha
            formato(SOLO_FECHA).parse(local)
        }
        return formato(ISO).format(fecha!!)
    }

    fun desdeIso(iso: String): String {
        // PostgREST puede incluir microsegundos, que SimpleDateFormat no interpreta
        val sinFraccion = iso.replace(FRACCION_SEGUNDOS, "")
        return formato(LOCAL).format(formato(ISO).parse(sinFraccion)!!)
    }

    private fun formato(patron: String) = SimpleDateFormat(patron, Locale.US).apply { timeZone = zona }

    private companion object {
        const val LOCAL = "yyyy-MM-dd HH:mm"
        const val SOLO_FECHA = "yyyy-MM-dd"
        const val ISO = "yyyy-MM-dd'T'HH:mm:ssXXX"
        val FRACCION_SEGUNDOS = Regex("""\.\d+""")
    }
}
