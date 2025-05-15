import extensions.androidMainDependencies
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

iosConfig {
    xcFrameworkName = "ComposeApp"
}

androidMainDependencies {
    implementations(
        libs.kotlin.corutines.android,
        libs.androidx.activity.compose
    )
}

commonMainDependencies {
    implementations(
        libs.kotlin.corutines.core,

        libs.androidx.lifecycle.viewmodel,
        libs.androidx.lifecycle.runtime.compose,
        libs.compose.navigation,

        libs.room.runtime,

        libs.koin.core,
        libs.koin.compose,
        libs.koin.composeViewModel,
    )
}

kotlin {

    sourceSets {

        androidMain.dependencies {
            implementation(compose.preview)
        }

        commonMain.dependencies {
            implementations(
                compose.runtime,
                compose.foundation,
                compose.material3,
                compose.ui,
                compose.components.resources,
                compose.components.uiToolingPreview
            )
        }
    }
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
}

dependencies {
    debugImplementation(compose.uiTooling)

    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
