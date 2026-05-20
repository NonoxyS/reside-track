import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.coreDatabase,
        projects.shared.designSystem,
        projects.shared.featureRooms.api,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
