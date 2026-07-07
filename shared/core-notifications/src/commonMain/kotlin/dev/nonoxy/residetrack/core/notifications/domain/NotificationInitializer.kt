package dev.nonoxy.residetrack.core.notifications.domain

import dev.nonoxy.residetrack.core.initializer.Initializer

/**
 * Стартовая задача: запускает [NotificationScheduler]. Разрешение на показ уведомлений
 * запрашивается отдельно из UI (splash), т.к. системный диалог требует Activity/композиции.
 *
 * priority > 0 — после сида комнат (priority 0). Некритична: сбой не должен ронять запуск,
 * т.к. напоминания — вспомогательная функция.
 */
internal class NotificationInitializer(
    private val scheduler: NotificationScheduler,
) : Initializer {

    override val priority: Int = NOTIFICATION_PRIORITY

    override val isCritical: Boolean = false

    override suspend fun initialize() {
        scheduler.start()
    }

    private companion object {
        const val NOTIFICATION_PRIORITY = 1
    }
}
