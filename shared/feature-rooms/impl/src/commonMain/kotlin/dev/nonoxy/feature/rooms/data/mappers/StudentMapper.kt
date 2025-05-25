package dev.nonoxy.feature.rooms.data.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.common.utils.toLocalDate
import dev.nonoxy.core.database.entities.StudentEntity
import dev.nonoxy.feature.rooms.models.Student

internal interface StudentMapper : Mapper<StudentEntity, Student>

internal class StudentMapperImpl : StudentMapper {

    override fun map(item: StudentEntity): Student {
        return Student(
            id = item.id,
            streamNumber = item.streamNumber,
            checkInDate = item.checkInDateEpochMillis.toLocalDate(),
            checkOutDate = item.checkOutDateEpochMillis.toLocalDate()
        )
    }
}
