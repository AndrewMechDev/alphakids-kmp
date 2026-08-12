package org.alphakids.app.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.SettingsTokenStorage
import org.alphakids.app.data.remote.TokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module = module {
    single<TokenStorage> { SettingsTokenStorage(get()) }
    single { AlphaKidsApiClient(get()) }
}
