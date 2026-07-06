package org.alphakids.app.onboarding.di

import org.alphakids.app.data.fake.FakeAuthRepository
import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.repository.AuthRepositoryImpl
import org.alphakids.app.di.OfflineMode
import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

val onboardingModule: Module = module {
    single<AuthRepository> {
        if (OfflineMode.enabled) FakeAuthRepository()
        else AuthRepositoryImpl(get<AlphaKidsApiClient>(), get<TokenStorage>())
    }
    single { MockPetsRepository() }
}
