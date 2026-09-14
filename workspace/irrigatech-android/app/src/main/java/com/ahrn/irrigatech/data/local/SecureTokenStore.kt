package com.ahrn.irrigatech.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores Blynk auth tokens encrypted at rest, keyed by device id.
 * Tokens are never written to source, logs, or plain SharedPreferences.
 */
class SecureTokenStore(context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveToken(deviceId: String, token: String) {
        prefs.edit().putString(key(deviceId), token.trim()).apply()
    }

    fun readToken(deviceId: String): String? =
        prefs.getString(key(deviceId), null)?.takeIf { it.isNotBlank() }

    fun deleteToken(deviceId: String) {
        prefs.edit().remove(key(deviceId)).apply()
    }

    fun hasToken(deviceId: String): Boolean = readToken(deviceId) != null

    private fun key(deviceId: String) = "blynk_token_$deviceId"

    companion object {
        private const val FILE_NAME = "irrigatech_secure"
    }
}
