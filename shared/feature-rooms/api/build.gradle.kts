import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.rooms.api"
}

commonMainDependencies {
    apis(
        projects.shared.coreRooms,
        projects.shared.coreBackup,
    )
}
