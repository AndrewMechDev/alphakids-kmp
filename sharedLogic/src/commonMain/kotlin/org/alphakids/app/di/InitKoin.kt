package org.alphakids.app.di

import org.koin.core.context.startKoin
import org.alphakids.app.onboarding.di.onboardingModule
import org.alphakids.app.parent.di.parentModule

fun initKoin() {
    startKoin {
        modules(commonModule, domainModule, dataModule, onboardingModule, parentModule)
    }
}
