package com.ahrn.irrigatech.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ahrn.irrigatech.data.auth.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "irrigatech_session")

/**
 * Caches the last signed-in identity so the splash screen can route immediately
 * and offline. Firebase remains the source of truth when it is available.
 */
class SessionStore(private val context: Context) {

    val user: Flow<AuthUser?> = context.sessionDataStore.data.map { prefs ->
        val uid = prefs[KEY_UID] ?: return@map null
        AuthUser(
            uid = uid,
            displayName = prefs[KEY_NAME],
            email = prefs[KEY_EMAIL],
            photoUrl = prefs[KEY_PHOTO],
            isDemo = prefs[KEY_DEMO] == "true",
        )
    }

    suspend fun save(user: AuthUser) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_UID] = user.uid
            prefs[KEY_NAME] = user.displayName.orEmpty()
            prefs[KEY_EMAIL] = user.email.orEmpty()
            prefs[KEY_PHOTO] = user.photoUrl.orEmpty()
            prefs[KEY_DEMO] = user.isDemo.toString()
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_UID = stringPreferencesKey("uid")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_PHOTO = stringPreferencesKey("photo")
        val KEY_DEMO = stringPreferencesKey("demo")
    }
}
