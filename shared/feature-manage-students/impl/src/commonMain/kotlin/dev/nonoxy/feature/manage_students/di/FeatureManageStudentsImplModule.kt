package dev.nonoxy.feature.manage_students.di

import dev.nonoxy.feature.manage_students.presentation.ManageStudentsViewModel
import dev.nonoxy.feature.manage_students.ui.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.ui.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.manage_students.ui.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.ui.mappers.UiStudentMapperImpl
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

    viewModel { parameters ->
        ManageStudentsViewModel(
            mode = parameters.get(),
            roomsRepository = get(),
            uiRoomMapper = get(),
            uiStudentMapper = get(),
            stringProvider = get()
        )
    }
}
