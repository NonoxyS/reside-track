package dev.nonoxy.feature.rooms.data.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.common.utils.toLocalDate
import dev.nonoxy.core.database.entities.StudentEntity
import dev.nonoxy.feature.rooms.models.Student
import kotlinx.datetime.Clock
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
