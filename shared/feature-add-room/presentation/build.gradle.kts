import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.add_room.presentation"
}

commonMainDependencies {
    implementations(
        projects.shared.commonResources,
        libs.kotlin.immutableCollections,
    )
}

commonTestDependencies {
    implementations(libs.kotlin.test)
}
