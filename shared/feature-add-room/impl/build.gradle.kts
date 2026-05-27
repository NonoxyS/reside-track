import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.commonUi,
        projects.shared.featureRooms.api,
        projects.shared.featureAddRoom.api,
        projects.shared.coreMvikotlin,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
