package dev.nonoxy.residetrack.di

import dev.nonoxy.common.di.AppEnvironmentQualifiers
import dev.nonoxy.residetrack.BuildConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    factory(named(AppEnvironmentQualifiers.VERSION)) {
        BuildConfig.VERSION_NAME
    }

    factory(named(AppEnvironmentQualifiers.FLAVOR)) {
        BuildConfig.FLAVOR
    }

    factory(named(AppEnvironmentQualifiers.APPLICATION_ID)) {
        BuildConfig.APPLICATION_ID
    }

    factory(named(AppEnvironmentQualifiers.VERSION_CODE)) {
        BuildConfig.VERSION_CODE
    }
}
