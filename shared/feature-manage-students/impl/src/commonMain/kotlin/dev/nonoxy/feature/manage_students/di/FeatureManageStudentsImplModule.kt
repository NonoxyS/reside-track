package dev.nonoxy.feature.manage_students.di

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

    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf<StringProvider>(::StringProviderImpl)

    // TODO Task 16: remove together with OldManageStudentsViewModel.
    viewModel { parameters ->
        OldManageStudentsViewModel(
            mode = parameters.get(),
            roomsRepository = get(),
            uiRoomMapper = get(),
            uiStudentMapper = get(),
            stringProvider = get()
        )
    }
}
