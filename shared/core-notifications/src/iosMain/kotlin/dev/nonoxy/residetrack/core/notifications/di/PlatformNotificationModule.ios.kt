package dev.nonoxy.residetrack.core.notifications.di

import dev.nonoxy.residetrack.core.notifications.domain.LocalNotifier
import dev.nonoxy.residetrack.core.notifications.ios.IosLocalNotifier
import org.koin.core.module.Module

internal actual fun Module.platformNotificationModules() {
    single<LocalNotifier> { IosLocalNotifier() }
}
