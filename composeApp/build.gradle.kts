import extensions.implementations

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeCompiler)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        androidMain.dependencies {
            implementation(libs.kotlin.corutines.android)

            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

        commonMain.dependencies {
            implementations(
                libs.kotlin.corutines.core,

                compose.runtime,
                compose.foundation,
                compose.material3,
                compose.ui,
                compose.components.resources,
                compose.components.uiToolingPreview,

                libs.androidx.lifecycle.viewmodel,
                libs.androidx.lifecycle.runtime.compose,
                libs.compose.navigation,

                libs.room.runtime,

                libs.koin.core,
                libs.koin.compose,
                libs.koin.composeViewModel,
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
