import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
