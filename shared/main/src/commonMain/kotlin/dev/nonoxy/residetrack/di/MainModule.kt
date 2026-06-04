package dev.nonoxy.residetrack.di

import dev.nonoxy.residetrack.ui.tabcontainer.TabContainerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val mainModule = module {
    viewModelOf(::TabContainerViewModel)
}
