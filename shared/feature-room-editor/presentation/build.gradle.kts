import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.room_editor.presentation"
}

commonMainDependencies {
    implementations(
        projects.shared.commonResources,
        projects.shared.coreRooms,
        libs.kotlin.immutableCollections,
        libs.kotlin.datetime,
    )
}

commonTestDependencies {
    implementations(libs.kotlin.test)
}
