plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)

    // Convention plugins
    alias(libs.plugins.conventionPlugin.composeCompiler) apply false
    alias(libs.plugins.conventionPlugin.androidLibrary) apply false
    alias(libs.plugins.conventionPlugin.kmpLibrary) apply false
}

dependencies {
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.libraries)
}

detekt {
    disableDefaultRuleSets = true
    buildUponDefaultConfig = true
    autoCorrect = true
    description = "Detekt with formatting."
    config.setFrom("$rootDir/linters/detekt/config.yml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    setupDetektFolders()

    reports {
        html {
            required.set(true)
            outputLocation.set(file("$rootDir/build/reports/detekt/detekt.html"))
        }
    }
}

fun SourceTask.setupDetektFolders() {
    setSource(files(projectDir))
    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    exclude("**/.gradle/**")
}

val changeGitHooksDir by tasks.registering(Exec::class) {
    group = "git"
    description = "Changing githooks dir to .githooks"

    fun ExecSpec.executeStringCommand(command: String) {
        val splitted = command.split(" ")
        commandLine(*splitted.toTypedArray())
    }

    doFirst {
        logger.error("hooksPath before")
        executeStringCommand("git config core.hooksPath")
    }

    exec {
        commandLine("git", "config", "core.hooksPath", ".githooks")
    }

    doLast {
        logger.error("hooksPath after")
        executeStringCommand("git config core.hooksPath")
    }

    onlyIf {
        System.getenv("IS_CI") == null
    }
}

tasks.getByPath(":composeApp:preBuild").dependsOn(changeGitHooksDir)
