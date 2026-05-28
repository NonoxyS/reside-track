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
    namespace = "dev.nonoxy.residetrack.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.material3,
        projects.shared.commonUi,
    )

    apis(
        libs.compose.multiplatform.navigation,
        // Explicit (not present in KMMTemplate): pins the renamed Compose 1.10.1 shared-transition
        // API for the Android target. See NavigationSharedTransitionUtils.kt for the full rationale.
        libs.compose.multiplatform.animation,
        libs.compose.multiplatform.backhandler,
    )
}
