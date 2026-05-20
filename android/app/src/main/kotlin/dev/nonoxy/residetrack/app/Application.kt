package dev.nonoxy.residetrack.app

import android.app.Application
import dev.nonoxy.residetrack.BuildConfig
import dev.nonoxy.residetrack.di.appModule
import dev.nonoxy.residetrack.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoin()
        setupLogging()
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog(defaultTag = "ResideTrack"))
        }
    }

    private fun setupKoin() {
        initKoin {
            androidContext(this@Application)
            modules(
                appModule,
            )
        }
    }
}
