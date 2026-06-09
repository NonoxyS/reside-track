import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.upcoming.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
    )
    apis(
        projects.shared.coreRooms,
    )
}
