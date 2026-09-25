package com.example.prestamolab.data.preferencias

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.prestamolab.model.FiltroCatalogo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** CA-HU01-04: el filtro del catálogo se conserva al cerrar y volver a abrir la app. */
interface PreferenciasCatalogo {
    val filtro: Flow<FiltroCatalogo>
    suspend fun guardar(filtro: FiltroCatalogo)
}

val Context.preferenciasDataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

class DataStorePreferenciasCatalogo(private val dataStore: DataStore<Preferences>) : PreferenciasCatalogo {

    override val filtro: Flow<FiltroCatalogo> = dataStore.data.map { prefs ->
        FiltroCatalogo(soloDisponibles = prefs[SOLO_DISPONIBLES] ?: false, categoria = prefs[CATEGORIA])
    }

    override suspend fun guardar(filtro: FiltroCatalogo) {
        dataStore.edit { prefs ->
            prefs[SOLO_DISPONIBLES] = filtro.soloDisponibles
            if (filtro.categoria == null) prefs.remove(CATEGORIA) else prefs[CATEGORIA] = filtro.categoria
        }
    }

    private companion object {
        val SOLO_DISPONIBLES = booleanPreferencesKey("catalogo_solo_disponibles")
        val CATEGORIA = stringPreferencesKey("catalogo_categoria")
    }
}

/** Para pruebas unitarias y valor por defecto: el filtro vive solo mientras exista el objeto. */
class PreferenciasCatalogoEnMemoria(inicial: FiltroCatalogo = FiltroCatalogo()) : PreferenciasCatalogo {
    private val estado = MutableStateFlow(inicial)
    override val filtro: Flow<FiltroCatalogo> = estado

    override suspend fun guardar(filtro: FiltroCatalogo) {
        estado.value = filtro
    }
}
