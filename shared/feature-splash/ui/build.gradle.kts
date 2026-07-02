import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.splash.ui"
}

commonMainDependencies {
    implementations(
        libs.moko.permissions.core,
        libs.moko.permissions.notifications,
        libs.moko.permissions.compose,
    )
}
