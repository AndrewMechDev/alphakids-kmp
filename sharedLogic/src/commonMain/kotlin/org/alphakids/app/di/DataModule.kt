package org.alphakids.app.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.InMemoryTokenStorage
import org.alphakids.app.data.remote.TokenStorage
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module = module {
    single<TokenStorage> {
        InMemoryTokenStorage().apply {
            if (OfflineMode.enabled) {
                accessToken = "fake-offline-token"
                refreshToken = "fake-offline-refresh"
            }
        }
    }
    single { AlphaKidsApiClient(get()) }
}
