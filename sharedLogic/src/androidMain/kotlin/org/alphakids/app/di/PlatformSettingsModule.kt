package org.alphakids.app.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Persistent key-value storage backed by SharedPreferences — survives
 * process death, unlike the in-memory state it replaced for tokens and the
 * active child id.
 */
val platformSettingsModule = module {
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("alphakids_settings", Context.MODE_PRIVATE)
        )
    }
}
