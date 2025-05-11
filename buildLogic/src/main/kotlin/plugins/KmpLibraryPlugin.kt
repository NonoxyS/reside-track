package plugins

import extensions.androidAppConfig
import extensions.androidConfig
import extensions.kotlinMultiplatformConfig
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension

class KmpLibraryPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            // val iosConfigExtension = extensions.create<IosConfig>("iosConfig")

            applyPlugins()
            configureKotlin()
            configureAndroid()
            // configureIos(iosConfigExtension)
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
    }

    /*
        private fun Project.configureIos(config: IosConfig) {
            kotlinMultiplatformConfig {

                val xcFramework = XCFramework(config.xcFrameworkName)

                val iosTargets = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

                iosTargets.forEach { iosTarget ->
                    iosTarget.binaries.framework {
                        baseName = config.baseName
                        isStatic = config.isStatic
                        xcFramework.add(this)
                    }
                }

                if (config.enableCocoapods) {
                    cocoapodsConfig {
                        version = config.cocoapods.podVersion ?: libs.versions.ios.pod.version.get()
                        summary = config.cocoapods.summary
                        homepage = config.cocoapods.homepage
                        ios.deploymentTarget = config.cocoapods.deploymentTarget
                            ?: libs.versions.ios.deployment.target.get()

                        framework {
                            baseName = config.cocoapods.frameworkName
                            isStatic = config.isStatic
                        }
                    }
                }
            }
        }
     */
}

/*open class IosConfig(
    var xcFrameworkName: String = "Shared",
    var baseName: String = "Shared",
    var isStatic: Boolean = true,
    var enableCocoapods: Boolean = true,
    var cocoapods: IosConfigCocaopods = IosConfigCocaopods()
) {

    class IosConfigCocaopods(
        var frameworkName: String = "shared",
        var summary: String = "Shared module",
        var homepage: String = "Link to the Shared module homepage",
        var podVersion: String? = null,
        var deploymentTarget: String? = null,
    )
}*/
