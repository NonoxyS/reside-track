import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.domain"
}
