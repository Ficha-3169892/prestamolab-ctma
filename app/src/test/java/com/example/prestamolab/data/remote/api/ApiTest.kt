package com.example.prestamolab.data.remote.api

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class ApiTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: PrestamoApiService
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PrestamoApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getEquipos returns correct data`() = runBlocking {
        val responseBody = """
            [
                {"id": 1, "nombre": "Multimetro", "categoria": "ELECTRONICA", "estado": "DISPONIBLE"},
                {"id": 2, "nombre": "Osciloscopio", "categoria": "ELECTRONICA", "estado": "PRESTADO"}
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = apiService.getEquipos()

        assertEquals(2, result.size)
        assertEquals("Multimetro", result[0].nombre)
        assertEquals("ELECTRONICA", result[0].categoria)
    }
}
