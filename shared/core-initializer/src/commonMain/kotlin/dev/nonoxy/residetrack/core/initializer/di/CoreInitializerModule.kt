package dev.nonoxy.residetrack.core.initializer.di

import dev.nonoxy.residetrack.core.initializer.AppInitializer
import org.koin.dsl.module

val coreInitializerModule = module {

    // Collects every Initializer bound across feature modules via getAll().
    single {
        AppInitializer(initializers = getAll())
    }
}
