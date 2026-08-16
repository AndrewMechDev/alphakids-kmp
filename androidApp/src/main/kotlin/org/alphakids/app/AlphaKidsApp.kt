package org.alphakids.app

import android.app.Application
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.alphakids.app.di.commonModule
import org.alphakids.app.di.dataModule
import org.alphakids.app.di.domainModule
import org.alphakids.app.di.platformSettingsModule
import org.alphakids.app.game.di.gameModule
import org.alphakids.app.onboarding.di.onboardingModule
import org.alphakids.app.parent.di.parentModule
import org.alphakids.app.parent.domain.model.SessionManager
import org.alphakids.app.store.di.storeModule
import org.alphakids.app.studentpet.di.studentPetModule

class AlphaKidsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val koinApp = startKoin {
            androidContext(this@AlphaKidsApp)
            properties(mapOf("apiBaseUrl" to BuildConfig.API_BASE_URL))
            modules(
                commonModule,
                domainModule,
                platformSettingsModule,
                dataModule,
                onboardingModule,
                parentModule,
                gameModule,
                studentPetModule,
                storeModule,
            )
        }
        SessionManager.init(koinApp.koin.get<Settings>())
    }
}
