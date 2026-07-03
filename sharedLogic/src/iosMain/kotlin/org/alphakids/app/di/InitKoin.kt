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
 *
 * Note: the Koin instance is exposed as `koin`, not `shared` — Kotlin objects
 * exported to Objective-C/Swift automatically get a synthetic `shared` class
 * property to access the singleton, so a member also named `shared` would collide.
 */
object AppKoin {
    val koin = run {
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
    // Force lazy initialization of AppKoin.koin
    AppKoin.koin
}
