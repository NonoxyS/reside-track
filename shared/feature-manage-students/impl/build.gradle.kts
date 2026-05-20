import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
}

android {
    namespace = "dev.nonoxy.feature.manage_students.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.icons.core,
        libs.compose.icons.core,
        libs.koin.composeViewModel,
        libs.kotlin.immutableCollections,

        projects.shared.coreNavigation,
        projects.shared.designSystem,

        projects.shared.featureRooms.api,
        projects.shared.featureManageStudents.api
    )
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
