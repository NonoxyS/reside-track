import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.main"
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
        }
    }
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.androidx.lifecycle.viewmodel,
        libs.androidx.lifecycle.runtimeCompose,
        libs.compose.multiplatform.navigation,
        libs.compose.navigation.material,
        libs.room.runtime,
        libs.koin.core,
        libs.koin.composeMultiplatform,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.napier,
        projects.shared.common,
        projects.shared.commonUi,
        projects.shared.coreNavigation,
        projects.shared.coreDatabase,
        projects.shared.featureRooms.impl,
        projects.shared.featureAddRoom.impl,
        projects.shared.featureManageStudents.impl,
    )
}
