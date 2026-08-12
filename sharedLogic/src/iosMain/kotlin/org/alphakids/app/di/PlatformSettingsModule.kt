package org.alphakids.app.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * Persistent key-value storage backed by NSUserDefaults — survives process
 * death, unlike the in-memory state it replaced for tokens and the active
 * child id.
 */
val platformSettingsModule = module {
    single<Settings> { NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults) }
}
