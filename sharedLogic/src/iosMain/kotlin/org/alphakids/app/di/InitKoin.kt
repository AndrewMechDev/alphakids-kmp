package org.alphakids.app.di

import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.alphakids.app.game.di.gameModule
import org.alphakids.app.game.domain.repository.GameRepository
import org.alphakids.app.onboarding.di.onboardingModule
import org.alphakids.app.onboarding.domain.repository.AuthRepository
import org.alphakids.app.parent.di.parentModule
import org.alphakids.app.parent.domain.repository.ParentRepository
import org.alphakids.app.store.di.storeModule
import org.alphakids.app.studentpet.di.studentPetModule

/**
 * Singleton holding the initialized Koin instance for iOS Swift access.
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

/**
 * Non-generic accessors for Swift.
 *
 * Koin's `Koin.get<T>()` is a reified inline function: reification happens at
 * the Kotlin call site, not across the Kotlin/Native-ObjC boundary. Calling it
 * directly from Swift erases T to `Any`, so Koin looks up the wrong bean and
 * throws `NoBeanDefFoundException`, which crosses into Swift as an uncaught
 * Kotlin exception and crashes the app. These wrappers resolve T inside
 * Kotlin, so Swift only ever sees the already-resolved, concrete type.
 */
fun getAuthRepository(): AuthRepository = AppKoin.koin.get()
fun getGameRepository(): GameRepository = AppKoin.koin.get()
fun getParentRepository(): ParentRepository = AppKoin.koin.get()
