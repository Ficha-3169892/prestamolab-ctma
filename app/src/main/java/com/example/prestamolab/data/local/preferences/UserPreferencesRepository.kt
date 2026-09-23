package com.example.prestamolab.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val DARK_THEME_KEY = booleanPreferencesKey("dark_theme_enabled")
        val LAST_ADMIN_EMAIL_KEY = stringPreferencesKey("last_admin_email")
    }

    val darkThemeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[DARK_THEME_KEY] ?: false
    }

    val lastAdminEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LAST_ADMIN_EMAIL_KEY] ?: ""
    }

    suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_KEY] = enabled
        }
    }

    suspend fun saveLastAdminEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_ADMIN_EMAIL_KEY] = email
        }
    }
}
