package com.example.prestamolab.data.auth

import com.example.prestamolab.data.remote.SupabaseRestClient
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Login y cierre de sesión contra las funciones de docs/supabase/009_seguridad.sql (R-01 a R-04). */
class SupabaseUsuariosDataSourceTest {

    private lateinit var servidor: MockWebServer
    private var token: String? = null
    private lateinit var remoto: SupabaseUsuariosDataSource

    @Before
    fun setup() {
        servidor = MockWebServer()
        servidor.start()
        val cliente = SupabaseRestClient(servidor.url("/").toString(), "anon-key", tiempoEsperaMs = 500) { token }
        remoto = SupabaseUsuariosDataSource(cliente)
    }

    @After
    fun cerrar() {
        servidor.shutdown()
    }

    private fun responder(codigo: Int, cuerpo: String) {
        servidor.enqueue(MockResponse().setResponseCode(codigo).setBody(cuerpo))
    }

    @Test
    fun `R-02 - El login llama a la funcion del servidor con el hash, sin consultar la tabla users`() = runTest {
        responder(
            200,
            """[{"token":"5a0e0000-0000-4000-8000-000000000001","id":"u1","email":"estudiante@sena.edu.co",
                "full_name":"Estudiante CTMA","role":"ESTUDIANTE"}]"""
        )

        val usuario = remoto.buscarPorCredenciales(CampoIdentificador.CORREO, "estudiante@sena.edu.co", "hash-sha256")!!

        val peticion = servidor.takeRequest()
        assertEquals("POST", peticion.method)
        assertEquals("/rest/v1/rpc/iniciar_sesion", peticion.path)
        val cuerpo = JSONObject(peticion.body.readUtf8())
        assertEquals("estudiante@sena.edu.co", cuerpo.getString("p_identificador"))
        assertEquals("hash-sha256", cuerpo.getString("p_hash"))
        assertEquals("5a0e0000-0000-4000-8000-000000000001", usuario.token)
        assertEquals("ESTUDIANTE", usuario.rol)
    }

    @Test
    fun `Credenciales incorrectas devuelven null`() = runTest {
        responder(200, "[]")

        assertNull(remoto.buscarPorCredenciales(CampoIdentificador.DOCUMENTO, "67890", "hash-incorrecto"))
    }

    @Test
    fun `R-04 - Con sesion, cada peticion lleva el token en la cabecera x-sesion`() = runTest {
        token = "5a0e0000-0000-4000-8000-000000000001"
        responder(204, "")

        remoto.cerrarSesion()

        val peticion = servidor.takeRequest()
        assertEquals("/rest/v1/rpc/cerrar_sesion", peticion.path)
        assertEquals("5a0e0000-0000-4000-8000-000000000001", peticion.getHeader(SupabaseRestClient.CABECERA_SESION))
    }

    @Test
    fun `Sin sesion no se envia la cabecera`() = runTest {
        responder(200, "[]")

        remoto.buscarPorCredenciales(CampoIdentificador.CORREO, "x@sena.edu.co", "h")

        assertNull(servidor.takeRequest().getHeader(SupabaseRestClient.CABECERA_SESION))
    }
}
