package com.example.prestamolab.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        val USER_ID = stringPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
        val USER_EMAIL = stringPreferencesKey("user_email")
    }

    val userIdFlow: Flow<String?> = context.sessionDataStore.data.map { it[USER_ID] }
    val userRoleFlow: Flow<String?> = context.sessionDataStore.data.map { it[USER_ROLE] }

    suspend fun saveSession(userId: String, role: String, email: String) {
        context.sessionDataStore.edit { preferences ->
            preferences[USER_ID] = userId
            preferences[USER_ROLE] = role
            preferences[USER_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { it.clear() }
    }
}
