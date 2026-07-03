package dev.nonoxy.residetrack.feature.upcoming.presentation.models

import dev.icerock.moko.resources.StringResource
import dev.nonoxy.residetrack.res.MR

data class UiUpcomingItem(
    val roomId: Long,
    val roomNumber: String,
    val floorNumber: String,
    val streamNumber: String,
    val checkOutDate: String, // ISO-8601
    val daysLeft: Int,
    val bucket: UiUpcomingBucket,
) {

    // Computed, not constructor args: keeps the model free of moko-resources at construction
    // so pure mapping logic (and its native unit tests) never trigger MR class init, mirroring
    // UiUpcomingBucket.title. The overdue/left choice is presentation logic, not UI's to decide.
    val daysLeftValue: String
        get() = if (daysLeft < 0) (-daysLeft).toString() else daysLeft.toString()

    val daysLeftLabel: StringResource
        get() = if (daysLeft < 0) MR.strings.upcoming_days_overdue else MR.strings.upcoming_days_left
}
