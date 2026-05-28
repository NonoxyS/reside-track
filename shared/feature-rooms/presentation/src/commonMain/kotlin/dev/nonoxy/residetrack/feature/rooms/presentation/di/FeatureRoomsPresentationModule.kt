package dev.nonoxy.residetrack.feature.rooms.presentation.di

import dev.nonoxy.residetrack.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomMapper
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomsLabelMapper
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomsLabelMapperImpl
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomsStateMapper
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiRoomsStateMapperImpl
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiStudentMapper
import dev.nonoxy.residetrack.feature.rooms.presentation.mappers.UiStudentMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureRoomsPresentationModule = module {

    // `bind` form used for parameterized Impls; typed form `factoryOf<T>(::Impl)` is reserved for
    // zero-arg Impls (Koin 4.1 type inference on the reified parameter is unreliable when ::Impl
    // is a function reference with arguments — switch to bind in that case).
    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf(::UiRoomsStateMapperImpl) bind UiRoomsStateMapper::class
    factoryOf<UiRoomsLabelMapper>(::UiRoomsLabelMapperImpl)

    viewModelOf(::RoomsViewModel)
}
