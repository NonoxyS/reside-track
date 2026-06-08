package dev.nonoxy.residetrack.feature.manage_students.presentation.di

import dev.nonoxy.residetrack.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.residetrack.feature.manage_students.presentation.ManageStudentsViewModel
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiManageStudentsLabelMapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiManageStudentsLabelMapperImpl
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiManageStudentsStateMapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiManageStudentsStateMapperImpl
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiRoomMapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiStudentMapper
import dev.nonoxy.residetrack.feature.manage_students.presentation.mappers.UiStudentMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManageStudentsPresentationModule = module {

    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf(::UiManageStudentsStateMapperImpl) bind UiManageStudentsStateMapper::class
    factoryOf(::UiManageStudentsLabelMapperImpl) bind UiManageStudentsLabelMapper::class

    viewModel { params ->
        val mode = params.get<ManageStudentsMode>()
        ManageStudentsViewModel(
            store = get { parametersOf(mode) },
            stateMapper = get(),
            labelMapper = get(),
        )
    }
}
