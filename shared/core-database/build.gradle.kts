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
    namespace = "dev.nonoxy.residetrack.core.database"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
        libs.room.runtime,
        libs.sqliteBundled,
    )
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}
