package org.alphakids.app.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.alphakids.app.data.remote.AndroidKeystoreTokenCipher
import org.alphakids.app.data.remote.TokenCipher
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Persistent key-value storage backed by SharedPreferences — survives
 * process death, unlike the in-memory state it replaced for tokens and the
 * active child id. Auth tokens in it are encrypted with an Android Keystore
 * key ([TokenCipher]), and the file is excluded from backup and device
 * transfer (see androidApp res/xml/data_extraction_rules.xml).
 */
val platformSettingsModule = module {
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("alphakids_settings", Context.MODE_PRIVATE)
        )
    }
    single<TokenCipher> { AndroidKeystoreTokenCipher() }
}
