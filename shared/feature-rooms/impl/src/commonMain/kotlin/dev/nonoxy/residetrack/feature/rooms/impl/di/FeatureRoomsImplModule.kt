package dev.nonoxy.residetrack.feature.rooms.impl.di

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.feature.rooms.api.store.RoomsStore
import dev.nonoxy.residetrack.feature.rooms.impl.domain.RoomsStoreFactory
import dev.nonoxy.residetrack.feature.rooms.impl.domain.seed.RoomsSeedInitializer
import dev.nonoxy.residetrack.core.initializer.Initializer
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureRoomsImplModule = module {

    factoryOf(::RoomsSeedInitializer) bind Initializer::class

    factory<RoomsStore> {
        RoomsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
            backupRepository = get(),
        ).create()
    }
}
