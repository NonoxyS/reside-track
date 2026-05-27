import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.ui"
}

compose.resources {
    publicResClass = false
    generateResClass = always
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.resources,
        libs.kotlin.immutableCollections,
        libs.compose.icons.core,
    )
}
