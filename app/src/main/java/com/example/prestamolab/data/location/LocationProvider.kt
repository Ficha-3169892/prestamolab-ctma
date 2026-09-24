package com.example.prestamolab.data.location

import android.annotation.SuppressLint
import android.content.Context
import com.example.prestamolab.model.Ubicacion
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class UbicacionNoDisponibleException :
    Exception("No se pudo obtener la ubicación. Verifica que la ubicación del dispositivo esté activada.")

interface LocationProvider {
    /** Una sola lectura de la ubicación actual. Requiere que el llamador ya tenga el permiso. */
    suspend fun ubicacionActual(): Result<Ubicacion>
}

/** Implementación con Fused Location Provider (Google Play Services). */
class FusedLocationProvider(context: Context) : LocationProvider {

    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    // Lectura única: no se registran actualizaciones continuas (mínimo privilegio)
    private val solicitud = CurrentLocationRequest.Builder()
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setDurationMillis(15_000)
        .setMaxUpdateAgeMillis(10_000)
        .build()

    @SuppressLint("MissingPermission") // La pantalla verifica el permiso antes de llamar
    override suspend fun ubicacionActual(): Result<Ubicacion> = try {
        val location = suspendCancellableCoroutine { cont ->
            val cancelacion = CancellationTokenSource()
            client.getCurrentLocation(solicitud, cancelacion.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
            cont.invokeOnCancellation { cancelacion.cancel() }
        }
        if (location == null) {
            Result.failure(UbicacionNoDisponibleException())
        } else {
            Result.success(
                Ubicacion(
                    latitud = location.latitude,
                    longitud = location.longitude,
                    precisionMetros = if (location.hasAccuracy()) location.accuracy else null
                )
            )
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
