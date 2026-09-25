package com.example.prestamolab.data.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.prestamolab.model.Rol
import com.example.prestamolab.model.Sesion
import com.example.prestamolab.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Persistencia de la sesión activa (id, correo y rol del usuario). */
interface SessionStore {
    val sesion: Flow<Sesion?>
    suspend fun guardar(sesion: Sesion)
    suspend fun limpiar()
}

val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

class DataStoreSessionStore(private val dataStore: DataStore<Preferences>) : SessionStore {

    override val sesion: Flow<Sesion?> = dataStore.data.map { prefs ->
        val id = prefs[USUARIO_ID] ?: return@map null
        // Un rol desconocido o corrupto se trata como sesión inválida
        val rol = Rol.entries.find { it.name == prefs[ROL] } ?: return@map null
        Sesion(Usuario(id, prefs[NOMBRE].orEmpty(), prefs[CORREO].orEmpty(), rol), prefs[TOKEN])
    }

    override suspend fun guardar(sesion: Sesion) {
        dataStore.edit { prefs ->
            prefs[USUARIO_ID] = sesion.usuario.id
            prefs[NOMBRE] = sesion.usuario.nombre
            prefs[CORREO] = sesion.usuario.correo
            prefs[ROL] = sesion.usuario.rol.name
            if (sesion.token == null) prefs.remove(TOKEN) else prefs[TOKEN] = sesion.token
        }
    }

    override suspend fun limpiar() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        // Mismos nombres que las columnas de public.users
        val USUARIO_ID = stringPreferencesKey("user_id")
        val NOMBRE = stringPreferencesKey("full_name")
        val CORREO = stringPreferencesKey("email")
        val ROL = stringPreferencesKey("role")
        val TOKEN = stringPreferencesKey("session_token")
    }
}
