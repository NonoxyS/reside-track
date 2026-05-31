package dev.nonoxy.residetrack.feature.add_room.impl.di

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.residetrack.feature.add_room.impl.domain.AddRoomStoreFactory
import org.koin.dsl.module

val featureAddRoomImplModule = module {

    factory<AddRoomStore> {
        AddRoomStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }
}
