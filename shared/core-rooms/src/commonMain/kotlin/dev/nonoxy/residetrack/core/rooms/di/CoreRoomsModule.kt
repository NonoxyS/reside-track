package dev.nonoxy.residetrack.core.rooms.di

import dev.nonoxy.residetrack.core.rooms.data.DraftRoomRepositoryImpl
import dev.nonoxy.residetrack.core.rooms.data.RoomsRepositoryImpl
import dev.nonoxy.residetrack.core.rooms.data.storage.DraftRoomStorage
import dev.nonoxy.residetrack.core.rooms.data.storage.InMemoryDraftRoomStorage
import dev.nonoxy.residetrack.core.rooms.data.mappers.RoomMapper
import dev.nonoxy.residetrack.core.rooms.data.mappers.RoomMapperImpl
import dev.nonoxy.residetrack.core.rooms.data.mappers.StudentMapper
import dev.nonoxy.residetrack.core.rooms.data.mappers.StudentMapperImpl
import dev.nonoxy.residetrack.core.rooms.domain.repository.DraftRoomRepository
import dev.nonoxy.residetrack.core.rooms.domain.repository.RoomsRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.new
import org.koin.dsl.module

val coreRoomsModule = module {

    factoryOf<StudentMapper>(::StudentMapperImpl)

    factory<RoomMapper> { new(::RoomMapperImpl) }

    single<RoomsRepository> { new(::RoomsRepositoryImpl) }

    single<DraftRoomStorage> { InMemoryDraftRoomStorage() }

    single<DraftRoomRepository> { new(::DraftRoomRepositoryImpl) }
}
