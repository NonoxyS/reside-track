package dev.nonoxy.residetrack.di

import dev.nonoxy.residetrack.common.di.commonModule
import dev.nonoxy.residetrack.core.database.di.coreDatabaseModule
import dev.nonoxy.residetrack.core.initializer.di.coreInitializerModule
import dev.nonoxy.residetrack.core.rooms.di.coreRoomsModule
import dev.nonoxy.residetrack.feature.splash.presentation.di.featureSplashPresentationModule
import dev.nonoxy.residetrack.feature.add_room.impl.di.featureAddRoomImplModule
import dev.nonoxy.residetrack.feature.add_room.presentation.di.featureAddRoomPresentationModule
import dev.nonoxy.residetrack.feature.manage_students.impl.di.featureManageStudentsImplModule
import dev.nonoxy.residetrack.feature.manage_students.presentation.di.featureManageStudentsPresentationModule
import dev.nonoxy.residetrack.feature.rooms.impl.di.featureRoomsImplModule
import dev.nonoxy.residetrack.feature.rooms.presentation.di.featureRoomsPresentationModule
import dev.nonoxy.residetrack.common.resources.di.commonResourcesModule
import dev.nonoxy.residetrack.core.domain.di.coreDomainModule
import dev.nonoxy.residetrack.core.mvikotlin.di.coreMVIKotlinModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            commonModule,
            commonResourcesModule,

            coreDomainModule,
            coreMVIKotlinModule,
            coreDatabaseModule,
            coreInitializerModule,
            coreRoomsModule,

            featureSplashPresentationModule,

            featureRoomsImplModule,
            featureRoomsPresentationModule,

            featureAddRoomImplModule,
            featureAddRoomPresentationModule,
            featureManageStudentsImplModule,
            featureManageStudentsPresentationModule,
        )
    }
}
