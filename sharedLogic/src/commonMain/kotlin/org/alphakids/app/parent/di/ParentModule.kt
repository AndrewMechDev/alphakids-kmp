package org.alphakids.app.parent.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.repository.ParentRepositoryImpl
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.koin.dsl.module

val parentModule = module {
    single<ParentRepository> { ParentRepositoryImpl(get<AlphaKidsApiClient>()) }
}
