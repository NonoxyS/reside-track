import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

android {
    namespace = "dev.nonoxy.feature.add_room.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        projects.shared.featureRooms.api
    )
}
