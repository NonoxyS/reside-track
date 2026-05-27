package dev.nonoxy.feature.add_room.presentation.di

import dev.nonoxy.feature.add_room.presentation.AddRoomViewModel
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomLabelMapper
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomLabelMapperImpl
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomStateMapper
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAddRoomPresentationModule = module {

    factoryOf<UiAddRoomStateMapper>(::UiAddRoomStateMapperImpl)
    factoryOf<UiAddRoomLabelMapper>(::UiAddRoomLabelMapperImpl)

    viewModelOf(::AddRoomViewModel)
}
