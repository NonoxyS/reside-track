package dev.nonoxy.residetrack.di

import dev.nonoxy.residetrack.common.di.commonModule
import dev.nonoxy.residetrack.core.database.di.coreDatabaseModule
import dev.nonoxy.residetrack.core.initializer.di.coreInitializerModule
import dev.nonoxy.residetrack.core.rooms.di.coreRoomsModule
import dev.nonoxy.residetrack.feature.splash.presentation.di.featureSplashPresentationModule
import dev.nonoxy.residetrack.feature.add_room.impl.di.featureAddRoomImplModule
import dev.nonoxy.residetrack.feature.add_room.presentation.di.featureAddRoomPresentationModule
import dev.nonoxy.residetrack.feature.room_editor.impl.di.featureRoomEditorImplModule
import dev.nonoxy.residetrack.feature.room_editor.presentation.di.featureRoomEditorPresentationModule
import dev.nonoxy.residetrack.feature.upcoming.impl.di.featureUpcomingImplModule
import dev.nonoxy.residetrack.feature.upcoming.presentation.di.featureUpcomingPresentationModule
import dev.nonoxy.residetrack.feature.rooms.impl.di.featureRoomsImplModule
import dev.nonoxy.residetrack.feature.rooms.presentation.di.featureRoomsPresentationModule
import dev.nonoxy.residetrack.common.resources.di.commonResourcesModule
import dev.nonoxy.residetrack.core.domain.di.coreDomainModule
import dev.nonoxy.residetrack.core.mvikotlin.di.coreMVIKotlinModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.mp.KoinPlatform

fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    Napier.d(message = "start initKoin")

    if (KoinPlatform.getKoinOrNull() != null) return
    startKoin {
        appDeclaration?.invoke(this)
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
            featureRoomEditorImplModule,
            featureRoomEditorPresentationModule,

            featureUpcomingImplModule,
            featureUpcomingPresentationModule,

            mainModule,
        )
    }

    Napier.d(message = "finish initKoin")
}
