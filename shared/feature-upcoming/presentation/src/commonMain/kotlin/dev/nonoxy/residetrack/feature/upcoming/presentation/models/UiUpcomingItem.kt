package dev.nonoxy.residetrack.feature.upcoming.presentation.models

import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingBucket

data class UiUpcomingItem(
    val roomId: Long,
    val roomLabel: String, // e.g. "14 · эт. 2"
    val streamNumber: String,
    val checkOutDate: String, // ISO-8601
    val daysLeft: Int,
    val bucket: UpcomingBucket,
)
