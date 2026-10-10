package org.alphakids.app.data.remote

import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * Exercises the AES/GCM framing with a software key. The Android Keystore key
 * provider itself only exists on a device, so it is not covered on the JVM host.
 */
class AesGcmTokenCipherTest {

    private fun softwareKey(): SecretKey = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()

    private val key = softwareKey()
    private val cipher = AesGcmTokenCipher { key }

    @Test
    fun roundTripsATokenWithoutExposingIt() {
        val token = "eyJhbGciOiJIUzI1NiJ9.payload.signature"

        val encrypted = cipher.encrypt(token)

        assertFalse(encrypted.contains(token))
        assertEquals(token, cipher.decrypt(encrypted))
    }

    @Test
    fun usesAFreshIvForEveryEncryption() {
        assertNotEquals(cipher.encrypt("same"), cipher.encrypt("same"))
    }

    @Test
    fun rejectsTamperedCiphertext() {
        val encrypted = cipher.encrypt("refresh-token")
        val middle = encrypted.length / 2
        val flipped = if (encrypted[middle] == 'A') 'B' else 'A'
        val tampered = encrypted.substring(0, middle) + flipped + encrypted.substring(middle + 1)

        assertFailsWith<Exception> { cipher.decrypt(tampered) }
    }

    @Test
    fun rejectsCiphertextFromAnotherKey() {
        val encrypted = AesGcmTokenCipher { softwareKey() }.encrypt("refresh-token")

        assertFailsWith<Exception> { cipher.decrypt(encrypted) }
    }

    @Test
    fun rejectsGarbageInput() {
        assertFailsWith<Exception> { cipher.decrypt("not-base64-!!") }
        assertFailsWith<Exception> { cipher.decrypt("") }
    }
}
