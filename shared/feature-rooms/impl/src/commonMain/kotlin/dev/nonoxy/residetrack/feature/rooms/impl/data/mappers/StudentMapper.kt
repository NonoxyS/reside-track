package dev.nonoxy.residetrack.feature.rooms.impl.data.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.common.utils.toLocalDate
import dev.nonoxy.residetrack.core.database.entities.StudentEntity
import dev.nonoxy.residetrack.feature.rooms.api.models.Student
import kotlin.time.Clock
import kotlinx.datetime.minus

internal interface StudentMapper : Mapper<StudentEntity, Student>

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
            isCheckOutDateNearOrExpired = checkOutDate.minus(other = currentDate).days <= 3
        )
    }
}
