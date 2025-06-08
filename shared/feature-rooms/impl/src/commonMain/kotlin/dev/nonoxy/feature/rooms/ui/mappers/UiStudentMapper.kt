package dev.nonoxy.feature.rooms.ui.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.rooms.models.Student
import dev.nonoxy.feature.rooms.ui.models.UiStudent

internal interface UiStudentMapper : Mapper<Student, UiStudent>

internal class UiStudentMapperImpl : UiStudentMapper {

    override fun map(item: Student): UiStudent = with(item) {
        UiStudent(
            streamNumber = streamNumber.toString(),
            checkInDate = checkInDate.toString(), // ISO-8601
            checkOutDate = checkOutDate.toString(), // ISO-8601
            isCheckOutDateNearOrExpired = isCheckOutDateNearOrExpired
        )
    }
}
