package dev.nonoxy.residetrack.feature.room_editor.presentation.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.models.UiStudent
import dev.nonoxy.residetrack.core.rooms.models.Student

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
