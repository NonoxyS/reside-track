package dev.nonoxy.residetrack.feature.upcoming.api.models

import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingBucket
import kotlinx.datetime.LocalDate

data class UpcomingItem(
    val roomId: Long,
    val floorNumber: Int,
    val roomNumber: Int,
    val studentId: Long,
    val streamNumber: Int,
    val checkOutDate: LocalDate,
    val daysLeft: Int,
    val bucket: UpcomingBucket,
)
