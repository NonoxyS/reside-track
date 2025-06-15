import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
}

android {
    namespace = "dev.nonoxy.feature.add_room.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.koin.composeViewModel,
        libs.kotlin.immutableCollections,

        projects.shared.coreNavigation,
        projects.shared.designSystem,

        projects.shared.featureRooms.api
    )
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
