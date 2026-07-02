package org.alphakids.app.onboarding.di

import org.alphakids.app.data.remote.AlphaKidsApiClient
import org.alphakids.app.data.remote.TokenStorage
import org.alphakids.app.data.repository.AuthRepositoryImpl
import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for onboarding feature bindings.
 *
 * - [AuthRepository] → [AuthRepositoryImpl] (real API via AlphaKidsApiClient)
 * - [MockPetsRepository] (in-memory pet data for onboarding wizard)
 */
val onboardingModule: Module = module {
    single<AuthRepository> { AuthRepositoryImpl(get<AlphaKidsApiClient>(), get<TokenStorage>()) }
    single { MockPetsRepository() }
}
