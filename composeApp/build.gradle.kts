import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle
import plugins.composeDeps

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
}

iosConfig {
    xcFrameworkName = "ComposeApp"
}

androidMainDependencies {
    implementations(
        libs.kotlin.corutines.android,
        libs.androidx.activity.compose,
        libs.koin.android,

        composeDeps.preview
    )
}

commonMainDependencies {
    implementations(
        libs.kotlin.corutines.core,

        *composeBundle,

        libs.androidx.lifecycle.viewmodel,
        libs.androidx.lifecycle.runtime.compose,
        libs.compose.navigation,
        libs.compose.navigation.material,

        libs.room.runtime,

        libs.koin.core,
        libs.koin.compose,
        libs.koin.composeViewModel,

        libs.napier,

        projects.shared.designSystem,
        projects.shared.coreNavigation,
        projects.shared.coreDatabase,

        projects.shared.featureRooms.impl,
        projects.shared.featureAddRoom.impl,
    )
}

android {
    namespace = "dev.nonoxy.residetrack"

    defaultConfig {
        applicationId = "dev.nonoxy.residetrack"

        versionCode = 1
        versionName = "0.0.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
