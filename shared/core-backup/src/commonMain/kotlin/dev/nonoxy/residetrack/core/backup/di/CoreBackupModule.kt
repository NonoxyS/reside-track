package dev.nonoxy.residetrack.core.backup.di

import dev.nonoxy.residetrack.core.backup.data.BackupRepositoryImpl
import dev.nonoxy.residetrack.core.backup.data.gateway.BackupGateway
import dev.nonoxy.residetrack.core.backup.data.gateway.RoomBackupGateway
import dev.nonoxy.residetrack.core.backup.domain.repository.BackupRepository
import org.koin.dsl.module

val coreBackupModule = module {
    single<BackupGateway> { RoomBackupGateway(roomDao = get()) }
    single<BackupRepository> { BackupRepositoryImpl(gateway = get(), json = get()) }
}
