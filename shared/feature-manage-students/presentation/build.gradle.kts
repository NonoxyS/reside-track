import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.manage_students.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
        libs.kotlin.datetime,
        projects.shared.featureRooms.api,
    )
}
