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
        projects.shared.commonUi,
        projects.shared.featureRooms.api,
        projects.shared.featureRooms.presentation, // временно, удалится в Task 7 вместе со старым кодом
        // Temporary: MVIKotlin deps for domain layer (will be removed in Task 8
        // when impl migrates to kmpFeatureSetup which provides them via core-mvikotlin)
        projects.shared.coreMvikotlin,
        libs.mvikotlin.core,
        libs.mvikotlin.coroutines,
        libs.mvikotlin.main,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
