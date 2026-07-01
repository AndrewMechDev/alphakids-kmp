package org.alphakids.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.alphakids.app.di.commonModule
import org.alphakids.app.di.dataModule
import org.alphakids.app.di.domainModule
import org.alphakids.app.onboarding.di.onboardingModule
import org.alphakids.app.parent.di.parentModule

class AlphaKidsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AlphaKidsApp)
            modules(commonModule, domainModule, dataModule, onboardingModule, parentModule)
        }
    }
}
