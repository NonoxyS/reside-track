package dev.nonoxy.feature.rooms.di

import dev.nonoxy.feature.rooms.data.RoomsRepositoryImpl
import dev.nonoxy.feature.rooms.data.mappers.RoomMapper
import dev.nonoxy.feature.rooms.data.mappers.RoomMapperImpl
import dev.nonoxy.feature.rooms.data.mappers.StudentMapper
import dev.nonoxy.feature.rooms.data.mappers.StudentMapperImpl
import dev.nonoxy.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.feature.rooms.repository.RoomsRepository
import dev.nonoxy.feature.rooms.ui.mappers.UiRoomMapper
import dev.nonoxy.feature.rooms.ui.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.rooms.ui.mappers.UiStudentMapper
import dev.nonoxy.feature.rooms.ui.mappers.UiStudentMapperImpl
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureRoomsImplModule = module {

    factory<StudentMapper> {
        StudentMapperImpl()
    }

    factory<RoomMapper> {
        RoomMapperImpl(studentMapper = get())
    }

    factory<RoomsRepository> {
        RoomsRepositoryImpl(
            roomDao = get(),
            roomMapper = get()
        )
    }

    factory<UiStudentMapper> {
        UiStudentMapperImpl()
    }

    factory<UiRoomMapper> {
        UiRoomMapperImpl(studentMapper = get())
    }

    viewModelOf(::RoomsViewModel)
}
