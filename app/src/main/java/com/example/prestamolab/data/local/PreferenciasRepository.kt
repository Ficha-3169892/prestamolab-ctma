package com.example.prestamolab.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_preferences")

class PreferenciasRepository(private val context: Context) {
    private val FILTRO_COMPLETADAS = booleanPreferencesKey("filtro_completadas")

    val mostrarSoloCompletadas: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[FILTRO_COMPLETADAS] ?: false }

    suspend fun guardarFiltroCompletadas(mostrar: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[FILTRO_COMPLETADAS] = mostrar
        }
    }
}