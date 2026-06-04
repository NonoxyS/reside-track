package dev.nonoxy.residetrack.core.rooms.di

import dev.nonoxy.residetrack.core.rooms.data.RoomsRepositoryImpl
import dev.nonoxy.residetrack.core.rooms.data.mappers.RoomMapper
import dev.nonoxy.residetrack.core.rooms.data.mappers.RoomMapperImpl
import dev.nonoxy.residetrack.core.rooms.data.mappers.StudentMapper
import dev.nonoxy.residetrack.core.rooms.data.mappers.StudentMapperImpl
import dev.nonoxy.residetrack.core.rooms.repository.RoomsRepository
import org.koin.dsl.module

val coreRoomsModule = module {

    factory<StudentMapper> { StudentMapperImpl() }

    factory<RoomMapper> { RoomMapperImpl(studentMapper = get()) }

    factory<RoomsRepository> {
        RoomsRepositoryImpl(
            roomDao = get(),
            roomMapper = get(),
            studentMapper = get(),
        )
    }
}
