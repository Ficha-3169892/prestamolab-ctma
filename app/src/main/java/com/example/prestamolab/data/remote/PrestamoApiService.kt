package com.example.prestamolab.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PrestamoApiService {

    @GET("equipos")
    suspend fun obtenerEquipos(): Response < List < EquipoDto > >

    @GET("solicitudes")
    suspend fun obtenerSolicitudes(): Response < List < SolicitudDto > >

    @POST("solicitudes")
    suspend fun crearSolicitud(@Body solicitud: SolicitudDto): Response < SolicitudDto >

    @PUT("solicitudes/{id}/cancelar")
    suspend fun cancelarSolicitud(@Path("id") id: Int): Response < Unit >
}
