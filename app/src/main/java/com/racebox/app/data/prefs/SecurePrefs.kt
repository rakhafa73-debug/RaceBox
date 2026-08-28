package com.racebox.app.data.prefs

import android.content.Context
import com.racebox.app.data.security.CryptoUtils

class SecurePrefs(
    context: Context,
    private val crypto: CryptoUtils
) {

    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun saveSession(session: UserSession) {
        prefs.edit()
            .putString(KEY_SESSION, crypto.encrypt(session.toJson()))
            .commit()
    }

    fun userSession(): UserSession? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return try {
            UserSession.fromJson(crypto.decrypt(raw))
        } catch (_: Exception) {
            prefs.edit().remove(KEY_SESSION).apply()
            null
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private companion object {
        const val NAME = "racebox_auth"
        const val KEY_SESSION = "session"
    }
}