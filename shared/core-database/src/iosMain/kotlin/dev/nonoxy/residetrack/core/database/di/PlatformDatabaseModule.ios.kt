package dev.nonoxy.residetrack.core.database.di

import androidx.room.RoomDatabase
import dev.nonoxy.residetrack.core.database.AppDatabase
import dev.nonoxy.residetrack.core.database.getDatabaseBuilder
import org.koin.core.module.Module

internal actual fun Module.platformDatabaseBuilderModules() {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
    }
}
