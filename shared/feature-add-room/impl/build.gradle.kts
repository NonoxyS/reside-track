import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.add_room.impl"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
