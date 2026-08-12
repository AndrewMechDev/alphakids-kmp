package org.alphakids.app.data.remote

import com.russhwolf.settings.Settings

/**
 * Thread-safe token storage for JWT access and refresh tokens.
 */
interface TokenStorage {
    var accessToken: String?
    var refreshToken: String?
    val isLoggedIn: Boolean

    fun clear()
}

/**
 * [Settings]-backed [TokenStorage] — persists across process death, unlike
 * a plain in-memory holder. Without this, an Android process kill while the
 * app is backgrounded (routine OS behavior, not an edge case) wipes the
 * tokens with no way to refresh, and the app gets stuck mid-request or
 * silently falls back to demo data instead of prompting a real re-login.
 */
class SettingsTokenStorage(private val settings: Settings) : TokenStorage {
    override var accessToken: String?
        get() = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        set(value) {
            if (value == null) settings.remove(KEY_ACCESS_TOKEN) else settings.putString(KEY_ACCESS_TOKEN, value)
        }

    override var refreshToken: String?
        get() = settings.getStringOrNull(KEY_REFRESH_TOKEN)
        set(value) {
            if (value == null) settings.remove(KEY_REFRESH_TOKEN) else settings.putString(KEY_REFRESH_TOKEN, value)
        }

    override val isLoggedIn: Boolean get() = accessToken != null

    override fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }
}
