import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.api"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
    )
}
