import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.rooms.ui"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}

androidMainDependencies {
    implementations(
        libs.androidx.activity.compose,
    )
}
