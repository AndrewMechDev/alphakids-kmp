package org.alphakids.app.data.remote

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EncryptedSettingsTokenStorageTest {

    /** Reversible fake: the real cipher (Android Keystore AES/GCM) can't run in commonTest. */
    private class FakeCipher(
        private val failEncrypt: Boolean = false,
        private val failDecrypt: Boolean = false,
    ) : TokenCipher {
        override fun encrypt(plaintext: String): String {
            if (failEncrypt) throw IllegalStateException("keystore unavailable")
            return "enc(" + plaintext.reversed() + ")"
        }

        override fun decrypt(ciphertext: String): String {
            if (failDecrypt || !ciphertext.startsWith("enc(")) throw IllegalArgumentException("bad ciphertext")
            return ciphertext.removePrefix("enc(").removeSuffix(")").reversed()
        }
    }

    private val settings = MapSettings()

    @Test
    fun storesTokensEncryptedAndNeverAsPlaintext() {
        val storage = EncryptedSettingsTokenStorage(settings, FakeCipher())

        storage.accessToken = "access-1"
        storage.refreshToken = "refresh-1"

        assertEquals("access-1", storage.accessToken)
        assertEquals("refresh-1", storage.refreshToken)
        assertTrue(storage.isLoggedIn)
        val rawValues = settings.keys.map { settings.getString(it, "") }
        assertFalse("access-1" in rawValues, "access token stored in plaintext")
        assertFalse("refresh-1" in rawValues, "refresh token stored in plaintext")
        assertNull(settings.getStringOrNull(LEGACY_ACCESS))
        assertNull(settings.getStringOrNull(LEGACY_REFRESH))
    }

    @Test
    fun migratesLegacyPlaintextTokensWithoutLoggingOut() {
        settings.putString(LEGACY_ACCESS, "old-access")
        settings.putString(LEGACY_REFRESH, "old-refresh")
        val storage = EncryptedSettingsTokenStorage(settings, FakeCipher())

        assertTrue(storage.isLoggedIn)
        assertEquals("old-access", storage.accessToken)
        assertEquals("old-refresh", storage.refreshToken)

        assertNull(settings.getStringOrNull(LEGACY_ACCESS), "plaintext access token not deleted")
        assertNull(settings.getStringOrNull(LEGACY_REFRESH), "plaintext refresh token not deleted")
        // A fresh instance reads the migrated (encrypted) values.
        val reopened = EncryptedSettingsTokenStorage(settings, FakeCipher())
        assertEquals("old-access", reopened.accessToken)
        assertEquals("old-refresh", reopened.refreshToken)
    }

    @Test
    fun writingATokenDropsAnyLeftoverPlaintextCopy() {
        settings.putString(LEGACY_REFRESH, "old-refresh")
        val storage = EncryptedSettingsTokenStorage(settings, FakeCipher())

        storage.refreshToken = "new-refresh"

        assertNull(settings.getStringOrNull(LEGACY_REFRESH))
        assertEquals("new-refresh", storage.refreshToken)
    }

    @Test
    fun clearAndNullRemoveEncryptedAndLegacyValues() {
        settings.putString(LEGACY_ACCESS, "old-access")
        val storage = EncryptedSettingsTokenStorage(settings, FakeCipher())
        storage.refreshToken = "refresh-1"

        storage.clear()

        assertNull(storage.accessToken)
        assertNull(storage.refreshToken)
        assertFalse(storage.isLoggedIn)
        assertEquals(0, settings.size)

        storage.accessToken = "access-2"
        storage.accessToken = null
        assertEquals(0, settings.size)
    }

    @Test
    fun undecryptableTokenIsDroppedSoTheUserLogsInAgain() {
        EncryptedSettingsTokenStorage(settings, FakeCipher()).accessToken = "access-1"
        assertNotEquals(0, settings.size)

        val storage = EncryptedSettingsTokenStorage(settings, FakeCipher(failDecrypt = true))

        assertNull(storage.accessToken)
        assertFalse(storage.isLoggedIn)
        assertEquals(0, settings.size, "corrupt ciphertext should be removed")
    }

    @Test
    fun encryptionFailureKeepsTheSessionInsteadOfLosingTokens() {
        settings.putString(LEGACY_ACCESS, "old-access")
        val storage = EncryptedSettingsTokenStorage(settings, FakeCipher(failEncrypt = true))

        assertEquals("old-access", storage.accessToken, "migration failure must not log out")
        assertEquals("old-access", settings.getStringOrNull(LEGACY_ACCESS))

        storage.refreshToken = "refresh-1"
        assertEquals("refresh-1", storage.refreshToken, "a failed encrypt must not drop the token")
    }

    private companion object {
        const val LEGACY_ACCESS = "auth_access_token"
        const val LEGACY_REFRESH = "auth_refresh_token"
    }
}
