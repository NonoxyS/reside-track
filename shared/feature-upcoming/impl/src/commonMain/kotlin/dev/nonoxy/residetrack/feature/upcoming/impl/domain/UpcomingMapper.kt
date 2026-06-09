package dev.nonoxy.residetrack.feature.upcoming.impl.domain

import dev.nonoxy.residetrack.core.rooms.models.Room
import dev.nonoxy.residetrack.core.rooms.upcoming.UpcomingCheckouts
import dev.nonoxy.residetrack.feature.upcoming.api.models.UpcomingItem
import kotlinx.datetime.LocalDate

internal object UpcomingMapper {

    fun map(rooms: List<Room>, today: LocalDate): List<UpcomingItem> =
        rooms.flatMap { room ->
            room.students.mapNotNull { student ->
                val daysLeft = UpcomingCheckouts.daysLeft(student.checkOutDate, today)
                if (daysLeft > UpcomingCheckouts.THRESHOLD_DAYS) return@mapNotNull null
                UpcomingItem(
                    roomId = room.id,
                    floorNumber = room.floorNumber,
                    roomNumber = room.roomNumber,
                    studentId = student.id,
                    streamNumber = student.streamNumber,
                    checkOutDate = student.checkOutDate,
                    daysLeft = daysLeft,
                    bucket = UpcomingCheckouts.bucketOf(daysLeft),
                )
            }
        }.sortedBy { it.daysLeft }
}
