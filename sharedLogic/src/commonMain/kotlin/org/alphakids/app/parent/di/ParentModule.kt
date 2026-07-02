package org.alphakids.app.parent.di

import io.ktor.client.*
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.repository.ParentRepositoryImpl
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.koin.dsl.module

val parentModule = module {
    single<ParentRepository> { ParentRepositoryImpl(get<HttpClient>(), get<TokenStorage>()) }
}
