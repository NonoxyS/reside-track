import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.common.ui"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.icons.core,
        libs.moko.resources.compose,
        projects.shared.common,
        projects.shared.commonResources,
    )
}
