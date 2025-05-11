package plugins

import extensions.androidConfig
import extensions.androidKotlinConfig
import extensions.kotlinJvmCompilerOptions
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class AndroidLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            applyPlugins()
            configureKotlin()
            configureAndroid()
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.androidLibrary.get().pluginId)
        }
    }

    private fun Project.configureAndroid() {
        androidConfig {
            compileSdk = libs.versions.android.compileSdk.get().toInt()

            defaultConfig {
                minSdk = libs.versions.android.minSdk.get().toInt()

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                consumerProguardFiles("consumer-rules.pro")
            }

            buildTypes {
                release {
                    isMinifyEnabled = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                }
            }

            compileOptions {
                sourceCompatibility(libs.versions.javaVersion.get().toInt())
                targetCompatibility(libs.versions.javaVersion.get().toInt())
            }

            afterEvaluate {
                println(
                    """
                    ╔════════ ANDROID CONFIG: ${project.name} ════════════╗
                    ║ namespace: $namespace
                    ║ explicitApi: ${kotlinExtension.explicitApi == ExplicitApiMode.Strict}
                    ║ compileSdk: $compileSdk
                    ║ minSdk: ${defaultConfig.minSdk}
                    ║ targetSdk: ${defaultConfig.targetSdk ?: "not set"}
                    ║ buildTypes: ${buildTypes.names.joinToString(", ")}
                    ║ release minify: ${buildTypes.getByName("release").isMinifyEnabled}
                    ║ compose enabled: ${buildFeatures.compose}
                    ╚═════════════════════════════════════════════════════╝
                    """.trimIndent()
                )
            }
        }
    }

    private fun Project.configureKotlin() {
        androidKotlinConfig {
            jvmToolchain(libs.versions.javaVersion.get().toInt())
        }

        kotlinJvmCompilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
        }
    }
}
