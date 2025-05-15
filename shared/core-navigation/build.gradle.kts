import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
    alias(libs.plugins.conventionPlugin.kmpSerialization)
}

iosConfig {
    xcFrameworkName = "core-navigation"
}

android {
    namespace = "dev.nonoxy.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.navigation,
        libs.androidx.lifecycle.runtime.compose
    )
}
