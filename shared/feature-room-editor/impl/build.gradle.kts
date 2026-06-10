import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.room_editor.impl"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
