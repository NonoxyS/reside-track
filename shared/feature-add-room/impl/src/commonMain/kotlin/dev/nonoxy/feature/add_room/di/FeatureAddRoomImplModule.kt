package dev.nonoxy.feature.add_room.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory
import dev.nonoxy.feature.add_room.presentation.OldAddRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAddRoomImplModule = module {

    factory<AddRoomStore> {
        AddRoomStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }

    // TODO Task 7: remove together with OldAddRoomViewModel.
    viewModelOf(::OldAddRoomViewModel)
}
