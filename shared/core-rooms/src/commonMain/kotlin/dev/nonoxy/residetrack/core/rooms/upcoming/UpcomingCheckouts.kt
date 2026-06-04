package dev.nonoxy.residetrack.core.rooms.upcoming

import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

object UpcomingCheckouts {

    const val THRESHOLD_DAYS = 7

    fun daysLeft(checkOutDate: LocalDate, today: LocalDate): Int =
        today.daysUntil(checkOutDate)

    fun isUpcoming(checkOutDate: LocalDate, today: LocalDate): Boolean =
        daysLeft(checkOutDate, today) <= THRESHOLD_DAYS

    fun bucketOf(daysLeft: Int): UpcomingBucket = when {
        daysLeft < 0 -> UpcomingBucket.OVERDUE
        daysLeft <= 1 -> UpcomingBucket.TODAY_TOMORROW
        else -> UpcomingBucket.THIS_WEEK
    }
}
