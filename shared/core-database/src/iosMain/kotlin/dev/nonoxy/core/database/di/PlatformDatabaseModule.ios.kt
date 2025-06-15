package dev.nonoxy.core.database.di

import androidx.room.RoomDatabase
import dev.nonoxy.core.database.AppDatabase
import dev.nonoxy.core.database.getDatabaseBuilder
import org.koin.core.module.Module

internal actual fun Module.platformDatabaseBuilderModules() {
    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder()
    }
}
