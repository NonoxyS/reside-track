import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.manage_students.impl"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
