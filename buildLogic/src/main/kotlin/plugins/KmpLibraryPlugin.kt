package plugins

import extensions.androidAppConfig
import extensions.androidConfig
import extensions.commonMainDependencies
import extensions.implementations
import extensions.isApiModule
import extensions.kotlinMultiplatformConfig
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

class KmpLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val iosConfigExtension = extensions.create<IosConfig>("iosConfig")

            applyPlugins()
            configureKotlin()
            configureFeatureModuleDependencies()
            configureAndroid()
            configureIos(iosConfigExtension)
        }
    }

    private fun Project.applyPlugins() {
        with(pluginManager) {
            apply(libs.plugins.kotlinMultiplatform.get().pluginId)

            if (!hasPlugin(libs.plugins.androidApplication.get().pluginId)) {
                apply(libs.plugins.androidLibrary.get().pluginId)
            }
        }
    }

    private fun Project.configureKotlin() {
        kotlinMultiplatformConfig {
            jvmToolchain(libs.versions.javaVersion.get().toInt())

            androidTarget {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
                }
            }
        }
    }

    private fun Project.configureFeatureModuleDependencies() {

        val implModuleDependencies = when (project.isApiModule) {
            true -> null
            else ->
                project
                    .parent
                    ?.childProjects
                    ?.values
                    ?.filter { project -> project.isApiModule }
        }

        val commonDependencies = listOf(
            project(":shared:common"),
        ).takeIf { project.name != "common" }

        commonMainDependencies {
            implementations(
                *commonDependencies?.toTypedArray().orEmpty(),
                *implModuleDependencies?.toTypedArray().orEmpty(),
            )
        }
    }

    private fun Project.configureAndroid() {
        if (pluginManager.hasPlugin(libs.plugins.androidApplication.get().pluginId)) {
            androidAppConfig {
                compileSdk = libs.versions.android.compileSdk.get().toInt()

                defaultConfig {
                    minSdk = libs.versions.android.minSdk.get().toInt()
                    targetSdk = libs.versions.android.targetSdk.get().toInt()

                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
                    ╔════════ KMP ANDROID CONFIG: ${project.name} ════════╗
                    ║ namespace: $namespace
                    ║ sourceCompatibility: ${compileOptions.sourceCompatibility}
                    ║ targetCompatibility: ${compileOptions.targetCompatibility}
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
        } else {
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
                    ╔════════ KMP ANDROID CONFIG: ${project.name} ════════╗
                    ║ namespace: $namespace
                    ║ compileSdk: $compileSdk
                    ║ minSdk: ${defaultConfig.minSdk}
                    ║ targetSdk: ${defaultConfig.targetSdk ?: "not set"}
                    ║ buildTypes: ${buildTypes.names.joinToString(", ")}
                    ║ release minify: ${buildTypes.getByName("release").isMinifyEnabled}
                    ║ compose enabled: ${buildFeatures.compose == true}
                    ╚═════════════════════════════════════════════════════╝
                        """.trimIndent()
                    )
                }
            }
        }
    }

    private fun Project.configureIos(config: IosConfig) {
        kotlinMultiplatformConfig {

            val iosTargets = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

            afterEvaluate {
                val xcFramework = XCFramework(config.xcFrameworkName)

                iosTargets.forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = config.baseName
                        isStatic = config.isStatic
                        xcFramework.add(this)
                    }
                }
            }
        }
    }
}

open class IosConfig(
    var xcFrameworkName: String = "Shared",
    var baseName: String = xcFrameworkName,
    var isStatic: Boolean = false,
)
