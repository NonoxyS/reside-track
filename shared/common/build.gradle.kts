import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.common"
}

commonMainDependencies {
    implementations(
        libs.androidx.lifecycle.viewmodel,
        libs.kotlin.immutableCollections,
    )
    apis(
        libs.koin.core,
        libs.kotlin.coroutines.core,
        libs.kotlin.datetime,
        libs.napier,
    )
}
