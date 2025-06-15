package plugins

import extensions.androidAppConfig
import extensions.androidConfig
import extensions.androidMainDependencies
import extensions.composeCompilerConfig
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

class ComposeCompilerPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyPlugins()

            if (pluginManager.hasPlugin(libs.plugins.androidApplication.get().pluginId)) {
                androidAppConfig {
                    buildFeatures {
                        compose = true
                    }
                }
            } else {
                androidConfig {
                    buildFeatures {
                        compose = true
                    }
                }
            }

            composeCompilerConfig {
                reportsDestination.set(layout.buildDirectory.dir("compose_compiler"))
            }

            androidMainDependencies {
                implementation(composeDeps.preview)
            }

            dependencies {
                add(
                    configurationName = "debugImplementation",
                    dependencyNotation = composeDeps.uiTooling
                )
            }
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.composeCompiler.get().pluginId)
            apply(libs.plugins.composeMultiplatform.get().pluginId)
        }
    }
}

val <T : KotlinDependencyHandler> T.composeDeps
    get() = ComposePlugin.Dependencies(this.project)

val <T : Project> T.composeDeps
    get() = ComposePlugin.Dependencies(this.project)

val <T : KotlinDependencyHandler> T.composeBundle
    get() = listOf(
        composeDeps.runtime,
        composeDeps.foundation,
        composeDeps.ui,
        composeDeps.material3,
        composeDeps.components.resources,
        composeDeps.components.uiToolingPreview
    ).toTypedArray()
