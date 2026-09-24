package com.example.prestamolab.testutil

import com.example.prestamolab.data.location.LocationProvider
import com.example.prestamolab.model.Ubicacion

/** Proveedor de ubicación controlado por la prueba; cuenta cuántas lecturas se piden. */
class FakeLocationProvider(
    var resultado: Result<Ubicacion> = Result.success(UBICACION_CTMA)
) : LocationProvider {
    var llamadas = 0
        private set

    override suspend fun ubicacionActual(): Result<Ubicacion> {
        llamadas++
        return resultado
    }

    companion object {
        val UBICACION_CTMA = Ubicacion(latitud = 6.2518, longitud = -75.5636, precisionMetros = 12f)
    }
}
