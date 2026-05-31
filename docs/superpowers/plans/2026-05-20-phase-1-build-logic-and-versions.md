# Phase 1 — build-logic и версии: Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Привести build-инфраструктуру reside-track (`build-logic`, version catalog, версии) к виду эталона KMMTemplate, сохранив текущую топологию модулей и собираемость проекта.

**Architecture:** Convention plugins reside-track заменяются на набор KMMTemplate (`kmp-library`, `kmp-library-legacy`, `android-library`, `compose-multiplatform-setup`, `kmp-feature-setup`, `json-serialization`). `KmpLibraryPlugin` переходит на новый AGP-9 KMP-Android-плагин (`com.android.kotlin.multiplatform.library`). Версии (Kotlin/AGP/Compose/Koin) бампаются до уровня KMMTemplate, добавляются библиотеки следующих фаз (MVIKotlin, moko-resources, moko-mvvm).

**Tech Stack:** Gradle convention plugins (`kotlin-dsl`), Kotlin Multiplatform, AGP 9, Compose Multiplatform, Gradle version catalog.

---

## Контекст и ограничения

- **Эталон:** `/Users/a.dobrov/StudioProjects/KMMTemplate`. При сомнении — смотреть, как сделано там; своего не выдумывать.
- **Спека:** `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`.
- **Топология модулей в этой фазе НЕ меняется** — фичи остаются 2-модульными (`api`/`impl`),
  `composeApp` остаётся единым app-модулем. 4-модульный split и реструктуризация app —
  более поздние фазы.
- **`KmpFeatureSetupPlugin`** создаётся в этой фазе (часть набора build-logic), но **не
  применяется** ни к одному модулю до Фазы 4 — он ссылается на core-модули, которых ещё нет.
- **`composeApp`** — это `androidApplication`; convention-плагин `kmp-library` (новый
  KMP-Android-library-плагин) к нему неприменим. В этой фазе `composeApp/build.gradle.kts`
  переводится на прямое применение плагинов. Корректный split на `:android:app` +
  `:shared:main` — Фаза 7.
- **Сеть доступна.** Сборка проверяется после задач. Если Gradle падает на TLS-handshake —
  это окружение, повторить/эскалировать пользователю.
- **Коммиты — в стиле пользователя:** короткая повелительная строка на английском,
  с заглавной буквы, без префиксов и тела (примеры из истории: `Add build logic with
  convention plugins`, `Add kotlin serialization and convention KmpSerializationPlugin`).
- **Pre-commit hook** гоняет `./gradlew detekt`. Коммиты с `.kt`-файлами должны проходить
  detekt. Если detekt недоступен (нет сети) — согласовать с пользователем.

## Конвенция этого плана

Многие файлы build-logic копируются из KMMTemplate **дословно** — для них шаг указывает
исходный путь в KMMTemplate и целевой путь в reside-track. Содержимое инлайнится только
там, где файл reside-track-специфичен (version catalog, регистрация плагинов, build.gradle.kts
модулей). Источник истины — файлы KMMTemplate.

## File Structure

| Файл | Ответственность | Действие |
|---|---|---|
| `gradle/libs.versions.toml` | Version catalog | Переписать |
| `buildLogic/` → `build-logic/` | Convention plugins | Переименовать каталог |
| `build-logic/settings.gradle.kts` | Настройка composite build | Переписать |
| `build-logic/build.gradle.kts` | Зависимости + регистрация плагинов | Переписать |
| `build-logic/src/main/kotlin/extensions/*` | Gradle DSL-расширения | Заменить набором KMMTemplate |
| `build-logic/src/main/kotlin/plugins/*` | Convention plugins | Заменить набором KMMTemplate |
| `settings.gradle.kts` (корень) | `includeBuild`, состав модулей | Обновить |
| `build.gradle.kts` (корень) | Алиасы плагинов, detekt | Обновить |
| `gradle.properties` | Флаги Gradle/Kotlin/Android | Выровнять по KMMTemplate |
| `composeApp/build.gradle.kts` | App-модуль | Переписать (прямое применение плагинов) |
| `shared/*/build.gradle.kts` (11 модулей) | Library-модули | Обновить под новый API плагинов |

---

## Task 1: Мигрировать version catalog

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: Переписать `gradle/libs.versions.toml`**

Целевое содержимое (база — KMMTemplate, плюс reside-track-специфика: Room, KSP, bottom-sheet
navigation, icons; ktor не добавляется — сети нет, YAGNI):

```toml
[versions]
# Build
agp = "9.0.0"
javaVersion = "17"

# Kotlin
kotlin = "2.3.10"
kotlin-coroutines = "1.10.2"
kotlin-serialization = "1.10.0"
kotlin-datetime = "0.7.1"
kotlin-immutableCollections = "0.4.0"

# KSP (версия ДОЛЖНА соответствовать kotlin — проверить на github.com/google/ksp/releases)
ksp = "2.3.10-2.0.4"

# Android
android-compileSdk = "36"
android-minSdk = "26"
android-targetSdk = "36"

# AppVersion
appVersion-major = "1"
appVersion-minor = "0"

# Compose Multiplatform
compose-multiplatform = "1.10.1"
compose-multiplatform-material3 = "1.9.0"
compose-multiplatform-navigation = "2.9.2"
compose-icons-core = "1.7.3"
compose-navigation-material = "1.8.0-beta05"

# AndroidX
androidx-activity = "1.12.4"
androidx-appcompat = "1.7.1"
androidx-core = "1.17.0"
androidx-lifecycle = "2.9.6"

# Room
room = "2.7.1"
sqliteBundled = "2.5.1"

# Test
androidx-espresso = "3.7.0"
androidx-testExt = "1.3.0"
junit = "4.13.2"

# DI
koin = "4.1.1"

# Detekt
detekt = "1.23.8"
detekt-compose-kode = "1.4.0"
detekt-compose-twitter = "0.0.26"

# Moko
moko-resources = "0.26.0"
moko-mvvm = "0.16.1"

# MVIKotlin
mvikotlin = "4.3.0"

# Logging
napier = "2.7.1"

[libraries]

# Plugins for composite build
gradleplugin-android = { module = "com.android.tools.build:gradle", version.ref = "agp" }
gradleplugin-composeCompiler = { module = "org.jetbrains.kotlin:compose-compiler-gradle-plugin", version.ref = "kotlin" }
gradleplugin-composeMultiplatform = { module = "org.jetbrains.compose:org.jetbrains.compose.gradle.plugin", version.ref = "compose-multiplatform" }
gradleplugin-kotlin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }

# Kotlin
kotlin-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "kotlin-coroutines" }
kotlin-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlin-coroutines" }
kotlin-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlin-coroutines" }
kotlin-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlin-serialization" }
kotlin-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlin-datetime" }
kotlin-immutableCollections = { group = "org.jetbrains.kotlinx", name = "kotlinx-collections-immutable", version.ref = "kotlin-immutableCollections" }

# Compose Multiplatform
compose-multiplatform-ui = { group = "org.jetbrains.compose.ui", name = "ui", version.ref = "compose-multiplatform" }
compose-multiplatform-runtime = { group = "org.jetbrains.compose.runtime", name = "runtime", version.ref = "compose-multiplatform" }
compose-multiplatform-foundation = { group = "org.jetbrains.compose.foundation", name = "foundation", version.ref = "compose-multiplatform" }
compose-multiplatform-material3 = { group = "org.jetbrains.compose.material3", name = "material3", version.ref = "compose-multiplatform-material3" }
compose-multiplatform-material = { group = "org.jetbrains.compose.material", name = "material", version.ref = "compose-multiplatform" }
compose-multiplatform-animation = { group = "org.jetbrains.compose.animation", name = "animation", version.ref = "compose-multiplatform" }
compose-multiplatform-resources = { group = "org.jetbrains.compose.components", name = "components-resources", version.ref = "compose-multiplatform" }
compose-multiplatform-uiToolingPreview = { group = "org.jetbrains.compose.components", name = "components-ui-tooling-preview", version.ref = "compose-multiplatform" }
compose-multiplatform-uiTooling = { group = "org.jetbrains.compose.ui", name = "ui-tooling", version.ref = "compose-multiplatform" }
compose-multiplatform-backhandler = { group = "org.jetbrains.compose.ui", name = "ui-backhandler", version.ref = "compose-multiplatform" }
compose-multiplatform-navigation = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "compose-multiplatform-navigation" }
compose-navigation-material = { module = "org.jetbrains.compose.material:material-navigation", version.ref = "compose-navigation-material" }
compose-icons-core = { module = "org.jetbrains.compose.material:material-icons-core", version.ref = "compose-icons-core" }

# Koin
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
koin-core-viewmodel = { group = "io.insert-koin", name = "koin-core-viewmodel", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-composeMultiplatform = { group = "io.insert-koin", name = "koin-compose", version.ref = "koin" }
koin-composeMultiplatform-viewmodel = { group = "io.insert-koin", name = "koin-compose-viewmodel", version.ref = "koin" }
koin-composeMultiplatform-viewmodelNavigation = { group = "io.insert-koin", name = "koin-compose-viewmodel-navigation", version.ref = "koin" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqliteBundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqliteBundled" }

# MVIKotlin
mvikotlin-core = { module = "com.arkivanov.mvikotlin:mvikotlin", version.ref = "mvikotlin" }
mvikotlin-main = { module = "com.arkivanov.mvikotlin:mvikotlin-main", version.ref = "mvikotlin" }
mvikotlin-logging = { module = "com.arkivanov.mvikotlin:mvikotlin-logging", version.ref = "mvikotlin" }
mvikotlin-coroutines = { module = "com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines", version.ref = "mvikotlin" }

# Moko
moko-resources-core = { module = "dev.icerock.moko:resources", version.ref = "moko-resources" }
moko-resources-compose = { module = "dev.icerock.moko:resources-compose", version.ref = "moko-resources" }
moko-mvvm-flow = { module = "dev.icerock.moko:mvvm-flow", version.ref = "moko-mvvm" }

# Logging
napier = { module = "io.github.aakira:napier", version.ref = "napier" }

# AndroidX
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "androidx-core" }
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "androidx-appcompat" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity" }
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "androidx-lifecycle" }
androidx-lifecycle-viewmodelCompose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "androidx-lifecycle" }
androidx-lifecycle-runtimeCompose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }

# Test
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlin-testJunit = { module = "org.jetbrains.kotlin:kotlin-test-junit", version.ref = "kotlin" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-testExt-junit = { module = "androidx.test.ext:junit", version.ref = "androidx-testExt" }
androidx-espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "androidx-espresso" }

# Detekt
detekt-formatting = { group = "io.gitlab.arturbosch.detekt", name = "detekt-formatting", version.ref = "detekt" }
detekt-libraries = { group = "io.gitlab.arturbosch.detekt", name = "detekt-rules-libraries", version.ref = "detekt" }
detekt-compose-kode = { module = "ru.kode:detekt-rules-compose", version.ref = "detekt-compose-kode" }
detekt-compose-twitter = { module = "com.twitter.compose.rules:detekt", version.ref = "detekt-compose-twitter" }

[plugins]
# Convention plugins
conventionPlugin-composeMultiplatformSetup = { id = "compose-multiplatform-setup", version = "" }
conventionPlugin-androidLibrary = { id = "android-library", version = "" }
conventionPlugin-kmpLibrary = { id = "kmp-library", version = "" }
conventionPlugin-kmpLibraryLegacy = { id = "kmp-library-legacy", version = "" }
conventionPlugin-kmpFeatureSetup = { id = "kmp-feature-setup", version = "" }
conventionPlugin-jsonSerialization = { id = "json-serialization", version = "" }

# Android
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidLibrary = { id = "com.android.library", version.ref = "agp" }

# Kotlin
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-multiplatformAndroidLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

# Compose
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

# Room / KSP
room = { id = "androidx.room", version.ref = "room" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

# Detekt
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

- [ ] **Step 2: Проверить совместимость KSP/Room с Kotlin 2.3.10**

KSP-версия (`ksp = "2.3.10-2.0.4"`) — пример; точную совместимую версию взять из
github.com/google/ksp/releases для Kotlin 2.3.10. Если Room 2.7.1 несовместим с
Kotlin 2.3.10 — поднять Room до совместимой версии (проверяется сборкой в Task 9).
Done-условие — `core-database` собирается в Task 9.

- [ ] **Step 3: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "Bump versions and align catalog with KMMTemplate"
```

(Если detekt-hook падает из-за отсутствия `.kt` в стейдже — он пропускается; `.toml` не триггерит detekt.)

---

## Task 2: Переименовать каталог build-logic и переписать его конфигурацию

**Files:**
- Rename: `buildLogic/` → `build-logic/`
- Modify: `settings.gradle.kts` (корень)
- Rewrite: `build-logic/settings.gradle.kts`, `build-logic/build.gradle.kts`

- [ ] **Step 1: Переименовать каталог**

```bash
git mv buildLogic build-logic
```

- [ ] **Step 2: Обновить `includeBuild` в корневом `settings.gradle.kts`**

Заменить строку `includeBuild("buildLogic")` на `includeBuild("build-logic")`.

- [ ] **Step 3: Переписать `build-logic/settings.gradle.kts`**

Скопировать дословно из `KMMTemplate/build-logic/settings.gradle.kts`:

```kotlin
rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }

    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
```

- [ ] **Step 4: Переписать `build-logic/build.gradle.kts`**

Скопировать дословно из `KMMTemplate/build-logic/build.gradle.kts` (использует
`compileOnly` для gradle-плагинов и регистрирует 6 плагинов: `android-library`,
`kmp-library`, `kmp-library-legacy`, `kmp-feature-setup`, `compose-multiplatform-setup`,
`json-serialization`).

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "Rename buildLogic to build-logic and align its configuration"
```

---

## Task 3: Заменить extensions build-logic набором KMMTemplate

**Files:**
- Delete: `build-logic/src/main/kotlin/extensions/BaseExtensions.kt`
- Rewrite: `build-logic/src/main/kotlin/extensions/ProjectExtensions.kt`
- Rewrite: `build-logic/src/main/kotlin/extensions/DependenciesExtensions.kt`
- Rewrite: `build-logic/src/main/kotlin/extensions/KmpDependenciesExtensions.kt`
- Create: `build-logic/src/main/kotlin/extensions/FeatureModuleExtensions.kt`
- Create: `build-logic/src/main/kotlin/extensions/ProviderExtenstions.kt`

- [ ] **Step 1: Скопировать файлы extensions из KMMTemplate дословно**

Из `KMMTemplate/build-logic/src/main/kotlin/extensions/` в одноимённые пути reside-track:
- `ProjectExtensions.kt` — содержит `androidLibraryConfig`, `androidAppConfig`,
  `androidConfig`, `kotlinMultiplatformConfig`, `composeCompilerConfig` и др.;
- `DependenciesExtensions.kt` — `implementations`, `apis`, `implementation`, `api`,
  `debugImplementation`;
- `KmpDependenciesExtensions.kt` — `commonMainDependencies`, `androidMainDependencies`,
  `iosMainDependencies`, `commonTestDependencies`;
- `FeatureModuleExtensions.kt` — `FeatureModuleType` (API/IMPL/PRESENTATION/UI),
  `isApiModule`/`isImplModule`/`isPresentationModule`/`isUiModule`, `getApiModule`,
  `getPresentationModule`, `asList`;
- `ProviderExtenstions.kt` — `property` helpers.

- [ ] **Step 2: Удалить устаревший `BaseExtensions.kt`**

```bash
git rm build-logic/src/main/kotlin/extensions/BaseExtensions.kt
```

Его содержимое (`androidConfig`, `androidAppConfig`, `kotlinMultiplatformConfig` и др.)
покрыто `ProjectExtensions.kt` KMMTemplate.

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "Align build-logic extensions with KMMTemplate"
```

---

## Task 4: Заменить convention plugins набором KMMTemplate

**Files:**
- Delete: `build-logic/src/main/kotlin/plugins/ComposeCompilerPlugin.kt`
- Delete: `build-logic/src/main/kotlin/plugins/KmpSerializationPlugin.kt`
- Rewrite: `build-logic/src/main/kotlin/plugins/KmpLibraryPlugin.kt`
- Rewrite: `build-logic/src/main/kotlin/plugins/AndroidLibraryPlugin.kt`
- Create: `build-logic/src/main/kotlin/plugins/KmpLibraryLegacyPlugin.kt`
- Create: `build-logic/src/main/kotlin/plugins/ComposeMultiplatformSetupPlugin.kt`
- Create: `build-logic/src/main/kotlin/plugins/KmpFeatureSetupPlugin.kt`
- Create: `build-logic/src/main/kotlin/plugins/JsonSerializationPlugin.kt`

- [ ] **Step 1: Скопировать плагины из KMMTemplate дословно**

Из `KMMTemplate/build-logic/src/main/kotlin/plugins/` в одноимённые пути reside-track:
`KmpLibraryPlugin.kt`, `KmpLibraryLegacyPlugin.kt`, `AndroidLibraryPlugin.kt`,
`ComposeMultiplatformSetupPlugin.kt`, `KmpFeatureSetupPlugin.kt`, `JsonSerializationPlugin.kt`.

Замечания:
- `KmpLibraryPlugin` KMMTemplate использует новый `com.android.kotlin.multiplatform.library`
  и конфигурируется через `androidLibraryConfig { namespace = ... }`. `androidTarget {}`
  больше не нужен.
- `KmpFeatureSetupPlugin` ссылается на `:shared:core-presentation`, `:shared:common-resources`,
  `:shared:common-ui`, `:shared:core-domain`, `:shared:core-mvikotlin`,
  `:shared:core-navigation`. Эти модули появятся в Фазах 2–3. Файл компилируется (это
  строковые пути), плагин **не применяется** ни к одному модулю в этой фазе — ошибок нет.
- `ComposeMultiplatformSetupPlugin` экспортирует `composeBundle` (runtime/foundation/ui/
  material3). Используется модулями с Compose.

- [ ] **Step 2: Удалить устаревшие плагины reside-track**

```bash
git rm build-logic/src/main/kotlin/plugins/ComposeCompilerPlugin.kt
git rm build-logic/src/main/kotlin/plugins/KmpSerializationPlugin.kt
```

`ComposeCompilerPlugin` заменяется на `ComposeMultiplatformSetupPlugin`;
`KmpSerializationPlugin` — на `JsonSerializationPlugin`.

- [ ] **Step 3: Удалить устаревший `tasks/RenameOutputTask.kt` если он есть в reside-track**

Проверить `build-logic/src/main/kotlin/`. Из KMMTemplate `tasks/`, `utils/AppVersion.kt`,
`valueSource/GitCommitCountValueSource.kt` нужны только app-модулю (Фаза 7) — в этой
фазе **не копировать**.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "Replace convention plugins with KMMTemplate set"
```

---

## Task 5: Обновить корневые build-файлы

**Files:**
- Modify: `build.gradle.kts` (корень)
- Modify: `settings.gradle.kts` (корень)

- [ ] **Step 1: Обновить алиасы плагинов в корневом `build.gradle.kts`**

В блоке `plugins {}` заменить convention-плагины на новый набор и убрать ставшие
ненужными прямые алиасы. Целевой блок `plugins {}`:

```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.multiplatformAndroidLibrary) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)

    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup) apply false
    alias(libs.plugins.conventionPlugin.androidLibrary) apply false
    alias(libs.plugins.conventionPlugin.kmpLibrary) apply false
    alias(libs.plugins.conventionPlugin.kmpLibraryLegacy) apply false
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup) apply false
    alias(libs.plugins.conventionPlugin.jsonSerialization) apply false
}
```

Блоки `dependencies { detektPlugins(...) }`, `detekt {}`, задачу `changeGitHooksDir`
и `tasks.getByPath(":composeApp:preBuild")...` — **оставить без изменений**.

- [ ] **Step 2: Проверить корневой `settings.gradle.kts`**

`includeBuild("build-logic")` уже обновлён в Task 2. Состав `include(...)` модулей в
этой фазе не меняется. Убедиться, что `:shared:template-module` ещё в списке (его
удаление — отдельная фаза).

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts settings.gradle.kts
git commit -m "Update root build files for new plugin set"
```

---

## Task 6: Выровнять gradle.properties

**Files:**
- Modify: `gradle.properties`

- [ ] **Step 1: Сравнить с `KMMTemplate/gradle.properties` и добавить недостающие флаги**

Сохранить текущие флаги reside-track, добавить флаги KMMTemplate, которых нет (например
`kotlin.daemon.jvmargs`, флаги KMP/iOS-сборки). Не удалять `android.nonTransitiveRClass`,
`android.useAndroidX`. Точный набор — по `KMMTemplate/gradle.properties`.

- [ ] **Step 2: Commit**

```bash
git add gradle.properties
git commit -m "Align gradle.properties with KMMTemplate"
```

---

## Task 7: Перевести library-модули на новый API плагинов

Каждый из 11 library-модулей переводится с `android { namespace = ... }` +
`iosConfig {}` на `androidLibraryConfig { namespace = ... }` (новый KMP-Android-плагин
`iosConfig` не имеет — XCFramework только у app-модуля). Convention-плагины применяются
по новым алиасам. `:shared:common` и `impl→api` зависимости теперь добавляются явно
(в этой фазе фичи ещё 2-модульные и `kmp-feature-setup` не применяется).

**Files (Modify):** `shared/common/build.gradle.kts`, `shared/core-navigation/build.gradle.kts`,
`shared/core-database/build.gradle.kts`, `shared/design-system/build.gradle.kts`,
`shared/template-module/build.gradle.kts`, `shared/feature-rooms/api/build.gradle.kts`,
`shared/feature-rooms/impl/build.gradle.kts`, `shared/feature-add-room/api/build.gradle.kts`,
`shared/feature-add-room/impl/build.gradle.kts`,
`shared/feature-manage-students/api/build.gradle.kts`,
`shared/feature-manage-students/impl/build.gradle.kts`

- [ ] **Step 1: `shared/common/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.common"
}

commonMainDependencies {
    implementations(
        libs.androidx.lifecycle.viewmodel,
        libs.kotlin.immutableCollections,
    )
    apis(
        libs.koin.core,
        libs.kotlin.coroutines.core,
        libs.kotlin.datetime,
        libs.napier,
    )
}
```

- [ ] **Step 2: `shared/core-navigation/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.navigation,
        libs.compose.navigation.material,
        libs.androidx.lifecycle.runtimeCompose,
        libs.compose.multiplatform.material3,
        projects.shared.common,
        projects.shared.designSystem,
    )
}
```

- [ ] **Step 3: `shared/core-database/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.dependencies
import extensions.implementation
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
```

Примечание: при новом KMP-Android-плагине имена KSP-конфигураций могут отличаться.
Если `kspAndroid` не резолвится — взять фактические имена из вывода
`./gradlew :shared:core-database:dependencies` и поправить. Done-условие — модуль
собирается в Task 9.

- [ ] **Step 4: `shared/design-system/build.gradle.kts`**

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
    namespace = "dev.nonoxy.core.design"
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
    nameOfResClass = "DesignSystemRes"
}
```

Примечание: `design-system` остаётся на Compose Resources до Фазы 6 (миграция на
moko-resources). Здесь только перевод на новый API плагинов.

- [ ] **Step 5: `shared/template-module/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.template"
}
```

- [ ] **Step 6: `shared/feature-rooms/api/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        projects.shared.common,
    )
}
```

- [ ] **Step 7: `shared/feature-rooms/impl/build.gradle.kts`**

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
    namespace = "dev.nonoxy.feature.rooms.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.coreDatabase,
        projects.shared.designSystem,
        projects.shared.featureRooms.api,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
```

- [ ] **Step 8: `shared/feature-add-room/api/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        projects.shared.common,
        projects.shared.featureRooms.api,
    )
}
```

- [ ] **Step 9: `shared/feature-add-room/impl/build.gradle.kts`**

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
    namespace = "dev.nonoxy.feature.add_room.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.designSystem,
        projects.shared.featureRooms.api,
        projects.shared.featureAddRoom.api,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
```

- [ ] **Step 10: `shared/feature-manage-students/api/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.api"
}

commonMainDependencies {
    implementations(
        projects.shared.common,
    )
}
```

- [ ] **Step 11: `shared/feature-manage-students/impl/build.gradle.kts`**

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
    namespace = "dev.nonoxy.feature.manage_students.impl"
}

commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.compose.icons.core,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.designSystem,
        projects.shared.featureRooms.api,
        projects.shared.featureManageStudents.api,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
```

(Дубликат `libs.compose.icons.core` из исходного файла устранён.)

- [ ] **Step 12: Commit**

```bash
git add shared/
git commit -m "Migrate library modules to new convention plugin API"
```

---

## Task 8: Перевести composeApp на прямое применение плагинов

`composeApp` — `androidApplication`, convention-плагин `kmp-library` неприменим.
В этой фазе модуль переводится на прямое применение плагинов с инлайн-конфигурацией
Android-приложения, iOS-таргетов и XCFramework. Полноценный split на `:android:app` +
`:shared:main` — Фаза 7.

**Files:**
- Rewrite: `composeApp/build.gradle.kts`

- [ ] **Step 1: Переписать `composeApp/build.gradle.kts`**

```kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    jvmToolchain(libs.versions.javaVersion.get().toInt())

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.versions.javaVersion.get()))
        }
    }

    val xcFramework = XCFramework("ComposeApp")
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
            xcFramework.add(this)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.kotlin.coroutines.android)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            implementation(compose.preview)
        }
        commonMain.dependencies {
            implementation(libs.kotlin.coroutines.core)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.multiplatform.navigation)
            implementation(libs.compose.navigation.material)
            implementation(libs.room.runtime)
            implementation(libs.koin.core)
            implementation(libs.koin.composeMultiplatform)
            implementation(libs.koin.composeMultiplatform.viewmodelNavigation)
            implementation(libs.napier)

            implementation(projects.shared.designSystem)
            implementation(projects.shared.coreNavigation)
            implementation(projects.shared.coreDatabase)
            implementation(projects.shared.featureRooms.impl)
            implementation(projects.shared.featureAddRoom.impl)
            implementation(projects.shared.featureManageStudents.impl)
        }
    }
}

android {
    namespace = "dev.nonoxy.residetrack"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.nonoxy.residetrack"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.0.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
```

Примечание: `compileSdk = 36` требует свежего Android SDK Platform 36. Если он не
установлен — поставить через SDK Manager или временно вернуть `compileSdk = 35`
(выровняется в Фазе 7).

- [ ] **Step 2: Commit**

```bash
git add composeApp/build.gradle.kts
git commit -m "Migrate composeApp build script to direct plugin application"
```

---

## Task 9: Проверить сборку и зафиксировать фазу

- [ ] **Step 1: Sync и сборка Android-приложения**

Run: `./gradlew :composeApp:assembleDebug`
Expected: `BUILD SUCCESSFUL`.

Типовые проблемы и действия:
- несовпадение KSP/Kotlin — поправить `ksp` в `libs.versions.toml`;
- Room несовместим с Kotlin 2.3.10 — поднять `room`;
- имена KSP-конфигураций в `core-database` — взять фактические из
  `./gradlew :shared:core-database:dependencies`;
- отсутствует Android SDK Platform 36 — установить или временно `compileSdk = 35`;
- ошибки нового KMP-Android-плагина по конкретному модулю — сверить его
  `build.gradle.kts` с аналогичным модулем KMMTemplate.

- [ ] **Step 2: Сборка iOS-фреймворка**

Run: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Detekt**

Run: `./gradlew detekt`
Expected: `BUILD SUCCESSFUL`. Detekt с `autoCorrect = true` сам исправит форматирование;
закоммитить изменения, если detekt их внёс.

- [ ] **Step 4: Финальный commit фазы (если detekt что-то поправил)**

```bash
git add -A
git commit -m "Apply detekt autocorrect after build-logic migration"
```

---

## Definition of Done (Фаза 1)

- `gradle/libs.versions.toml` соответствует структуре KMMTemplate, версии бампнуты,
  добавлены библиотеки MVIKotlin / moko-resources / moko-mvvm.
- `build-logic` (переименован из `buildLogic`) содержит набор convention plugins
  KMMTemplate; `KmpFeatureSetupPlugin` создан, но не применён.
- Все library-модули используют `androidLibraryConfig {}` и новый KMP-Android-плагин.
- `composeApp` собирается под Android и линкует iOS-фреймворк.
- `./gradlew :composeApp:assembleDebug`, `linkDebugFrameworkIosSimulatorArm64` и
  `detekt` проходят успешно.
- Топология модулей не изменилась (фичи 2-модульные, app-модуль единый).

## Out of Scope (последующие фазы)

- Новые core-модули (`core-mvikotlin`, `core-presentation`, `core-domain`,
  `common-ui`, `common-resources`) — Фаза 2.
- Применение `kmp-feature-setup` к фичам — Фаза 4.
- Split `composeApp` → `:android:app` + `:shared:main`, копирование
  `tasks/`/`utils/AppVersion.kt`/`valueSource/` из KMMTemplate — Фаза 7.
- Удаление `:shared:template-module`, добавление `module-templates/` — отдельным шагом.
