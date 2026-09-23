package com.example.prestamolab.data.remote.api

import com.example.prestamolab.data.remote.dto.EquipoDto
import com.example.prestamolab.data.remote.dto.SolicitudDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface PrestamoApiService {
    @GET("equipos?select=*")
    suspend fun getEquipos(): List<EquipoDto>

    @Headers("Prefer: return=representation")
    @POST("equipos")
    suspend fun createEquipo(@Body equipo: EquipoDto): List<EquipoDto>

    @Headers("Prefer: return=representation")
    @PATCH("equipos")
    suspend fun updateEquipo(
        @Query("id") idQuery: String,
        @Body equipo: EquipoDto
    ): List<EquipoDto>

    @DELETE("equipos")
    suspend fun deleteEquipo(@Query("id") idQuery: String)

    @GET("solicitudes?select=*")
    suspend fun getSolicitudes(): List<SolicitudDto>

    @Headers("Prefer: return=representation")
    @POST("solicitudes")
    suspend fun createSolicitud(@Body solicitud: SolicitudDto): List<SolicitudDto>

    @Headers("Prefer: return=representation")
    @PATCH("solicitudes")
    suspend fun updateSolicitud(
        @Query("id") idQuery: String,
        @Body updates: Map<String, String?>
    ): List<SolicitudDto>

    @DELETE("solicitudes")
    suspend fun deleteSolicitudes(@Query("estado") estadoQuery: String)
}
