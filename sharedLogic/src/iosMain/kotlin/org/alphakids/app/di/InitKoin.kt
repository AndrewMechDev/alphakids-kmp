package org.alphakids.app.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.alphakids.app.game.di.gameModule
import org.alphakids.app.onboarding.di.onboardingModule
import org.alphakids.app.parent.di.parentModule
import org.alphakids.app.store.di.storeModule
import org.alphakids.app.studentpet.di.studentPetModule

/**
 * Singleton holding the initialized Koin instance for iOS Swift access.
 *
 * Usage from Swift:
 * ```
 * SharedLogicKt.doInitKoin()
 * let repo = SharedLogic.AppKoin.shared.koin.get(objCClass: GameRepository.self) as! GameRepository
 * ```
 */
object AppKoin {
    val shared = run {
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
        }.koin
    }
}

/**
 * Initializes Koin on iOS. Must be called BEFORE any ViewModel uses KoinHelper.
 * Safe to call multiple times — subsequent calls are ignored.
 */
fun doInitKoin() {
    // Force lazy initialization of AppKoin.shared
    val _ = AppKoin.shared
}
