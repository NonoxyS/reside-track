package dev.nonoxy.feature.add_room.di

import dev.nonoxy.feature.add_room.presentation.AddRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAddRoomImplModule = module {

    viewModelOf(::AddRoomViewModel)
}
