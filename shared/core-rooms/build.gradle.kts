import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.rooms"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        projects.shared.coreDatabase,
        libs.kotlin.datetime,
        libs.kotlin.coroutines.core,
    )
}
