package com.example.prestamolab.data.remote.api

import com.example.prestamolab.data.remote.dto.EquipoDto
import com.example.prestamolab.data.remote.dto.SolicitudDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PrestamoApiService {
    @GET("equipos")
    suspend fun getEquipos(): List<EquipoDto>

    @GET("solicitudes")
    suspend fun getSolicitudes(): List<SolicitudDto>

    @POST("solicitudes")
    suspend fun createSolicitud(@Body solicitud: SolicitudDto): SolicitudDto

    @POST("solicitudes/{id}/cancelar")
    suspend fun cancelarSolicitud(@Path("id") id: Int): SolicitudDto

    @POST("solicitudes/{id}/devolucion")
    suspend fun registrarDevolucion(
        @Path("id") id: Int, 
        @Body devolucion: Map<String, String?>
    ): SolicitudDto
}
