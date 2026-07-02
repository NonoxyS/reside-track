package dev.nonoxy.residetrack.feature.room_editor.impl.di

import dev.nonoxy.residetrack.common.coroutines.CoroutineDispatchers
import dev.nonoxy.residetrack.feature.room_editor.api.models.RoomEditorMode
import dev.nonoxy.residetrack.feature.room_editor.api.store.RoomEditorStore
import dev.nonoxy.residetrack.feature.room_editor.impl.domain.RoomEditorStoreFactory
import org.koin.dsl.bind
import org.koin.dsl.module

val featureRoomEditorImplModule = module {

    factory { (mode: RoomEditorMode) ->
        RoomEditorStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
            draftRoomRepository = get(),
        ).create(mode = mode)
    } bind RoomEditorStore::class
}
