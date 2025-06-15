package dev.nonoxy.residetrack.di

import dev.nonoxy.core.database.di.coreDatabaseModule
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
import org.koin.core.KoinApplication
import org.koin.core.module.Module

fun KoinApplication.appModule() {
    modules(
        coreDatabaseModule,

        featureRoomsImplModule,
        featureAddRoomImplModule,
    )
}

internal expect fun koinPlatformModules(): List<Module>
