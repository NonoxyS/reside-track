import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.rooms.api"
}
