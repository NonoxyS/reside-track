import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.ui"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.resources,
        libs.kotlin.immutableCollections,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = always
}
