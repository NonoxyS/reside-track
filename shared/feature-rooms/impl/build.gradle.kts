import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
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
        projects.shared.common,
        projects.shared.coreDatabase,
        projects.shared.coreInitializer,
        projects.shared.coreRooms,
        projects.shared.coreBackup,
        projects.shared.commonResources,
    )
}

commonTestDependencies {
    implementations(
        libs.kotlin.test,
        libs.kotlin.coroutines.test,
    )
}
