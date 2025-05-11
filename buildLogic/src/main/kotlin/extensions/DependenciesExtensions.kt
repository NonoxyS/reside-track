package extensions

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

internal fun Project.dependencies(dependencies: DependencyHandlerScope.() -> Unit) {
    dependencies(dependencies)
}

internal fun DependencyHandler.implementation(dependency: Any) {
    add("implementation", dependency)
}

fun KotlinDependencyHandler.implementations(
    vararg dependencies: Any,
) {
    dependencies.forEach(::implementation)
}

fun KotlinDependencyHandler.apis(
    vararg dependencies: Any,
) {
    dependencies.forEach(::api)
}
