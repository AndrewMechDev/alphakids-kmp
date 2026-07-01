package org.alphakids.app.onboarding.di

import org.alphakids.app.onboarding.data.mock.MockAuthRepository
import org.alphakids.app.onboarding.data.mock.MockPetsRepository
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for onboarding feature bindings.
 *
 * Provides mock implementations for:
 * - [AuthRepository] → [MockAuthRepository]
 * - [MockPetsRepository] (in-memory pet data)
 */
val onboardingModule: Module = module {
    single<AuthRepository> { MockAuthRepository.createWithDemoUser() }
    single { MockPetsRepository() }
}
