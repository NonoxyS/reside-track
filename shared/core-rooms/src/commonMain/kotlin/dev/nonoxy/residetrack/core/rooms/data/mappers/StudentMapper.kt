package dev.nonoxy.residetrack.core.rooms.data.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.common.utils.toLocalDate
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.core.rooms.domain.model.Student
import dev.nonoxy.residetrack.core.rooms.domain.upcoming.UpcomingCheckouts
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal interface StudentMapper : Mapper<StudentEntity, Student> {
    fun map(item: Student, roomId: Long): StudentEntity
    fun map(items: List<Student>, roomId: Long): List<StudentEntity> =
        items.map { map(it, roomId) }
}

internal class StudentMapperImpl : StudentMapper {

    override fun map(item: StudentEntity): Student {
        val checkInDate = item.checkInDateEpochMillis.toLocalDate()
        val checkOutDate = item.checkOutDateEpochMillis.toLocalDate()
        val currentDate = Clock.System.now().toLocalDate()

        return Student(
            id = item.id,
            streamNumber = item.streamNumber,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            isCheckOutDateNearOrExpired = UpcomingCheckouts.daysLeft(checkOutDate, currentDate) <= 3
        )
    }

    override fun map(item: Student, roomId: Long): StudentEntity {
        val tz = TimeZone.currentSystemDefault()
        return StudentEntity(
            id = item.id,
            roomId = roomId,
            streamNumber = item.streamNumber,
            checkInDateEpochMillis = item.checkInDate.atStartOfDayIn(tz).toEpochMilliseconds(),
            checkOutDateEpochMillis = item.checkOutDate.atStartOfDayIn(tz).toEpochMilliseconds(),
        )
    }
}
