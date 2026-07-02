package org.alphakids.app.di

import org.koin.core.context.startKoin
import org.alphakids.app.game.di.gameModule
import org.alphakids.app.onboarding.di.onboardingModule
import org.alphakids.app.parent.di.parentModule
import org.alphakids.app.store.di.storeModule
import org.alphakids.app.studentpet.di.studentPetModule

fun initKoin() {
    startKoin {
        modules(
            commonModule,
            domainModule,
            dataModule,
            onboardingModule,
            parentModule,
            gameModule,
            studentPetModule,
            storeModule,
        )
    }
}
