package dev.nonoxy.residetrack.core.notifications.di

import dev.nonoxy.residetrack.core.initializer.Initializer
import dev.nonoxy.residetrack.core.notifications.domain.NotificationInitializer
import dev.nonoxy.residetrack.core.notifications.domain.NotificationScheduler
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreNotificationsModule = module {

    platformNotificationModules()

    single { NotificationScheduler(roomStorage = get(), localNotifier = get(), dispatchers = get()) }

    factoryOf(::NotificationInitializer) bind Initializer::class
}

internal expect fun Module.platformNotificationModules()
