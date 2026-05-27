import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.compose.icons.core,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.commonUi,
        projects.shared.featureRooms.api,
        projects.shared.featureManageStudents.api,
        projects.shared.featureManageStudents.presentation, // TEMP for Tasks 13–16; removed in Task 17
        projects.shared.coreMvikotlin,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
