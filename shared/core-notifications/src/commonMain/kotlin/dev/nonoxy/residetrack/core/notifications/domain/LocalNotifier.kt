package dev.nonoxy.residetrack.core.notifications.domain

/**
 * Платформенный планировщик локальных пушей о выезде.
 *
 * Реализации идемпотентны: [sync] полностью заменяет ранее запланированные пуши текущим набором
 * дайджестов (removeAll + reschedule), поэтому повторный вызов с тем же входом безопасен.
 */
interface LocalNotifier {

    /**
     * Запрашивает/проверяет разрешение на показ уведомлений.
     *
     * @return `true`, если уведомления разрешены.
     */
    suspend fun requestPermission(): Boolean

    /** Переустанавливает запланированные пуши под текущий список [digests]. */
    suspend fun sync(digests: List<CheckoutDigest>)
}
