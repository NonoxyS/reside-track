import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.backup"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        projects.shared.coreDatabase,
        libs.kotlin.serialization.json,
        libs.kotlin.coroutines.core,
    )
}

commonTestDependencies {
    implementations(
        libs.kotlin.test,
        libs.kotlin.coroutines.test,
    )
}
