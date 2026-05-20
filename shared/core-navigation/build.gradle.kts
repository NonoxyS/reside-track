import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.navigation,
        libs.compose.navigation.material,
        libs.androidx.lifecycle.runtimeCompose,
        libs.compose.multiplatform.material3,
        projects.shared.common,
        projects.shared.designSystem,
    )
}
