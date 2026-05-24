package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.rooms.api.models.Student
import dev.nonoxy.feature.rooms.presentation.models.UiStudent

// TODO Task 7: restore `internal` on both interface and Impl — currently public so OldRoomsViewModel
//  in :shared:feature-rooms:impl can construct UiStudentMapperImpl via Koin DSL `::Impl` constructor ref.
interface UiStudentMapper : Mapper<Student, UiStudent>

class UiStudentMapperImpl : UiStudentMapper {

    override fun map(item: Student): UiStudent = with(item) {
        UiStudent(
            streamNumber = streamNumber.toString(),
            checkInDate = checkInDate.toString(), // ISO-8601
            checkOutDate = checkOutDate.toString(), // ISO-8601
            isCheckOutDateNearOrExpired = isCheckOutDateNearOrExpired
        )
    }
}
