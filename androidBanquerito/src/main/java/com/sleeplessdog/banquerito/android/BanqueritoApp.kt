package com.sleeplessdog.banquerito.android

import android.app.Application
import com.sleeplessdog.banquerito.data.DatabaseDriverFactory
import com.sleeplessdog.banquerito.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class BanqueritoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BanqueritoApp)
            modules(
                appModule, module {
                    single { DatabaseDriverFactory(androidContext()) }
                })
        }
    }
}