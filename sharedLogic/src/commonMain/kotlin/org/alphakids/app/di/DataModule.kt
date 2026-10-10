package org.alphakids.app.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.ApiConstants
import org.alphakids.app.data.remote.EncryptedSettingsTokenStorage
import org.alphakids.app.data.remote.SettingsTokenStorage
import org.alphakids.app.data.remote.TokenCipher
import org.alphakids.app.data.remote.TokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module = module {
    // Platforms that bind a TokenCipher (Android: Keystore AES/GCM) get
    // encrypted tokens with transparent migration of old plaintext values;
    // the rest (iOS for now) keep the plaintext SettingsTokenStorage.
    single<TokenStorage> {
        val cipher = getOrNull<TokenCipher>()
        if (cipher != null) EncryptedSettingsTokenStorage(get(), cipher) else SettingsTokenStorage(get())
    }
    // "apiBaseUrl" is set from BuildConfig.API_BASE_URL in AlphaKidsApp.kt
    // (Android). Platforms/tests that never call startKoin { properties(...) }
    // fall back to ApiConstants.BASE_URL, same as before this was configurable.
    single { AlphaKidsApiClient(get(), baseUrl = getProperty("apiBaseUrl", ApiConstants.BASE_URL)) }
}
