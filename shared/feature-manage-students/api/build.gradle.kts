import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.manage_students.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        libs.kotlin.immutableCollections,
    )
    apis(
        projects.shared.coreRooms,
    )
}
