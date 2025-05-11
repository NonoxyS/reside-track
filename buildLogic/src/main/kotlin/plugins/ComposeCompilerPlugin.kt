package plugins

import extensions.androidAppConfig
import extensions.androidConfig
import extensions.composeCompilerConfig
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project

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
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.composeCompiler.get().pluginId)
            apply(libs.plugins.composeMultiplatform.get().pluginId)
        }
    }
}
