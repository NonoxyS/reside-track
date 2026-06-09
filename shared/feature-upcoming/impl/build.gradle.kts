import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.feature.upcoming.impl"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
    )
}

commonTestDependencies {
    implementations(libs.kotlin.test)
}
