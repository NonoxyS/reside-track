import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.core.database"
}

commonMainDependencies {
    implementations(
        libs.room.runtime,
        libs.sqliteBundled,
        projects.shared.common,
    )
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosX64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
