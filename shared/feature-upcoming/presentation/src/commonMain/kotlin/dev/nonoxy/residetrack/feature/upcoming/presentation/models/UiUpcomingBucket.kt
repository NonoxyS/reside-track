package dev.nonoxy.residetrack.feature.upcoming.presentation.models

import dev.icerock.moko.resources.StringResource
import dev.nonoxy.residetrack.res.MR

enum class UiUpcomingBucket {
    OVERDUE,
    TODAY_TOMORROW,
    THIS_WEEK;

    // Computed, not a constructor arg: keeps enum init free of moko-resources so
    // pure mapping logic (and its native unit tests) never trigger MR class init.
    val title: StringResource
        get() = when (this) {
            OVERDUE -> MR.strings.upcoming_bucket_overdue
            TODAY_TOMORROW -> MR.strings.upcoming_bucket_today_tomorrow
            THIS_WEEK -> MR.strings.upcoming_bucket_this_week
        }
}
