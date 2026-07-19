package dev.nonoxy.residetrack.di

import dev.nonoxy.residetrack.core.presentation.snackbar.SnackbarBus
import dev.nonoxy.residetrack.ui.tabcontainer.TabContainerViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    singleOf(::SnackbarBus)
    viewModelOf(::TabContainerViewModel)
}
