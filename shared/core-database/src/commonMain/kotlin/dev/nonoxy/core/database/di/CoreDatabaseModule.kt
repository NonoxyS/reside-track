package dev.nonoxy.core.database.di

import dev.nonoxy.core.database.AppDatabase
import dev.nonoxy.core.database.dao.RoomDao
import dev.nonoxy.core.database.dao.StudentDao
import org.koin.core.module.Module
import org.koin.dsl.module

val coreDatabaseModule = module {

    platformDatabaseBuilderModules()

    single<AppDatabase> {
        AppDatabase.getAppDatabase(builder = get())
    }

    single<RoomDao> {
        get<AppDatabase>().getRoomDao()
    }

    single<StudentDao> {
        get<AppDatabase>().getStudentDao()
    }
}

internal expect fun Module.platformDatabaseBuilderModules()
