import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.upcoming.presentation"
}

commonMainDependencies {
    implementations(
        projects.shared.commonResources,
        libs.kotlin.immutableCollections,
        libs.kotlin.datetime,
    )
}

commonTestDependencies {
    implementations(libs.kotlin.test)
}
