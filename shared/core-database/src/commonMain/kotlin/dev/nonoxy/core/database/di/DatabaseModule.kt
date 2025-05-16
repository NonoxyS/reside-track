package dev.nonoxy.core.database.di

import dev.nonoxy.core.database.AppDatabase
import org.koin.core.module.Module
import org.koin.dsl.module

val coreDatabaseModule = module {

    platformDatabaseBuilderModules()

    single<AppDatabase> {
        AppDatabase.getAppDatabase(builder = get())
    }
}

internal expect fun Module.platformDatabaseBuilderModules()
