import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
}

android {
    namespace = "dev.nonoxy.feature.rooms.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.koin.composeViewModel,
        libs.kotlin.immutableCollections,

        projects.shared.coreNavigation,
        projects.shared.coreDatabase,
    )
}
