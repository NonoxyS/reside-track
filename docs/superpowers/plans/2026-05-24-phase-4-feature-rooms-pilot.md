# Phase 4: Пилот feature-rooms → 4 модуля + MVIKotlin — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Разделить `:shared:feature-rooms` на 4 модуля (api/impl/presentation/ui) и перевести MVI с самописного `BaseViewModel<State, Event, Action>` на MVIKotlin Store/Executor/Reducer по эталону KMMTemplate `feature-demo-first`. Поведение приложения не меняется.

**Architecture:** Структурный рефакторинг одной фичи + малый pre-flight для отсутствующей инфраструктуры из Фазы 2 (`CoroutineDispatchers` + регистрация core-модулей в Koin). Стратегия — "create new alongside old, then remove old": создаём новые модули и файлы рядом со старыми (build на каждом шаге зелёный), потом переключаем `:shared:main` на новые точки входа, потом удаляем мёртвый код в `impl`. Пакеты во всей фиче приводятся к KMMTemplate-стилю с суффиксом модуля (`.api.*`, `.impl.*`). `RoomsStore.State` и `Intent` чистятся от мёртвых полей.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, MVIKotlin 4.3.0, Koin 4.1.1, kotlinx.collections.immutable, Gradle convention plugins (`kmpFeatureSetup`).

---

## Контекст и границы фазы

**Что делаем (спека, разделы 8, 9 и 14, пункт 4):**
- `:shared:feature-rooms` → 4 модуля: `api`, `impl`, `presentation`, `ui` (в Фазах 1–3 жили только `api` и `impl`).
- Все feature-build-файлы переходят на convention-plugin `kmpFeatureSetup` (он автоматически разводит зависимости по типу модуля).
- Самописный MVI `BaseViewModel<State, Event, Action>` → MVIKotlin Store: `Intent` (бывшие `RoomsEvent`), `State` (бывший `RoomsViewState`, очищенный от мёртвых полей), `Label` (бывшие `RoomsAction`); `Executor` + `Reducer` + `StoreFactory` + `SimpleBootstrapper`.
- UI-модели (`UiRoom`, `UiStudent`, их мапперы) и Screen API (`composableRoomsScreen` / `navigateToRoomsScreen`) переезжают в новые модули по правилам KMMTemplate.
- Пакеты внутри фичи получают суффикс модуля: `dev.nonoxy.feature.rooms.api.*`, `dev.nonoxy.feature.rooms.impl.*`. `presentation.*` и `ui.*` сохраняют имена (совпадают с KMMTemplate).
- Pre-flight: достраиваются забытые в Фазе 2 части — `CoroutineDispatchers` интерфейс в `:shared:common` и регистрация `commonModule` + `coreDomainModule` + `coreMVIKotlinModule` в `shared/main/di/Koin.kt`. Без этого DI MVIKotlin Store в KMMTemplate-стиле не собирается.

**Чего НЕ делаем в этой фазе:**
- Миграцию `feature-add-room` и `feature-manage-students` — это Фаза 5. Их `*ViewModel` остаются на старом `dev.nonoxy.common.presentation.BaseViewModel` (=> файл `shared/common/.../presentation/BaseViewModel.kt` тоже остаётся, удаляется в Фазе 5).
- Compose Resources → moko-resources — это Фаза 6.
- Переименование пакетов `dev.nonoxy.*` → `dev.nonoxy.residetrack.*` — это Фаза 8. В Фазе 4 префикс остаётся `dev.nonoxy.feature.rooms.*`.

**Зафиксированные пользователем решения:**
- Phase 2 carry-overs (`CoroutineDispatchers` + регистрация модулей в Koin) **включаем в Фазу 4 как pre-flight** (Task 1).
- `RoomsStore.State` и `Intent` — **чистые** (мёртвые поля и `OnTabSelect` удаляются). `Preview` в `RoomsScreenDetails` чистится соответственно.
- Пакеты — **полная перепаковка под KMMTemplate** (`.api.*`, `.impl.*`); 5 импортов в `feature-add-room` и `feature-manage-students` правятся механически.

**Поведенческое отличие после фазы:** наблюдаемого пользовательского изменения нет. Внутренний side-effect: `RoomsViewModel.obtainEvent(RoomsEvent.OnTabSelect(...))` сейчас бросал бы `NotImplementedError` (`TODO()`); в новом коде такого Intent нет вообще. UI этот путь не использует — поэтому пользователь разницы не увидит.

**Контрольная точка после фазы:**
- `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug` зелёный.
- `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64` зелёный.
- `./gradlew detekt` зелёный.
- Ручная сверка экрана Rooms на Android (`dev/debug` flavor): открывается, показывает список комнат сгруппированный по этажам, клик по комнате открывает Manage Students, клик по «Добавить комнату» открывает Add Room. Floor tabs работают (свайп/тап).

## Целевая структура модуля feature-rooms

```
shared/feature-rooms/
├── api/                                       (существует, перепаковывается)
│   ├── build.gradle.kts                       (kmpFeatureSetup)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/
│       ├── models/Room.kt                      (мигрирует из api/models/)
│       ├── models/Student.kt                   (мигрирует из api/models/)
│       ├── repository/RoomsRepository.kt       (мигрирует из api/repository/)
│       └── store/RoomsStore.kt                 (НОВЫЙ — Intent/State/Label)
│
├── impl/                                       (существует, гутается)
│   ├── build.gradle.kts                       (kmpFeatureSetup, без Compose)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/
│       ├── data/RoomsRepositoryImpl.kt         (мигрирует из impl/data/)
│       ├── data/mappers/RoomMapper.kt          (мигрирует из impl/data/mappers/)
│       ├── data/mappers/StudentMapper.kt       (мигрирует из impl/data/mappers/)
│       ├── domain/RoomsExecutor.kt             (НОВЫЙ)
│       ├── domain/RoomsReducer.kt              (НОВЫЙ)
│       ├── domain/RoomsStoreFactory.kt         (НОВЫЙ, содержит Action + Message)
│       └── di/FeatureRoomsImplModule.kt        (переписан)
│
├── presentation/                               (НОВЫЙ модуль)
│   ├── build.gradle.kts                       (kmpFeatureSetup)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/
│       ├── RoomsViewModel.kt                   (НОВЫЙ — BaseViewModel<State, Label>)
│       ├── models/UiRoomsState.kt              (НОВЫЙ)
│       ├── models/UiRoomsLabel.kt              (НОВЫЙ)
│       ├── models/UiRoom.kt                    (мигрирует из impl/ui/models/)
│       ├── models/UiStudent.kt                 (мигрирует из impl/ui/models/)
│       ├── mappers/UiRoomsStateMapper.kt       (НОВЫЙ — Store.State → UiRoomsState)
│       ├── mappers/UiRoomsLabelMapper.kt       (НОВЫЙ — Store.Label → UiRoomsLabel)
│       ├── mappers/UiRoomMapper.kt             (мигрирует из impl/ui/mappers/)
│       ├── mappers/UiStudentMapper.kt          (мигрирует из impl/ui/mappers/)
│       └── di/FeatureRoomsPresentationModule.kt (НОВЫЙ)
│
└── ui/                                         (НОВЫЙ модуль)
    ├── build.gradle.kts                       (kmpFeatureSetup + composeMultiplatformSetup)
    └── src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/
        ├── RoomsScreen.kt                      (мигрирует из impl/ui/RoomsScreen.kt, адаптирован под BaseViewModel)
        ├── views/RoomList.kt                   (мигрирует)
        ├── views/RoomListItem.kt               (мигрирует)
        ├── views/RoomsScreenDetails.kt         (мигрирует + Preview очищен от мёртвых полей)
        ├── views/RoomsTabRow.kt                (мигрирует)
        ├── views/RoomsTopBar.kt                (мигрирует)
        ├── views/ScrollableTabRowImpl.kt       (мигрирует)
        └── api/FeatureRoomsScreenApi.kt        (мигрирует из impl/presentation/navigation/FeatureRoomsNavigation.kt)
```

## Карта затрагиваемых файлов

**Создаются:**
- Все файлы под `presentation/` и `ui/` (см. структуру выше).
- `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/store/RoomsStore.kt`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/{RoomsExecutor.kt,RoomsReducer.kt,RoomsStoreFactory.kt}`
- `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchers.kt`
- `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchersImpl.kt`
- `shared/common/src/commonMain/kotlin/dev/nonoxy/common/di/CommonModule.kt`

**Перепаковываются (git mv с обновлением `package`):**
- `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/{models,repository}` → `.../feature/rooms/api/{models,repository}`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/{data,di}` → `.../feature/rooms/impl/{data,di}`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/models/{UiRoom.kt,UiStudent.kt}` → `shared/feature-rooms/presentation/src/.../presentation/models/`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/mappers/{UiRoomMapper.kt,UiStudentMapper.kt}` → `shared/feature-rooms/presentation/src/.../presentation/mappers/`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/{RoomsScreen.kt,views/*}` → `shared/feature-rooms/ui/src/.../ui/`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/navigation/FeatureRoomsNavigation.kt` → `shared/feature-rooms/ui/src/.../ui/api/FeatureRoomsScreenApi.kt`

**Удаляются:**
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/{RoomsEvent.kt,RoomsAction.kt,RoomsViewState.kt}`
- Пустые директории `shared/feature-rooms/impl/src/.../{presentation,ui}/` (по факту перепаковки)

**Модифицируются (без переноса):**
- `shared/feature-rooms/api/build.gradle.kts` (Task 3)
- `shared/feature-rooms/impl/build.gradle.kts` (Task 8)
- `settings.gradle.kts` (Task 2)
- `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/Dispatchers.kt` (Task 1, добавляются `defaultDispatcher`/`unconfinedDispatcher`)
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` (Task 1 + Task 7)
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt` (Task 7)
- `shared/feature-add-room/impl/src/.../presentation/AddRoomViewModel.kt` (Task 3 — 2 import-строки)
- `shared/feature-manage-students/impl/src/.../presentation/ManageStudentsViewModel.kt` (Task 3 — 2 import-строки)
- `shared/feature-manage-students/impl/src/.../ui/mappers/{UiRoomMapper.kt,UiStudentMapper.kt}` (Task 3 — по 1 import-строке)
- `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` (Task 10 — статус)

---

### Task 1: Pre-flight — Phase 2 carry-overs (CoroutineDispatchers + регистрация core-модулей в Koin)

**Files:**
- Create: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchers.kt`
- Create: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchersImpl.kt`
- Create: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/di/CommonModule.kt`
- Modify: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/Dispatchers.kt`
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`

**Контекст.** Фаза 2 была помечена готовой, но три обязательных элемента из KMMTemplate в `:shared:common` отсутствуют: интерфейс `CoroutineDispatchers`, его реализация и `commonModule`. Без них DI MVIKotlin Store в стиле KMMTemplate (`mainDispatcher = get<CoroutineDispatchers>().main`) не соберётся. Также не зарегистрированы `coreDomainModule` и `coreMVIKotlinModule` в `shared/main/di/Koin.kt` — без них `StoreFactory` и `Json` нельзя получить через `get()`.

- [ ] **Step 1: Расширить `Dispatchers.kt` (default + unconfined)**

Полное новое содержимое `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/Dispatchers.kt`:

```kotlin
package dev.nonoxy.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
val mainImmediateDispatcher: CoroutineDispatcher = Dispatchers.Main.immediate
val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
val unconfinedDispatcher: CoroutineDispatcher = Dispatchers.Unconfined
```

(Явные типы — паритет с KMMTemplate `Dispatchers.kt`.)

- [ ] **Step 2: Создать `CoroutineDispatchers.kt` (интерфейс)**

Файл `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchers.kt`:

```kotlin
package dev.nonoxy.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher

interface CoroutineDispatchers {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}
```

- [ ] **Step 3: Создать `CoroutineDispatchersImpl.kt`**

Файл `shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchersImpl.kt`:

```kotlin
package dev.nonoxy.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher

internal class CoroutineDispatchersImpl : CoroutineDispatchers {
    override val io: CoroutineDispatcher
        get() = ioDispatcher

    override val main: CoroutineDispatcher
        get() = mainDispatcher

    override val default: CoroutineDispatcher
        get() = defaultDispatcher

    override val unconfined: CoroutineDispatcher
        get() = unconfinedDispatcher
}
```

- [ ] **Step 4: Создать `CommonModule.kt`**

Файл `shared/common/src/commonMain/kotlin/dev/nonoxy/common/di/CommonModule.kt`:

```kotlin
package dev.nonoxy.common.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.common.coroutines.CoroutineDispatchersImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val commonModule = module {

    singleOf<CoroutineDispatchers>(::CoroutineDispatchersImpl)
}
```

- [ ] **Step 5: Зарегистрировать `commonModule`, `coreDomainModule`, `coreMVIKotlinModule` в `shared/main/di/Koin.kt`**

Полное новое содержимое `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

```kotlin
package dev.nonoxy.residetrack.di

import dev.nonoxy.common.di.commonModule
import dev.nonoxy.core.database.di.coreDatabaseModule
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
import dev.nonoxy.residetrack.core.domain.di.coreDomainModule
import dev.nonoxy.residetrack.core.mvikotlin.di.coreMVIKotlinModule
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    Napier.d(message = "initKoin")
    startKoin {
        appDeclaration()
        modules(
            commonModule,

            coreDomainModule,
            coreMVIKotlinModule,
            coreDatabaseModule,

            featureRoomsImplModule,
            featureAddRoomImplModule,
            featureManageStudentsImplModule,
        )
    }
}
```

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/Dispatchers.kt \
        shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchers.kt \
        shared/common/src/commonMain/kotlin/dev/nonoxy/common/coroutines/CoroutineDispatchersImpl.kt \
        shared/common/src/commonMain/kotlin/dev/nonoxy/common/di/CommonModule.kt \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt
git commit -m "Phase 4 pre-flight: add CoroutineDispatchers + register core modules in Koin"
```

---

### Task 2: Settings.gradle.kts + пустые submodule-каркасы (presentation, ui)

**Files:**
- Modify: `settings.gradle.kts`
- Create: `shared/feature-rooms/presentation/build.gradle.kts`
- Create: `shared/feature-rooms/presentation/src/commonMain/kotlin/.gitkeep`
- Create: `shared/feature-rooms/ui/build.gradle.kts`
- Create: `shared/feature-rooms/ui/src/commonMain/kotlin/.gitkeep`

**Контекст.** Создаём пустые директории новых модулей с минимальными `build.gradle.kts` (только `kmpFeatureSetup` + namespace). Это безопасно: Gradle регистрирует модули, конвенция-плагин видит `presentation`/`ui` имя проекта и навешивает правильные зависимости. Source-set пустой, компиляция Kotlin для commonMain ничего не делает. Файлы `.gitkeep` нужны, чтобы пустые директории попали в git.

- [ ] **Step 1: Добавить два include в `settings.gradle.kts`**

В существующем блоке `include(...)` (после блока `feature-rooms`):

Найти:
```
    ":shared:feature-rooms:api",
    ":shared:feature-rooms:impl",
```

Заменить на:
```
    ":shared:feature-rooms:api",
    ":shared:feature-rooms:impl",
    ":shared:feature-rooms:presentation",
    ":shared:feature-rooms:ui",
```

- [ ] **Step 2: Создать `shared/feature-rooms/presentation/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
```

(`libs.kotlin.immutableCollections` нужен для `ImmutableList`/`ImmutableMap` в `UiRoomsState` и `UiRoom`. KMMTemplate `feature-demo-first/presentation` обходится без него — у него только примитивные типы в state.)

- [ ] **Step 3: Создать `shared/feature-rooms/presentation/src/commonMain/kotlin/.gitkeep`**

Пустой файл (нужен для git, чтобы зафиксировать пустую директорию source-set; в Шаге 2 plugin требует наличия source-root):

```
```

- [ ] **Step 4: Создать `shared/feature-rooms/ui/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.ui"
}
```

(Без `commonMainDependencies` — `kmpFeatureSetup` автоматически даёт `composeBundle`, `core-navigation`, `common-resources`, `common-ui`, `koin-composeMP-viewmodel`, `moko-resources-compose`, presentation-модуль и т.д. Compose Resources блок `compose.resources {...}` не нужен — feature-rooms не пользуется строками из Compose Resources.)

- [ ] **Step 5: Создать `shared/feature-rooms/ui/src/commonMain/kotlin/.gitkeep`**

Пустой файл.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :shared:feature-rooms:presentation:assemble :shared:feature-rooms:ui:assemble`
Expected: BUILD SUCCESSFUL (модули конфигурируются, компилируется пустой source-set).

Затем полная проверка:
Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL (новые модули в `:shared:main` подключаются автоматически через reflection в `addFeatureModules()`, но они пусты — никаких символов не добавляют).

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts \
        shared/feature-rooms/presentation/ \
        shared/feature-rooms/ui/
git commit -m "Phase 4: scaffold empty feature-rooms presentation+ui submodules"
```

---

### Task 3: Перепаковка api в `.api.*` + RoomsStore + переход на `kmpFeatureSetup`

**Files:**
- Move: `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/models/` → `.../feature/rooms/api/models/`
- Move: `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/repository/` → `.../feature/rooms/api/repository/`
- Modify: `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/models/Room.kt` (package)
- Modify: `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/models/Student.kt` (package)
- Modify: `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/repository/RoomsRepository.kt` (package + import)
- Create: `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/store/RoomsStore.kt`
- Modify: `shared/feature-rooms/api/build.gradle.kts`
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/data/RoomsRepositoryImpl.kt` (imports)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/data/mappers/RoomMapper.kt` (imports)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/data/mappers/StudentMapper.kt` (imports)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt` (imports — старый ViewModel пока остаётся жив)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsViewState.kt` (нет ничего из api — пропускаем)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di/FeatureRoomsImplModule.kt` (imports)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/mappers/UiRoomMapper.kt` (imports)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/mappers/UiStudentMapper.kt` (imports)
- Modify: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/AddRoomViewModel.kt` (2 import-строки)
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt` (2 import-строки)
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/mappers/UiRoomMapper.kt` (1 import)
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/mappers/UiStudentMapper.kt` (1 import)

**Контекст.** Перенос domain-моделей и repository-интерфейса под суффикс `.api.*` (KMMTemplate compliance, см. memory `kmmtemplate-full-compliance`). После переноса добавляется новый `RoomsStore` в `api/store/` и `build.gradle.kts` переключается с `kmpLibrary` на `kmpFeatureSetup` (он автоматически приносит compose-runtime, core-domain, core-mvikotlin, что требуется для `Store` и стабильности).

- [ ] **Step 1: Переместить директории `models/` и `repository/` под `api/`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/models \
       shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/models
git mv shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/repository \
       shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/repository
```

- [ ] **Step 2: Поправить package в трёх файлах api**

Файл `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/models/Room.kt` — заменить первую строку:

Старое:
```kotlin
package dev.nonoxy.feature.rooms.models
```
Новое:
```kotlin
package dev.nonoxy.feature.rooms.api.models
```

Файл `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/models/Student.kt` — заменить:

Старое:
```kotlin
package dev.nonoxy.feature.rooms.models
```
Новое:
```kotlin
package dev.nonoxy.feature.rooms.api.models
```

Файл `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/repository/RoomsRepository.kt` — заменить package и обновить импорт `Room`:

Старое (первые 4 строки):
```kotlin
package dev.nonoxy.feature.rooms.repository

import dev.nonoxy.feature.rooms.models.Room
import kotlinx.coroutines.flow.Flow
```
Новое:
```kotlin
package dev.nonoxy.feature.rooms.api.repository

import dev.nonoxy.feature.rooms.api.models.Room
import kotlinx.coroutines.flow.Flow
```

- [ ] **Step 3: Создать `RoomsStore.kt`**

Файл `shared/feature-rooms/api/src/commonMain/kotlin/dev/nonoxy/feature/rooms/api/store/RoomsStore.kt`:

```kotlin
package dev.nonoxy.feature.rooms.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.feature.rooms.api.store.RoomsStore.State

interface RoomsStore : Store<Intent, State, Label> {

    data class State(
        val roomsOnFloor: Map<Int, List<Room>> = emptyMap(),
    )

    sealed interface Intent {
        data class OnRoomClick(val roomId: Long) : Intent
        data object OnAddRoomClick : Intent
    }

    sealed interface Label {
        data object NavigateToAddRoomScreen : Label
        data class NavigateToManageStudentsExistingRoom(val roomId: String) : Label
    }
}
```

- [ ] **Step 4: Переписать `shared/feature-rooms/api/build.gradle.kts`**

Полное новое содержимое:

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.api"
}
```

(`kmpFeatureSetup` в режиме api-модуля автоматически даёт compose-compiler + compose-runtime + core-domain + core-mvikotlin. `kotlin.datetime` и `kotlin.coroutines.core` приходят транзитивно от базового `kmpLibrary`, который `kmpFeatureSetup` применяет внутри.)

- [ ] **Step 5: Обновить импорты во ВСЕХ consumer-файлах (одной массовой заменой)**

В фиче feature-rooms (impl/), feature-add-room, feature-manage-students все импорты `dev.nonoxy.feature.rooms.models.*` → `dev.nonoxy.feature.rooms.api.models.*` и `dev.nonoxy.feature.rooms.repository.*` → `dev.nonoxy.feature.rooms.api.repository.*`.

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
find shared/feature-rooms/impl shared/feature-add-room shared/feature-manage-students \
     -type f -name '*.kt' \
     -not -path '*/build/*' \
     -exec sed -i '' \
       -e 's|dev\.nonoxy\.feature\.rooms\.models\.|dev.nonoxy.feature.rooms.api.models.|g' \
       -e 's|dev\.nonoxy\.feature\.rooms\.repository\.|dev.nonoxy.feature.rooms.api.repository.|g' \
       {} +
```

После выполнения проверьте, что больше нигде не осталось старых путей:

```bash
grep -rn "dev\.nonoxy\.feature\.rooms\.models\.\|dev\.nonoxy\.feature\.rooms\.repository\." \
     shared --include='*.kt' | grep -v build/
```
Expected: пусто.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

**Возможный блокер:** `kmpFeatureSetup` ожидает наличия source-root, но api теперь содержит только `dev/nonoxy/feature/rooms/api/{models,repository,store}/`. Это нормально — старый префиксный path остался физически (просто директория `dev/nonoxy/feature/rooms/` сама не пуста). Проблем быть не должно.

- [ ] **Step 7: Commit**

```bash
git add shared/feature-rooms/api shared/feature-rooms/impl shared/feature-add-room shared/feature-manage-students
git commit -m "Phase 4: repackage feature-rooms api under .api.* and introduce RoomsStore"
```

(Pre-commit hook прогонит detekt по затронутым файлам. Если что-то падает — сначала фикс, потом commit.)

---

### Task 4: impl/domain — Executor + Reducer + StoreFactory (коэкзистенс со старым кодом)

**Files:**
- Create: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/RoomsExecutor.kt`
- Create: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/RoomsReducer.kt`
- Create: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/RoomsStoreFactory.kt`

**Контекст.** Новые файлы создаются под `.impl.domain.*` пакетом, рядом со старым кодом в `dev.nonoxy.feature.rooms.{data,presentation,ui,di}.*`. Они пока никем не используются — нужны только чтобы компилироваться. Связь с DI и старым ViewModel будет сделана в следующих задачах.

KMMTemplate-аналог: `shared/feature-demo-first/impl/src/commonMain/kotlin/dev/nonoxy/kmmtemplate/feature/demo/first/impl/domain/{DemoFeatureFirstExecutor,DemoFeatureFirstReducer,DemoFeatureFirstStoreFactory}.kt`.

Executor получает `Action.LoadInitial` от `SimpleBootstrapper`, грузит данные через `RoomsRepository`, диспатчит `Message`. Reducer применяет Message. StoreFactory собирает всё вместе.

- [ ] **Step 1: Создать `RoomsStoreFactory.kt`**

Файл `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/RoomsStoreFactory.kt`:

```kotlin
package dev.nonoxy.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.SimpleBootstrapper
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.feature.rooms.api.store.RoomsStore.State
import kotlinx.coroutines.CoroutineDispatcher

internal class RoomsStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(): RoomsStore =
        object :
            RoomsStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = RoomsStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadInitial),
                executorFactory = {
                    RoomsExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                    )
                },
                reducer = RoomsReducer()
            ) {}

    internal sealed interface Action {
        data object LoadInitial : Action
    }

    internal sealed interface Message {
        data class SetRoomsOnFloor(val roomsOnFloor: Map<Int, List<Room>>) : Message
    }
}
```

- [ ] **Step 2: Создать `RoomsReducer.kt`**

Файл `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/RoomsReducer.kt`:

```kotlin
package dev.nonoxy.feature.rooms.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.feature.rooms.impl.domain.RoomsStoreFactory.Message

internal class RoomsReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetRoomsOnFloor -> copy(roomsOnFloor = msg.roomsOnFloor)
    }
}
```

- [ ] **Step 3: Создать `RoomsExecutor.kt`**

Файл `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain/RoomsExecutor.kt`:

```kotlin
package dev.nonoxy.feature.rooms.impl.domain

import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Label
import dev.nonoxy.feature.rooms.api.store.RoomsStore.State
import dev.nonoxy.feature.rooms.impl.domain.RoomsStoreFactory.Action
import dev.nonoxy.feature.rooms.impl.domain.RoomsStoreFactory.Message
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.coroutines.CoroutineDispatcher

internal class RoomsExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadInitial -> loadRoomsData()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnRoomClick -> publish(
                Label.NavigateToManageStudentsExistingRoom(roomId = intent.roomId.toString())
            )
            Intent.OnAddRoomClick -> publish(Label.NavigateToAddRoomScreen)
        }
    }

    private suspend fun loadRoomsData() {
        val rooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        if (rooms.isNotEmpty()) {
            val grouped = rooms.groupBy { room -> room.floorNumber }
            dispatch(Message.SetRoomsOnFloor(roomsOnFloor = grouped))
        }
    }
}
```

- [ ] **Step 4: Проверка сборки**

Run: `./gradlew :shared:feature-rooms:impl:compileKotlinAndroid :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. Старый код в impl продолжает работать; новые файлы под `impl.domain.*` компилируются, но ещё никем не используются.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/domain
git commit -m "Phase 4: add MVIKotlin domain layer (Executor, Reducer, StoreFactory) for feature-rooms"
```

---

### Task 5: Presentation-модуль — модели, мапперы, ViewModel, DI

**Files:**
- Move: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/models/UiRoom.kt` → `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiRoom.kt`
- Move: `.../impl/.../ui/models/UiStudent.kt` → `.../presentation/.../presentation/models/UiStudent.kt`
- Move: `.../impl/.../ui/mappers/UiRoomMapper.kt` → `.../presentation/.../presentation/mappers/UiRoomMapper.kt`
- Move: `.../impl/.../ui/mappers/UiStudentMapper.kt` → `.../presentation/.../presentation/mappers/UiStudentMapper.kt`
- Modify: каждый из четырёх перенесённых файлов (package + внутренние импорты)
- Create: `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiRoomsState.kt`
- Create: `.../presentation/models/UiRoomsLabel.kt`
- Create: `.../presentation/mappers/UiRoomsStateMapper.kt`
- Create: `.../presentation/mappers/UiRoomsLabelMapper.kt`
- Create: `.../presentation/RoomsViewModel.kt`
- Create: `.../presentation/di/FeatureRoomsPresentationModule.kt`
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt` (адаптировать импорт `UiRoomMapper` под новый путь — старый ViewModel ещё жив, но теперь импортирует из presentation-модуля)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di/FeatureRoomsImplModule.kt` (обновить импорты `UiRoomMapper`/`UiStudentMapper` и т.д.)
- Modify: `.../impl/.../ui/views/RoomList.kt` (импорт `UiRoom`)
- Modify: `.../impl/.../ui/views/RoomListItem.kt` (импорты `UiRoom`, `UiStudent`)
- Modify: `.../impl/.../ui/views/RoomsScreenDetails.kt` (импорты `UiRoom`, `UiStudent`)
- Delete: пустую директорию `shared/feature-rooms/presentation/src/commonMain/kotlin/.gitkeep`

**Контекст.** Главный шаг переноса: `UiRoom`/`UiStudent` и их мапперы — это **presentation-layer** типы (по KMMTemplate), а не UI. Они переезжают в новый presentation-модуль. Заодно создаются совершенно новые presentation-классы: `UiRoomsState`, `UiRoomsLabel`, мапперы `Store.State → UiRoomsState` и `Store.Label → UiRoomsLabel`, сам `RoomsViewModel` на `BaseViewModel<State, Label>`, и DI-модуль.

Старый `RoomsViewModel.kt` в `impl/presentation/` остаётся живым (он будет удалён в Task 7). После Task 5 в проекте сосуществуют:
- Старый: `dev.nonoxy.feature.rooms.presentation.RoomsViewModel` (impl-модуль) — пользуется самописным `BaseViewModel<S,E,A>`.
- Новый: `dev.nonoxy.feature.rooms.presentation.RoomsViewModel` (presentation-модуль) — на `BaseViewModel<State,Label>`.

Они в **разных Gradle-модулях**, но в **одинаковом пакете**. Это сработает, потому что в Kotlin (JVM/native) разрешение классов идёт по classpath, и одинаковые FQN из разных модулей дадут конфликт только при попытке использовать оба сразу. `:shared:main` пока подключает `feature-rooms:impl` (старый ViewModel в Koin), новый presentation-модуль он тоже видит (через `addFeatureModules()`), но никто не вызывает новый `RoomsViewModel`. После Task 7 импорт старого ViewModel пропадёт, потом удалим старый файл.

**Внимание:** дубликат FQN — потенциальный риск. Если линкер запутается на одной из платформ, fallback — временно переименовать классы на новые `RoomsViewModelV2`, потом откатить в Task 7. На практике для commonMain с двумя разными модулями этого не должно произойти (один кладёт klib в `feature-rooms:impl.klib`, другой — в `feature-rooms:presentation.klib`; `:shared:main` зависит от обоих, но конкретное использование импорта в `:shared:main/Koin.kt` идёт через DSL `featureRoomsImplModule` — без прямой ссылки на класс `RoomsViewModel`). Тем не менее, если шаг сборки в этом задании падает с "duplicate class" — ОСТАНОВИТЬСЯ и сообщить, не маскировать переименованием втихую.

- [ ] **Step 1: Удалить плейсхолдер `.gitkeep` в presentation source-set**

```bash
rm shared/feature-rooms/presentation/src/commonMain/kotlin/.gitkeep
```

- [ ] **Step 2: Перенести `UiRoom`, `UiStudent` под presentation/models/**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
mkdir -p shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/models/UiRoom.kt \
       shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiRoom.kt
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/models/UiStudent.kt \
       shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiStudent.kt
```

Затем поправить пакет в обоих файлах.

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiRoom.kt`:

Старая первая строка:
```kotlin
package dev.nonoxy.feature.rooms.ui.models
```
Новая:
```kotlin
package dev.nonoxy.feature.rooms.presentation.models
```

(тело файла остаётся прежним: `internal data class UiRoom(...)`)

Аналогично `UiStudent.kt`:
Старая:
```kotlin
package dev.nonoxy.feature.rooms.ui.models
```
Новая:
```kotlin
package dev.nonoxy.feature.rooms.presentation.models
```

- [ ] **Step 3: Перенести мапперы `UiRoomMapper`, `UiStudentMapper`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
mkdir -p shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/mappers/UiRoomMapper.kt \
       shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers/UiRoomMapper.kt
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/mappers/UiStudentMapper.kt \
       shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers/UiStudentMapper.kt
```

Поправить пакет и внутренние импорты `UiRoom`/`UiStudent` (они теперь в presentation.models).

Полное новое содержимое `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers/UiRoomMapper.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.presentation.models.UiRoom
import kotlinx.collections.immutable.toImmutableList

internal interface UiRoomMapper : Mapper<Room, UiRoom>

internal class UiRoomMapperImpl(
    private val studentMapper: UiStudentMapper
) : UiRoomMapper {

    override fun map(item: Room): UiRoom = with(item) {
        return UiRoom(
            id = id,
            floorNumber = floorNumber.toString(),
            roomNumber = roomNumber.toString(),
            bedsCount = bedsCount.toString(),
            students = students.let(studentMapper::map).toImmutableList()
        )
    }
}
```

Полное новое содержимое `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers/UiStudentMapper.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.rooms.api.models.Student
import dev.nonoxy.feature.rooms.presentation.models.UiStudent

internal interface UiStudentMapper : Mapper<Student, UiStudent>

internal class UiStudentMapperImpl : UiStudentMapper {

    override fun map(item: Student): UiStudent = with(item) {
        UiStudent(
            streamNumber = streamNumber.toString(),
            checkInDate = checkInDate.toString(), // ISO-8601
            checkOutDate = checkOutDate.toString(), // ISO-8601
            isCheckOutDateNearOrExpired = isCheckOutDateNearOrExpired
        )
    }
}
```

- [ ] **Step 4: Создать `UiRoomsState.kt`**

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiRoomsState.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

internal data class UiRoomsState(
    val roomsOnFloor: ImmutableMap<Int, ImmutableList<UiRoom>> = persistentMapOf(),
)
```

- [ ] **Step 5: Создать `UiRoomsLabel.kt`**

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/UiRoomsLabel.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.models

internal sealed interface UiRoomsLabel {
    data object NavigateToAddRoomScreen : UiRoomsLabel
    data class NavigateToManageStudentsExistingRoom(val roomId: String) : UiRoomsLabel
}
```

- [ ] **Step 6: Создать `UiRoomsStateMapper.kt`**

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers/UiRoomsStateMapper.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap

internal interface UiRoomsStateMapper {
    fun map(item: RoomsStore.State): UiRoomsState
}

internal class UiRoomsStateMapperImpl(
    private val roomMapper: UiRoomMapper
) : UiRoomsStateMapper {

    override fun map(item: RoomsStore.State): UiRoomsState =
        UiRoomsState(
            roomsOnFloor = item.roomsOnFloor
                .mapValues { entry -> entry.value.let(roomMapper::map).toPersistentList() }
                .toPersistentMap()
        )
}
```

(`UiRoomsStateMapper` не наследует `Mapper<From,To>`, потому что `Mapper.map(list)` нам не нужен — это однозначный mapper.)

- [ ] **Step 7: Создать `UiRoomsLabelMapper.kt`**

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/mappers/UiRoomsLabelMapper.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.mappers

import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsLabel

internal interface UiRoomsLabelMapper {
    fun map(item: RoomsStore.Label): UiRoomsLabel
}

internal class UiRoomsLabelMapperImpl : UiRoomsLabelMapper {

    override fun map(item: RoomsStore.Label): UiRoomsLabel = when (item) {
        RoomsStore.Label.NavigateToAddRoomScreen ->
            UiRoomsLabel.NavigateToAddRoomScreen

        is RoomsStore.Label.NavigateToManageStudentsExistingRoom ->
            UiRoomsLabel.NavigateToManageStudentsExistingRoom(roomId = item.roomId)
    }
}
```

- [ ] **Step 8: Создать новый `RoomsViewModel.kt`**

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.api.store.RoomsStore.Intent
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsLabelMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsStateMapper
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsState
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class RoomsViewModel internal constructor(
    private val store: RoomsStore,
    private val stateMapper: UiRoomsStateMapper,
    private val labelMapper: UiRoomsLabelMapper,
) : BaseViewModel<UiRoomsState, UiRoomsLabel>(initialState = UiRoomsState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRoomClick(roomId: Long) = store.accept(Intent.OnRoomClick(roomId = roomId))

    fun onAddRoomClick() = store.accept(Intent.OnAddRoomClick)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
```

- [ ] **Step 9: Создать `FeatureRoomsPresentationModule.kt`**

Файл `shared/feature-rooms/presentation/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/di/FeatureRoomsPresentationModule.kt`:

```kotlin
package dev.nonoxy.feature.rooms.presentation.di

import dev.nonoxy.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsLabelMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsLabelMapperImpl
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsStateMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomsStateMapperImpl
import dev.nonoxy.feature.rooms.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiStudentMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureRoomsPresentationModule = module {

    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf<UiRoomMapper>(::UiRoomMapperImpl)
    factoryOf<UiRoomsStateMapper>(::UiRoomsStateMapperImpl)
    factoryOf<UiRoomsLabelMapper>(::UiRoomsLabelMapperImpl)

    viewModelOf(::RoomsViewModel)
}
```

(Заметьте: маппер-`Impl` помечен `internal`, но `factoryOf` (Koin) использует функцию-ссылку, которая работает с internal классами в commonMain — пакет тот же.)

- [ ] **Step 10: Обновить старый `impl/.../presentation/RoomsViewModel.kt` — он импортирует `UiRoomMapper` из старого пути, который теперь пуст**

В файле `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt` заменить импорт:

Старое:
```kotlin
import dev.nonoxy.feature.rooms.ui.mappers.UiRoomMapper
```
Новое:
```kotlin
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapper
```

(Старый ViewModel теперь зависит от мапперов в новом presentation-модуле. Это нормально — он будет удалён в Task 7, мы просто латаем ему компиляцию ещё на один шаг.)

- [ ] **Step 11: Обновить старый `impl/.../di/FeatureRoomsImplModule.kt` — импорты UiRoomMapper/UiStudentMapper и их `*Impl`**

Файл `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di/FeatureRoomsImplModule.kt`.

Найти блок:
```kotlin
import dev.nonoxy.feature.rooms.ui.mappers.UiRoomMapper
import dev.nonoxy.feature.rooms.ui.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.rooms.ui.mappers.UiStudentMapper
import dev.nonoxy.feature.rooms.ui.mappers.UiStudentMapperImpl
```

Заменить на:
```kotlin
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.rooms.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.rooms.presentation.mappers.UiStudentMapperImpl
```

(DI-привязки `factory<UiStudentMapper> {...}` и `factory<UiRoomMapper> {...}` остаются, чтобы старый ViewModel в impl продолжал получать мапперы. После Task 7 эти привязки переедут в `featureRoomsPresentationModule` — уже сделано в Step 9 выше; в Task 7 удалим дубликаты из `featureRoomsImplModule`.)

**Блокер риска dupplicate-binding:** Koin будет в курсе обоих биндингов на один и тот же тип `UiStudentMapper` (в `featureRoomsImplModule` И в `featureRoomsPresentationModule`), но `featureRoomsPresentationModule` ещё не зарегистрирован в `:shared:main/Koin.kt` — это произойдёт в Task 7. До Task 7 регистрирован только `featureRoomsImplModule` (через старый код), так что конфликта нет.

- [ ] **Step 12: Обновить импорты `UiRoom`/`UiStudent` в `impl/.../ui/views/`**

Файлы:
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomList.kt`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomListItem.kt`
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsScreenDetails.kt`

Массовая замена импорта:

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
find shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui \
     -type f -name '*.kt' \
     -exec sed -i '' \
       -e 's|dev\.nonoxy\.feature\.rooms\.ui\.models\.UiRoom|dev.nonoxy.feature.rooms.presentation.models.UiRoom|g' \
       -e 's|dev\.nonoxy\.feature\.rooms\.ui\.models\.UiStudent|dev.nonoxy.feature.rooms.presentation.models.UiStudent|g' \
       {} +
```

Дополнительно — `impl/.../ui/RoomsScreen.kt` импортирует `RoomsViewModel` из `presentation/`. Импорт остаётся без изменения (оба пакета называются `dev.nonoxy.feature.rooms.presentation`, но **разные модули**: старый ViewModel лежит в impl-модуле, новый в presentation-модуле). impl при компиляции видит ОБА, но **новый RoomsViewModel** в presentation-модуле использует другую сигнатуру — `viewState()`/`viewAction()` отсутствуют, есть `state`/`label`. Если bytecode/klib линкер ругается на дубликат — придётся в новом презентационном модуле временно дать классу имя `RoomsViewModelV2` (см. Внимание в шапке Task 5). На повторюсь — это маловероятный путь.

Чтобы избежать риска, `impl/.../ui/RoomsScreen.kt` пока продолжает ссылаться на **старый** `RoomsViewModel` (он в том же impl-модуле — компилятор сам выберет local-first из commonMain). После Task 7 старый RoomsViewModel будет удалён, и `impl/.../ui/RoomsScreen.kt` тоже (мы его перенесём в ui-модуль).

- [ ] **Step 13: Гарантия — обновить `impl/build.gradle.kts` для доступа к presentation мапперам**

Файл `shared/feature-rooms/impl/build.gradle.kts` — добавить `projects.shared.featureRooms.presentation` в `implementations(...)`. Это временная зависимость (она пропадёт в Task 8). Старый ViewModel и DI в impl импортируют классы из presentation; без этой зависимости импорты не разрешатся.

Найти блок:
```kotlin
commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.coreDatabase,
        projects.shared.commonUi,
        projects.shared.featureRooms.api,
    )
}
```

Заменить на:
```kotlin
commonMainDependencies {
    implementations(
        *composeBundle,
        libs.compose.multiplatform.resources,
        libs.koin.composeMultiplatform.viewmodelNavigation,
        libs.kotlin.immutableCollections,
        projects.shared.common,
        projects.shared.coreNavigation,
        projects.shared.coreDatabase,
        projects.shared.commonUi,
        projects.shared.featureRooms.api,
        projects.shared.featureRooms.presentation, // временно, удалится в Task 8 вместе со старым кодом
    )
}
```

- [ ] **Step 14: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

**Если падает "duplicate class dev.nonoxy.feature.rooms.presentation.RoomsViewModel"** — это тот самый риск из шапки. Откатиться нельзя, нужно действовать: в `presentation/.../RoomsViewModel.kt` переименовать класс `RoomsViewModel` → `RoomsViewModelImpl`, в `featureRoomsPresentationModule` — `viewModelOf(::RoomsViewModelImpl)`, обновить ui-импорты в Task 6. В Task 7 после удаления старого ViewModel вернуть имя `RoomsViewModel`.

- [ ] **Step 15: Commit**

```bash
git add shared/feature-rooms/presentation shared/feature-rooms/impl
git commit -m "Phase 4: extract presentation module (ViewModel, UiState/Label, mappers, DI)"
```

---

### Task 6: UI-модуль — RoomsScreen, views, FeatureRoomsScreenApi

**Files:**
- Move: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/RoomsScreen.kt` → `shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/RoomsScreen.kt`
- Move: `.../impl/.../ui/views/RoomList.kt` → `.../ui/.../ui/views/RoomList.kt`
- Move: `.../impl/.../ui/views/RoomListItem.kt` → `.../ui/.../ui/views/RoomListItem.kt`
- Move: `.../impl/.../ui/views/RoomsScreenDetails.kt` → `.../ui/.../ui/views/RoomsScreenDetails.kt`
- Move: `.../impl/.../ui/views/RoomsTabRow.kt` → `.../ui/.../ui/views/RoomsTabRow.kt`
- Move: `.../impl/.../ui/views/RoomsTopBar.kt` → `.../ui/.../ui/views/RoomsTopBar.kt`
- Move: `.../impl/.../ui/views/ScrollableTabRowImpl.kt` → `.../ui/.../ui/views/ScrollableTabRowImpl.kt`
- Move: `.../impl/.../presentation/navigation/FeatureRoomsNavigation.kt` → `.../ui/.../ui/api/FeatureRoomsScreenApi.kt`
- Modify: `RoomsScreen.kt` (под новый ViewModel API: state/label → CFlow; onObtainEvent → onRoomClick/onAddRoomClick)
- Modify: `RoomsScreenDetails.kt` (под новый State signature; Preview очищен от мёртвых полей; обработчики `onObtainEvent` → отдельные коллбэки)
- Modify: `FeatureRoomsScreenApi.kt` (под новые имена + пакет ui.api)
- Delete: `.gitkeep` в ui source-set
- Modify: `impl/build.gradle.kts` (УБРАТЬ временную зависимость на presentation, добавленную в Task 5 — она уже не нужна для оставшегося в impl кода; в impl после Task 7 останется только data/di)
- Modify: остальные view-файлы (только package на новый `dev.nonoxy.feature.rooms.ui.views`)

**Контекст.** UI-код целиком переезжает в новый `ui`-модуль. Главное преобразование: `RoomsScreen.kt` адаптируется под `BaseViewModel<State, Label>` — берёт `state` (CStateFlow) через `collectAsStateWithLifecycle()`, `label` (CFlow) через `CollectFlow`, вместо `viewModel::obtainEvent` использует прямые методы `viewModel::onRoomClick` / `viewModel::onAddRoomClick`. `RoomsScreenDetails.kt` принимает `UiRoomsState` и два коллбэка вместо `(RoomsEvent) -> Unit`. `FeatureRoomsScreenApi.kt` (бывш. `FeatureRoomsNavigation.kt`) переезжает под `ui.api` пакет — это KMMTemplate-стиль.

- [ ] **Step 1: Удалить плейсхолдер `.gitkeep` в ui source-set**

```bash
rm shared/feature-rooms/ui/src/commonMain/kotlin/.gitkeep
```

- [ ] **Step 2: Перенести view-файлы в ui-модуль**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
mkdir -p shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views
mkdir -p shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/api

git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomList.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomList.kt
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomListItem.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomListItem.kt
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsTabRow.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsTabRow.kt
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsTopBar.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsTopBar.kt
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/ScrollableTabRowImpl.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/ScrollableTabRowImpl.kt
```

Эти 5 файлов package декларируют `dev.nonoxy.feature.rooms.ui.views` (что и так корректно для нового модуля). Изменений в их содержимом не нужно.

- [ ] **Step 3: Перенести `RoomsScreen.kt` и переписать его под `BaseViewModel<State, Label>`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/RoomsScreen.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/RoomsScreen.kt
```

Полное новое содержимое `shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/RoomsScreen.kt`:

```kotlin
package dev.nonoxy.feature.rooms.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.feature.rooms.presentation.RoomsViewModel
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsLabel
import dev.nonoxy.feature.rooms.ui.views.RoomsScreenDetails
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToManageStudentsExistingRoom: (String) -> Unit,
    viewModel: RoomsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiRoomsLabel.NavigateToAddRoomScreen -> onNavigateToAddRoomScreen()
            is UiRoomsLabel.NavigateToManageStudentsExistingRoom ->
                onNavigateToManageStudentsExistingRoom(label.roomId)
        }
    }

    RoomsScreenDetails(
        state = state,
        onRoomClick = viewModel::onRoomClick,
        onAddRoomClick = viewModel::onAddRoomClick,
        modifier = Modifier.fillMaxSize()
    )
}
```

- [ ] **Step 4: Перенести и переписать `RoomsScreenDetails.kt` (Preview очищен от мёртвых полей)**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsScreenDetails.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsScreenDetails.kt
```

Полное новое содержимое `shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views/RoomsScreenDetails.kt`:

```kotlin
package dev.nonoxy.feature.rooms.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nonoxy.common.utils.orEmptyPersist
import dev.nonoxy.feature.rooms.presentation.models.UiRoom
import dev.nonoxy.feature.rooms.presentation.models.UiRoomsState
import dev.nonoxy.feature.rooms.presentation.models.UiStudent
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoomsScreenDetails(
    state: UiRoomsState,
    onRoomClick: (Long) -> Unit,
    onAddRoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { state.roomsOnFloor.keys.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        RoomsTopBar(
            modifier = Modifier.padding(horizontal = padding_size_16),
            totalPlaces = 0,
            availablePlaces = 0,
            onAddRoomClick = onAddRoomClick
        )

        if (state.roomsOnFloor.keys.size > 1) {
            RoomsTabRow(
                modifier = Modifier.fillMaxWidth(),
                floors = state.roomsOnFloor.keys,
                selectedTabIndex = pagerState.currentPage,
                onTabClick = { index ->
                    scope.launch { pagerState.animateScrollToPage(page = index) }
                }
            )
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { page ->
            RoomList(
                rooms = state.roomsOnFloor.get(
                    key = state.roomsOnFloor.keys.elementAtOrNull(index = page)
                ).orEmptyPersist(),
                onRoomClick = onRoomClick
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    ResideTrackTheme {
        RoomsScreenDetails(
            state = UiRoomsState(
                roomsOnFloor = persistentMapOf(
                    3 to persistentListOf(
                        UiRoom(
                            id = 1,
                            floorNumber = "3",
                            roomNumber = "329",
                            bedsCount = "5",
                            students = persistentListOf(
                                UiStudent(
                                    streamNumber = "1234",
                                    checkInDate = "12-03-2024",
                                    checkOutDate = "31-03-2024",
                                    isCheckOutDateNearOrExpired = false
                                ),
                                UiStudent(
                                    streamNumber = "5646",
                                    checkInDate = "12-03-2024",
                                    checkOutDate = "15-03-2024",
                                    isCheckOutDateNearOrExpired = true
                                ),
                            )
                        ),
                    ),
                    4 to persistentListOf(
                        UiRoom(
                            id = 2,
                            floorNumber = "4",
                            roomNumber = "401",
                            bedsCount = "3",
                            students = persistentListOf()
                        ),
                    )
                )
            ),
            onRoomClick = {},
            onAddRoomClick = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

**Изменения относительно старого `RoomsScreenDetails.kt`:**
1. Параметры: было `state: RoomsViewState, onObtainEvent: (RoomsEvent) -> Unit` → стало `state: UiRoomsState, onRoomClick: (Long) -> Unit, onAddRoomClick: () -> Unit`.
2. `RoomsTopBar.totalPlaces/availablePlaces` — раньше шли из `state.selectedFloorTotalBeds/AvailableBeds` (всегда 0); теперь литералы `0`. Поведение идентично.
3. Preview очищен от `allRooms` и `tabsState` (мёртвые поля).
4. `RoomsTopBar`, `RoomsTabRow`, `RoomList` вызовы по факту те же, без `onObtainEvent`.

- [ ] **Step 5: Перенести `FeatureRoomsNavigation.kt` → `ui/api/FeatureRoomsScreenApi.kt` и переписать**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/navigation/FeatureRoomsNavigation.kt \
       shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/api/FeatureRoomsScreenApi.kt
```

Полное новое содержимое `shared/feature-rooms/ui/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/api/FeatureRoomsScreenApi.kt`:

```kotlin
package dev.nonoxy.feature.rooms.ui.api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.nonoxy.core.navigation.RoomsRoute
import dev.nonoxy.core.navigation.navigateOnResumed
import dev.nonoxy.feature.rooms.ui.RoomsScreen

fun NavController.navigateToRoomsScreen() {
    navigateOnResumed(RoomsRoute)
}

fun NavGraphBuilder.composableRoomsScreen(
    onNavigateToAddRoomScreen: () -> Unit,
    onNavigateToManageStudentsExistingRoom: (String) -> Unit
) {
    composable<RoomsRoute> {
        RoomsScreen(
            onNavigateToAddRoomScreen = onNavigateToAddRoomScreen,
            onNavigateToManageStudentsExistingRoom = onNavigateToManageStudentsExistingRoom
        )
    }
}
```

**Изменения:** только `package` (`presentation.navigation` → `ui.api`) и `import dev.nonoxy.feature.rooms.ui.RoomsScreen` (тот же). Логика идентична.

- [ ] **Step 6: Удалить пустую директорию `impl/.../presentation/navigation/`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/navigation 2>/dev/null || true
```

- [ ] **Step 7: Обновить импорт в `shared/main/navigation/ResideTrackNavHost.kt`**

После Step 5 `:shared:main/navigation/ResideTrackNavHost.kt` импортирует `composableRoomsScreen` из СТАРОГО пути (`dev.nonoxy.feature.rooms.presentation.navigation.composableRoomsScreen`), который перестал существовать. Правим до проверки сборки.

В файле `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt` заменить:

Старое:
```kotlin
import dev.nonoxy.feature.rooms.presentation.navigation.composableRoomsScreen
```
Новое:
```kotlin
import dev.nonoxy.feature.rooms.ui.api.composableRoomsScreen
```

- [ ] **Step 8: Проверка сборки**

Run: `./gradlew :shared:feature-rooms:ui:compileKotlinAndroid :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Commit**

```bash
git add shared/feature-rooms/ui shared/feature-rooms/impl shared/main
git commit -m "Phase 4: extract ui module (Screen, views, FeatureRoomsScreenApi)"
```

---

### Task 7: Переключить `:shared:main` на новый presentation-модуль; удалить мёртвый код из impl

**Files:**
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` (зарегистрировать `featureRoomsPresentationModule`)
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di/FeatureRoomsImplModule.kt` (удалить ViewModel биндинг + UiRoomMapper/UiStudentMapper биндинги — они уезжают в `featureRoomsPresentationModule`)
- Delete: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt`
- Delete: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsAction.kt`
- Delete: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsEvent.kt`
- Delete: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsViewState.kt`
- Delete: пустые директории `impl/.../presentation/{models,navigation,/}` и `impl/.../ui/`
- Modify: `shared/feature-rooms/impl/build.gradle.kts` (УБРАТЬ временную зависимость `projects.shared.featureRooms.presentation`, добавленную в Task 5 Step 13)

**Контекст.** Это «момент истины»: переключаем DI на новый ViewModel и удаляем весь старый MVI-код из impl. После задачи в impl остаются ТОЛЬКО `data/` и `di/` — и они ещё в старых пакетах (без `.impl.` суффикса). Перепаковка impl в `.impl.*` будет в Task 8.

**Внимание:** перед удалением старого `RoomsViewModel.kt` из impl нужно убрать его DI-биндинг. Иначе compile-time ошибка (Koin DSL ссылается на `RoomsViewModel` через `viewModelOf(::RoomsViewModel)`).

- [ ] **Step 1: Переписать `featureRoomsImplModule` — оставить только Repository + RoomMapper + StudentMapper + RoomsStore**

Полное новое содержимое `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di/FeatureRoomsImplModule.kt`:

```kotlin
package dev.nonoxy.feature.rooms.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.feature.rooms.api.store.RoomsStore
import dev.nonoxy.feature.rooms.data.RoomsRepositoryImpl
import dev.nonoxy.feature.rooms.data.mappers.RoomMapper
import dev.nonoxy.feature.rooms.data.mappers.RoomMapperImpl
import dev.nonoxy.feature.rooms.data.mappers.StudentMapper
import dev.nonoxy.feature.rooms.data.mappers.StudentMapperImpl
import dev.nonoxy.feature.rooms.impl.domain.RoomsStoreFactory
import org.koin.dsl.module

val featureRoomsImplModule = module {

    factory<StudentMapper> {
        StudentMapperImpl()
    }

    factory<RoomMapper> {
        RoomMapperImpl(studentMapper = get())
    }

    factory<RoomsRepository> {
        RoomsRepositoryImpl(
            roomDao = get(),
            roomMapper = get()
        )
    }

    factory<RoomsStore> {
        RoomsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }
}
```

(Уезжает: `viewModelOf(::RoomsViewModel)`, `factory<UiStudentMapper>`, `factory<UiRoomMapper>` — они теперь в `featureRoomsPresentationModule`. Приезжает: `factory<RoomsStore>` через `RoomsStoreFactory`.)

- [ ] **Step 2: Зарегистрировать `featureRoomsPresentationModule` в `shared/main/di/Koin.kt`**

Найти в `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

```kotlin
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
```

Добавить после:
```kotlin
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
import dev.nonoxy.feature.rooms.presentation.di.featureRoomsPresentationModule
```

И в блоке `modules(...)` найти:
```kotlin
            featureRoomsImplModule,
            featureAddRoomImplModule,
```

Заменить на:
```kotlin
            featureRoomsImplModule,
            featureRoomsPresentationModule,

            featureAddRoomImplModule,
```

- [ ] **Step 3: Удалить мёртвые файлы старого MVI**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git rm shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/RoomsViewModel.kt
git rm shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsAction.kt
git rm shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsEvent.kt
git rm shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models/RoomsViewState.kt
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/models 2>/dev/null || true
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation 2>/dev/null || true
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/mappers 2>/dev/null || true
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/models 2>/dev/null || true
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui/views 2>/dev/null || true
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/ui 2>/dev/null || true
```

- [ ] **Step 4: Убрать временную зависимость `projects.shared.featureRooms.presentation` из `impl/build.gradle.kts`**

В файле `shared/feature-rooms/impl/build.gradle.kts` найти и удалить строку:

```kotlin
        projects.shared.featureRooms.presentation, // временно, удалится в Task 8 вместе со старым кодом
```

(Эта зависимость была временно добавлена в Task 5 Step 13. Теперь импорт не нужен — старый ViewModel удалён, презентационные импорты в impl исчезли.)

Промежуточная сборка может ещё работать с этой строкой; для чистоты удаляем именно сейчас.

- [ ] **Step 5: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

**Если падает с `Class 'RoomsViewModel' not found in module ...impl`** — значит где-то ещё остался импорт старого VM. Грепнуть:
```bash
grep -rn "dev\.nonoxy\.feature\.rooms\.presentation\.RoomsViewModel\|featureRoomsImplModule" shared --include='*.kt' --include='*.kts' | grep -v build/
```
Все случаи должны вести либо в `:shared:feature-rooms:presentation` (новый ViewModel), либо в DI shared/main/Koin.kt.

- [ ] **Step 6: Commit**

```bash
git add shared/feature-rooms/impl shared/main shared/feature-rooms/ui
git commit -m "Phase 4: wire feature-rooms presentation module + delete old MVI code"
```

---

### Task 8: Перепаковка impl в `.impl.*` + переход на `kmpFeatureSetup`

**Files:**
- Move: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/data/` → `.../feature/rooms/impl/data/`
- Move: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di/` → `.../feature/rooms/impl/di/`
- Modify: каждый из 4 перенесённых файлов (package)
- Modify: `shared/feature-rooms/impl/build.gradle.kts` (переход на kmpFeatureSetup, удаление Compose-зависимостей)
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` (импорт `featureRoomsImplModule` обновляется под новый пакет)

**Контекст.** На этом этапе в impl остался только data-слой и DI. Перепаковываем под `.impl.*`, переключаем build на конвенционный `kmpFeatureSetup` (он автоматом приносит `:shared:common`, `:shared:feature-rooms:api`, `core-mvikotlin`, `core-domain`). Остаётся явная зависимость — `:shared:core-database` (нужна для RoomDao).

- [ ] **Step 1: Перенести директории `data/` и `di/`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/data \
       shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/data
git mv shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/di \
       shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl/di
```

- [ ] **Step 2: Поправить пакеты во всех перемещённых файлах**

Массовая замена:

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
find shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/impl \
     -type f -name '*.kt' \
     -exec sed -i '' \
       -e 's|^package dev\.nonoxy\.feature\.rooms\.data\.mappers|package dev.nonoxy.feature.rooms.impl.data.mappers|' \
       -e 's|^package dev\.nonoxy\.feature\.rooms\.data|package dev.nonoxy.feature.rooms.impl.data|' \
       -e 's|^package dev\.nonoxy\.feature\.rooms\.di|package dev.nonoxy.feature.rooms.impl.di|' \
       -e 's|dev\.nonoxy\.feature\.rooms\.data\.mappers\.|dev.nonoxy.feature.rooms.impl.data.mappers.|g' \
       -e 's|dev\.nonoxy\.feature\.rooms\.data\.|dev.nonoxy.feature.rooms.impl.data.|g' \
       {} +
```

После запуска ВРУЧНУЮ проверить, что нет ложных замен в комментариях/строках. Грепнуть:

```bash
grep -rn 'dev\.nonoxy\.feature\.rooms\.data\.\|dev\.nonoxy\.feature\.rooms\.di\b' shared --include='*.kt' | grep -v build/
```
Expected: пусто (кроме git ignore'd build/).

- [ ] **Step 3: Обновить импорт `featureRoomsImplModule` в `shared/main/di/Koin.kt`**

Найти:
```kotlin
import dev.nonoxy.feature.rooms.di.featureRoomsImplModule
```
Заменить на:
```kotlin
import dev.nonoxy.feature.rooms.impl.di.featureRoomsImplModule
```

- [ ] **Step 4: Переписать `shared/feature-rooms/impl/build.gradle.kts` под `kmpFeatureSetup`**

Полное новое содержимое:

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.rooms.impl"
}

commonMainDependencies {
    implementations(
        projects.shared.coreDatabase,
    )
}
```

Что удалилось из старого файла:
- `composeMultiplatformSetup` plugin (в impl больше нет Compose-кода).
- `*composeBundle`, `compose.multiplatform.resources`, `koin.composeMultiplatform.viewmodelNavigation` (UI ушёл).
- `kotlin.immutableCollections` (immutable list/map использовались в маппере UiRoom — переехало в presentation).
- `projects.shared.common`, `projects.shared.coreNavigation`, `projects.shared.commonUi`, `projects.shared.featureRooms.api` (всё это идёт автоматически через `kmpFeatureSetup`).
- `compose.resources { ... }` блок (без Compose в модуле он не нужен).

Что осталось:
- `projects.shared.coreDatabase` — единственная не-стандартная зависимость, нужна для `RoomDao` и `RoomEntity` в `RoomsRepositoryImpl`.

- [ ] **Step 5: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Удалить пустые директории старых пакетов в impl**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
rmdir shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms 2>/dev/null || true
```

(Если ругается — внутри ещё остались директории. Проверить `find shared/feature-rooms/impl/src -type d -empty` и удалить вручную.)

- [ ] **Step 7: Commit**

```bash
git add shared/feature-rooms/impl shared/main
git commit -m "Phase 4: repackage feature-rooms impl under .impl.* and switch to kmpFeatureSetup"
```

---

### Task 9: Полная проверка — assemble, link, detekt + manual sanity

**Files:** (только верификация, без правок)

- [ ] **Step 1: Полная Android-сборка обоих flavor**

Run: `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: iOS framework-линкование**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Detekt**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL. Все отчёты пустые. Если падает — починить и повторить.

- [ ] **Step 4: Ручная проверка Rooms на устройстве**

Установить dev-debug APK и проверить:
1. Открыть приложение → стартовый экран — Rooms.
2. Если есть данные (или предварительно добавить пару комнат на разных этажах): убедиться, что список рендерится сгруппированный по этажам, табы переключают этажи (свайп/тап).
3. Тап по комнате открывает Manage Students Existing Room (роут с roomId).
4. Тап на «Добавить комнату» в TopBar открывает Add Room.
5. Возврат назад не падает.

Это закрывает раздел 16 спеки: "После миграции каждой фичи: поведение экрана сверяется вручную (флоу, навигация, валидация полей)".

Если поведение отличается от Phase-3 baseline — ОСТАНОВИТЬСЯ и сообщить, не маскировать "косметической правкой".

- [ ] **Step 5: Промежуточный коммит не нужен — Task 9 без модификаций. Идём в Task 10.**

---

### Task 10: Обновить статус в спеке + summary commit

**Files:**
- Modify: `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`

- [ ] **Step 1: Обновить статус в спеке**

В файле `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` найти:

```markdown
**Статус (2026-05-21):** Фазы 1–3 завершены; следующая — Фаза 4 (пилот feature-rooms).
```

Заменить на:

```markdown
**Статус (2026-05-24):** Фазы 1–4 завершены; следующая — Фаза 5 (миграция feature-add-room и feature-manage-students).
```

И в списке фаз найти:
```markdown
4. **Пилот** — `feature-rooms` → 4 модуля + MVIKotlin.
```
Заменить на:
```markdown
4. **Пилот** ✅ — `feature-rooms` → 4 модуля + MVIKotlin.
```

- [ ] **Step 2: Финальный коммит спеки**

```bash
git add docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md
git commit -m "docs: mark Phase 4 (feature-rooms pilot) complete in migration spec"
```

- [ ] **Step 3: Проверить, что nothing-to-commit и история чистая**

```bash
git status
git log --oneline main..HEAD
```

Должно быть 8–9 коммитов на текущей ветке поверх main (по числу Task'ов с коммитами: Task 1–8 + Task 10 = 9).

---

## Спека-coverage чек

| Спека (раздел) | Покрыто |
|---|---|
| §8 MVI: самописный → MVIKotlin | Task 3 (Store), Task 4 (Executor/Reducer/Factory), Task 5 (ViewModel/мапперы/UiLabel/UiState) |
| §9 4-модульный split фичи | Task 2 (каркасы), Task 3 (api), Task 5 (presentation), Task 6 (ui), Task 7+8 (impl) |
| §11 Screen API → ui/api/ | Task 6 Step 5 (`FeatureRoomsScreenApi.kt`) |
| §14 пункт 4 «Пилот» | Tasks 1–10 целиком |
| Phase 2 carry-overs (CoroutineDispatchers, регистрация core-модулей в Koin) | Task 1 |

## Допустимые расхождения с KMMTemplate

- `RoomsStore.State` содержит `Map<Int, List<Room>>`, тогда как KMMTemplate's `DemoFeatureFirstStore.State` оперирует только примитивами. Это естественно — наша фича отображает структурированные данные. Документировать не нужно.
- В `featureRoomsImplModule` используется `factory<RoomsStore>`, тогда как KMMTemplate использует тот же паттерн (`factory<DemoFeatureFirstStore>`). Полное соответствие.
- `RoomsExecutor` использует `Action.LoadInitial` через `SimpleBootstrapper`. KMMTemplate's `DemoFeatureFirstExecutor` имеет `Nothing` action (без bootstrapper). Расхождение оправдано — нашей фиче нужен initial load.

## Что НЕ надо делать в этой фазе

- Не трогать `dev.nonoxy.common.presentation.BaseViewModel` (старый кастомный) — он ещё используется `feature-add-room` и `feature-manage-students`. Будет удалён в Фазе 5.
- Не мигрировать строковые ресурсы — Фаза 6.
- Не переименовывать пакеты в `dev.nonoxy.residetrack.*` — Фаза 8.
- Не "заодно" чинить мёртвую логику в `feature-add-room` или `feature-manage-students`.
