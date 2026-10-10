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
 * Encrypts token values before they reach [Settings]. On Android the key lives
 * in the Android Keystore (see `AndroidKeystoreTokenCipher`), so a copied
 * preferences file is useless without the device.
 */
interface TokenCipher {
    fun encrypt(plaintext: String): String

    /** Throws when [ciphertext] was tampered with or the key is gone. */
    fun decrypt(ciphertext: String): String
}

/**
 * [Settings]-backed [TokenStorage] — persists across process death, unlike
 * a plain in-memory holder. Without this, an Android process kill while the
 * app is backgrounded (routine OS behavior, not an edge case) wipes the
 * tokens with no way to refresh, and the app gets stuck mid-request or
 * silently falls back to demo data instead of prompting a real re-login.
 *
 * Stores values in plaintext; used only where no [TokenCipher] is bound
 * (iOS, until it moves to the Keychain). Android uses
 * [EncryptedSettingsTokenStorage].
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

    internal companion object {
        const val KEY_ACCESS_TOKEN = "auth_access_token"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token"
    }
}

/**
 * [TokenStorage] that keeps only [TokenCipher]-encrypted values in [Settings].
 *
 * Plaintext tokens written by [SettingsTokenStorage] (app versions before
 * encryption) are migrated on first read: encrypted under a new key, then the
 * plaintext entry is deleted, so signed-in users stay signed in.
 *
 * Failure policy: a value that can't be decrypted (tampered, or the Keystore
 * key was wiped) is dropped and reads as signed out. If encryption itself
 * fails (broken Keystore on some devices), the token is kept in the legacy
 * plaintext entry instead of being lost; backup exclusion still keeps it on
 * the device, and the next read retries the migration.
 */
class EncryptedSettingsTokenStorage(
    private val settings: Settings,
    private val cipher: TokenCipher,
) : TokenStorage {
    override var accessToken: String?
        get() = read(KEY_ACCESS_TOKEN, SettingsTokenStorage.KEY_ACCESS_TOKEN)
        set(value) = write(KEY_ACCESS_TOKEN, SettingsTokenStorage.KEY_ACCESS_TOKEN, value)

    override var refreshToken: String?
        get() = read(KEY_REFRESH_TOKEN, SettingsTokenStorage.KEY_REFRESH_TOKEN)
        set(value) = write(KEY_REFRESH_TOKEN, SettingsTokenStorage.KEY_REFRESH_TOKEN, value)

    override val isLoggedIn: Boolean get() = accessToken != null

    override fun clear() {
        write(KEY_ACCESS_TOKEN, SettingsTokenStorage.KEY_ACCESS_TOKEN, null)
        write(KEY_REFRESH_TOKEN, SettingsTokenStorage.KEY_REFRESH_TOKEN, null)
    }

    private fun read(key: String, legacyKey: String): String? {
        settings.getStringOrNull(key)?.let { encrypted ->
            try {
                return cipher.decrypt(encrypted)
            } catch (_: Exception) {
                settings.remove(key)
            }
        }
        val legacy = settings.getStringOrNull(legacyKey) ?: return null
        write(key, legacyKey, legacy)
        return legacy
    }

    private fun write(key: String, legacyKey: String, value: String?) {
        if (value == null) {
            settings.remove(key)
            settings.remove(legacyKey)
            return
        }
        val encrypted = try {
            cipher.encrypt(value)
        } catch (_: Exception) {
            null
        }
        if (encrypted == null) {
            settings.remove(key)
            settings.putString(legacyKey, value)
            return
        }
        // Write the encrypted copy before deleting the plaintext one, so an
        // interruption in between never loses the token.
        settings.putString(key, encrypted)
        settings.remove(legacyKey)
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "auth_access_token_enc"
        const val KEY_REFRESH_TOKEN = "auth_refresh_token_enc"
    }
}
