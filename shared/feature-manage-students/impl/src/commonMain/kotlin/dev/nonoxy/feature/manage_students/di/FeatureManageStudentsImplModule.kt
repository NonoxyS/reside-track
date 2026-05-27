package dev.nonoxy.feature.manage_students.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory
import dev.nonoxy.feature.manage_students.presentation.OldManageStudentsViewModel
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapperImpl
import dev.nonoxy.feature.manage_students.utils.StringProvider
import dev.nonoxy.feature.manage_students.utils.StringProviderImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManageStudentsImplModule = module {

    // TODO Task 16: drop these duplicates once presentation module's DI fully takes over.
    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf<StringProvider>(::StringProviderImpl)

    factory { (mode: ManageStudentsMode) ->
        ManageStudentsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create(mode = mode)
    } bind ManageStudentsStore::class

    // TODO Task 16: remove together with OldManageStudentsViewModel.
    viewModel { parameters ->
        OldManageStudentsViewModel(
            mode = parameters.get(),
            roomsRepository = get(),
            uiRoomMapper = get(),
            uiStudentMapper = get(),
            stringProvider = get(),
        )
    }
}
