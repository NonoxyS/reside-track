# Фаза 2: Core-модули + доводка app-слоя — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Создать новые core-модули KMMTemplate (`core-mvikotlin`, `core-presentation`, `core-domain`, `common-resources`), переименовать `design-system` → `common-ui`, и довести `:android:app` / `:shared:main` / паттерн Koin до парности с KMMTemplate.

**Architecture:** Аддитивная миграция. Новые core-модули создаются изолированно (никто их не использует до Фаз 4–5) — копируются из KMMTemplate с заменой пакета. `:android:app` доводится до полного образца KMMTemplate (flavors, версионирование, signing, переименование артефактов, `Application`). Koin переводится с `KoinMultiplatformApplication` внутри `App()` на `initKoin` + класс `Application` (Android) и `initKoin` в iOS-входе. Сборка проверяется после каждой задачи.

**Tech Stack:** Kotlin 2.3.10, AGP 9.0.0, Gradle 9.4.1, Compose Multiplatform 1.10.1, Koin 4.1.1, MVIKotlin 4.3.0, moko-resources 0.26.0, moko-mvvm 0.16.1, Room 2.8.4, convention plugins из `build-logic`.

**Зафиксированные решения (ответы пользователя):**
- Пакеты модулей Фазы 2 — сразу финальные `dev.nonoxy.residetrack.*` (Фаза 8 переименует только остаток).
- Koin на iOS — `initKoin` вызывается в `shared/main/iosMain/MainViewController.kt` (KMMTemplate этого не делает — вынужденное отклонение для корректности DI на iOS).

**Решения исполнителя (зафиксированы здесь, при возражении — поправить план):**
- `:shared:common` в Фазе 2 трогается **только аддитивно** (`OneTimeEvent`, `OneTimeGetValue`, `AppEnvironmentQualifiers`). Старые `BaseViewModel<S,E,A>`, `Mapper`, `coRunCatching`, `Dispatchers` **не удаляются и не реструктурируются** — их используют фичи на старом MVI. Полное приведение `common` к раскладке KMMTemplate происходит при миграции фич (Фазы 4–5) и переименовании пакетов (Фаза 8). Причина: иначе ломается сборка фич либо вводится дубль-код без потребителя (YAGNI).
- `:shared:common-ui` (бывший `design-system`) в Фазе 2 **сохраняет Compose Resources** (строки + шрифты Poppins). Перевод его ресурсов на moko — Фаза 6 (по спеке раздел 10). Поэтому build-файл common-ui не повторяет build-файл common-ui KMMTemplate целиком.
- `:shared:common-resources` создаётся как **каркас** (инфраструктура `StringConverter` + DI + пустой `strings.xml`). Реальные строки переезжают в него в Фазе 6.
- iOS-фреймворк `shared/main` остаётся `isStatic = false` (как сейчас). KMMTemplate использует `isStatic = true`; смена затрагивает встраивание фреймворка в Xcode, а спека в пункте про `:shared:main` этого не требует — оставлено как известное отличие.
- `enableEdgeToEdge()` в `MainActivity` **не добавляется** (KMMTemplate его имеет) — это видимое изменение поведения, а спека требует поведение не менять.
- Задача `changeGitHooksDir` из `shared/main` KMMTemplate **не копируется** — у reside-track свой механизм git-хуков (detekt pre-commit, Фаза 1).

**Контрольная точка после Фазы 2:** `./gradlew :android:app:assembleProdDebug` зелёный, `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64` зелёный, `./gradlew detekt` зелёный.

---

## Группа 1 — Инфраструктура и новые core-модули

### Task 1: Version catalog — Gradle-плагин moko-resources

**Files:**
- Modify: `gradle/libs.versions.toml` (секция `[plugins]`)

В каталоге уже есть `moko-resources = "0.26.0"` в `[versions]` и библиотеки `moko-resources-core`/`moko-resources-compose`, но **нет алиаса для Gradle-плагина** генерации ресурсов. `:shared:common-resources` применяет его через `alias(libs.plugins.moko.resources)`.

- [ ] **Step 1: Добавить алиас плагина**

В `gradle/libs.versions.toml`, в секции `[plugins]`, после блока `# Room / KSP` (перед `# Detekt`) добавить:

```toml
# Moko
moko-resources = { id = "dev.icerock.mobile.multiplatform-resources", version.ref = "moko-resources" }
```

- [ ] **Step 2: Проверить, что каталог парсится**

Run: `./gradlew help`
Expected: `BUILD SUCCESSFUL`. Если падает с `Invalid TOML catalog definition` — проверить синтаксис добавленной строки.

---

### Task 2: build-logic — `AppVersion` + `GitCommitCountValueSource`

**Files:**
- Create: `build-logic/src/main/kotlin/utils/AppVersion.kt`
- Create: `build-logic/src/main/kotlin/valueSource/GitCommitCountValueSource.kt`

Эти классы нужны `:android:app` для версионирования (`versionCode` = число git-коммитов, `versionName` = `major.minor.commitCount`). В `build-logic` reside-track их нет — копируются из KMMTemplate дословно. Зависят от уже существующего `extensions.libs` (`build-logic/src/main/kotlin/extensions/ProjectExtensions.kt`) и каталожных ключей `appVersion-major`/`appVersion-minor` (уже есть в `[versions]`).

- [ ] **Step 1: Создать `GitCommitCountValueSource.kt`**

```kotlin
package valueSource

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import javax.inject.Inject

abstract class GitCommitCountValueSource : ValueSource<Int, ValueSourceParameters.None> {

    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): Int {
        val output = ByteArrayOutputStream()

        return try {
            execOperations.exec {
                commandLine("git", "rev-list", "--count", "HEAD")
                // The stream is closed after the process completes. <- From documentation for standardOutput
                standardOutput = output
            }.rethrowFailure()

            val count = output.toString().trim().toInt()
            count
        } catch (throwable: Throwable) {
            throw IllegalStateException(
                "Error occur during getting Git commit count",
                throwable
            )
        }
    }
}
```

- [ ] **Step 2: Создать `AppVersion.kt`**

```kotlin
@file:Suppress("Filename")

package utils

import extensions.libs
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import valueSource.GitCommitCountValueSource

object AppVersion {

    /** Returns version name in format "$majorVersion.$minorVersion.{commitCount}" */
    fun getVersionName(project: Project): Provider<String> {
        val majorVersion = project.libs.versions.appVersion.major.get()
        val minorVersion = project.libs.versions.appVersion.minor.get()

        return getVersionCode(project).map { count -> "$majorVersion.$minorVersion.$count" }
    }

    /** Returns version code equal to the number of Git commits */
    fun getVersionCode(project: Project): Provider<Int> {
        return project.providers.of(GitCommitCountValueSource::class.java) {}
    }
}
```

- [ ] **Step 3: Проверить компиляцию build-logic**

Run: `./gradlew :build-logic:compileKotlin` (или `./gradlew help` — composite build компилируется при конфигурации)
Expected: `BUILD SUCCESSFUL`. Если `extensions.libs` не резолвится — проверить, что `ProjectExtensions.kt` экспортирует `val Project.libs`.

---

### Task 3: Создать `:shared:core-mvikotlin`

**Files:**
- Create: `shared/core-mvikotlin/build.gradle.kts`
- Create: `shared/core-mvikotlin/src/commonMain/kotlin/dev/nonoxy/residetrack/core/mvikotlin/BaseExecutor.kt`
- Create: `shared/core-mvikotlin/src/commonMain/kotlin/dev/nonoxy/residetrack/core/mvikotlin/di/CoreMVIKotlinModule.kt`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: `build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.mvikotlin"
}

commonMainDependencies {
    apis(
        libs.mvikotlin.core,
        libs.mvikotlin.coroutines,
        libs.mvikotlin.main,
        libs.mvikotlin.logging
    )
}
```

- [ ] **Step 2: `BaseExecutor.kt`**

```kotlin
package dev.nonoxy.residetrack.core.mvikotlin

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseExecutor<in Intent : Any, Action : Any, State : Any, Message : Any, Label : Any>(
    mainContext: CoroutineContext = Dispatchers.Main,
) : CoroutineExecutor<Intent, Action, State, Message, Label>(mainContext = mainContext) {

    final override fun executeAction(action: Action) {
        scope.launch {
            suspendExecuteAction(action)
        }
    }

    final override fun executeIntent(intent: Intent) {
        scope.launch {
            suspendExecuteIntent(intent)
        }
    }

    open suspend fun suspendExecuteIntent(intent: Intent) {
        // no-op
    }

    open suspend fun suspendExecuteAction(action: Action) {
        // no-op
    }
}
```

- [ ] **Step 3: `di/CoreMVIKotlinModule.kt`**

```kotlin
package dev.nonoxy.residetrack.core.mvikotlin.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.logger.Logger
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import io.github.aakira.napier.Napier
import org.koin.dsl.module

val coreMVIKotlinModule = module {

    factory<StoreFactory> {
        val logger = object : Logger {
            override fun log(text: String) {
                Napier.v(text)
            }
        }

        LoggingStoreFactory(delegate = DefaultStoreFactory(), logger = logger)
    }
}
```

- [ ] **Step 4: Подключить модуль в `settings.gradle.kts`**

В `settings.gradle.kts`, в блоке `include(...)`, в секцию `// Core / Common` добавить строку `":shared:core-mvikotlin",` (полный итоговый блок include приведён в Task 7, Step 5 — здесь добавляется по одной строке за задачу).

- [ ] **Step 5: Проверить сборку модуля**

Run: `./gradlew :shared:core-mvikotlin:build`
Expected: `BUILD SUCCESSFUL`.

---

### Task 4: Создать `:shared:core-domain`

**Files:**
- Create: `shared/core-domain/build.gradle.kts`
- Create: `shared/core-domain/src/commonMain/kotlin/dev/nonoxy/residetrack/core/domain/di/CoreDomainModule.kt`
- Create: `shared/core-domain/src/commonMain/kotlin/dev/nonoxy/residetrack/core/domain/exception/NetworkUnavailableException.kt`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: `build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.domain"
}
```

- [ ] **Step 2: `di/CoreDomainModule.kt`**

```kotlin
package dev.nonoxy.residetrack.core.domain.di

import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreDomainModule = module {

    factory {
        Json { ignoreUnknownKeys = true }
    }
}
```

- [ ] **Step 3: `exception/NetworkUnavailableException.kt`**

```kotlin
package dev.nonoxy.residetrack.core.domain.exception

class NetworkUnavailableException(message: String) : Exception(message)
```

- [ ] **Step 4: Подключить в `settings.gradle.kts`**

Добавить `":shared:core-domain",` в секцию `// Core / Common` блока `include(...)`.

- [ ] **Step 5: Проверить сборку**

Run: `./gradlew :shared:core-domain:build`
Expected: `BUILD SUCCESSFUL`.

---

### Task 5: `:shared:common` — добавить `OneTimeEvent` и `OneTimeGetValue`

**Files:**
- Create: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/utils/OneTimeEvent.kt`
- Create: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/utils/OneTimeGetValue.kt`

Аддитивно. `core-presentation` (Task 6) импортирует `OneTimeEvent` из `common`. Пакет — текущий `dev.nonoxy.common` (модуль `common` в Фазе 2 не переименовывается; пакет получит `residetrack` в Фазе 8). Существующие файлы `common` не трогаются.

- [ ] **Step 1: `utils/OneTimeEvent.kt`**

```kotlin
package dev.nonoxy.common.utils

import kotlinx.coroutines.channels.Channel

@Suppress("FunctionName")
fun <T> OneTimeEvent(): Channel<T> = Channel(Channel.BUFFERED)
```

- [ ] **Step 2: `utils/OneTimeGetValue.kt`**

```kotlin
package dev.nonoxy.common.utils

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
data class OneTimeGetValue<T>(
    val defaultValue: T,
) {
    private var _value: AtomicReference<T?> = AtomicReference(value = null)

    fun readAndClear(): T = _value.exchange(newValue = null) ?: defaultValue

    fun setValue(value: T) = _value.store(newValue = value)
}
```

- [ ] **Step 3: Проверить сборку `common`**

Run: `./gradlew :shared:common:build`
Expected: `BUILD SUCCESSFUL`. Если `kotlin.concurrent.atomics` не резолвится — Kotlin 2.3.10 его поддерживает; проверить, что не указан более старый языковой уровень.

---

### Task 6: Создать `:shared:core-presentation`

**Files:**
- Create: `shared/core-presentation/build.gradle.kts`
- Create: `shared/core-presentation/src/commonMain/kotlin/dev/nonoxy/residetrack/core/presentation/viewmodel/BaseIosViewModel.kt`
- Create: `shared/core-presentation/src/commonMain/kotlin/dev/nonoxy/residetrack/core/presentation/viewmodel/BaseViewModel.kt`
- Modify: `settings.gradle.kts`

`BaseViewModel` импортирует `OneTimeEvent` из `common` (Task 5) — пакет `dev.nonoxy.common.utils` (НЕ `residetrack` — `common` пока не переименован).

- [ ] **Step 1: `build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.core.presentation"
}

commonMainDependencies {
    implementations(
        projects.shared.common,

        libs.mvikotlin.core,
        libs.mvikotlin.coroutines,
    )

    apis(
        libs.moko.mvvm.flow,
        libs.androidx.lifecycle.viewmodel,
    )
}
```

- [ ] **Step 2: `viewmodel/BaseIosViewModel.kt`**

```kotlin
package dev.nonoxy.residetrack.core.presentation.viewmodel

import dev.icerock.moko.mvvm.flow.CFlow
import dev.icerock.moko.mvvm.flow.CStateFlow

interface BaseIosViewModel<State, Label> {

    val state: CStateFlow<State>
    val label: CFlow<Label>

    fun onCleared()

    fun start()

    fun stop()
}
```

- [ ] **Step 3: `viewmodel/BaseViewModel.kt`**

```kotlin
package dev.nonoxy.residetrack.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.binder.Binder
import com.arkivanov.mvikotlin.extensions.coroutines.BindingsBuilder
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import dev.icerock.moko.mvvm.flow.CFlow
import dev.icerock.moko.mvvm.flow.CStateFlow
import dev.icerock.moko.mvvm.flow.cFlow
import dev.icerock.moko.mvvm.flow.cMutableStateFlow
import dev.icerock.moko.mvvm.flow.cStateFlow
import dev.nonoxy.common.utils.OneTimeEvent
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel<State, Label>(
    initialState: State,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
) : ViewModel(), BaseIosViewModel<State, Label> {

    private val mutableState = MutableStateFlow(initialState).cMutableStateFlow()
    private val mutableLabel = OneTimeEvent<Label>()

    override val state: CStateFlow<State>
        get() = mutableState.cStateFlow()

    override val label: CFlow<Label>
        get() = mutableLabel.receiveAsFlow().cFlow()

    private var binder: Binder? = null

    private var binderIsStarted = false

    init {
        Napier.v("VM $this init")
    }

    protected open fun acceptState(state: State) {
        mutableState.value = state
    }

    protected open fun acceptLabel(label: Label) {
        viewModelScope.launch {
            mutableLabel.send(label)
        }
    }

    protected fun bindAndStart(
        mainContext: CoroutineContext = mainDispatcher,
        builder: BindingsBuilder.() -> Unit
    ) = bind(mainContext, builder).run {
        binder = this
        this@BaseViewModel.start()
    }

    override fun onCleared() {
        super.onCleared()
        stop()
        Napier.v("VM $this onCleared")
    }

    /** Only for iOS */
    override fun start() {
        if (binderIsStarted) return
        binderIsStarted = true
        Napier.v("VM $this start")
        binder?.start()
    }

    /** Only for iOS */
    override fun stop() {
        if (!binderIsStarted) return
        binderIsStarted = false
        Napier.v("VM $this stop")
        binder?.stop()
    }
}
```

- [ ] **Step 4: Подключить в `settings.gradle.kts`**

Добавить `":shared:core-presentation",` в секцию `// Core / Common`.

- [ ] **Step 5: Проверить сборку**

Run: `./gradlew :shared:core-presentation:build`
Expected: `BUILD SUCCESSFUL`. Если `cMutableStateFlow`/`cFlow` не резолвятся — проверить, что `libs.moko.mvvm.flow` подключён как `api`.

---

### Task 7: Создать `:shared:common-resources`

**Files:**
- Create: `shared/common-resources/build.gradle.kts`
- Create: `shared/common-resources/src/commonMain/moko-resources/base/strings.xml`
- Create: `shared/common-resources/src/commonMain/kotlin/dev/nonoxy/residetrack/common/resources/StringConverter.kt`
- Create: `shared/common-resources/src/commonMain/kotlin/dev/nonoxy/residetrack/common/resources/di/CommonResourcesModule.kt`
- Create: `shared/common-resources/src/androidMain/kotlin/dev/nonoxy/residetrack/common/resources/AndroidStringConverter.kt`
- Create: `shared/common-resources/src/androidMain/kotlin/dev/nonoxy/residetrack/common/resources/di/CommonResourcesModule.android.kt`
- Create: `shared/common-resources/src/iosMain/kotlin/dev/nonoxy/residetrack/common/resources/IosStringConverter.kt`
- Create: `shared/common-resources/src/iosMain/kotlin/dev/nonoxy/residetrack/common/resources/di/CommonResourcesModule.ios.kt`
- Modify: `settings.gradle.kts`, `gradle.properties`

Каркас. Реальные строки переедут сюда в Фазе 6. moko генерирует класс `MR` в пакете `dev.nonoxy.residetrack.res`.

- [ ] **Step 1: `build.gradle.kts`**

```kotlin
import dev.icerock.gradle.MRVisibility
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.moko.resources)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.common.resources"
}

multiplatformResources {
    resourcesPackage.set("dev.nonoxy.residetrack.res")
    iosBaseLocalizationRegion.set("en")
    resourcesClassName.set("MR")
    resourcesVisibility.set(MRVisibility.Public)
}

commonMainDependencies {
    apis(
        libs.moko.resources.core,
    )
}
```

- [ ] **Step 2: `moko-resources/base/strings.xml`**

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<resources>
</resources>
```

- [ ] **Step 3: `StringConverter.kt`**

```kotlin
package dev.nonoxy.residetrack.common.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

interface StringConverter {

    fun convert(stringResource: StringResource): String
    fun convert(resourceFormattedString: ResourceFormattedStringDesc): String
}
```

- [ ] **Step 4: `di/CommonResourcesModule.kt`**

```kotlin
package dev.nonoxy.residetrack.common.resources.di

import org.koin.core.module.Module
import org.koin.dsl.module

internal expect val platformResourcesModule: Module

val commonResourcesModule = module {
    includes(platformResourcesModule)
}
```

- [ ] **Step 5: `androidMain/.../AndroidStringConverter.kt`**

```kotlin
package dev.nonoxy.residetrack.common.resources

import android.content.Context
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

internal class AndroidStringConverter(
    private val context: Context
) : StringConverter {

    override fun convert(stringResource: StringResource) =
        context.getString(stringResource.resourceId)

    override fun convert(resourceFormattedString: ResourceFormattedStringDesc): String =
        resourceFormattedString.toString(context)
}
```

- [ ] **Step 6: `androidMain/.../di/CommonResourcesModule.android.kt`**

```kotlin
package dev.nonoxy.residetrack.common.resources.di

import dev.nonoxy.residetrack.common.resources.AndroidStringConverter
import dev.nonoxy.residetrack.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.new
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factory<StringConverter> { new(::AndroidStringConverter) }
}
```

- [ ] **Step 7: `iosMain/.../IosStringConverter.kt`**

```kotlin
package dev.nonoxy.residetrack.common.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc
import dev.icerock.moko.resources.desc.desc

internal class IosStringConverter : StringConverter {
    override fun convert(stringResource: StringResource) =
        stringResource.desc().localized()

    override fun convert(resourceFormattedString: ResourceFormattedStringDesc) =
        resourceFormattedString.localized()
}
```

- [ ] **Step 8: `iosMain/.../di/CommonResourcesModule.ios.kt`**

```kotlin
package dev.nonoxy.residetrack.common.resources.di

import dev.nonoxy.residetrack.common.resources.IosStringConverter
import dev.nonoxy.residetrack.common.resources.StringConverter
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

internal actual val platformResourcesModule: Module = module {

    factoryOf<StringConverter>(::IosStringConverter)
}
```

- [ ] **Step 9: `gradle.properties` — флаг moko**

Проверить наличие строки `moko.resources.disableStaticFrameworkWarning=true` в `gradle.properties`. Если её нет — добавить в конец файла.

- [ ] **Step 10: Подключить в `settings.gradle.kts` (итоговый блок include)**

Привести блок `include(...)` к виду (порядок и комментарии — стиль KMMTemplate):

```kotlin
include(
    ":android:app",
    ":shared:main",

    // Core / Common
    ":shared:common",
    ":shared:common-resources",
    ":shared:core-domain",
    ":shared:core-mvikotlin",
    ":shared:core-presentation",
    ":shared:core-navigation",
    ":shared:core-database",
    ":shared:common-ui",

    // Features
    ":shared:feature-rooms:api",
    ":shared:feature-rooms:impl",

    ":shared:feature-add-room:api",
    ":shared:feature-add-room:impl",

    ":shared:feature-manage-students:api",
    ":shared:feature-manage-students:impl",

    ":shared:template-module"
)
```

Примечание: `:shared:common-ui` здесь уже фигурирует — каталог переименуют в Task 9; до этого Gradle упадёт на конфигурации. Поэтому Step 11 проверяет только модуль `common-resources` точечно; полная сборка проекта — после Task 9. Если исполнитель идёт строго по шагам, временно оставить в `include` строку `":shared:design-system",` вместо `":shared:common-ui",` и заменить её в Task 9, Step 4.

- [ ] **Step 11: Проверить сборку модуля**

Run: `./gradlew :shared:common-resources:build`
Expected: `BUILD SUCCESSFUL`, и сгенерирован класс `MR` (пакет `dev.nonoxy.residetrack.res`). Если moko-плагин не найден — проверить алиас из Task 1 и наличие `gradlePluginPortal()` в `pluginManagement` (он там есть).

---

### Task 8: `:shared:core-database` — нейминг DI под стиль

**Files:**
- Rename: `shared/core-database/src/commonMain/kotlin/dev/nonoxy/core/database/di/DatabaseModule.kt` → `CoreDatabaseModule.kt`

`core-database` после Фазы 1 уже использует convention plugins. Единственная «доводка под стиль» в объёме Фазы 2 — нейминг файла DI-модуля по образцу KMMTemplate (`CoreXxxModule.kt`). Содержимое (`val coreDatabaseModule`, пакет) не меняется — переименование пакета `dev.nonoxy.core.database` → `dev.nonoxy.residetrack.core.database` относится к Фазе 8.

- [ ] **Step 1: Переименовать файл**

Run: `git mv shared/core-database/src/commonMain/kotlin/dev/nonoxy/core/database/di/DatabaseModule.kt shared/core-database/src/commonMain/kotlin/dev/nonoxy/core/database/di/CoreDatabaseModule.kt`
(Имя `package` и `val coreDatabaseModule` внутри файла не трогать — переименовывается только файл.)

- [ ] **Step 2: Проверить сборку**

Run: `./gradlew :shared:core-database:build`
Expected: `BUILD SUCCESSFUL`.

---

### Task 9: Переименовать `:shared:design-system` → `:shared:common-ui`

**Files:**
- Rename: каталог `shared/design-system/` → `shared/common-ui/`
- Rename: пакет `dev/nonoxy/core/design/` → `dev/nonoxy/residetrack/common/ui/` внутри модуля
- Modify: `shared/common-ui/build.gradle.kts`, `settings.gradle.kts`
- Modify: все потребители (build-файлы фич, `shared/main`, исходники, импортирующие `dev.nonoxy.core.design.*`)

Это единственная ломающая структурная правка Фазы 2. Делается атомарно: модуль + пакет + все потребители — в одном проходе, сборка остаётся зелёной. Compose Resources в модуле сохраняются (перевод на moko — Фаза 6).

- [ ] **Step 1: Переименовать каталог модуля**

Run: `git mv shared/design-system shared/common-ui`

- [ ] **Step 2: Переименовать каталог пакета исходников**

```bash
mkdir -p shared/common-ui/src/commonMain/kotlin/dev/nonoxy/residetrack/common
git mv shared/common-ui/src/commonMain/kotlin/dev/nonoxy/core/design shared/common-ui/src/commonMain/kotlin/dev/nonoxy/residetrack/common/ui
```
Затем удалить осиротевшие пустые каталоги: `rmdir shared/common-ui/src/commonMain/kotlin/dev/nonoxy/core 2>/dev/null; true`.

- [ ] **Step 3: Заменить пакет во всех исходниках модуля**

Во всех `.kt` внутри `shared/common-ui/src/` заменить `dev.nonoxy.core.design` → `dev.nonoxy.residetrack.common.ui` (и в `package`, и во внутримодульных `import`).

Run: `grep -rl 'dev\.nonoxy\.core\.design' shared/common-ui/src` — для каждого файла заменить подстроку `dev.nonoxy.core.design` на `dev.nonoxy.residetrack.common.ui`.

- [ ] **Step 4: Обновить `build.gradle.kts` модуля**

Привести `shared/common-ui/build.gradle.kts` к виду (изменены `namespace` и `nameOfResClass`; блок `compose.resources` сохранён — ресурсы остаются на Compose Resources до Фазы 6):

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.common.ui"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.compose.icons.core,
        projects.shared.common,
    )
}

compose.resources {
    publicResClass = true
    generateResClass = auto
    nameOfResClass = "CommonUiRes"
}
```

Также в `settings.gradle.kts` убедиться, что строка модуля — `":shared:common-ui",` (если в Task 7 Step 10 временно оставляли `":shared:design-system",` — заменить сейчас).

- [ ] **Step 5: Обновить ссылку на сгенерированный класс ресурсов**

После переименования модуля Compose Resources генерирует класс в новом пакете. Старое имя класса — `DesignSystemRes` (пакет `residetrack.shared.design_system.generated.resources`), новое — `CommonUiRes` (ожидаемый пакет `residetrack.shared.common_ui.generated.resources`).

Файлы-потребители внутри модуля: `theme/Fonts.kt`, `common/state/ErrorLoadingState.kt`, `common/state/LoadingState.kt`. В каждом заменить импорт `...design_system.generated.resources.DesignSystemRes` на `...common_ui.generated.resources.CommonUiRes` и все обращения `DesignSystemRes.` → `CommonUiRes.`.

Run для поиска: `grep -rln 'DesignSystemRes' shared/common-ui/src`
Если фактический сгенерированный пакет отличается от ожидаемого — взять точный пакет из ошибки компиляции в Step 8.

- [ ] **Step 6: Обновить зависимость в `shared/main/build.gradle.kts`**

В `shared/main/build.gradle.kts` заменить `projects.shared.designSystem` → `projects.shared.commonUi`. (Этот build-файл будет полностью переписан в Task 15 — но на момент Task 9 правка нужна, чтобы сборка осталась зелёной.)

- [ ] **Step 7: Обновить зависимость в build-файлах фич**

Run: `grep -rln 'designSystem\|design-system' --include=build.gradle.kts shared/feature-*`
В каждом найденном `build.gradle.kts` заменить `projects.shared.designSystem` → `projects.shared.commonUi` (и `:shared:design-system` → `:shared:common-ui`, если встречается строковая нотация).

- [ ] **Step 8: Обновить импорты `dev.nonoxy.core.design.*` во всех потребителях**

Run: `grep -rln 'dev\.nonoxy\.core\.design' --include=*.kt shared android`
Ожидаемые потребители: `shared/main/src/.../App.kt` (`import dev.nonoxy.core.design.theme.ResideTrackTheme`) и UI-код в `feature-*/impl`. В каждом файле заменить подстроку `dev.nonoxy.core.design` → `dev.nonoxy.residetrack.common.ui`.

- [ ] **Step 9: Полная проверка сборки**

Run: `./gradlew :android:app:assembleDebug`
Expected: `BUILD SUCCESSFUL`. Типичные ошибки: пропущенный импорт `dev.nonoxy.core.design.*` (вернуться к Step 8) или неверный пакет сгенерированного класса ресурсов (взять точный пакет из ошибки, поправить Step 5).

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: `BUILD SUCCESSFUL`.

---

### Task 10: Контрольная точка — Commit A

- [ ] **Step 1: detekt**

Run: `./gradlew detekt`
Expected: `BUILD SUCCESSFUL`. Если copied-файлы дают замечания — применять только те `@Suppress`, что уже стоят в исходниках KMMTemplate; новые подавления не выдумывать.

- [ ] **Step 2: Commit**

```bash
git add -A
git commit -m "Add KMMTemplate core modules and rename design-system to common-ui

- Add :shared:core-mvikotlin, :shared:core-domain, :shared:core-presentation
- Add :shared:common-resources (moko-resources skeleton)
- Add OneTimeEvent/OneTimeGetValue to :shared:common
- Add AppVersion/GitCommitCountValueSource to build-logic
- Rename :shared:design-system to :shared:common-ui (module + package)

Phase 2 (part 1) of KMMTemplate migration."
```
Pre-commit hook прогонит detekt. Если упадёт — починить и повторить.

---

## Группа 2 — Доводка app-слоя: `:android:app`

### Task 11: `:android:app` — debug keystore и property подписи

**Files:**
- Create: `keystores/debug.keystore.jks`
- Modify: `gradle.properties`
- Modify: `.gitignore` (при необходимости)

- [ ] **Step 1: Сгенерировать debug keystore**

```bash
mkdir -p keystores
keytool -genkeypair -v \
  -keystore keystores/debug.keystore.jks \
  -storepass android -keypass android \
  -alias androiddebugkey \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Android Debug,O=Android,C=US"
```
Expected: создан файл `keystores/debug.keystore.jks` (~2.5 КБ).

- [ ] **Step 2: Добавить property подписи в `gradle.properties`**

В конец `gradle.properties` добавить блок:

```properties
# Keystore
DEBUG_KEY_ALIAS=androiddebugkey
DEBUG_STORE_PASSWORD=android
```

- [ ] **Step 3: Закоммитить keystore в git**

Debug-keystore по образцу KMMTemplate коммитится в репозиторий (он не секретный). Если `.gitignore` содержит правило `*.jks` — добавить его принудительно:

Run: `git add -f keystores/debug.keystore.jks`
Проверить: `git status --short` показывает `keystores/debug.keystore.jks` как staged.

- [ ] **Step 4: Проверка**

Run: `./gradlew :android:app:tasks --group=help`
Expected: `BUILD SUCCESSFUL` (конфигурация проходит; property `DEBUG_STORE_PASSWORD`/`DEBUG_KEY_ALIAS` ещё не читаются — это Task 12).

---

### Task 12: `:android:app` — build.gradle.kts до парности с KMMTemplate

**Files:**
- Modify: `android/app/build.gradle.kts` (полная замена)

Полный образец KMMTemplate: product flavors `dev`/`prod`, версионирование через `AppVersion`, `buildConfig = true`, подпись debug-keystore, переименование APK/AAB.

- [ ] **Step 1: Заменить `android/app/build.gradle.kts` целиком**

```kotlin
import com.android.build.api.variant.impl.VariantOutputImpl
import com.android.build.gradle.internal.tasks.FinalizeBundleTask
import utils.AppVersion
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(libs.versions.javaVersion.get().toInt())

    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-Xexpect-actual-classes",
        )
    }
}

android {
    namespace = "dev.nonoxy.residetrack"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.nonoxy.residetrack"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = AppVersion.getVersionCode(project).get()
        versionName = AppVersion.getVersionName(project).get()
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        named("debug").configure {
            val DEBUG_STORE_PASSWORD: String by project
            val DEBUG_KEY_ALIAS: String by project

            storeFile = file("$rootDir/keystores/debug.keystore.jks")
            storePassword = DEBUG_STORE_PASSWORD
            keyAlias = DEBUG_KEY_ALIAS
            keyPassword = DEBUG_STORE_PASSWORD
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // TODO: After create release signing config change "debug" -> "release"
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        register("dev") {
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-DEV"
            dimension = "environment"
        }

        register("prod") {
            dimension = "environment"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
        targetCompatibility = JavaVersion.toVersion(libs.versions.javaVersion.get())
    }
}

dependencies {
    implementation(projects.shared.main)
    implementation(libs.koin.android)
    implementation(libs.napier)

    debugImplementation(compose.uiTooling)
    implementation(compose.preview)
    implementation(libs.androidx.activity.compose)
}

androidComponents {
    onVariants(selector().all()) { variant ->
        val flavorName = variant.flavorName.orEmpty().capitalize()
        val buildTypeName = variant.buildType.orEmpty().capitalize()
        val versionName = AppVersion.getVersionName(project).get()

        // appDevRelease-1.0.0 -> app-dev-release-1.0.0
        val appName = "${project.name}-$flavorName-$buildTypeName-$versionName"
            .replace(Regex("([a-z])([A-Z])"), "$1-$2")
            .lowercase(Locale.getDefault())

        val outputAABTaskName = "sign${variant.name.capitalize()}Bundle"

        afterEvaluate {
            tasks.named<FinalizeBundleTask>(outputAABTaskName) {
                val parentFile = finalBundleFile.asFile.get().parentFile
                val finalFile = File(parentFile, "$appName.aab")
                finalBundleFile.set(finalFile)
            }
        }

        // Rename APK
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                output.outputFileName.set("$appName.apk")
            }
        }
    }
}
```

Примечание: `signing.properties`/release-keystore намеренно не заводятся (release-engineering вне объёма Фазы 2 по бэклогу). Release собирается debug-подписью, как в KMMTemplate.

- [ ] **Step 2: Проверка конфигурации (исходники ещё старые — сборка упадёт на компиляции, это ожидаемо)**

Run: `./gradlew :android:app:assembleProdDebug`
Expected: конфигурация проходит (flavors/signing/AppVersion разрешаются); компиляция может упасть, т.к. `Application`/пакеты исходников ещё не приведены — это чинит Task 13. Если падает именно на конфигурации (`DEBUG_STORE_PASSWORD not found`, `Unresolved reference: AppVersion`) — вернуться к Task 11 / Task 2.

---

### Task 13: `:android:app` — `Application`, `MainActivity`, `AppModule`, манифест

**Files:**
- Create: `shared/common/src/androidMain/kotlin/dev/nonoxy/common/di/AppEnvironmentQualifiers.kt`
- Create: `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/Application.kt`
- Create: `android/app/src/main/kotlin/dev/nonoxy/residetrack/di/AppModule.kt`
- Rename: `android/app/src/main/kotlin/dev/nonoxy/residetrack/MainActivity.kt` → `app/MainActivity.kt`
- Modify: `android/app/src/main/AndroidManifest.xml`

`AppModule` (по образцу KMMTemplate) пробрасывает значения `BuildConfig` в Koin под квалификаторами `AppEnvironmentQualifiers` (он лежит в `common/androidMain`). `MainActivity` переезжает в подпакет `.app`. `App()` (composable) импортируется из `shared/main` — она будет переписана в Task 14; здесь `MainActivity` ссылается на `dev.nonoxy.residetrack.App`.

- [ ] **Step 1: `AppEnvironmentQualifiers.kt` в `common/androidMain`**

Создать `shared/common/src/androidMain/kotlin/dev/nonoxy/common/di/AppEnvironmentQualifiers.kt`:

```kotlin
package dev.nonoxy.common.di

object AppEnvironmentQualifiers {
    const val VERSION = "version"
    const val APPLICATION_ID = "applicationId"
    const val FLAVOR = "flavor"
    const val VERSION_CODE = "versionCode"
}
```

- [ ] **Step 2: Переместить `MainActivity.kt` в подпакет `.app`**

```bash
mkdir -p android/app/src/main/kotlin/dev/nonoxy/residetrack/app
git mv android/app/src/main/kotlin/dev/nonoxy/residetrack/MainActivity.kt android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt
```

Заменить содержимое `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt` на:

```kotlin
package dev.nonoxy.residetrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.nonoxy.residetrack.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
```

Примечание: инициализация Napier перенесена из `MainActivity` в `Application` (Step 3). `enableEdgeToEdge()` намеренно не добавляется (см. «Решения исполнителя»).

- [ ] **Step 3: `Application.kt`**

Создать `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/Application.kt`:

```kotlin
package dev.nonoxy.residetrack.app

import android.app.Application
import dev.nonoxy.residetrack.BuildConfig
import dev.nonoxy.residetrack.di.appModule
import dev.nonoxy.residetrack.di.initKoin
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()

        setupKoin()
        setupLogging()
    }

    private fun setupLogging() {
        if (BuildConfig.DEBUG) {
            Napier.base(DebugAntilog(defaultTag = "ResideTrack"))
        }
    }

    private fun setupKoin() {
        initKoin {
            androidContext(this@Application)
            modules(
                appModule,
            )
        }
    }
}
```

- [ ] **Step 4: `di/AppModule.kt`**

Создать `android/app/src/main/kotlin/dev/nonoxy/residetrack/di/AppModule.kt`:

```kotlin
package dev.nonoxy.residetrack.di

import dev.nonoxy.common.di.AppEnvironmentQualifiers
import dev.nonoxy.residetrack.BuildConfig
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    factory(named(AppEnvironmentQualifiers.VERSION)) {
        BuildConfig.VERSION_NAME
    }

    factory(named(AppEnvironmentQualifiers.FLAVOR)) {
        BuildConfig.FLAVOR
    }

    factory(named(AppEnvironmentQualifiers.APPLICATION_ID)) {
        BuildConfig.APPLICATION_ID
    }

    factory(named(AppEnvironmentQualifiers.VERSION_CODE)) {
        BuildConfig.VERSION_CODE
    }
}
```

- [ ] **Step 5: Обновить `AndroidManifest.xml`**

В `android/app/src/main/AndroidManifest.xml` добавить `android:name=".app.Application"` в тег `<application>` и изменить `android:name=".MainActivity"` → `android:name=".app.MainActivity"`. Атрибут `android:configChanges` у активити сохранить без изменений (поведение). Итог:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".app.Application"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:exported="true"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|mnc|colorMode|density|fontScale|fontWeightAdjustment|keyboard|layoutDirection|locale|mcc|navigation|smallestScreenSize|touchscreen|uiMode"
            android:name=".app.MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 6: Проверка**

`Application`/`AppModule` ссылаются на `initKoin` и `App` из `shared/main`, которые перерабатываются в Task 14. Поэтому сборка `:android:app` станет зелёной только после Task 14. Здесь — проверка сборки `common`:

Run: `./gradlew :shared:common:build`
Expected: `BUILD SUCCESSFUL` (новый `AppEnvironmentQualifiers` в `androidMain` компилируется).

---

## Группа 3 — `:shared:main` и паттерн Koin

### Task 14: `:shared:main` — `Koin.kt` (`initKoin`) и `App()`

**Files:**
- Create: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`
- Delete: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/AppModule.kt`
- Delete: `shared/main/src/androidMain/kotlin/dev/nonoxy/residetrack/di/KoinPlatform.android.kt`
- Delete: `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/di/KoinPlatform.ios.kt`
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/App.kt`

Переход с `KoinMultiplatformApplication` внутри `App()` на `initKoin` (= `startKoin`). `koinPlatformModules()` (мёртвый код — нигде не вызывался) удаляется.

- [ ] **Step 1: Создать `di/Koin.kt`**

```kotlin
package dev.nonoxy.residetrack.di

import dev.nonoxy.core.database.di.coreDatabaseModule
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            coreDatabaseModule,

            featureRoomsImplModule,
            featureAddRoomImplModule,
            featureManageStudentsImplModule,
        )
    }
}
```

Примечание: список Koin-модулей собирается вручную (как в KMMTemplate). Имена feature-модулей взяты из удаляемого `AppModule.kt`. Параметр `appDeclaration` имеет дефолт `{}`, чтобы iOS мог вызвать `initKoin()` без аргументов.

- [ ] **Step 2: Удалить мёртвые DI-файлы**

```bash
git rm shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/AppModule.kt
git rm shared/main/src/androidMain/kotlin/dev/nonoxy/residetrack/di/KoinPlatform.android.kt
git rm shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/di/KoinPlatform.ios.kt
```

- [ ] **Step 3: Переписать `App.kt`**

Заменить `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/App.kt` на:

```kotlin
package dev.nonoxy.residetrack

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.navigation.ResideTrackNavHost

@Composable
fun App() {
    ResideTrackTheme {
        ResideTrackNavHost(modifier = Modifier.fillMaxSize())
    }
}
```

Удалены: `KoinMultiplatformApplication`, `koinConfiguration`, `appModule()`, `@Preview`, `@OptIn(KoinExperimentalAPI::class)`. Импорт темы — `dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme` (новый пакет common-ui из Task 9).

- [ ] **Step 4: Проверка сборки moduля**

Сборка `:shared:main` зависит от `build.gradle.kts`, который ещё содержит старые зависимости — это чинит Task 15. Здесь компиляцию не проверяем; переходим к Task 15.

---

### Task 15: `:shared:main` — build.gradle.kts с авто-агрегацией модулей

**Files:**
- Modify: `shared/main/build.gradle.kts` (полная замена)

Явный список `projects.shared.*` заменяется на авто-агрегацию через `rootProject.childProjects` (по образцу KMMTemplate). Внешние библиотеки, нужные `ResideTrackNavHost` (навигация, lifecycle), сохраняются явно — фичи ещё не разбиты на `ui`-модули (Фазы 4–5), поэтому навигация живёт в `shared/main`.

- [ ] **Step 1: Заменить `shared/main/build.gradle.kts` целиком**

```kotlin
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import plugins.composeBundle

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.residetrack.main"
}

kotlin {
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
        }
    }

    addCommonModules()
    addCoreModules()
    addFeatureModules()
}

commonMainDependencies {
    apis(
        projects.shared.common,
        projects.shared.commonResources,
    )

    implementations(
        *composeBundle,
        libs.androidx.lifecycle.viewmodel,
        libs.androidx.lifecycle.runtimeCompose,
        libs.compose.multiplatform.navigation,
        libs.compose.navigation.material,
        libs.koin.composeMultiplatform,
        libs.koin.composeMultiplatform.viewmodelNavigation,
    )
}

private fun KotlinMultiplatformExtension.addFeatureModules() {

    val featureProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "feature-") }
        .values
        .flatMap { project -> project.childProjects.values }

    commonMainDependencies {
        implementations(*featureProjects.toTypedArray())
    }
}

private fun KotlinMultiplatformExtension.addCoreModules() {

    val coreProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "core-") }
        .values
        .flatMap { project -> project.childProjects.values.takeIf { it.isNotEmpty() } ?: listOf(project) }

    commonMainDependencies {
        implementations(*coreProjects.toTypedArray())
    }
}

private fun KotlinMultiplatformExtension.addCommonModules() {

    val commonProjects = rootProject.childProjects
        .filterKeys { name -> name == "shared" }
        .map { (_, project) -> project.childProjects }
        .first()
        .filterKeys { name -> name.startsWith(prefix = "common") }
        .values
        .flatMap { project -> project.childProjects.values.takeIf { it.isNotEmpty() } ?: listOf(project) }

    commonMainDependencies {
        implementations(*commonProjects.toTypedArray())
    }
}
```

Пояснение: `addCommonModules` (префикс `common`) подхватывает `common`, `common-resources`, `common-ui`; `addCoreModules` (префикс `core-`) — `core-domain`, `core-mvikotlin`, `core-presentation`, `core-navigation`, `core-database`; `addFeatureModules` (префикс `feature-`) — подмодули `api`/`impl` всех фич. `:shared:main` и `:shared:template-module` ни под один префикс не попадают. Явный список `room.runtime`/`koin.core`/`napier` убран — он приходит транзитивно (`kmp-library` добавляет `koin.core`/`napier` каждому модулю; Room — из `core-database`).

- [ ] **Step 2: Проверка сборки Android**

Run: `./gradlew :android:app:assembleProdDebug`
Expected: `BUILD SUCCESSFUL`. Возможные ошибки:
  - `Unresolved reference: ResideTrackNavHost`/навигация — проверить, что `core-navigation` агрегируется (`addCoreModules`) и что навигационные библиотеки в `commonMainDependencies`.
  - `Unresolved reference: featureRoomsImplModule` в `Koin.kt` — проверить, что `addFeatureModules` подключил `feature-*:impl`.

- [ ] **Step 3: Проверка iOS-фреймворка**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: `BUILD SUCCESSFUL`.

---

### Task 16: Koin на iOS — вызов `initKoin` в `MainViewController`

**Files:**
- Modify: `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt`

KMMTemplate `initKoin` на iOS не вызывает. reside-track использует DI на iOS — добавляем вызов с защитой от повторного `startKoin`.

- [ ] **Step 1: Переписать `MainViewController.kt`**

```kotlin
package dev.nonoxy.residetrack

import androidx.compose.ui.window.ComposeUIViewController
import dev.nonoxy.residetrack.di.initKoin
import org.koin.core.context.GlobalContext
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    if (GlobalContext.getOrNull() == null) {
        initKoin()
    }
    return ComposeUIViewController { App() }
}
```

Защита `GlobalContext.getOrNull() == null` страхует от повторного `startKoin`, если `MainViewController()` будет вызван более одного раза.

- [ ] **Step 2: Проверка iOS-фреймворка**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: `BUILD SUCCESSFUL`.

---

### Task 17: Финальная верификация и Commit B

- [ ] **Step 1: Полная сборка Android (все flavor'ы)**

Run: `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug`
Expected: `BUILD SUCCESSFUL`. Проверить, что APK переименованы: в `android/app/build/outputs/apk/` файлы вида `app-dev-debug-<version>.apk` и `app-prod-debug-<version>.apk`.

- [ ] **Step 2: Сборка iOS-фреймворка**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: detekt**

Run: `./gradlew detekt`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit B**

```bash
git add -A
git commit -m "Bring android:app and shared:main to KMMTemplate parity

- android:app: product flavors dev/prod, AppVersion versioning,
  buildConfig, debug keystore signing, APK/AAB renaming, Application class
- shared:main: initKoin pattern replaces KoinMultiplatformApplication,
  module auto-aggregation via childProjects helpers
- iOS: initKoin called in MainViewController

Phase 2 (part 2) of KMMTemplate migration."
```
Pre-commit hook прогонит detekt.

- [ ] **Step 5: Обновить спеку и память**

В `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`, раздел «14. Фазы исполнения», пометить Фазу 2 как завершённую (по образцу пометки Фазы 1).

Обновить файл памяти `kmmtemplate-migration.md`: отметить Фазу 2 завершённой, перечислить созданные модули, указать, что следующая — Фаза 3 (навигация).

- [ ] **Step 6: Ручная проверка поведения (пользователь)**

Запросить у пользователя проверку, что Android-приложение запускается и базовые экраны (список комнат, добавление комнаты, управление студентами) работают как прежде. iOS-сборку в Xcode пользователь проверяет отдельно (как и в Фазе 1).

---

## Самопроверка плана (проведена)

**Покрытие спеки (Фаза 2):**
- core-mvikotlin / core-presentation / core-domain — Tasks 3, 6, 4. ✅
- common под стиль — Task 5 (аддитивно; полное приведение — Фазы 4–5/8, обосновано в «Решениях исполнителя»). ✅
- common-ui (бывший design-system) — Task 9. ✅
- common-resources (новый, moko) — Task 7. ✅
- core-database под стиль — Task 8. ✅
- :android:app до парности (flavors, AppVersion, buildConfig, keystore, переименование артефактов, Application+AppModule) — Tasks 11, 12, 13. ✅
- :shared:main до парности (reflection-агрегация) — Task 15. ✅
- Koin до паттерна KMMTemplate (initKoin + Application) — Tasks 14, 13, 16. ✅
- Контрольная точка: сборка + detekt — Tasks 10, 17. ✅

**Известные отклонения от буквального KMMTemplate (зафиксированы в «Решениях исполнителя» вверху):** `common` трогается аддитивно; `common-ui` сохраняет Compose Resources до Фазы 6; `common-resources` — каркас; iOS-фреймворк `isStatic = false`; нет `enableEdgeToEdge()`; нет `changeGitHooksDir`; добавлен вызов `initKoin` на iOS (одобрено пользователем).

**Согласованность типов:** `initKoin(appDeclaration: KoinAppDeclaration = {})` — Task 14, вызовы в Task 13 (`initKoin { ... }`) и Task 16 (`initKoin()`) совместимы. `coreDatabaseModule`/`featureXxxImplModule` — имена сохранены из существующего `AppModule.kt`. `appModule` (Android) — Task 13, используется только там. `CommonUiRes` — Task 9, единое имя.
