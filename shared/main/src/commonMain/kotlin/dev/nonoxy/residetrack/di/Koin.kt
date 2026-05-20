package dev.nonoxy.residetrack.di

import dev.nonoxy.core.database.di.coreDatabaseModule
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            coreDatabaseModule,

            featureRoomsImplModule,
            featureAddRoomImplModule,
            featureManageStudentsImplModule,
        )
    }
}
