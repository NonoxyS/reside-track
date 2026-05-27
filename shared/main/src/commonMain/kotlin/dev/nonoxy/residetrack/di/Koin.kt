package dev.nonoxy.residetrack.di

import dev.nonoxy.common.di.commonModule
import dev.nonoxy.core.database.di.coreDatabaseModule
import dev.nonoxy.feature.add_room.impl.di.featureAddRoomImplModule
import dev.nonoxy.feature.add_room.presentation.di.featureAddRoomPresentationModule
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.manage_students.presentation.di.featureManageStudentsPresentationModule
import dev.nonoxy.feature.rooms.impl.di.featureRoomsImplModule
import dev.nonoxy.feature.rooms.presentation.di.featureRoomsPresentationModule
import dev.nonoxy.residetrack.core.domain.di.coreDomainModule
import dev.nonoxy.residetrack.core.mvikotlin.di.coreMVIKotlinModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            commonModule,

            coreDomainModule,
            coreMVIKotlinModule,
            coreDatabaseModule,

            featureRoomsImplModule,
            featureRoomsPresentationModule,

            featureAddRoomImplModule,
            featureAddRoomPresentationModule,
            featureManageStudentsImplModule,
            featureManageStudentsPresentationModule,
        )
    }
}
