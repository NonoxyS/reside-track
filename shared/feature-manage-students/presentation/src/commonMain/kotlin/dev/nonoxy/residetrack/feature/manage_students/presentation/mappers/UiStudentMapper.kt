package dev.nonoxy.residetrack.feature.manage_students.presentation.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.models.UiStudent
import dev.nonoxy.residetrack.feature.rooms.api.models.Student

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
