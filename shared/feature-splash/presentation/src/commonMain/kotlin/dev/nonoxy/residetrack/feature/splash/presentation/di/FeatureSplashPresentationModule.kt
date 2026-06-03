package dev.nonoxy.residetrack.feature.splash.presentation.di

import dev.nonoxy.residetrack.feature.splash.presentation.SplashScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureSplashPresentationModule = module {

    viewModelOf(::SplashScreenViewModel)
}
