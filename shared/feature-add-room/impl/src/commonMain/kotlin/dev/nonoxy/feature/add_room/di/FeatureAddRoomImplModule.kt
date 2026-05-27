package dev.nonoxy.feature.add_room.di

import dev.nonoxy.feature.add_room.presentation.OldAddRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAddRoomImplModule = module {

    // TODO Task 7: remove together with OldAddRoomViewModel.
    viewModelOf(::OldAddRoomViewModel)
}
