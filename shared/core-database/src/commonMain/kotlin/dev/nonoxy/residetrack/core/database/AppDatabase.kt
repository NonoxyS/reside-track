package dev.nonoxy.residetrack.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.nonoxy.residetrack.common.coroutines.ioDispatcher
import dev.nonoxy.residetrack.core.database.dao.RoomDao
import dev.nonoxy.residetrack.core.database.dao.StudentDao
import dev.nonoxy.residetrack.core.database.entities.RoomEntity
import dev.nonoxy.residetrack.core.database.entities.StudentEntity

@Database(entities = [RoomEntity::class, StudentEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun getRoomDao(): RoomDao
    abstract fun getStudentDao(): StudentDao

    internal companion object {
        internal fun getAppDatabase(
            builder: Builder<AppDatabase>
        ): AppDatabase {
            return builder
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(ioDispatcher)
                .build()
        }
    }
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
