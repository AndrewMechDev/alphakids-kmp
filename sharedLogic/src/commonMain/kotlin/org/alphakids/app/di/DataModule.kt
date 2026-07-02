package org.alphakids.app.di

import io.ktor.client.*
import org.alphakids.app.data.remote.InMemoryTokenStorage
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.remote.createApiClient
import org.koin.core.module.Module
import org.koin.dsl.module

val dataModule: Module = module {
    single<HttpClient> { createApiClient() }
    single<TokenStorage> { InMemoryTokenStorage() }
}
