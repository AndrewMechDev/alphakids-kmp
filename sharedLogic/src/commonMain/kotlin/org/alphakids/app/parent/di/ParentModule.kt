package org.alphakids.app.parent.di

import org.alphakids.app.data.fake.FakeParentRepository
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.repository.ParentRepositoryImpl
import org.alphakids.app.di.OfflineMode
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.koin.dsl.module

val parentModule = module {
    single<ParentRepository> {
        if (OfflineMode.enabled) FakeParentRepository()
        else ParentRepositoryImpl(get<AlphaKidsApiClient>())
    }
}
