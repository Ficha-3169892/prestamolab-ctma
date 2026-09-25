package com.example.prestamolab.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.prestamolab.data.repository.RoomPrestamoRepository
import com.example.prestamolab.data.sync.ResultadoSincronizacion
import com.example.prestamolab.data.sync.SincronizadorPrestamos
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Usuario
import com.example.prestamolab.testutil.FakePrestamosRemoteDataSource
import com.example.prestamolab.testutil.FakeUsuariosDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * CA-HU06-01: con datos guardados, al reiniciar la app en modo avión el catálogo y mis préstamos siguen
 * visibles. Se usa un archivo de base propio: cerrar y volver a abrir Room equivale a reiniciar la app, y un
 * servidor que lanza IOException equivale a no tener red.
 */
@RunWith(AndroidJUnit4::class)
class PersistenciaSinConexionTest {

    private val contexto = ApplicationProvider.getApplicationContext<Context>()
    private val archivo = "persistencia-sin-conexion.db"
    private val estudiante = Usuario(
        FakeUsuariosDataSource.ESTUDIANTE.usuario.id, "Estudiante CTMA", FakeUsuariosDataSource.CORREO_ESTUDIANTE, Rol.ESTUDIANTE
    )

    @After
    fun borrarArchivo() {
        contexto.deleteDatabase(archivo)
    }

    @Test
    fun TC_HU06_01_TrasReiniciarSinRedElCatalogoYMisPrestamosSiguenVisibles() = runTest {
        PrestamoLabDatabase.construir(contexto, nombre = archivo).apply {
            DatosSemilla.reiniciar(this)
            close()
        }

        // "Reinicio" en modo avión: base nueva sobre el mismo archivo y un servidor inalcanzable
        val db = PrestamoLabDatabase.construir(contexto, nombre = archivo)
        try {
            val sinRed = FakePrestamosRemoteDataSource().apply { error = IOException("Sin conexión") }
            val resultado = SincronizadorPrestamos(db, sinRed).sincronizar(estudiante)
            val repository = RoomPrestamoRepository(db)

            assertTrue(resultado is ResultadoSincronizacion.ErrorTemporal)
            assertEquals(5, repository.equipos.first().size)
            assertEquals(
                listOf(1, 2),
                repository.solicitudes.first().filter { it.usuarioId == estudiante.id }.map { it.id }
            )
        } finally {
            db.close()
        }
    }
}
