import extensions.androidLibraryConfig
import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.commonTestDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.notifications"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        projects.shared.commonResources,
        projects.shared.coreDatabase,
        projects.shared.coreInitializer,
        libs.kotlin.coroutines.core,
    )
}

androidMainDependencies {
    implementations(
        libs.androidx.core.ktx,
        libs.androidx.work.runtime,
    )
}

commonTestDependencies {
    implementations(
        libs.kotlin.test,
        libs.kotlin.coroutines.test,
    )
}
