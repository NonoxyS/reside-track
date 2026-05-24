package dev.nonoxy.feature.rooms.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.data.RoomsRepositoryImpl
import dev.nonoxy.feature.rooms.data.mappers.RoomMapper
import dev.nonoxy.feature.rooms.data.mappers.RoomMapperImpl
import dev.nonoxy.feature.rooms.data.mappers.StudentMapper
import dev.nonoxy.feature.rooms.data.mappers.StudentMapperImpl
import dev.nonoxy.feature.rooms.impl.domain.RoomsStoreFactory
import dev.nonoxy.feature.rooms.presentation.OldRoomsViewModel
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.rooms.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiStudentMapperImpl
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

    factory<RoomsStore> {
        RoomsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }

    // TODO Task 7: remove — OldRoomsViewModel is the only consumer of these mapper bindings.
    //  After OldRoomsViewModel is deleted, mappers come exclusively from featureRoomsPresentationModule.
    factory<UiStudentMapper> {
        UiStudentMapperImpl()
    }

    factory<UiRoomMapper> {
        UiRoomMapperImpl(studentMapper = get())
    }

    viewModelOf(::OldRoomsViewModel)
}
