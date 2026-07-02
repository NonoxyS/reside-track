package dev.nonoxy.residetrack.core.notifications.di

import dev.nonoxy.residetrack.core.notifications.android.AndroidLocalNotifier
import dev.nonoxy.residetrack.core.notifications.domain.LocalNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module

internal actual fun Module.platformNotificationModules() {
    single<LocalNotifier> { AndroidLocalNotifier(context = androidContext()) }
}
