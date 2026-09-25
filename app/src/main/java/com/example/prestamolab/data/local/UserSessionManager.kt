package com.example.prestamolab.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore < Preferences > by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_ROLE = stringPreferencesKey("user_role")
    }

    val userIdFlow: Flow < String? > = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ID]
    }

    val userEmailFlow: Flow < String? > = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_EMAIL]
    }

    val userRoleFlow: Flow < String > = context.dataStore.data.map { prefs ->
        prefs[KEY_USER_ROLE] ?: "usuario"
    }

    suspend fun saveSession(userId: String, email: String, role: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USER_EMAIL] = email
            prefs[KEY_USER_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
