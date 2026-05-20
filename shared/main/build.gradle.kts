import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
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

    addCommonModules()
    addCoreModules()
    addFeatureModules()
}

commonMainDependencies {
    apis(
        projects.shared.common,
        projects.shared.commonResources,
    )

    implementations(
        *composeBundle,
        libs.androidx.lifecycle.viewmodel,
        libs.androidx.lifecycle.runtimeCompose,
        libs.compose.multiplatform.navigation,
        libs.compose.navigation.material,
        libs.koin.composeMultiplatform,
        libs.koin.composeMultiplatform.viewmodelNavigation,
    )
}

private fun KotlinMultiplatformExtension.addFeatureModules() {

    val featureProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "feature-") }
        .values
        .flatMap { project -> project.childProjects.values }

    commonMainDependencies {
        implementations(*featureProjects.toTypedArray())
    }
}

private fun KotlinMultiplatformExtension.addCoreModules() {

    val coreProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "core-") }
        .values
        .flatMap { project -> project.childProjects.values.takeIf { it.isNotEmpty() } ?: listOf(project) }

    commonMainDependencies {
        implementations(*coreProjects.toTypedArray())
    }
}

private fun KotlinMultiplatformExtension.addCommonModules() {

    val commonProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "common") }
        .values
        .flatMap { project -> project.childProjects.values.takeIf { it.isNotEmpty() } ?: listOf(project) }

    commonMainDependencies {
        implementations(*commonProjects.toTypedArray())
    }
}
