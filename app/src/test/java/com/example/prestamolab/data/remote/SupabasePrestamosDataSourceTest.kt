package com.example.prestamolab.data.remote

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

/** Contrato HTTP con PostgREST, contra un servidor simulado (CA-HU07-01 a CA-HU07-05). */
class SupabasePrestamosDataSourceTest {

    private lateinit var servidor: MockWebServer
    private lateinit var remoto: SupabasePrestamosDataSource

    @Before
    fun setup() {
        servidor = MockWebServer()
        servidor.start()
        val cliente = SupabaseRestClient(servidor.url("/").toString(), "anon-key", tiempoEsperaMs = 500)
        remoto = SupabasePrestamosDataSource(cliente)
    }

    @After
    fun cerrar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, cuerpo: String = "[]") {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    private fun rutaDecodificada() = URLDecoder.decode(servidor.takeRequest().path!!, "UTF-8")

    @Test
    fun `TC-HU07-02 - Respuesta 200 con equipos se convierte en EquipoRemoto`() = runTest {
        responder(
            200,
            """[{"id":"0b1e0000-0000-4000-8000-000000000001","name":"Multímetro Digital",
                "category":"Herramienta","status":"DISPONIBLE"},
               {"id":"e2","name":"Proyector","category":null,"status":"PRESTADO"}]"""
        )

        val equipos = remoto.equipos()

        assertEquals(
            listOf(
                EquipoRemoto("0b1e0000-0000-4000-8000-000000000001", "Multímetro Digital", "Herramienta", "DISPONIBLE"),
                EquipoRemoto("e2", "Proyector", "", "PRESTADO")
            ),
            equipos
        )
        val peticion = servidor.takeRequest()
        assertEquals("anon-key", peticion.getHeader("apikey"))
        assertEquals("Bearer anon-key", peticion.getHeader("Authorization"))
    }

    @Test
    fun `Los prestamos del estudiante se filtran por user_id y traen el nombre del solicitante`() = runTest {
        responder(
            200,
            """[{"id":"l1","user_id":"u1","equipment_id":"e1","status":"PRESTADO",
                "request_date":"2026-09-24T13:00:00+00:00","return_date":"2026-09-24T15:00:00+00:00",
                "environment":"Lab 1","purpose":"Practica de redes","duration_hours":2,
                "solicitante":{"full_name":"Estudiante CTMA"}}]"""
        )

        val prestamo = remoto.prestamos("u1").single()

        assertEquals("Estudiante CTMA", prestamo.nombreSolicitante)
        assertEquals(2, prestamo.duracionHoras)
        assertEquals("2026-09-24T15:00:00+00:00", prestamo.fechaLimite)
        assertTrue(rutaDecodificada().contains("&user_id=eq.u1"))
    }

    @Test
    fun `El instructor recibe los prestamos sin filtro de usuario`() = runTest {
        responder(200)

        remoto.prestamos(null)

        assertFalse(rutaDecodificada().contains("user_id=eq."))
    }

    @Test
    fun `Las devoluciones conservan coordenadas nulas`() = runTest {
        responder(
            200,
            """[{"id":"r1","loan_id":"l1","equipment_condition":"BUENO","notes":null,
                "return_date":"2026-09-24T15:00:00+00:00","latitude":null,"longitude":null}]"""
        )

        val devolucion = remoto.devoluciones(null).single()

        assertNull(devolucion.latitud)
        assertNull(devolucion.longitud)
        assertEquals("", devolucion.notas)
    }

    @Test
    fun `TC-HU07-01 - Guardar un prestamo hace upsert por id con los nombres de columna de Supabase`() = runTest {
        responder(201, "")

        remoto.guardarPrestamo(
            PrestamoRemoto(
                "l1", "u1", "e1", "SOLICITADA", "2026-09-24T08:00:00-05:00", "2026-09-24T10:00:00-05:00",
                "Lab 1", "Practica de redes", 2
            )
        )

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/rest/v1/loans", peticion.path)
        assertEquals("resolution=merge-duplicates,return=minimal", peticion.getHeader("Prefer"))
        val cuerpo = JSONObject(peticion.body.readUtf8())
        assertEquals("u1", cuerpo.getString("user_id"))
        assertEquals("e1", cuerpo.getString("equipment_id"))
        assertEquals("2026-09-24T10:00:00-05:00", cuerpo.getString("return_date"))
        // loans.user_role es NOT NULL en Supabase
        assertEquals("ESTUDIANTE", cuerpo.getString("user_role"))
        assertFalse(cuerpo.has("solicitante"))
    }

    @Test
    fun `La revision del instructor se envia en reviewed_by y rejection_reason`() = runTest {
        responder(201, "")

        remoto.guardarPrestamo(
            PrestamoRemoto(
                "l1", "u1", "e1", "RECHAZADA", "2026-09-24T08:00:00-05:00", "2026-09-24T10:00:00-05:00",
                "Lab 1", "Practica de redes", 2, revisadoPor = "i1", motivoRechazo = "Equipo en calibración"
            )
        )

        val cuerpo = JSONObject(servidor.takeRequest().body.readUtf8())
        assertEquals("i1", cuerpo.getString("reviewed_by"))
        assertEquals("Equipo en calibración", cuerpo.getString("rejection_reason"))
    }

    @Test
    fun `Un prestamo sin revisar envia la revision como null`() = runTest {
        responder(201, "")

        remoto.guardarPrestamo(
            PrestamoRemoto(
                "l1", "u1", "e1", "SOLICITADA", "2026-09-24T08:00:00-05:00", "2026-09-24T10:00:00-05:00",
                "Lab 1", "Practica de redes", 2
            )
        )

        val cuerpo = JSONObject(servidor.takeRequest().body.readUtf8())
        assertTrue(cuerpo.isNull("reviewed_by"))
        assertTrue(cuerpo.isNull("rejection_reason"))
    }

    @Test
    fun `Se recibe el motivo de rechazo y quien reviso`() = runTest {
        responder(
            200,
            """[{"id":"l1","user_id":"u1","equipment_id":"e1","status":"RECHAZADA",
                "request_date":"2026-09-24T13:00:00+00:00","return_date":"2026-09-24T15:00:00+00:00",
                "environment":"Lab 1","purpose":"Practica de redes","duration_hours":2,
                "reviewed_by":"i1","rejection_reason":"Equipo en calibración","solicitante":null}]"""
        )

        val prestamo = remoto.prestamos("u1").single()

        assertEquals("i1", prestamo.revisadoPor)
        assertEquals("Equipo en calibración", prestamo.motivoRechazo)
        assertTrue(rutaDecodificada().contains("reviewed_by,rejection_reason"))
    }

    @Test
    fun `El instructor guarda el equipo completo con title igual al nombre`() = runTest {
        responder(201, "")

        remoto.guardarEquipo(EquipoRemoto("e9", "Proyector Epson", "Audiovisual", "DISPONIBLE"))

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/rest/v1/equipments", peticion.path)
        assertEquals("resolution=merge-duplicates,return=minimal", peticion.getHeader("Prefer"))
        val cuerpo = JSONObject(peticion.body.readUtf8())
        assertEquals("Proyector Epson", cuerpo.getString("name"))
        // equipments.title es NOT NULL en Supabase
        assertEquals("Proyector Epson", cuerpo.getString("title"))
        assertEquals("Audiovisual", cuerpo.getString("category"))
        assertEquals("DISPONIBLE", cuerpo.getString("status"))
    }

    @Test
    fun `Eliminar un equipo envia DELETE filtrado por id`() = runTest {
        responder(204, "")

        remoto.eliminarEquipo("e9")

        val peticion = servidor.takeRequest()
        assertEquals("DELETE", peticion.method)
        assertEquals("/rest/v1/equipments?id=eq.e9", peticion.path)
    }

    @Test
    fun `El estado del equipo se envia con PATCH y solo la columna status`() = runTest {
        responder(204, "")

        remoto.actualizarEstadoEquipo("0b1e0000-0000-4000-8000-000000000005", "DISPONIBLE")

        val peticion = servidor.takeRequest()
        assertEquals("PATCH", peticion.method)
        assertEquals("/rest/v1/equipments?id=eq.0b1e0000-0000-4000-8000-000000000005", peticion.path)
        // Un upsert fallaba: equipments tiene columnas NOT NULL (title) que la app no maneja
        assertEquals("""{"status":"DISPONIBLE"}""", peticion.body.readUtf8())
    }

    @Test
    fun `Guardar una devolucion sin GPS envia latitude y longitude en null`() = runTest {
        responder(201, "")

        remoto.guardarDevolucion(DevolucionRemota("r1", "l1", "DANADO", "Pantalla rota", "2026-09-24T10:00:00-05:00", null, null))

        val cuerpo = JSONObject(servidor.takeRequest().body.readUtf8())
        assertTrue(cuerpo.isNull("latitude"))
        assertTrue(cuerpo.isNull("longitude"))
    }

    @Test
    fun `TC-HU07-03 - Respuesta 401 lanza SupabaseHttpException con el codigo`() = runTest {
        responder(401, """{"message":"Invalid API key"}""")

        val error = runCatching { remoto.equipos() }.exceptionOrNull()

        assertEquals(401, (error as SupabaseHttpException).codigo)
        assertTrue(error.cuerpo.contains("Invalid API key"))
    }

    @Test
    fun `TC-HU07-04 - Respuesta 404 lanza SupabaseHttpException 404`() = runTest {
        responder(404, """{"code":"42P01","message":"relation does not exist"}""")

        val error = runCatching { remoto.prestamos(null) }.exceptionOrNull()

        assertEquals(404, (error as SupabaseHttpException).codigo)
    }

    @Test
    fun `TC-HU07-05 - Respuesta 503 lanza SupabaseHttpException 503`() = runTest {
        responder(503, "Service Unavailable")

        val error = runCatching { remoto.actualizarEstadoEquipo("e1", "DISPONIBLE") }.exceptionOrNull()

        assertEquals(503, (error as SupabaseHttpException).codigo)
    }

    @Test
    fun `TC-HU07-05 - Una respuesta mas lenta que el tiempo de espera lanza IOException`() = runTest {
        servidor.enqueue(MockResponse().setBody("[]").setHeadersDelay(2, TimeUnit.SECONDS))

        val error = runCatching { remoto.equipos() }.exceptionOrNull()

        assertTrue(error is IOException)
    }

    @Test
    fun `Sin conexion con el servidor lanza IOException`() = runTest {
        servidor.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val error = runCatching { remoto.equipos() }.exceptionOrNull()

        assertTrue(error is IOException)
    }

    @Test
    fun `Sin SUPABASE_URL configurada falla como error de red`() = runTest {
        val sinUrl = SupabasePrestamosDataSource(SupabaseRestClient("", "anon-key"))

        assertTrue(runCatching { sinUrl.equipos() }.exceptionOrNull() is IOException)
    }
}
