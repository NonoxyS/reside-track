package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.manage_students.presentation.models.UiStudent
import dev.nonoxy.feature.rooms.api.models.Student

interface UiStudentMapper : Mapper<Student, UiStudent>

class UiStudentMapperImpl : UiStudentMapper {

    override fun map(item: Student): UiStudent = with(item) {
        UiStudent(
            streamNumber = streamNumber.toString(),
            checkInDate = checkInDate.toString(),
            checkOutDate = checkOutDate.toString(),
            isCheckOutDateNearOrExpired = isCheckOutDateNearOrExpired,
        )
    }
}
