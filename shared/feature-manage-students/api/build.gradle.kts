import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

android {
    namespace = "dev.nonoxy.feature.manage_students.api"
}

commonMainDependencies {
    implementations(
    )
}