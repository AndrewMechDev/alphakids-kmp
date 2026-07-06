package org.alphakids.app.store.di

import org.alphakids.app.data.fake.FakeStoreRepository
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.di.OfflineMode
import org.alphakids.app.store.data.StoreRepositoryImpl
import org.alphakids.app.store.domain.repository.StoreRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val storeModule: Module = module {
    single<StoreRepository> {
        if (OfflineMode.enabled) FakeStoreRepository()
        else StoreRepositoryImpl(get<AlphaKidsApiClient>())
    }
}
