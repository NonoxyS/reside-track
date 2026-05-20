import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        projects.shared.common,
        projects.shared.featureRooms.api,
    )
}
