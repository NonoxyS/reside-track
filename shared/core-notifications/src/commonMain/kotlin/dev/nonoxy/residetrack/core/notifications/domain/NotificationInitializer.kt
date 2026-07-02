package dev.nonoxy.residetrack.core.notifications.domain

import dev.nonoxy.residetrack.core.initializer.Initializer

/**
 * Стартовая задача: запрашивает разрешение на уведомления и запускает [NotificationScheduler].
 *
 * priority > 0 — после сида комнат (priority 0). Некритична: сбой не должен ронять запуск,
 * т.к. напоминания — вспомогательная функция.
 */
internal class NotificationInitializer(
    private val localNotifier: LocalNotifier,
    private val scheduler: NotificationScheduler,
) : Initializer {

    override val priority: Int = NOTIFICATION_PRIORITY

    override val isCritical: Boolean = false

    override suspend fun initialize() {
        localNotifier.requestPermission()
        scheduler.start()
    }

    private companion object {
        const val NOTIFICATION_PRIORITY = 1
    }
}
