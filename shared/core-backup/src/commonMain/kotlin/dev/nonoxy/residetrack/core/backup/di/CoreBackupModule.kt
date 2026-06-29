package dev.nonoxy.residetrack.core.backup.di

import dev.nonoxy.residetrack.core.backup.data.BackupRepositoryImpl
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import org.koin.dsl.module

val coreBackupModule = module {
    single<BackupRepository> { BackupRepositoryImpl(roomStorage = get(), json = get(), dispatchers = get()) }
}
