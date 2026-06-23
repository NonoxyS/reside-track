package dev.nonoxy.residetrack.feature.room_editor.presentation.di

import dev.nonoxy.residetrack.feature.room_editor.api.models.RoomEditorMode
import dev.nonoxy.residetrack.feature.room_editor.presentation.RoomEditorViewModel
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomEditorLabelMapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomEditorLabelMapperImpl
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomEditorStateMapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomEditorStateMapperImpl
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomMapper
import dev.nonoxy.residetrack.feature.room_editor.presentation.mappers.UiRoomMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureRoomEditorPresentationModule = module {

    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf(::UiRoomEditorStateMapperImpl) bind UiRoomEditorStateMapper::class
    factoryOf(::UiRoomEditorLabelMapperImpl) bind UiRoomEditorLabelMapper::class

    viewModel { params ->
        val mode = params.get<RoomEditorMode>()
        RoomEditorViewModel(
            store = get { parametersOf(mode) },
            stateMapper = get(),
            labelMapper = get(),
        )
    }
}
