package dev.nonoxy.residetrack.di

import dev.nonoxy.core.database.di.coreDatabaseModule
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
import dev.nonoxy.residetrack.navigation.Bt1VM
import dev.nonoxy.residetrack.navigation.Bt2VM
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun KoinApplication.appModule() {
    modules(
        coreDatabaseModule,

        featureRoomsImplModule,

        module {
            viewModelOf(::Bt1VM)
            viewModelOf(::Bt2VM)
        }
    )
}

internal expect fun koinPlatformModules(): List<Module>
