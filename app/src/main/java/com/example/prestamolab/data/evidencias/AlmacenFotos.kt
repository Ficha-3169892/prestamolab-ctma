package com.example.prestamolab.data.evidencias

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Archivo reservado para una foto que la cámara todavía no ha tomado. */
data class FotoReservada(
    /** content:// que recibe la cámara y que se guarda en Room. */
    val uri: String,
    /** Ruta del archivo, para borrarlo si se cancela la captura. */
    val ruta: String
)

/** Fotos de evidencia en el almacenamiento privado de la app (HU-08). */
interface AlmacenFotos {
    fun reservar(): FotoReservada

    /** CA-HU08-04: al cancelar la cámara no debe quedar ningún archivo. */
    fun descartar(foto: FotoReservada)

    /** Bytes de la foto para subirla a Storage; null si el archivo ya no existe. */
    fun leer(uri: String): ByteArray?
}

class FileProviderAlmacenFotos(private val contexto: Context) : AlmacenFotos {

    private val carpeta get() = File(contexto.filesDir, CARPETA).apply { mkdirs() }

    override fun reservar(): FotoReservada {
        val archivo = File(carpeta, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(contexto, autoridad(contexto), archivo)
        return FotoReservada(uri.toString(), archivo.absolutePath)
    }

    override fun descartar(foto: FotoReservada) {
        File(foto.ruta).delete()
    }

    override fun leer(uri: String): ByteArray? = try {
        contexto.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
    } catch (e: Exception) {
        null
    }

    /** Miniatura reducida para la lista; null si la foto no se puede leer. */
    fun miniatura(uri: String, ladoMaximo: Int = 256): Bitmap? = try {
        val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contexto.contentResolver.openInputStream(Uri.parse(uri))?.use { BitmapFactory.decodeStream(it, null, opciones) }
        var muestreo = 1
        while (maxOf(opciones.outWidth, opciones.outHeight) / (muestreo * 2) >= ladoMaximo) muestreo *= 2
        contexto.contentResolver.openInputStream(Uri.parse(uri))?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = muestreo })
        }
    } catch (e: Exception) {
        null
    }

    companion object {
        /** Debe coincidir con res/xml/rutas_fotos.xml. */
        const val CARPETA = "evidencias"

        fun autoridad(contexto: Context) = "${contexto.packageName}.fileprovider"
    }
}
