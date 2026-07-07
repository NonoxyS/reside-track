package dev.nonoxy.residetrack.core.notifications.domain

import kotlinx.datetime.LocalDate

/**
 * Один локальный пуш на дату выезда: сколько мест освобождается и в каких комнатах.
 *
 * @param date дата выезда (local).
 * @param roomNumbers номера комнат, из которых выезжают в эту дату (distinct, по возрастанию).
 * @param placesCount сколько койко-мест освобождается в эту дату.
 * @param fireAtEpochMillis момент показа пуша: [date] минус лид-тайм, 09:00 local.
 */
data class CheckoutDigest(
    val date: LocalDate,
    val roomNumbers: List<Int>,
    val placesCount: Int,
    val fireAtEpochMillis: Long,
)
