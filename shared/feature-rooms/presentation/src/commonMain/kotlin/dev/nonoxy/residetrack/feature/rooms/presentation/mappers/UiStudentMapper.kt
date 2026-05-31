package dev.nonoxy.residetrack.feature.rooms.presentation.mappers

import dev.nonoxy.residetrack.common.utils.mapper.Mapper
import dev.nonoxy.residetrack.feature.rooms.api.models.Student
import dev.nonoxy.residetrack.feature.rooms.presentation.models.UiStudent

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
