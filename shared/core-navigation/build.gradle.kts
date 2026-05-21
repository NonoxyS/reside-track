import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.material3,
        projects.shared.commonUi,
    )

    apis(
        libs.compose.multiplatform.navigation,
        libs.compose.multiplatform.backhandler,
    )
}
