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
