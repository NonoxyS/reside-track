import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.rooms.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreDatabase,
        projects.shared.coreInitializer,
        projects.shared.coreRooms,
        projects.shared.commonResources,
    )
}
