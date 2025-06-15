package extensions

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

enum class FeatureModuleType {
    API,
    IMPL;

    val actualName: String = name.lowercase()
}

val Project.libs: LibrariesForLibs
    get() = the<LibrariesForLibs>()

val Project.isApiModule
    get() = name.lowercase() == FeatureModuleType.API.actualName

val Project.isImplModule
    get() = name.lowercase() == FeatureModuleType.IMPL.actualName
