import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

android {
    namespace = "dev.nonoxy.feature.rooms.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
    )
}
