package dev.nonoxy.residetrack.feature.upcoming.presentation.models

data class UiUpcomingItem(
    val roomId: Long,
    val roomNumber: String,
    val floorNumber: String,
    val streamNumber: String,
    val checkOutDate: String, // ISO-8601
    val daysLeft: Int,
    val bucket: UiUpcomingBucket,
)
