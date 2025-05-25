import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
}

android {
    namespace = "dev.nonoxy.core.design"
}

commonMainDependencies {
    implementations(
        *composeBundle
    )
}
