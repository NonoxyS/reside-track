plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Workaround for version catalog working inside precompiled scripts
    // Issue - https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.gradleplugin.kotlin)
    implementation(libs.gradleplugin.android)
    implementation(libs.gradleplugin.composeCompiler)
}

gradlePlugin {
    plugins {

        register("AndroidLibrary") {
            id = "android-library"
            implementationClass = "plugins.AndroidLibraryPlugin"
        }

        register("KmpLibrary") {
            id = "kmp-library"
            implementationClass = "plugins.KmpLibraryPlugin"
        }

        register("ComposeCompiler") {
            id = "compose-compiler"
            implementationClass = "plugins.ComposeCompilerPlugin"
        }
    }
}
