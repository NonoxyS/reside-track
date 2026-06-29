package dev.nonoxy.residetrack.core.database.di

import dev.nonoxy.residetrack.core.database.AppDatabase
import dev.nonoxy.residetrack.core.database.dao.StudentDao
import dev.nonoxy.residetrack.core.database.storage.LocalRoomStorage
import dev.nonoxy.residetrack.core.database.storage.RoomStorage
import org.koin.core.module.Module
import org.koin.dsl.module

val coreDatabaseModule = module {

    platformDatabaseBuilderModules()

    single<AppDatabase> {
        AppDatabase.getAppDatabase(builder = get())
    }

    single<RoomStorage> {
        LocalRoomStorage(roomDao = get<AppDatabase>().getRoomDao())
    }

    single<StudentDao> {
        get<AppDatabase>().getStudentDao()
    }
}

internal expect fun Module.platformDatabaseBuilderModules()
