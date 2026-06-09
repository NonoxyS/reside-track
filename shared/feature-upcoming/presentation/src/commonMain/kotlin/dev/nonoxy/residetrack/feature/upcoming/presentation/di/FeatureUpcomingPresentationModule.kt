package dev.nonoxy.residetrack.feature.upcoming.presentation.di

import dev.nonoxy.residetrack.feature.upcoming.presentation.UpcomingViewModel
import dev.nonoxy.residetrack.feature.upcoming.presentation.mappers.UiUpcomingLabelMapper
import dev.nonoxy.residetrack.feature.upcoming.presentation.mappers.UiUpcomingLabelMapperImpl
import dev.nonoxy.residetrack.feature.upcoming.presentation.mappers.UiUpcomingStateMapper
import dev.nonoxy.residetrack.feature.upcoming.presentation.mappers.UiUpcomingStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureUpcomingPresentationModule = module {

    // `factoryOf<T>(::Impl)` form is safe here — both Impls are zero-arg constructors.
    factoryOf<UiUpcomingStateMapper>(::UiUpcomingStateMapperImpl)
    factoryOf<UiUpcomingLabelMapper>(::UiUpcomingLabelMapperImpl)

    viewModelOf(::UpcomingViewModel)
}
