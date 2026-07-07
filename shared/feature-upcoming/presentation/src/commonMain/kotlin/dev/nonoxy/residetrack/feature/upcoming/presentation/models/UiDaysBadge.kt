package dev.nonoxy.residetrack.feature.upcoming.presentation.models

/**
 * Готовое для показа значение бейджа. Для просрочки единица (дни/месяцы/годы) выбирается в
 * presentation по величине, чтобы бейдж оставался коротким даже при просрочке на месяцы/годы.
 */
sealed interface UiDaysBadge {

    data class Remaining(val days: Int) : UiDaysBadge

    data class Overdue(val amount: Int, val unit: UiOverdueUnit) : UiDaysBadge
}

enum class UiOverdueUnit { DAYS, MONTHS, YEARS }
