import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

iosConfig {
    xcFrameworkName = "common"
}

android {
    namespace = "dev.nonoxy.common"
}

commonMainDependencies {
    implementations(
        libs.androidx.lifecycle.viewmodel,
        libs.kotlin.immutableCollections,
    )

    apis(
        libs.koin.core,
        libs.kotlin.corutines.core,
        libs.kotlin.datetime,
        libs.napier,
    )
}
