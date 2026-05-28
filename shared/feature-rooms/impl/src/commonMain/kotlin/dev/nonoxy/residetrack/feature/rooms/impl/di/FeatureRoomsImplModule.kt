package dev.nonoxy.residetrack.feature.rooms.impl.di

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.impl.data.RoomsRepositoryImpl
import dev.nonoxy.residetrack.feature.rooms.impl.data.mappers.RoomMapper
import dev.nonoxy.residetrack.feature.rooms.impl.data.mappers.RoomMapperImpl
import dev.nonoxy.residetrack.feature.rooms.impl.data.mappers.StudentMapper
import dev.nonoxy.residetrack.feature.rooms.impl.data.mappers.StudentMapperImpl
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory
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
}
