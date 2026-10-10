package org.alphakids.app.data.remote

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

/**
 * AES-256/GCM [TokenCipher]. Output is Base64(iv || ciphertext+tag); GCM's tag
 * makes tampered or foreign values fail to decrypt instead of returning junk.
 *
 * The key comes from [keyProvider] so the framing can be tested on the JVM
 * host with a software key; production uses [AndroidKeystoreTokenCipher].
 * A new [Cipher] is created per call because instances are not thread-safe.
 */
internal class AesGcmTokenCipher(private val keyProvider: () -> SecretKey) : TokenCipher {

    override fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        // No IV supplied: the provider generates a random one (required by
        // the Android Keystore, which rejects caller-chosen IVs by default).
        cipher.init(Cipher.ENCRYPT_MODE, keyProvider())
        val iv = cipher.iv
        check(iv.size == IV_SIZE_BYTES) { "Unexpected GCM IV size ${iv.size}" }
        return Base64.encode(iv + cipher.doFinal(plaintext.encodeToByteArray()))
    }

    override fun decrypt(ciphertext: String): String {
        val bytes = Base64.decode(ciphertext)
        require(bytes.size > IV_SIZE_BYTES) { "Ciphertext too short" }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            keyProvider(),
            GCMParameterSpec(TAG_SIZE_BITS, bytes, 0, IV_SIZE_BYTES),
        )
        return cipher.doFinal(bytes, IV_SIZE_BYTES, bytes.size - IV_SIZE_BYTES).decodeToString()
    }

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_SIZE_BYTES = 12
        const val TAG_SIZE_BITS = 128
    }
}

/**
 * [TokenCipher] whose AES key is generated inside and never leaves the Android
 * Keystore (API 23+, minSdk is 24). Keystore keys are not part of backups or
 * device transfer, so even a copied preferences file can't be decrypted.
 *
 * Chosen over androidx.security:security-crypto (EncryptedSharedPreferences),
 * whose APIs are deprecated, and to avoid a new dependency for two strings.
 */
internal class AndroidKeystoreTokenCipher(
    private val alias: String = KEY_ALIAS,
) : TokenCipher {
    @Volatile
    private var cachedKey: SecretKey? = null

    private val delegate = AesGcmTokenCipher(::key)

    override fun encrypt(plaintext: String): String = delegate.encrypt(plaintext)

    override fun decrypt(ciphertext: String): String = delegate.decrypt(ciphertext)

    private fun key(): SecretKey = cachedKey ?: synchronized(this) {
        cachedKey ?: loadOrCreateKey().also { cachedKey = it }
    }

    private fun loadOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "alphakids_auth_tokens"
    }
}
