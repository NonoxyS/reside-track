rootProject.name = "ResideTrack"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

includeBuild("buildLogic")

include(
    ":composeApp",

    // Core / Common
    ":shared:common",
    ":shared:core-navigation",
    ":shared:core-database",
    ":shared:design-system",

    // Features
    ":shared:feature-rooms:api",
    ":shared:feature-rooms:impl",

    ":shared:template-module"
)
