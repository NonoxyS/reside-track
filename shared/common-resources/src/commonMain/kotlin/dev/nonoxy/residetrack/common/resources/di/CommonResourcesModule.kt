package dev.nonoxy.residetrack.common.resources.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformResourcesModule: Module

val commonResourcesModule = module {
    includes(platformResourcesModule)
}
