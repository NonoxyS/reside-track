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

includeBuild("build-logic")

include(
    ":android:app",
    ":android:benchmark",
    ":shared:main",

    // Core / Common
    ":shared:common",
    ":shared:common-resources",
    ":shared:core-domain",
    ":shared:core-mvikotlin",
    ":shared:core-presentation",
    ":shared:core-navigation",
    ":shared:core-database",
    ":shared:core-initializer",
    ":shared:core-rooms",
    ":shared:core-backup",
    ":shared:core-notifications",
    ":shared:common-ui",

    // Features
    ":shared:feature-splash:presentation",
    ":shared:feature-splash:ui",

    ":shared:feature-rooms:api",
    ":shared:feature-rooms:impl",
    ":shared:feature-rooms:presentation",
    ":shared:feature-rooms:ui",

    ":shared:feature-add-room:api",
    ":shared:feature-add-room:impl",
    ":shared:feature-add-room:presentation",
    ":shared:feature-add-room:ui",

    ":shared:feature-room-editor:api",
    ":shared:feature-room-editor:impl",
    ":shared:feature-room-editor:presentation",
    ":shared:feature-room-editor:ui",

    ":shared:feature-upcoming:api",
    ":shared:feature-upcoming:impl",
    ":shared:feature-upcoming:presentation",
    ":shared:feature-upcoming:ui",
)
