package com.example.prestamolab.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val FILTRO_CATEGORIA = stringPreferencesKey("filtro_categoria")
    }

    val filtroCategoriaFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[FILTRO_CATEGORIA]
        }

    suspend fun guardarFiltroCategoria(categoria: String) {
        context.dataStore.edit { preferences ->
            preferences[FILTRO_CATEGORIA] = categoria
        }
    }
}
