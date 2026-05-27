# Phase 5: feature-add-room + feature-manage-students → 4 модуля + MVIKotlin — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Перевести оставшиеся две фичи (`feature-add-room`, `feature-manage-students`) на тот же 4-модульный split (api/impl/presentation/ui) и MVIKotlin Store/Executor/Reducer, что и `feature-rooms` после Phase 4. Полностью удалить старый `dev.nonoxy.common.presentation.BaseViewModel` и крутель `StringProvider`. Поведение приложения не меняется.

**Architecture:** Зеркальная Phase 4 миграция, но дважды — сначала `feature-add-room` (проще, без параметров), потом `feature-manage-students` (с параметризованной фабрикой `ManageStudentsMode` и более крупным State/Label-набором). Стратегия — «create new alongside old, then remove old»: новые модули `presentation/` и `ui/` создаются и наполняются рядом со старым кодом в `impl/`, `:shared:main` переключается на новые точки входа, потом мёртвый код в `impl/` удаляется и `impl` репакуется под `.impl.*` + `kmpFeatureSetup`. Локализация уходит из Executor: ошибки и success-сообщения публикуются как типизированные `ErrorKind`/`SuccessKind` в `Store.Label` и `Store.State`, а конвертация в строки происходит **только в `ui/`-слое** через `@Composable stringResource(...)`. `StringProvider` удаляется в Фазе 5 (не дожидаясь Phase 6 → moko-resources) — после миграции он никому не нужен.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, MVIKotlin 4.3.0, Koin 4.1.1, kotlinx.collections.immutable, Gradle convention plugins (`kmpFeatureSetup`, `composeMultiplatformSetup`).

---

## Контекст и границы фазы

**Что делаем (спека, разделы 9 и 14, пункт 5):**
- `:shared:feature-add-room` → 4 модуля: `api`, `impl`, `presentation`, `ui` (сейчас живут только `api` (пустой) и `impl`).
- `:shared:feature-manage-students` → 4 модуля: `api`, `impl`, `presentation`, `ui` (сейчас `api` содержит только `ManageStudentsMode`, остальное в `impl`).
- Самописный MVI `BaseViewModel<State, Event, Action>` (`dev.nonoxy.common.presentation.BaseViewModel`) → MVIKotlin Store + `BaseViewModel<State, Label>` из `core-presentation` (тот же, что использует `feature-rooms` после Phase 4).
- Все feature-build-файлы переходят на convention-plugin `kmpFeatureSetup`. `impl`-модули обеих фич, кроме того, продолжают использовать `composeMultiplatformSetup` для compose-resources — но только до Фазы 6.
- Пакеты внутри фич получают суффикс модуля: `dev.nonoxy.feature.add_room.api.*`, `dev.nonoxy.feature.add_room.impl.*`, `dev.nonoxy.feature.manage_students.api.*`, `dev.nonoxy.feature.manage_students.impl.*`. `presentation.*` и `ui.*` сохраняют имена.
- Локализация ошибок и success-сообщений вынесена из бизнес-логики:
  - `AddRoomStore.Label` несёт `AddRoomMessageKind` (sealed), `UiAddRoomLabel` — тоже типизированный, разрешение в строку только внутри `@Composable AddRoomScreen` через `stringResource(...)`/`getString(...)`.
  - То же для `ManageStudentsStore.Label`.
  - `errorMessage: String?` в `TextFieldState` (inline-валидация AddRoom) меняется на `errorKind: AddRoomErrorKind?`; конвертация — в `@Composable`-views.
- `StringProvider` (`shared/feature-manage-students/impl/utils/StringProvider.kt` + `StringProviderImpl`) удаляется и из Koin, и из исходников.
- compose-resources обеих фич мигрируют из `impl/src/commonMain/composeResources/` в `ui/src/commonMain/composeResources/` (как Phase 4 сделал для feature-rooms).
- Старый `dev.nonoxy.common.presentation.BaseViewModel` (`shared/common/.../presentation/BaseViewModel.kt`) удаляется в самом конце фазы — после миграции обеих оставшихся ViewModel.

**Чего НЕ делаем в этой фазе:**
- Полная миграция Compose Resources → moko-resources в `:shared:common-resources` — это Фаза 6 (она пройдёт по всем трём `ui/`-модулям сразу, включая feature-rooms).
- Переименование пакетов `dev.nonoxy.*` → `dev.nonoxy.residetrack.*` — это Фаза 8. В Фазе 5 префиксы остаются `dev.nonoxy.feature.add_room.*` и `dev.nonoxy.feature.manage_students.*`.
- Изменение поведения приложения. Пользовательские флоу (Open AddRoom → создать комнату → Manage Students draft, открыть Manage Students existing room) работают точно так же.

**Зафиксированные пользователем решения для Фазы 5:**
- Executor чистый — **никаких** обращений к `Res.string.*` / `StringProvider` / прочим источникам строк. Локализация выполняется максимум в UI-слое.
- Sequential single-doc plan (этот файл) — сначала вся feature-add-room (Tasks 1–9), потом вся feature-manage-students (Tasks 10–17), потом cleanup + verify + spec (Tasks 18–20).
- Per-handler callbacks в UI (`onCreateRoomClick: () -> Unit`, `onFloorNumberInputValueChange: (String) -> Unit` и т.д.) — отказываемся от паттерна «один `onObtainEvent: (Event) -> Unit`», как сделали для `RoomsScreen` после Phase 4.
- compose-resources остаются compose-resources до Фазы 6 — Phase 5 их просто переносит из `impl/` в `ui/`.

**Поведенческое отличие после фазы:** нет.
- Внутренний side-effect: в текущем AddRoom-MVI `getString(...)` могла бросить exception, перехваченный `try { ... } catch (_: Exception)` → `ShowErrorMessage("Unknown error")`. В новом коде Executor не имеет шанса свалиться на ресурсах — ошибки только бизнес-логические.
- `viewState = viewState.copy(...)` цепочки в обработчиках с одновременной публикацией двух `viewAction`-ов подряд (как в `handleSaveAndClose`: `ShowSuccess` + сразу `NavigateBack`) пересобираются в один `Label.SavedAndClose` → UI решает что показать. Поведенчески идентично.

**Контрольная точка после фазы:**
- `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug` зелёный.
- `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64` зелёный.
- `./gradlew detekt` зелёный.
- Ручная сверка на Android (`dev/debug` flavor):
  1. Главный экран Rooms → нажать «Добавить комнату» → открывается AddRoom bottom-sheet.
  2. В AddRoom: ввести этаж, номер, мест; нажать «Создать» → snackbar успеха, открывается Manage Students draft.
  3. В Manage Students (draft): «Добавить слушателя» → заполнить поток, даты → «Сохранить» → snackbar успеха, возврат на Rooms.
  4. На Rooms кликнуть по созданной комнате → открывается Manage Students existing room с уже добавленным студентом.
  5. Ошибочные сценарии: создать дубль комнаты (тот же этаж+номер) → snackbar «Комната … на этаже … уже существует». Поток не-число → snackbar «Номер потока должен быть числом». Дата выселения ≤ заселения → snackbar «Дата выселения должна быть позже даты заселения».

---

## Целевая структура модулей после Фазы 5

```
shared/feature-add-room/
├── api/                                              (существует, перепаковывается)
│   ├── build.gradle.kts                             (kmpFeatureSetup)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/add_room/api/
│       └── store/AddRoomStore.kt                    (НОВЫЙ — Intent/State/Label + ErrorKind/SuccessKind)
│
├── impl/                                             (существует, гутается)
│   ├── build.gradle.kts                             (kmpFeatureSetup; composeMultiplatformSetup НЕ нужен — резины уехали в ui)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/
│       ├── domain/AddRoomExecutor.kt                (НОВЫЙ)
│       ├── domain/AddRoomReducer.kt                 (НОВЫЙ)
│       ├── domain/AddRoomStoreFactory.kt            (НОВЫЙ, содержит Action + Message)
│       └── di/FeatureAddRoomImplModule.kt           (переписан)
│
├── presentation/                                     (НОВЫЙ модуль)
│   ├── build.gradle.kts                             (kmpFeatureSetup)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/
│       ├── AddRoomViewModel.kt                       (НОВЫЙ — BaseViewModel<State, Label>)
│       ├── models/UiAddRoomState.kt                  (НОВЫЙ)
│       ├── models/UiAddRoomLabel.kt                  (НОВЫЙ)
│       ├── mappers/UiAddRoomStateMapper.kt           (НОВЫЙ — Store.State → UiAddRoomState)
│       ├── mappers/UiAddRoomLabelMapper.kt           (НОВЫЙ — Store.Label → UiAddRoomLabel)
│       └── di/FeatureAddRoomPresentationModule.kt    (НОВЫЙ)
│
└── ui/                                               (НОВЫЙ модуль)
    ├── build.gradle.kts                             (kmpFeatureSetup + composeMultiplatformSetup)
    ├── src/commonMain/composeResources/values/strings.xml  (МИГРИРУЕТ из impl/)
    └── src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/
        ├── AddRoomScreen.kt                          (мигрирует из impl/ui/AddRoomScreen.kt, адаптирован под BaseViewModel + per-handler callbacks)
        ├── views/AddRoomScreenContent.kt             (мигрирует + per-handler callbacks)
        ├── views/AddRoomActionsSection.kt            (мигрирует)
        ├── views/BedsSelectionSection.kt             (мигрирует)
        ├── views/FloorSelectionSection.kt            (мигрирует)
        ├── views/RoomNumberSection.kt                (мигрирует)
        ├── views/SelectionChips.kt                   (мигрирует)
        ├── views/AddRoomMessageMapping.kt            (НОВЫЙ — ErrorKind/SuccessKind → stringResource)
        └── api/FeatureAddRoomScreenApi.kt            (мигрирует из impl/presentation/navigation/FeatureAddRoomNavigation.kt)

shared/feature-manage-students/
├── api/                                              (существует, перепаковывается)
│   ├── build.gradle.kts                             (kmpFeatureSetup)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/manage_students/api/
│       ├── models/ManageStudentsMode.kt              (мигрирует из api/models/)
│       └── store/ManageStudentsStore.kt              (НОВЫЙ — Intent/State/Label + ErrorKind)
│
├── impl/                                             (существует, гутается)
│   ├── build.gradle.kts                             (kmpFeatureSetup; composeMultiplatformSetup НЕ нужен)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/
│       ├── domain/ManageStudentsExecutor.kt          (НОВЫЙ)
│       ├── domain/ManageStudentsReducer.kt           (НОВЫЙ)
│       ├── domain/ManageStudentsStoreFactory.kt      (НОВЫЙ — параметризованная фабрика create(mode), Action + Message)
│       └── di/FeatureManageStudentsImplModule.kt     (переписан под parametersOf(mode))
│
├── presentation/                                     (НОВЫЙ модуль)
│   ├── build.gradle.kts                             (kmpFeatureSetup)
│   └── src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/
│       ├── ManageStudentsViewModel.kt                (НОВЫЙ — BaseViewModel<State, Label>)
│       ├── models/UiManageStudentsState.kt           (НОВЫЙ)
│       ├── models/UiManageStudentsLabel.kt           (НОВЫЙ)
│       ├── models/UiEditableStudent.kt               (НОВЫЙ — переезжает из impl/presentation/models/)
│       ├── models/UiRoom.kt                          (мигрирует из impl/ui/models/)
│       ├── models/UiStudent.kt                       (мигрирует из impl/ui/models/)
│       ├── mappers/UiManageStudentsStateMapper.kt    (НОВЫЙ)
│       ├── mappers/UiManageStudentsLabelMapper.kt    (НОВЫЙ)
│       ├── mappers/UiRoomMapper.kt                   (мигрирует из impl/ui/mappers/)
│       ├── mappers/UiStudentMapper.kt                (мигрирует из impl/ui/mappers/)
│       └── di/FeatureManageStudentsPresentationModule.kt (НОВЫЙ)
│
└── ui/                                               (НОВЫЙ модуль)
    ├── build.gradle.kts                             (kmpFeatureSetup + composeMultiplatformSetup)
    ├── src/commonMain/composeResources/values/strings.xml  (МИГРИРУЕТ из impl/)
    ├── src/commonMain/composeResources/drawable/ic_delete_circle.xml (МИГРИРУЕТ)
    └── src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/
        ├── ManageStudentsScreen.kt                   (мигрирует + per-handler callbacks)
        ├── views/ManageStudentsContent.kt            (мигрирует + per-handler callbacks)
        ├── views/ManageStudentsTopBar.kt             (мигрирует)
        ├── views/ManageStudentsList.kt               (мигрирует + per-handler callbacks)
        ├── views/ManageStudentsBottomButtons.kt      (мигрирует)
        ├── views/EditableStudentCard.kt              (мигрирует)
        ├── views/EmptyStudentsState.kt               (мигрирует)
        ├── views/StudentCardContent.kt               (мигрирует)
        ├── views/StudentCardHeader.kt                (мигрирует)
        ├── views/ManageStudentsMessageMapping.kt     (НОВЫЙ — ErrorKind/SuccessKind → stringResource)
        └── api/FeatureManageStudentsScreenApi.kt     (мигрирует из impl/presentation/navigation/FeatureManageStudentsNavigation.kt)

shared/common/src/commonMain/kotlin/dev/nonoxy/common/presentation/
└── BaseViewModel.kt                                  (УДАЛЯЕТСЯ в Task 18)
```

## Карта затрагиваемых файлов

**Создаются (новые модули и файлы под ними):**
- `settings.gradle.kts` (Task 1, 10 — добавление новых include)
- Все 4 build-файла `feature-add-room/{api,impl,presentation,ui}/build.gradle.kts` (Tasks 1, 5, 8)
- Все 4 build-файла `feature-manage-students/{api,impl,presentation,ui}/build.gradle.kts` (Tasks 10, 14, 17)
- AddRoom: `api/store/AddRoomStore.kt`, `impl/domain/{AddRoomExecutor,AddRoomReducer,AddRoomStoreFactory}.kt`, все файлы под `presentation/` и `ui/` (см. структуру).
- ManageStudents: `api/store/ManageStudentsStore.kt`, `impl/domain/{ManageStudentsExecutor,ManageStudentsReducer,ManageStudentsStoreFactory}.kt`, все файлы под `presentation/` и `ui/` (см. структуру).

**Перепаковываются (git mv с обновлением `package`):**
- `feature-manage-students/api/.../models/ManageStudentsMode.kt` → `.../api/models/ManageStudentsMode.kt`
- `feature-add-room/impl/.../ui/{AddRoomScreen.kt,views/*}` → `feature-add-room/ui/src/.../ui/`
- `feature-add-room/impl/.../presentation/navigation/FeatureAddRoomNavigation.kt` → `feature-add-room/ui/src/.../ui/api/FeatureAddRoomScreenApi.kt`
- `feature-add-room/impl/src/commonMain/composeResources/` → `feature-add-room/ui/src/commonMain/composeResources/`
- `feature-manage-students/impl/.../ui/{ManageStudentsScreen.kt,views/*,mappers/*,models/*}` → `feature-manage-students/{ui,presentation}/src/.../`
- `feature-manage-students/impl/.../presentation/navigation/FeatureManageStudentsNavigation.kt` → `feature-manage-students/ui/src/.../ui/api/FeatureManageStudentsScreenApi.kt`
- `feature-manage-students/impl/src/commonMain/composeResources/` → `feature-manage-students/ui/src/commonMain/composeResources/`

**Удаляются:**
- AddRoom-старое: `impl/.../presentation/AddRoomViewModel.kt`, `impl/.../presentation/models/{AddRoomEvent,AddRoomAction,AddRoomViewState}.kt`, `impl/.../presentation/navigation/` (пустеет после переезда navigation в ui), `impl/.../ui/` (пустеет после переезда экрана + views в ui), `impl/src/commonMain/composeResources/`.
- ManageStudents-старое: `impl/.../presentation/ManageStudentsViewModel.kt`, `impl/.../presentation/models/{ManageStudentsEvent,ManageStudentsAction,ManageStudentsViewState}.kt`, `impl/.../presentation/navigation/`, `impl/.../ui/`, `impl/.../utils/StringProvider.kt` (полностью удаляется), `impl/src/commonMain/composeResources/`.
- `shared/common/src/commonMain/kotlin/dev/nonoxy/common/presentation/BaseViewModel.kt` (Task 18).

**Модифицируются (без переноса):**
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` (Tasks 6, 15 — регистрация новых presentation-модулей).
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt` (Tasks 6, 15 — импорты экрана-API).
- `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` (Task 20 — статус).

---

### Task 1: Settings.gradle.kts + пустые submodule-каркасы (presentation, ui) для feature-add-room

**Files:**
- Modify: `settings.gradle.kts`
- Create: `shared/feature-add-room/presentation/build.gradle.kts`
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/.gitkeep`
- Create: `shared/feature-add-room/ui/build.gradle.kts`
- Create: `shared/feature-add-room/ui/src/commonMain/kotlin/.gitkeep`

**Контекст.** Зеркальный шаг Task 2 из Phase 4 — добавляем два пустых модуля рядом со старыми `api` и `impl`. Source-set пустой, build-файлы используют `kmpFeatureSetup` (convention plugin сам разрулит зависимости по имени модуля). `.gitkeep` нужен, чтобы пустые директории попали в git и Gradle их увидел как валидный source-root.

- [ ] **Step 1: Добавить include в `settings.gradle.kts`**

В блоке `include(...)` найти:

```
    ":shared:feature-add-room:api",
    ":shared:feature-add-room:impl",
```

Заменить на:

```
    ":shared:feature-add-room:api",
    ":shared:feature-add-room:impl",
    ":shared:feature-add-room:presentation",
    ":shared:feature-add-room:ui",
```

- [ ] **Step 2: Создать `shared/feature-add-room/presentation/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
```

(`libs.kotlin.immutableCollections` нужен для `ImmutableList<Int>` в `existingFloors`/`existingBedsCounts` внутри `UiAddRoomState`.)

- [ ] **Step 3: Создать `shared/feature-add-room/presentation/src/commonMain/kotlin/.gitkeep`**

Пустой файл.

- [ ] **Step 4: Создать `shared/feature-add-room/ui/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.ui"
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
```

(`kmpFeatureSetup` уже подтягивает `composeBundle`, `core-navigation`, `common-resources`, `common-ui`, `koin-composeMP-viewmodel`, presentation-модуль и т.д. Блок `compose.resources` нужен — в `ui/` живут реальные `strings.xml`.)

- [ ] **Step 5: Создать `shared/feature-add-room/ui/src/commonMain/kotlin/.gitkeep`**

Пустой файл.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :shared:feature-add-room:presentation:assemble :shared:feature-add-room:ui:assemble`
Expected: BUILD SUCCESSFUL.

Затем полная проверка:
Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts \
        shared/feature-add-room/presentation/ \
        shared/feature-add-room/ui/
git commit -m "Phase 5: scaffold empty feature-add-room presentation+ui submodules"
```

---

### Task 2: feature-add-room api — перепаковка + AddRoomStore + переход на kmpFeatureSetup

**Files:**
- Modify: `shared/feature-add-room/api/build.gradle.kts`
- Create: `shared/feature-add-room/api/src/commonMain/kotlin/dev/nonoxy/feature/add_room/api/store/AddRoomStore.kt`

**Контекст.** В отличие от feature-rooms, `feature-add-room/api` сейчас пустой (нет своих моделей или репозитория — фича переиспользует `Room`/`RoomsRepository` из `feature-rooms/api`). Поэтому никакого `git mv` для перепаковки не нужно — мы только добавляем новый файл `AddRoomStore.kt` сразу в правильную папку `api/store/`. `build.gradle.kts` переключается с `kmpLibrary` на `kmpFeatureSetup`, чтобы автоматически получить `core-domain`, `core-mvikotlin`, compose-runtime (для стабильности публичных моделей) и зависимость на feature-rooms/api сохраняется через прямой `projects.shared.featureRooms.api`.

В `AddRoomStore` сразу заводим типизированные `AddRoomErrorKind` и `AddRoomSuccessKind` — Executor будет публиковать `Label.ShowMessage(kind)`, без строк.

- [ ] **Step 1: Переписать `shared/feature-add-room/api/build.gradle.kts`**

Полное новое содержимое:

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        libs.kotlin.immutableCollections,
        projects.shared.featureRooms.api,
    )
}
```

Изменения относительно старого файла:
- `kmpLibrary` → `kmpFeatureSetup`.
- `projects.shared.common` убран — `kmpFeatureSetup` для api НЕ добавляет `:shared:common` автоматически, но в api-модуле он и не нужен: AddRoomStore не использует ничего из common (Mapper и т.д. там не место). Если что-то понадобится — можно добавить отдельно, но пока нет.
- Добавлен `libs.kotlin.immutableCollections` — нужен для `ImmutableList<Int>` в `AddRoomStore.State.floorSelection.existingFloors` и `bedsSelection.existingBedsCounts`.
- `libs.kotlin.datetime` оставлен — формально не используется AddRoomStore, но добавлен в текущем api на всякий случай. Можно убрать после ревью — если detekt не зафиксит, оставляем; если зафиксит — убираем.

- [ ] **Step 2: Создать `AddRoomStore.kt`**

Файл `shared/feature-add-room/api/src/commonMain/kotlin/dev/nonoxy/feature/add_room/api/store/AddRoomStore.kt`:

```kotlin
package dev.nonoxy.feature.add_room.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.State
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface AddRoomStore : Store<Intent, State, Label> {

    data class State(
        val floorSelection: FloorSelectionState = FloorSelectionState(),
        val roomNumber: TextFieldState = TextFieldState(),
        val bedsSelection: BedsSelectionState = BedsSelectionState(),
        val isLoading: Boolean = false,
        val isFormValid: Boolean = false,
        val hasExistingRooms: Boolean = false,
    ) {
        data class TextFieldState(
            val value: String = "",
            val errorKind: AddRoomErrorKind? = null,
        )

        data class FloorSelectionState(
            val textField: TextFieldState = TextFieldState(),
            val existingFloors: ImmutableList<Int> = persistentListOf(),
            val showInput: Boolean = false,
        )

        data class BedsSelectionState(
            val textField: TextFieldState = TextFieldState(),
            val existingBedsCounts: ImmutableList<Int> = persistentListOf(),
            val showInput: Boolean = false,
        )
    }

    sealed interface Intent {
        data class OnFloorNumberInputValueChange(val floorNumber: String) : Intent
        data class OnFloorNumberSelect(val floorNumber: Int) : Intent
        data class OnRoomNumberInputValueChange(val roomNumber: String) : Intent
        data class OnBedsCountInputValueChange(val bedsCount: String) : Intent
        data class OnBedsCountSelect(val bedsCount: Int) : Intent
        data object OnCreateRoomClick : Intent
        data object OnCancelClick : Intent
        data object OnToggleFloorInput : Intent
        data object OnToggleBedsInput : Intent
    }

    sealed interface Label {
        data object CloseScreen : Label
        data object NavigateToManageStudentsDraftRoom : Label
        data class ShowSuccess(val kind: AddRoomSuccessKind) : Label
        data class ShowError(val kind: AddRoomErrorKind) : Label
    }
}

sealed interface AddRoomErrorKind {
    data object UnknownError : AddRoomErrorKind
    data object SaveFailed : AddRoomErrorKind
    data class RoomAlreadyExists(val roomNumber: Int, val floorNumber: Int) : AddRoomErrorKind
    data object FloorNumberRequired : AddRoomErrorKind
    data object RoomNumberRequired : AddRoomErrorKind
    data object BedsCountRequired : AddRoomErrorKind
}

sealed interface AddRoomSuccessKind {
    data class RoomCreated(val roomNumber: Int) : AddRoomSuccessKind
}
```

Поведение vs старый код:
- `AddRoomViewState.Initial` исчезает — все поля State имеют default-значения, `State()` достаточен.
- `TextFieldState.errorMessage: String?` → `TextFieldState.errorKind: AddRoomErrorKind?`. UI разрешает в строку.
- Старые `AddRoomViewState.isError` / прочие битые флаги отсутствуют (их и не было).
- Старый `AddRoomAction.ShowSuccessMessage(message: String)` и `ShowErrorMessage(message: String)` → типизированные `Label.ShowSuccess(kind)` / `Label.ShowError(kind)`.

- [ ] **Step 3: Проверка сборки**

Run: `./gradlew :shared:feature-add-room:api:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL — старый `feature-add-room/impl` ещё ссылается на `dev.nonoxy.feature.rooms.api.models.Room` через api → потому что `kmpFeatureSetup` транзитивно открывает feature-rooms/api как `api()` зависимость; компилируется без правок.

- [ ] **Step 4: Commit**

```bash
git add shared/feature-add-room/api/
git commit -m "Phase 5: feature-add-room api — add AddRoomStore + switch to kmpFeatureSetup"
```

---

### Task 3: feature-add-room impl — создать domain (Executor + Reducer + StoreFactory) рядом со старым кодом

**Files:**
- Create: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/AddRoomExecutor.kt`
- Create: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/AddRoomReducer.kt`
- Create: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/AddRoomStoreFactory.kt`

**Контекст.** Создаём MVIKotlin domain рядом со старым `presentation/AddRoomViewModel.kt` — старый код пока живёт, новый код собирается параллельно. Executor чистый: никаких `getString()`/`StringProvider`. Бизнес-логика валидации возвращает `AddRoomErrorKind?`, не строку.

Пакет `impl.domain.*` — суффикс `.impl.*` будет применён ко всему `impl/` в Task 8 (отложено, чтобы пока не ломать импорты в старом коде, который ещё ссылается на `dev.nonoxy.feature.add_room.presentation.*`). Здесь сразу пишем под `impl.domain` — этот пакет ещё не существовал, конфликта нет.

- [ ] **Step 1: Создать `AddRoomStoreFactory.kt` (Action + Message + create())**

Файл `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/AddRoomStoreFactory.kt`:

```kotlin
package dev.nonoxy.feature.add_room.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.State
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher

internal class AddRoomStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(): AddRoomStore =
        object :
            AddRoomStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = AddRoomStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadExistingRooms),
                executorFactory = {
                    AddRoomExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                    )
                },
                reducer = AddRoomReducer(),
            ) {}

    internal sealed interface Action {
        data object LoadExistingRooms : Action
    }

    internal sealed interface Message {
        data class SetExistingRoomsData(
            val existingFloors: ImmutableList<Int>,
            val existingBedsCounts: ImmutableList<Int>,
        ) : Message

        data class SetFloorNumberInput(val value: String) : Message
        data class SetFloorNumberSelected(val floor: Int) : Message
        data object ToggleFloorInput : Message

        data class SetRoomNumberInput(val value: String) : Message

        data class SetBedsCountInput(val value: String) : Message
        data class SetBedsCountSelected(val bedsCount: Int) : Message
        data object ToggleBedsInput : Message

        data class SetValidationErrors(
            val floorNumberError: AddRoomErrorKindOrNull,
            val roomNumberError: AddRoomErrorKindOrNull,
            val bedsCountError: AddRoomErrorKindOrNull,
        ) : Message

        data class SetIsFormValid(val isValid: Boolean) : Message
        data class SetIsLoading(val isLoading: Boolean) : Message
    }

    /** Wrapper used because `Reducer.reduce` can't carry a triple of nullable values nicely
     *  without losing type-safety; also lets us swap to a Map<Field, ErrorKind?> later. */
    internal data class AddRoomErrorKindOrNull(val value: dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind?)
}
```

(`AddRoomErrorKindOrNull` — лёгкая обёртка, чтобы `Message.SetValidationErrors` оставалась типобезопасной без `Triple<X?, X?, X?>`. Можно заменить на map позже.)

- [ ] **Step 2: Создать `AddRoomReducer.kt`**

Файл `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/AddRoomReducer.kt`:

```kotlin
package dev.nonoxy.feature.add_room.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.State
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.Message

internal class AddRoomReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetExistingRoomsData -> copy(
            floorSelection = floorSelection.copy(existingFloors = msg.existingFloors),
            bedsSelection = bedsSelection.copy(existingBedsCounts = msg.existingBedsCounts),
            hasExistingRooms = true,
        )

        is Message.SetFloorNumberInput -> copy(
            floorSelection = floorSelection.copy(
                textField = floorSelection.textField.copy(value = msg.value, errorKind = null),
            ),
        )

        is Message.SetFloorNumberSelected -> copy(
            floorSelection = floorSelection.copy(
                textField = floorSelection.textField.copy(
                    value = msg.floor.toString(),
                    errorKind = null,
                ),
                showInput = false,
            ),
        )

        Message.ToggleFloorInput -> copy(
            floorSelection = floorSelection.copy(
                showInput = !floorSelection.showInput,
                textField = if (floorSelection.showInput) floorSelection.textField.copy(value = "") else floorSelection.textField,
            ),
        )

        is Message.SetRoomNumberInput -> copy(
            roomNumber = roomNumber.copy(value = msg.value, errorKind = null),
        )

        is Message.SetBedsCountInput -> copy(
            bedsSelection = bedsSelection.copy(
                textField = bedsSelection.textField.copy(value = msg.value, errorKind = null),
            ),
        )

        is Message.SetBedsCountSelected -> copy(
            bedsSelection = bedsSelection.copy(
                textField = bedsSelection.textField.copy(
                    value = msg.bedsCount.toString(),
                    errorKind = null,
                ),
                showInput = false,
            ),
        )

        Message.ToggleBedsInput -> copy(
            bedsSelection = bedsSelection.copy(
                showInput = !bedsSelection.showInput,
                textField = if (bedsSelection.showInput) bedsSelection.textField.copy(value = "") else bedsSelection.textField,
            ),
        )

        is Message.SetValidationErrors -> copy(
            floorSelection = floorSelection.copy(
                textField = floorSelection.textField.copy(errorKind = msg.floorNumberError.value),
            ),
            roomNumber = roomNumber.copy(errorKind = msg.roomNumberError.value),
            bedsSelection = bedsSelection.copy(
                textField = bedsSelection.textField.copy(errorKind = msg.bedsCountError.value),
            ),
        )

        is Message.SetIsFormValid -> copy(isFormValid = msg.isValid)
        is Message.SetIsLoading -> copy(isLoading = msg.isLoading)
    }
}
```

- [ ] **Step 3: Создать `AddRoomExecutor.kt`**

Файл `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/AddRoomExecutor.kt`:

```kotlin
package dev.nonoxy.feature.add_room.impl.domain

import dev.nonoxy.common.utils.isDigitsOnly
import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Label
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.State
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.Action
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.AddRoomErrorKindOrNull
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory.Message
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineDispatcher

internal class AddRoomExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadExistingRooms -> loadExistingRoomsData()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            is Intent.OnFloorNumberInputValueChange -> handleFloorNumberInput(intent.floorNumber)
            is Intent.OnFloorNumberSelect -> handleFloorNumberSelect(intent.floorNumber)
            is Intent.OnRoomNumberInputValueChange -> handleRoomNumberInput(intent.roomNumber)
            is Intent.OnBedsCountInputValueChange -> handleBedsCountInput(intent.bedsCount)
            is Intent.OnBedsCountSelect -> handleBedsCountSelect(intent.bedsCount)
            Intent.OnCreateRoomClick -> handleCreateRoom()
            Intent.OnCancelClick -> publish(Label.CloseScreen)
            Intent.OnToggleFloorInput -> dispatch(Message.ToggleFloorInput)
            Intent.OnToggleBedsInput -> dispatch(Message.ToggleBedsInput)
        }
    }

    private suspend fun loadExistingRoomsData() {
        val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        if (existingRooms.isEmpty()) return

        val existingFloors = existingRooms
            .map { it.floorNumber }
            .distinct()
            .sorted()
            .toPersistentList()

        val existingBedsCounts = existingRooms
            .map { it.bedsCount }
            .distinct()
            .sorted()
            .toPersistentList()

        dispatch(
            Message.SetExistingRoomsData(
                existingFloors = existingFloors,
                existingBedsCounts = existingBedsCounts,
            )
        )
    }

    private fun handleFloorNumberInput(value: String) {
        if (!value.isDigitsOnly()) return
        dispatch(Message.SetFloorNumberInput(value = value))
        revalidateForm()
    }

    private fun handleFloorNumberSelect(floor: Int) {
        dispatch(Message.SetFloorNumberSelected(floor = floor))
        revalidateForm()
    }

    private fun handleRoomNumberInput(value: String) {
        if (!value.isDigitsOnly()) return
        dispatch(Message.SetRoomNumberInput(value = value))
        revalidateForm()
    }

    private fun handleBedsCountInput(value: String) {
        if (!value.isDigitsOnly()) return
        dispatch(Message.SetBedsCountInput(value = value))
        revalidateForm()
    }

    private fun handleBedsCountSelect(bedsCount: Int) {
        dispatch(Message.SetBedsCountSelected(bedsCount = bedsCount))
        revalidateForm()
    }

    /** Inline form revalidation on every input — same as old AddRoomViewModel.validateFormOnInput().
     *  Only updates the isFormValid flag (does not surface errors in textfields). */
    private fun revalidateForm() {
        val current = state()
        val isValid = validateFloorNumber(current.floorSelection.textField.value) == null &&
            validateRoomNumber(current.roomNumber.value) == null &&
            validateBedsCount(current.bedsSelection.textField.value) == null
        dispatch(Message.SetIsFormValid(isValid = isValid))
    }

    private suspend fun handleCreateRoom() {
        dispatch(Message.SetIsLoading(isLoading = true))

        val current = state()
        val floorError = validateFloorNumber(current.floorSelection.textField.value)
        val roomError = validateRoomNumber(current.roomNumber.value)
        val bedsError = validateBedsCount(current.bedsSelection.textField.value)

        dispatch(
            Message.SetValidationErrors(
                floorNumberError = AddRoomErrorKindOrNull(floorError),
                roomNumberError = AddRoomErrorKindOrNull(roomError),
                bedsCountError = AddRoomErrorKindOrNull(bedsError),
            )
        )

        if (floorError != null || roomError != null || bedsError != null) {
            dispatch(Message.SetIsFormValid(isValid = false))
            dispatch(Message.SetIsLoading(isLoading = false))
            return
        }
        dispatch(Message.SetIsFormValid(isValid = true))

        val floorNumber = current.floorSelection.textField.value.toInt()
        val roomNumber = current.roomNumber.value.toInt()
        val bedsCount = current.bedsSelection.textField.value.toInt()

        val existingRooms = roomsRepository.getAllRooms().getOrElse { emptyList() }
        val duplicateExists = existingRooms.any { room ->
            room.floorNumber == floorNumber && room.roomNumber == roomNumber
        }
        if (duplicateExists) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(
                Label.ShowError(
                    kind = AddRoomErrorKind.RoomAlreadyExists(
                        roomNumber = roomNumber,
                        floorNumber = floorNumber,
                    )
                )
            )
            return
        }

        val newRoom = Room(
            floorNumber = floorNumber,
            roomNumber = roomNumber,
            bedsCount = bedsCount,
            students = emptyList(),
        )

        roomsRepository.saveRoom(newRoom).fold(
            onSuccess = {
                roomsRepository.saveDraftRoom(newRoom).fold(
                    onSuccess = {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.NavigateToManageStudentsDraftRoom)
                    },
                    onFailure = {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(
                            Label.ShowSuccess(
                                kind = AddRoomSuccessKind.RoomCreated(roomNumber = roomNumber)
                            )
                        )
                    },
                )
            },
            onFailure = {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = AddRoomErrorKind.SaveFailed))
            },
        )
    }

    private fun validateFloorNumber(value: String): AddRoomErrorKind? =
        if (value.isBlank()) AddRoomErrorKind.FloorNumberRequired else null

    private fun validateRoomNumber(value: String): AddRoomErrorKind? =
        if (value.isBlank()) AddRoomErrorKind.RoomNumberRequired else null

    private fun validateBedsCount(value: String): AddRoomErrorKind? =
        if (value.isBlank()) AddRoomErrorKind.BedsCountRequired else null
}
```

Поведенческие отличия от старого AddRoomViewModel:
- Старый `try { ... } catch (_: Exception) { ShowErrorMessage(unknown) }` в `handleOnCreateRoomClick` убран — Executor не имеет шанса упасть на ресурсах (их больше нет). Если `roomsRepository.saveRoom` сам бросит exception — это пройдёт в scope как onFailure, обработано. `UnknownError` остаётся в `AddRoomErrorKind` на случай явных вызовов из UI (например, для будущих retries), но из Executor больше не публикуется.
- Старый код после `saveDraftRoom().onFailure` показывал `ShowSuccessMessage("...")` (тогда draft не сохранился, но комната создана). Это сохраняется через `Label.ShowSuccess(RoomCreated(...))`. Старый код после `saveDraftRoom().onSuccess` сразу публиковал `NavigateToManageStudentsDraftRoom` — тоже сохраняется.

- [ ] **Step 4: Проверка сборки**

Run: `./gradlew :shared:feature-add-room:impl:assemble`
Expected: BUILD SUCCESSFUL — старый `AddRoomViewModel.kt` ещё на месте, новый domain собран рядом.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/domain/
git commit -m "Phase 5: feature-add-room impl — add MVIKotlin domain (Executor + Reducer + StoreFactory) alongside old MVI"
```

---

### Task 4: feature-add-room presentation — ViewModel + UiState + UiLabel + mappers + DI

**Files:**
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/UiAddRoomState.kt`
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/UiAddRoomLabel.kt`
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/mappers/UiAddRoomStateMapper.kt`
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/mappers/UiAddRoomLabelMapper.kt`
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/AddRoomViewModel.kt`
- Create: `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/di/FeatureAddRoomPresentationModule.kt`
- Delete: `shared/feature-add-room/presentation/src/commonMain/kotlin/.gitkeep`

**Контекст.** Аналог Task 5 из Phase 4 — наполняем новый модуль `presentation/`. ViewModel наследует `BaseViewModel<UiAddRoomState, UiAddRoomLabel>` из `core-presentation`, биндит Store и предоставляет per-handler методы для UI. Mappers — plain (не `@Composable`) и работают только с типизированными `ErrorKind`/`SuccessKind` (никаких строк). State-маппинг — почти идентичность (UiState структурно совпадает со Store.State, но переиспользует те же datalcasses из api), label-маппинг — переключение sealed-веток.

Поскольку `AddRoomStore.State` и `UiAddRoomState` структурно совпадают, можно было бы сделать `typealias UiAddRoomState = AddRoomStore.State` — но это нарушает паттерн KMMTemplate (`UiXxxState` всегда отдельный тип, ui не видит api напрямую). Делаем отдельный `UiAddRoomState` как тонкую обёртку с теми же полями: `presentation` модуль зависит от `api` (через `kmpFeatureSetup` транзитивно — нет, не зависит! см. ниже).

**Важный нюанс зависимостей:** `kmpFeatureSetup` для presentation-модуля **не** автоматически добавляет `api()` зависимость на feature-api. Нужно добавить вручную в `build.gradle.kts` через `implementations(projects.shared.featureAddRoom.api)` — без этого `UiAddRoomStateMapper.map(item: AddRoomStore.State)` не скомпилируется.

- [ ] **Step 1: Доточить `shared/feature-add-room/presentation/build.gradle.kts` — добавить зависимость на api**

Заменить содержимое целиком на:

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
        projects.shared.featureAddRoom.api,
    )
}
```

Сравни с feature-rooms/presentation/build.gradle.kts (Phase 4) — там нет явной зависимости на `feature-rooms/api`. Почему? Проверь `KmpFeatureSetupPlugin.kt`: `implModuleDependencies` добавляет `project.getApiModule()` только когда `!isApiModule` И **`isImplModule`** не вычисляется явно — на самом деле `implModuleDependencies` навешивается на **любой не-api** модуль, включая presentation/ui. То есть feature-rooms/presentation получает feature-rooms/api автоматически. **Проверь это до Step 2** и убери ручную зависимость, если оно само подтянется:

```bash
grep -n "implModuleDependencies\|isApiModule\|isImplModule" build-logic/src/main/kotlin/plugins/KmpFeatureSetupPlugin.kt
```

Ожидаемый вывод (текущая логика):
```
implModuleDependencies = when (project.isApiModule) {
    true -> null
    false -> listOfNotNull(project(":shared:common"), project.getApiModule())
}
```

Значит presentation и ui модули **автоматически получают `:shared:common` и api-собрата** через `implementations(...)`. Тогда **ручную строку `projects.shared.featureAddRoom.api` можно убрать**. Финальный вариант `build.gradle.kts`:

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
    )
}
```

(Идентично feature-rooms/presentation/build.gradle.kts после Phase 4.)

- [ ] **Step 2: Создать `UiAddRoomState.kt`**

Файл `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/UiAddRoomState.kt`:

```kotlin
package dev.nonoxy.feature.add_room.presentation.models

import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiAddRoomState(
    val floorSelection: FloorSelection = FloorSelection(),
    val roomNumber: TextField = TextField(),
    val bedsSelection: BedsSelection = BedsSelection(),
    val isLoading: Boolean = false,
    val isFormValid: Boolean = false,
    val hasExistingRooms: Boolean = false,
) {
    data class TextField(
        val value: String = "",
        val errorKind: AddRoomErrorKind? = null,
    )

    data class FloorSelection(
        val textField: TextField = TextField(),
        val existingFloors: ImmutableList<Int> = persistentListOf(),
        val showInput: Boolean = false,
    )

    data class BedsSelection(
        val textField: TextField = TextField(),
        val existingBedsCounts: ImmutableList<Int> = persistentListOf(),
        val showInput: Boolean = false,
    )
}
```

Структурный близнец `AddRoomStore.State`, но: (1) в собственном пакете presentation; (2) `TextField` — простое имя (без префикса), потому что весь набор UiState теперь живёт в `presentation/models`; (3) `AddRoomErrorKind` тащим из api — это допустимо, presentation видит api.

- [ ] **Step 3: Создать `UiAddRoomLabel.kt`**

Файл `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/UiAddRoomLabel.kt`:

```kotlin
package dev.nonoxy.feature.add_room.presentation.models

import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind

sealed interface UiAddRoomLabel {
    data object CloseScreen : UiAddRoomLabel
    data object NavigateToManageStudentsDraftRoom : UiAddRoomLabel
    data class ShowSuccess(val kind: AddRoomSuccessKind) : UiAddRoomLabel
    data class ShowError(val kind: AddRoomErrorKind) : UiAddRoomLabel
}
```

- [ ] **Step 4: Создать `UiAddRoomStateMapper.kt`**

Файл `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/mappers/UiAddRoomStateMapper.kt`:

```kotlin
package dev.nonoxy.feature.add_room.presentation.mappers

import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState

internal interface UiAddRoomStateMapper {
    fun map(item: AddRoomStore.State): UiAddRoomState
}

internal class UiAddRoomStateMapperImpl : UiAddRoomStateMapper {

    override fun map(item: AddRoomStore.State): UiAddRoomState = UiAddRoomState(
        floorSelection = UiAddRoomState.FloorSelection(
            textField = UiAddRoomState.TextField(
                value = item.floorSelection.textField.value,
                errorKind = item.floorSelection.textField.errorKind,
            ),
            existingFloors = item.floorSelection.existingFloors,
            showInput = item.floorSelection.showInput,
        ),
        roomNumber = UiAddRoomState.TextField(
            value = item.roomNumber.value,
            errorKind = item.roomNumber.errorKind,
        ),
        bedsSelection = UiAddRoomState.BedsSelection(
            textField = UiAddRoomState.TextField(
                value = item.bedsSelection.textField.value,
                errorKind = item.bedsSelection.textField.errorKind,
            ),
            existingBedsCounts = item.bedsSelection.existingBedsCounts,
            showInput = item.bedsSelection.showInput,
        ),
        isLoading = item.isLoading,
        isFormValid = item.isFormValid,
        hasExistingRooms = item.hasExistingRooms,
    )
}
```

- [ ] **Step 5: Создать `UiAddRoomLabelMapper.kt`**

Файл `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/mappers/UiAddRoomLabelMapper.kt`:

```kotlin
package dev.nonoxy.feature.add_room.presentation.mappers

import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomLabel

internal interface UiAddRoomLabelMapper {
    fun map(item: AddRoomStore.Label): UiAddRoomLabel
}

internal class UiAddRoomLabelMapperImpl : UiAddRoomLabelMapper {

    override fun map(item: AddRoomStore.Label): UiAddRoomLabel = when (item) {
        AddRoomStore.Label.CloseScreen -> UiAddRoomLabel.CloseScreen
        AddRoomStore.Label.NavigateToManageStudentsDraftRoom -> UiAddRoomLabel.NavigateToManageStudentsDraftRoom
        is AddRoomStore.Label.ShowSuccess -> UiAddRoomLabel.ShowSuccess(kind = item.kind)
        is AddRoomStore.Label.ShowError -> UiAddRoomLabel.ShowError(kind = item.kind)
    }
}
```

- [ ] **Step 6: Создать `AddRoomViewModel.kt`**

Файл `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/AddRoomViewModel.kt`:

```kotlin
package dev.nonoxy.feature.add_room.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.api.store.AddRoomStore.Intent
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomLabelMapper
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomStateMapper
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomLabel
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class AddRoomViewModel internal constructor(
    private val store: AddRoomStore,
    private val stateMapper: UiAddRoomStateMapper,
    private val labelMapper: UiAddRoomLabelMapper,
) : BaseViewModel<UiAddRoomState, UiAddRoomLabel>(initialState = UiAddRoomState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onFloorNumberInputValueChange(value: String) =
        store.accept(Intent.OnFloorNumberInputValueChange(floorNumber = value))

    fun onFloorNumberSelect(floor: Int) =
        store.accept(Intent.OnFloorNumberSelect(floorNumber = floor))

    fun onRoomNumberInputValueChange(value: String) =
        store.accept(Intent.OnRoomNumberInputValueChange(roomNumber = value))

    fun onBedsCountInputValueChange(value: String) =
        store.accept(Intent.OnBedsCountInputValueChange(bedsCount = value))

    fun onBedsCountSelect(bedsCount: Int) =
        store.accept(Intent.OnBedsCountSelect(bedsCount = bedsCount))

    fun onCreateRoomClick() = store.accept(Intent.OnCreateRoomClick)

    fun onCancelClick() = store.accept(Intent.OnCancelClick)

    fun onToggleFloorInput() = store.accept(Intent.OnToggleFloorInput)

    fun onToggleBedsInput() = store.accept(Intent.OnToggleBedsInput)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
```

- [ ] **Step 7: Создать `FeatureAddRoomPresentationModule.kt`**

Файл `shared/feature-add-room/presentation/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/di/FeatureAddRoomPresentationModule.kt`:

```kotlin
package dev.nonoxy.feature.add_room.presentation.di

import dev.nonoxy.feature.add_room.presentation.AddRoomViewModel
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomLabelMapper
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomLabelMapperImpl
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomStateMapper
import dev.nonoxy.feature.add_room.presentation.mappers.UiAddRoomStateMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAddRoomPresentationModule = module {

    factoryOf<UiAddRoomStateMapper>(::UiAddRoomStateMapperImpl)
    factoryOf<UiAddRoomLabelMapper>(::UiAddRoomLabelMapperImpl)

    viewModelOf(::AddRoomViewModel)
}
```

- [ ] **Step 8: Удалить `.gitkeep`**

```bash
git rm shared/feature-add-room/presentation/src/commonMain/kotlin/.gitkeep
```

- [ ] **Step 9: Проверка сборки**

Run: `./gradlew :shared:feature-add-room:presentation:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL (presentation модуль ещё не подключён к Koin — это будет в Task 6 — но изолированно компилируется).

- [ ] **Step 10: Commit**

```bash
git add shared/feature-add-room/presentation/
git commit -m "Phase 5: feature-add-room presentation — ViewModel, UiState, UiLabel, mappers, DI"
```

---

### Task 5: feature-add-room ui — Screen + views + composeResources move + Screen API + Message mapping

**Files:**
- Move: `shared/feature-add-room/impl/src/commonMain/composeResources/` → `shared/feature-add-room/ui/src/commonMain/composeResources/`
- Move: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/` → `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/`
- Move: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation/FeatureAddRoomNavigation.kt` → `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/api/FeatureAddRoomScreenApi.kt`
- Modify: все 7 файлов под `ui/` (Screen + 6 views) — заменить старые `AddRoomEvent` / `AddRoomViewState` / `AddRoomAction` / `AddRoomViewModel` (impl) на новые из `presentation`, переключить на per-handler callbacks
- Create: `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/AddRoomMessageMapping.kt`
- Delete: `shared/feature-add-room/ui/src/commonMain/kotlin/.gitkeep`

**Контекст.** Самая объёмная задача — переезд UI. План:
1. Перенос файлов через `git mv` (сохраняет историю).
2. Поправка пакетов (`...add_room.ui` сохраняется, только корневой `impl/` → `ui/`).
3. Удаление `onObtainEvent: (AddRoomEvent) -> Unit` — заменить на per-handler callbacks; `AddRoomEvent` доступа из ui больше нет (он в api как `AddRoomStore.Intent`, но ui его не видит).
4. `AddRoomScreen` биндится на новый `AddRoomViewModel`, через `viewModel.label.CollectFlow {...}` обрабатывает `UiAddRoomLabel.ShowSuccess/ShowError`.
5. `AddRoomMessageMapping.kt` — `@Composable` функции, переводящие `AddRoomErrorKind`/`AddRoomSuccessKind` в строки через `stringResource(...)` или `getString(...)` (для snackbar).

- [ ] **Step 1: Переместить compose-resources из impl в ui**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-add-room/impl/src/commonMain/composeResources \
       shared/feature-add-room/ui/src/commonMain/composeResources
```

(После этого `impl` больше не содержит `composeResources/` — в Task 8 уберём блок `compose.resources` и плагин из `impl/build.gradle.kts`.)

- [ ] **Step 2: Переместить директорию `ui/` целиком**

```bash
git mv shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui \
       shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui
```

Пакеты в перенесённых файлах остаются `dev.nonoxy.feature.add_room.ui.*` — править ничего не нужно.

- [ ] **Step 3: Переместить и переименовать navigation → Screen API**

```bash
mkdir -p shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/api
git mv shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation/FeatureAddRoomNavigation.kt \
       shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/api/FeatureAddRoomScreenApi.kt
rmdir shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation
```

- [ ] **Step 4: Переписать `FeatureAddRoomScreenApi.kt`**

Полное новое содержимое `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/api/FeatureAddRoomScreenApi.kt`:

```kotlin
package dev.nonoxy.feature.add_room.ui.api

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import dev.nonoxy.core.navigation.AddRoomRoute
import dev.nonoxy.core.navigation.bottomsheet.ModalBottomSheetConfiguration
import dev.nonoxy.core.navigation.bottomsheet.bottomSheet
import dev.nonoxy.core.navigation.navigateOnResumed
import dev.nonoxy.feature.add_room.ui.AddRoomScreen

fun NavController.navigateToAddRoomScreen() {
    navigateOnResumed(AddRoomRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetAddRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageStudentsDraftRoom: () -> Unit,
) {
    bottomSheet<AddRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) {
        AddRoomScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToManageStudentsDraftRoom = onNavigateToManageStudentsDraftRoom,
        )
    }
}
```

Изменения:
- `launchSingleTop = true` через `navigateOnResumed(...)` (как в feature-rooms ScreenApi после Phase 4 — этот helper уже сам делает `launchSingleTop`).
- Параметр backStackEntry в lambda больше не нужен — он не использовался.

- [ ] **Step 5: Создать `AddRoomMessageMapping.kt`**

Файл `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/AddRoomMessageMapping.kt`:

```kotlin
package dev.nonoxy.feature.add_room.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource as androidxStringResource
import dev.nonoxy.feature.add_room.api.store.AddRoomErrorKind
import dev.nonoxy.feature.add_room.api.store.AddRoomSuccessKind
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import residetrack.shared.feature_add_room.ui.generated.resources.Res
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_error_room_already_exists
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_error_save_failed
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_error_unknown
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_success_message
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_validation_beds_count_required
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_validation_floor_number_required
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_validation_room_number_required

/** For inline error rendering inside @Composable views (TextField supportingText etc.). */
@Composable
internal fun AddRoomErrorKind.localized(): String = when (this) {
    AddRoomErrorKind.UnknownError -> stringResource(Res.string.add_room_error_unknown)
    AddRoomErrorKind.SaveFailed -> stringResource(Res.string.add_room_error_save_failed)
    is AddRoomErrorKind.RoomAlreadyExists ->
        stringResource(Res.string.add_room_error_room_already_exists, roomNumber, floorNumber)
    AddRoomErrorKind.FloorNumberRequired -> stringResource(Res.string.add_room_validation_floor_number_required)
    AddRoomErrorKind.RoomNumberRequired -> stringResource(Res.string.add_room_validation_room_number_required)
    AddRoomErrorKind.BedsCountRequired -> stringResource(Res.string.add_room_validation_beds_count_required)
}

/** For one-shot snackbars triggered from CollectFlow lambdas (suspend, non-Composable scope). */
internal suspend fun AddRoomErrorKind.localizedSuspend(): String = when (this) {
    AddRoomErrorKind.UnknownError -> getString(Res.string.add_room_error_unknown)
    AddRoomErrorKind.SaveFailed -> getString(Res.string.add_room_error_save_failed)
    is AddRoomErrorKind.RoomAlreadyExists ->
        getString(Res.string.add_room_error_room_already_exists, roomNumber, floorNumber)
    AddRoomErrorKind.FloorNumberRequired -> getString(Res.string.add_room_validation_floor_number_required)
    AddRoomErrorKind.RoomNumberRequired -> getString(Res.string.add_room_validation_room_number_required)
    AddRoomErrorKind.BedsCountRequired -> getString(Res.string.add_room_validation_beds_count_required)
}

internal suspend fun AddRoomSuccessKind.localizedSuspend(): String = when (this) {
    is AddRoomSuccessKind.RoomCreated -> getString(Res.string.add_room_success_message, roomNumber)
}
```

(Импорт `androidxStringResource` оставлен как пример паттерна, но не используется — удали при review, если detekt-unused-import зафиксит. Импорт правильный — `org.jetbrains.compose.resources.stringResource`.)

После завершения Task 5 (когда `ui/` собралась) сгенерированные `Res` будут лежать в пакете `residetrack.shared.feature_add_room.ui.generated.resources` (а не `.impl.generated.resources` — потому что resources переехали в `ui/`-модуль с namespace `dev.nonoxy.feature.add_room.ui`).

- [ ] **Step 6: Переписать `AddRoomScreen.kt`**

Полное новое содержимое `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/AddRoomScreen.kt`:

```kotlin
package dev.nonoxy.feature.add_room.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.feature.add_room.presentation.AddRoomViewModel
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomLabel
import dev.nonoxy.feature.add_room.ui.views.AddRoomScreenContent
import dev.nonoxy.residetrack.common.ui.common.dialog.DialogScaffold
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun AddRoomScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageStudentsDraftRoom: () -> Unit,
    viewModel: AddRoomViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiAddRoomLabel.CloseScreen -> onNavigateBack()
            UiAddRoomLabel.NavigateToManageStudentsDraftRoom -> onNavigateToManageStudentsDraftRoom()
            is UiAddRoomLabel.ShowSuccess -> {
                currentSnackbarType = SnackbarType.INFO
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localizedSuspend(),
                    withDismissAction = true,
                )
            }
            is UiAddRoomLabel.ShowError -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localizedSuspend(),
                    withDismissAction = true,
                )
            }
        }
    }

    DialogScaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    when (currentSnackbarType) {
                        SnackbarType.ERROR -> ResideTrackErrorSnackbar(snackbarData = snackbarData)
                        SnackbarType.INFO -> ResideTrackSnackbar(snackbarData = snackbarData)
                        else -> null
                    }
                },
            )
        },
    ) { paddingValues ->
        AddRoomScreenContent(
            modifier = Modifier.fillMaxWidth().padding(paddingValues),
            state = state,
            onFloorNumberInputValueChange = viewModel::onFloorNumberInputValueChange,
            onFloorNumberSelect = viewModel::onFloorNumberSelect,
            onRoomNumberInputValueChange = viewModel::onRoomNumberInputValueChange,
            onBedsCountInputValueChange = viewModel::onBedsCountInputValueChange,
            onBedsCountSelect = viewModel::onBedsCountSelect,
            onCreateRoomClick = viewModel::onCreateRoomClick,
            onCancelClick = viewModel::onCancelClick,
            onToggleFloorInput = viewModel::onToggleFloorInput,
            onToggleBedsInput = viewModel::onToggleBedsInput,
        )
    }
}
```

- [ ] **Step 7: Переписать `AddRoomScreenContent.kt` под per-handler callbacks и новый `UiAddRoomState`**

Полное новое содержимое `shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/views/AddRoomScreenContent.kt`:

```kotlin
package dev.nonoxy.feature.add_room.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState
import dev.nonoxy.residetrack.common.ui.theme.ResideTrackTheme
import dev.nonoxy.residetrack.common.ui.theme.padding_size_16
import dev.nonoxy.residetrack.common.ui.theme.padding_size_20
import dev.nonoxy.residetrack.common.ui.theme.padding_size_24
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import residetrack.shared.feature_add_room.ui.generated.resources.Res
import residetrack.shared.feature_add_room.ui.generated.resources.add_room_title

@Composable
internal fun AddRoomScreenContent(
    state: UiAddRoomState,
    onFloorNumberInputValueChange: (String) -> Unit,
    onFloorNumberSelect: (Int) -> Unit,
    onRoomNumberInputValueChange: (String) -> Unit,
    onBedsCountInputValueChange: (String) -> Unit,
    onBedsCountSelect: (Int) -> Unit,
    onCreateRoomClick: () -> Unit,
    onCancelClick: () -> Unit,
    onToggleFloorInput: () -> Unit,
    onToggleBedsInput: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = padding_size_24),
        verticalArrangement = Arrangement.spacedBy(padding_size_20),
    ) {
        Text(
            text = stringResource(Res.string.add_room_title),
            style = ResideTrackTheme.typography.head2.copy(
                color = ResideTrackTheme.colors.textPrimary,
            ),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(padding_size_16),
        ) {
            FloorSelectionSection(
                textFieldState = state.floorSelection.textField,
                existingFloors = state.floorSelection.existingFloors,
                showInput = state.floorSelection.showInput,
                hasExistingRooms = state.hasExistingRooms,
                isLoading = state.isLoading,
                onInputValueChange = onFloorNumberInputValueChange,
                onFloorSelect = onFloorNumberSelect,
                onToggleInput = onToggleFloorInput,
            )

            RoomNumberSection(
                textFieldState = state.roomNumber,
                isLoading = state.isLoading,
                onInputValueChange = onRoomNumberInputValueChange,
            )

            BedsSelectionSection(
                textFieldState = state.bedsSelection.textField,
                existingBedsCounts = state.bedsSelection.existingBedsCounts,
                showInput = state.bedsSelection.showInput,
                isLoading = state.isLoading,
                onInputValueChange = onBedsCountInputValueChange,
                onBedsSelect = onBedsCountSelect,
                onToggleInput = onToggleBedsInput,
            )
        }

        Spacer(modifier = Modifier.height(padding_size_16))

        AddRoomActionsSection(
            isFormValid = state.isFormValid,
            isLoading = state.isLoading,
            onCancelClick = onCancelClick,
            onCreateClick = onCreateRoomClick,
        )
    }
}

@Preview
@Composable
private fun AddRoomContentPreview() {
    ResideTrackTheme {
        AddRoomScreenContent(
            state = UiAddRoomState(
                floorSelection = UiAddRoomState.FloorSelection(
                    textField = UiAddRoomState.TextField(value = "3"),
                    existingFloors = persistentListOf(1, 2, 3, 4, 5),
                ),
                roomNumber = UiAddRoomState.TextField(value = "301"),
                bedsSelection = UiAddRoomState.BedsSelection(
                    textField = UiAddRoomState.TextField(value = "2"),
                    existingBedsCounts = persistentListOf(1, 2, 3, 4),
                ),
                isFormValid = true,
                hasExistingRooms = true,
            ),
            onFloorNumberInputValueChange = {},
            onFloorNumberSelect = {},
            onRoomNumberInputValueChange = {},
            onBedsCountInputValueChange = {},
            onBedsCountSelect = {},
            onCreateRoomClick = {},
            onCancelClick = {},
            onToggleFloorInput = {},
            onToggleBedsInput = {},
        )
    }
}
```

- [ ] **Step 8: Поправить остальные 5 views — заменить старые типы на UiAddRoomState.*, исправить импорт Res**

Для каждого из файлов:
- `views/FloorSelectionSection.kt`
- `views/RoomNumberSection.kt`
- `views/BedsSelectionSection.kt`
- `views/AddRoomActionsSection.kt`
- `views/SelectionChips.kt`

Действия:
1. Все импорты `residetrack.shared.feature_add_room.impl.generated.resources.*` → `residetrack.shared.feature_add_room.ui.generated.resources.*` (`sed` или вручную).
2. Все упоминания `TextFieldState` (старого из `presentation.models`) → `UiAddRoomState.TextField`. Импорт `dev.nonoxy.feature.add_room.presentation.models.TextFieldState` → `dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState`.
3. Если в views используется `errorMessage: String?` — заменить на `errorKind?.localized()` (вызвать @Composable extension из `AddRoomMessageMapping.kt`).

Команда для массовой замены импорта Res:

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
find shared/feature-add-room/ui/src/commonMain/kotlin -name "*.kt" -exec sed -i '' \
  's|residetrack\.shared\.feature_add_room\.impl\.generated\.resources|residetrack.shared.feature_add_room.ui.generated.resources|g' \
  {} +
```

Для замены `TextFieldState` на `UiAddRoomState.TextField`:

```bash
find shared/feature-add-room/ui/src/commonMain/kotlin -name "*.kt" -exec sed -i '' \
  -e 's|import dev\.nonoxy\.feature\.add_room\.presentation\.models\.TextFieldState|import dev.nonoxy.feature.add_room.presentation.models.UiAddRoomState|g' \
  -e 's|: TextFieldState|: UiAddRoomState.TextField|g' \
  -e 's|TextFieldState(|UiAddRoomState.TextField(|g' \
  {} +
```

Затем вручную проверить каждую вьюшку: если она читает `textFieldState.errorMessage`, заменить на `textFieldState.errorKind?.localized()` и добавить импорт `import dev.nonoxy.feature.add_room.ui.localized`.

**Конкретно по AddRoomActionsSection / FloorSelectionSection / RoomNumberSection / BedsSelectionSection** — проверь, есть ли в них валидация / supportingText с `errorMessage`. Если нет (все используют textField.value напрямую) — массовая замена выше достаточна.

```bash
grep -n "errorMessage" shared/feature-add-room/ui/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui/views/*.kt
```

Для каждого совпадения — ручная правка по шаблону:

Старое:
```kotlin
supportingText = textFieldState.errorMessage?.let { msg ->
    { Text(msg) }
}
```

Новое:
```kotlin
supportingText = textFieldState.errorKind?.let { kind ->
    { Text(kind.localized()) }
}
```

С новым импортом `import dev.nonoxy.feature.add_room.ui.localized`.

- [ ] **Step 9: Удалить `.gitkeep` в ui/**

```bash
git rm shared/feature-add-room/ui/src/commonMain/kotlin/.gitkeep
```

- [ ] **Step 10: Проверка сборки**

Run: `./gradlew :shared:feature-add-room:ui:assemble`
Expected: BUILD SUCCESSFUL. Если падает «не найден `Res`» — проверь что в `ui/build.gradle.kts` есть блок `compose.resources { ... }` (он там есть с Task 1, Step 4).

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL — старый ui код в `impl/` уже не существует (его перенесли), но `impl/presentation/` ещё ссылается на `dev.nonoxy.feature.add_room.ui.AddRoomScreen` через старый navigation. **Этот импорт ушёл вместе с navigation файлом в Step 3 — старый `impl/presentation/navigation/` директория уже удалена**. Остался `impl/presentation/AddRoomViewModel.kt`, который компилится сам по себе (не зависит от ui). Должно собраться.

Если упадёт — проверь `git status` и убедись что не осталось дубликатов.

- [ ] **Step 11: Commit**

```bash
git add shared/feature-add-room/ui/ \
        shared/feature-add-room/impl/src/commonMain/composeResources \
        shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/ui \
        shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation
git commit -m "Phase 5: feature-add-room ui — Screen + views + composeResources move + Screen API + ErrorKind localization"
```

---

### Task 6: Wire feature-add-room presentation+ui to :shared:main (Koin + NavHost)

**Files:**
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt`

**Контекст.** Аналог Task 7 из Phase 4. После этого шага NavHost открывает новый `AddRoomScreen` (из ui), Koin отдаёт новый `AddRoomViewModel` (из presentation). Старый `AddRoomViewModel` в `impl/presentation/` ещё существует и зарегистрирован в `featureAddRoomImplModule` — но больше никем не запрашивается (новый ViewModel зарегистрирован в `featureAddRoomPresentationModule` под тем же типом `AddRoomViewModel`, но из другого пакета). Удалим старое в Task 7.

**Конфликт имён:** Старый класс `dev.nonoxy.feature.add_room.presentation.AddRoomViewModel` (в impl) и новый `dev.nonoxy.feature.add_room.presentation.AddRoomViewModel` (в presentation модуле) **имеют одинаковое полное имя** — оба в пакете `dev.nonoxy.feature.add_room.presentation`. Это вызовет конфликт классов при сборке `:shared:main` (одно и то же FQN в двух модулях).

Решение: перед Task 6 нужно сначала переименовать ИЛИ удалить старый `AddRoomViewModel` в impl. Чтобы сохранить «alongside»-подход — переименуем старый класс в `OldAddRoomViewModel` на время и так же его DI:

- [ ] **Step 1: Переименовать старый класс в `OldAddRoomViewModel`**

В файле `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/AddRoomViewModel.kt` заменить:

Старое (строка ~27):
```kotlin
internal class AddRoomViewModel(
```

Новое:
```kotlin
internal class OldAddRoomViewModel(
```

И в `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/di/FeatureAddRoomImplModule.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.add_room.presentation.AddRoomViewModel
...
val featureAddRoomImplModule = module {

    viewModelOf(::AddRoomViewModel)
}
```

Новое:
```kotlin
import dev.nonoxy.feature.add_room.presentation.OldAddRoomViewModel
...
val featureAddRoomImplModule = module {

    // TODO Task 7: remove together with OldAddRoomViewModel.
    viewModelOf(::OldAddRoomViewModel)
}
```

Переименовать файл тоже (для аккуратности и быстрого ориентирования):

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/AddRoomViewModel.kt \
       shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/OldAddRoomViewModel.kt
```

Старый Koin-bean больше никто не запрашивает (NavHost после Task 6 пойдёт через новый presentation-модуль), но Koin не возражает против неиспользуемых beans.

- [ ] **Step 2: Зарегистрировать `featureAddRoomPresentationModule` в Koin.kt**

В `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

Старое (фрагмент):
```kotlin
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.rooms.impl.di.featureRoomsImplModule
import dev.nonoxy.feature.rooms.presentation.di.featureRoomsPresentationModule
...
            featureRoomsImplModule,
            featureRoomsPresentationModule,

            featureAddRoomImplModule,
            featureManageStudentsImplModule,
```

Новое:
```kotlin
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
import dev.nonoxy.feature.add_room.presentation.di.featureAddRoomPresentationModule
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.rooms.impl.di.featureRoomsImplModule
import dev.nonoxy.feature.rooms.presentation.di.featureRoomsPresentationModule
...
            featureRoomsImplModule,
            featureRoomsPresentationModule,

            featureAddRoomImplModule,
            featureAddRoomPresentationModule,
            featureManageStudentsImplModule,
```

- [ ] **Step 3: Переключить NavHost на новый Screen API**

В `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.add_room.presentation.navigation.bottomSheetAddRoomScreen
import dev.nonoxy.feature.add_room.presentation.navigation.navigateToAddRoomScreen
```

Новое:
```kotlin
import dev.nonoxy.feature.add_room.ui.api.bottomSheetAddRoomScreen
import dev.nonoxy.feature.add_room.ui.api.navigateToAddRoomScreen
```

(Тело NavHost — `bottomSheetAddRoomScreen(...)` и `navigateToAddRoomScreen()` — остаётся неизменным, сигнатуры идентичны.)

- [ ] **Step 4: Зарегистрировать Store-фабрику в featureAddRoomImplModule**

Сейчас `featureAddRoomImplModule` (после step 1) содержит только `OldAddRoomViewModel`. Чтобы новый `AddRoomViewModel` (из presentation) смог создаться, Koin должен уметь резолвить `AddRoomStore` — а его создаёт `AddRoomStoreFactory` из impl.

Полный новый `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/di/FeatureAddRoomImplModule.kt`:

```kotlin
package dev.nonoxy.feature.add_room.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory
import dev.nonoxy.feature.add_room.presentation.OldAddRoomViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureAddRoomImplModule = module {

    factory<AddRoomStore> {
        AddRoomStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }

    // TODO Task 7: remove together with OldAddRoomViewModel.
    viewModelOf(::OldAddRoomViewModel)
}
```

- [ ] **Step 5: Проверка сборки + ручная сверка**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

Run приложение на эмуляторе → открыть Rooms → нажать «Добавить комнату» → ввести этаж/номер/мест → «Создать» → snackbar успеха + автопереход на Manage Students draft.

Ожидаемо: всё работает как раньше. Если экран не открывается / падает с Koin-ошибкой — проверь Step 2 и Step 4.

- [ ] **Step 6: Commit**

```bash
git add shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt \
        shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/ \
        shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/di/FeatureAddRoomImplModule.kt
git commit -m "Phase 5: wire feature-add-room presentation+ui to :shared:main; rename OldAddRoomViewModel"
```

---

### Task 7: Удалить мёртвый MVI-код feature-add-room/impl

**Files:**
- Delete: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/OldAddRoomViewModel.kt`
- Delete: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/AddRoomAction.kt`
- Delete: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/AddRoomEvent.kt`
- Delete: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/AddRoomViewState.kt`
- Modify: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/di/FeatureAddRoomImplModule.kt`

**Контекст.** После Task 6 старый ViewModel и его модели больше никому не нужны. Удаляем файлы, чистим импорт в DI-модуле. Не трогаем структуру пакета — Task 8 перепакует всё в `.impl.*` целиком.

- [ ] **Step 1: Удалить старые файлы**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git rm shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/OldAddRoomViewModel.kt \
       shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/AddRoomAction.kt \
       shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/AddRoomEvent.kt \
       shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models/AddRoomViewState.kt
```

Папка `presentation/models/` опустеет — Git её автоматически снимет с трека.

- [ ] **Step 2: Поправить `FeatureAddRoomImplModule.kt`**

Финальное содержимое:

```kotlin
package dev.nonoxy.feature.add_room.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.add_room.api.store.AddRoomStore
import dev.nonoxy.feature.add_room.impl.domain.AddRoomStoreFactory
import org.koin.dsl.module

val featureAddRoomImplModule = module {

    factory<AddRoomStore> {
        AddRoomStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create()
    }
}
```

- [ ] **Step 3: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add shared/feature-add-room/impl/
git commit -m "Phase 5: feature-add-room impl — delete old MVI code (OldAddRoomViewModel + models)"
```

---

### Task 8: feature-add-room impl — репакет под `.impl.*` + переход на kmpFeatureSetup

**Files:**
- Move: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/di/` → `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/di/`
- Modify: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/di/FeatureAddRoomImplModule.kt` (package + namespace для DI)
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` (импорт после переезда `featureAddRoomImplModule`)
- Modify: `shared/feature-add-room/impl/build.gradle.kts` (полная замена)
- Delete: пустая директория `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/`

**Контекст.** Аналог Task 8 Phase 4. После Task 7 в impl остался только `domain/` (уже под `.impl.*`) и `di/` (под `.add_room.di.*` без `.impl.*`). Доводим до KMMTemplate-стиля: `di/` → `impl/di/`, build-файл → `kmpFeatureSetup` (без `kmpLibrary` и `composeMultiplatformSetup`, без блока `compose.resources` — они уехали в ui).

- [ ] **Step 1: Переместить `di/` под `impl/di/`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
mkdir -p shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl
git mv shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/di \
       shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/impl/di
```

- [ ] **Step 2: Поправить package в `FeatureAddRoomImplModule.kt`**

Заменить первую строку:

Старое:
```kotlin
package dev.nonoxy.feature.add_room.di
```

Новое:
```kotlin
package dev.nonoxy.feature.add_room.impl.di
```

- [ ] **Step 3: Удалить пустую директорию `presentation/`**

```bash
rmdir shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/models \
      shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation
```

(Если `rmdir` ругается «not empty» — `git status` покажет лишние untracked файлы. Удали их и повтори.)

- [ ] **Step 4: Поправить импорт в `Koin.kt`**

В `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.add_room.di.featureAddRoomImplModule
```

Новое:
```kotlin
import dev.nonoxy.feature.add_room.impl.di.featureAddRoomImplModule
```

- [ ] **Step 5: Переписать `shared/feature-add-room/impl/build.gradle.kts`**

Полное новое содержимое:

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.add_room.impl"
}
```

Изменения относительно старого:
- `kmpLibrary` + `composeMultiplatformSetup` → один `kmpFeatureSetup`.
- Блок `compose.resources { ... }` удалён (резины уехали в ui).
- Весь блок `commonMainDependencies { implementations(...) }` удалён — `kmpFeatureSetup` автоматически даёт:
  - `:shared:common` (через `implModuleDependencies`),
  - `:shared:feature-add-room:api` (через `project.getApiModule()`),
  - `:shared:core-domain` (через `commonDependencies`),
  - `:shared:core-mvikotlin` (через `nonUiModuleDependencies`).
  Старые `compose.multiplatform.resources`, `kotlin.immutableCollections`, `:shared:core-navigation`, `:shared:common-ui`, `:shared:feature-rooms:api`, `koin.composeMultiplatform.viewmodelNavigation` — больше не нужны в impl (использовались только ui-кодом, который теперь в ui-модуле). Single source: `feature-rooms.api` понадобится Executor (Room + RoomsRepository), но он подтянется транзитивно через `:shared:feature-add-room:api` (api зависит от `feature-rooms.api`).

**Проверка зависимости feature-rooms.api → доступна в impl?** Текущий `feature-add-room/api/build.gradle.kts` объявляет `implementations(projects.shared.featureRooms.api)`. `implementations(...)` — это `implementation()` для api-модуля, который **не** транзитивно открывает feature-rooms/api для тех, кто зависит от feature-add-room/api. Значит, в `feature-add-room/impl` Executor увидит `dev.nonoxy.feature.add_room.api.store.AddRoomStore` (через api-собрата), но **НЕ** увидит `dev.nonoxy.feature.rooms.api.repository.RoomsRepository` напрямую.

Решение: поменять `implementations` на `apis` в `feature-add-room/api/build.gradle.kts`, либо явно добавить `feature-rooms.api` в impl. Аккуратнее — поменять на `apis`:

В `shared/feature-add-room/api/build.gradle.kts` (Task 2 был):
```kotlin
commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        libs.kotlin.immutableCollections,
        projects.shared.featureRooms.api,
    )
}
```

Сделать:
```kotlin
import extensions.apis
...
commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        libs.kotlin.immutableCollections,
    )
    apis(
        projects.shared.featureRooms.api,
    )
}
```

(`apis(...)` экспортирует feature-rooms/api как `api()` — теперь impl, presentation, ui модули feature-add-room тоже увидят символы из feature-rooms/api.)

После этого `impl/build.gradle.kts` остаётся минимальным как выше.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :shared:feature-add-room:impl:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL, no findings.

- [ ] **Step 7: Commit**

```bash
git add shared/feature-add-room/ \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt
git commit -m "Phase 5: feature-add-room impl repackaged under .impl.* + switched to kmpFeatureSetup"
```

---

### Task 9: feature-add-room build verification

**Files:** (нет изменений в коде)

**Контекст.** Контрольная точка после миграции feature-add-room. Аналог Task 9 Phase 4. Никаких правок — только запуск проверок и фиксация состояния.

- [ ] **Step 1: Android dev+prod sanity**

Run:
```bash
./gradlew clean :android:app:assembleDevDebug :android:app:assembleProdDebug
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: iOS framework link**

Run:
```bash
./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Detekt**

Run:
```bash
./gradlew detekt
```
Expected: BUILD SUCCESSFUL. Открыть `build/reports/detekt/detekt.html` — должно быть 0 findings (или только pre-existing minor нитки, как в Phase 4).

- [ ] **Step 4: Ручная сверка экрана AddRoom на Android**

Запустить приложение на эмуляторе (dev/debug flavor):

1. Главный экран Rooms открывается.
2. Тап «Добавить комнату» → открывается AddRoom bottom-sheet с заголовком «Добавить новую комнату».
3. Поля пустые, чипсы для выбора этажа доступны (если в БД есть комнаты).
4. Ввести этаж = 5, номер = 500, мест = 2 → кнопка «Создать» активна.
5. Тап «Создать» → snackbar успеха исчезает быстро (потому что сразу автопереход на Manage Students draft).
6. Тап «Отмена» (повторить сценарий): открыть AddRoom, ничего не ввести → «Создать» неактивна; ввести только этаж → «Создать» неактивна; «Отмена» → закрыло bottom-sheet.
7. Дубль-проверка: открыть AddRoom, ввести этаж = 5, номер = 500 (тот, что уже создан), мест = 2 → «Создать» → snackbar «Комната 500 на этаже 5 уже существует».

Если все 7 пунктов прошли — Task 9 закрыт.

- [ ] **Step 5: Зафиксировать промежуточное состояние (commit опционален)**

Если выявлены недочёты (опечатки, лишний код) — поправить, добавить fix-commit. Иначе просто переходим к Task 10.

---

### Task 10: Settings.gradle.kts + пустые submodule-каркасы для feature-manage-students

**Files:**
- Modify: `settings.gradle.kts`
- Create: `shared/feature-manage-students/presentation/build.gradle.kts`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/.gitkeep`
- Create: `shared/feature-manage-students/ui/build.gradle.kts`
- Create: `shared/feature-manage-students/ui/src/commonMain/kotlin/.gitkeep`

**Контекст.** Точная копия Task 1, только для другой фичи.

- [ ] **Step 1: Добавить два include в `settings.gradle.kts`**

Найти:
```
    ":shared:feature-manage-students:api",
    ":shared:feature-manage-students:impl",
```

Заменить на:
```
    ":shared:feature-manage-students:api",
    ":shared:feature-manage-students:impl",
    ":shared:feature-manage-students:presentation",
    ":shared:feature-manage-students:ui",
```

- [ ] **Step 2: Создать `shared/feature-manage-students/presentation/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.presentation"
}

commonMainDependencies {
    implementations(
        libs.kotlin.immutableCollections,
        libs.kotlin.datetime,
    )
}
```

(`kotlin.datetime` — для `LocalDate`, который встречается в `EditableStudent`/`UiStudent` после маппинга.)

- [ ] **Step 3: Создать `shared/feature-manage-students/presentation/src/commonMain/kotlin/.gitkeep`**

Пустой файл.

- [ ] **Step 4: Создать `shared/feature-manage-students/ui/build.gradle.kts`**

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.ui"
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
```

- [ ] **Step 5: Создать `shared/feature-manage-students/ui/src/commonMain/kotlin/.gitkeep`**

Пустой файл.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :shared:feature-manage-students:presentation:assemble :shared:feature-manage-students:ui:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add settings.gradle.kts \
        shared/feature-manage-students/presentation/ \
        shared/feature-manage-students/ui/
git commit -m "Phase 5: scaffold empty feature-manage-students presentation+ui submodules"
```

---

### Task 11: feature-manage-students api — перепаковка + ManageStudentsStore + переход на kmpFeatureSetup

**Files:**
- Move: `shared/feature-manage-students/api/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/models/` → `.../manage_students/api/models/`
- Modify: `shared/feature-manage-students/api/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/api/models/ManageStudentsMode.kt` (package)
- Create: `shared/feature-manage-students/api/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/api/store/ManageStudentsStore.kt`
- Modify: `shared/feature-manage-students/api/build.gradle.kts`
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt` (импорт ManageStudentsMode — старый VM пока живёт)
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt` (импорт ManageStudentsMode)

**Контекст.** В отличие от feature-add-room/api (был пустой), у feature-manage-students/api есть один файл — `ManageStudentsMode.kt` — его перепаковываем под `.api.models.*`. Дополнительно создаём `ManageStudentsStore.kt` со всеми Intent/State/Label + типизированными `ManageStudentsErrorKind`/`SuccessKind`. Build-файл переключается на `kmpFeatureSetup` (как api/feature-add-room). `kotlin.datetime` теперь нужен api (там `LocalDate` в `EditableStudent`-эквиваленте).

- [ ] **Step 1: Переместить `models/` под `api/`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-manage-students/api/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/models \
       shared/feature-manage-students/api/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/api/models
```

- [ ] **Step 2: Поправить package в `ManageStudentsMode.kt`**

Заменить первую строку:

Старое:
```kotlin
package dev.nonoxy.feature.manage_students.models
```

Новое:
```kotlin
package dev.nonoxy.feature.manage_students.api.models
```

- [ ] **Step 3: Поправить импорт в `ManageStudentsViewModel.kt` (старый, в impl)**

В `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.models.ManageStudentsMode
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
```

И в `FeatureManageStudentsNavigation.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.models.ManageStudentsMode
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
```

- [ ] **Step 4: Переписать `shared/feature-manage-students/api/build.gradle.kts`**

Полное новое содержимое:

```kotlin
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.api"
}

commonMainDependencies {
    implementations(
        libs.kotlin.datetime,
        libs.kotlin.immutableCollections,
    )
    apis(
        projects.shared.featureRooms.api,
    )
}
```

(`apis(...)` — открываем feature-rooms/api для impl/presentation/ui модулей feature-manage-students, чтобы они видели `Student`, `Room`, `RoomsRepository` без явных зависимостей.)

- [ ] **Step 5: Создать `ManageStudentsStore.kt`**

Файл `shared/feature-manage-students/api/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/api/store/ManageStudentsStore.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.api.store

import com.arkivanov.mvikotlin.core.store.Store
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Label
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.feature.rooms.api.models.Room
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

interface ManageStudentsStore : Store<Intent, State, Label> {

    data class State(
        val isLoading: Boolean = false,
        val isError: Boolean = false,
        val errorKind: ManageStudentsErrorKind? = null,
        val room: Room? = null,
        val editableStudents: ImmutableList<EditableStudent> = persistentListOf(),
    ) {
        data class EditableStudent(
            val id: String,
            val studentId: Long?,
            val streamNumber: String,
            val checkInDate: String,
            val checkOutDate: String,
            val checkInDateMillis: Long? = null,
            val checkOutDateMillis: Long? = null,
            val isNew: Boolean = false,
        )
    }

    sealed interface Intent {
        data object LoadStudents : Intent
        data object OnAddStudent : Intent
        data class OnRemoveStudent(val studentId: String) : Intent
        data class OnStreamNumberChange(val studentId: String, val value: String) : Intent
        data class OnCheckInDateChange(val studentId: String, val value: String) : Intent
        data class OnCheckOutDateChange(val studentId: String, val value: String) : Intent
        data class OnCheckInDateMillisChange(val studentId: String, val millis: Long) : Intent
        data class OnCheckOutDateMillisChange(val studentId: String, val millis: Long) : Intent
        data object OnSaveAndClose : Intent
        data object OnClose : Intent
    }

    sealed interface Label {
        data object NavigateBack : Label
        data class ShowError(val kind: ManageStudentsErrorKind) : Label
        data class ShowSuccess(val kind: ManageStudentsSuccessKind) : Label
    }
}

sealed interface ManageStudentsErrorKind {
    data object FailedToLoadStudents : ManageStudentsErrorKind
    data object FailedToSaveStudents : ManageStudentsErrorKind
    data object RoomNotFound : ManageStudentsErrorKind
    data object DraftRoomNotFound : ManageStudentsErrorKind
    data object StreamNumberInvalid : ManageStudentsErrorKind
    data object InvalidDateRange : ManageStudentsErrorKind
    data object InvalidDateFormat : ManageStudentsErrorKind
    data object DuplicateStreamNumbers : ManageStudentsErrorKind
}

sealed interface ManageStudentsSuccessKind {
    data object StudentsSaved : ManageStudentsSuccessKind
}
```

Поведение vs старый код:
- `ManageStudentsViewState.errorMessage: String?` → `errorKind: ManageStudentsErrorKind?` + флаг `isError` сохраняется.
- `Action.ShowError(message: String)` → `Label.ShowError(kind)` (типизированный).
- `Action.ShowSuccess(message: String)` → `Label.ShowSuccess(kind)`.
- `Action.NavigateBack` → `Label.NavigateBack`.
- Старая `LoadStudents` была `Event`, в новой архитектуре это и `Intent` (UI может перезапросить через retry), и `Action` (Executor сам перевыдает при initialization, см. StoreFactory bootstrapper). Оставляем как Intent + bootstrapper вызывает Action.LoadInitial, который мапится на тот же loadStudents().

**Mode хранится не в Store.State, а как ctor-параметр StoreFactory** — в фабрике передаётся в Executor, который выбирает ветку `roomsRepository.getRoomById(...)` vs `roomsRepository.getDraftRoom()`. Так в API не торчит implementation-specific параметр.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :shared:feature-manage-students:api:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add shared/feature-manage-students/api/ \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt
git commit -m "Phase 5: feature-manage-students api — repackage + add ManageStudentsStore + switch to kmpFeatureSetup"
```

---

### Task 12: feature-manage-students impl — domain (Executor + Reducer + StoreFactory) с параметризованной фабрикой

**Files:**
- Create: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/ManageStudentsStoreFactory.kt`
- Create: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/ManageStudentsReducer.kt`
- Create: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/ManageStudentsExecutor.kt`

**Контекст.** Зеркало Task 3, но `StoreFactory.create()` принимает `mode: ManageStudentsMode` и проталкивает его в Executor. Бизнес-логика (валидация stream/дат, сериализация EditableStudent → Student) переехала из старого ViewModel в Executor. Локализация ошибок ВЫРЕЗАНА — Executor только дёргает `publish(Label.ShowError(kind))`.

Старый `currentLocalDate` из `dev.nonoxy.common.utils` остаётся (для проверки `isCheckOutDateNearOrExpired`).

- [ ] **Step 1: Создать `ManageStudentsStoreFactory.kt`**

Файл `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/ManageStudentsStoreFactory.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.impl.domain

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Label
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineDispatcher

internal class ManageStudentsStoreFactory(
    private val storeFactory: StoreFactory,
    private val mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
) {

    fun create(mode: ManageStudentsMode): ManageStudentsStore =
        object :
            ManageStudentsStore,
            Store<Intent, State, Label> by storeFactory.create(
                name = ManageStudentsStore::class.simpleName,
                initialState = State(),
                bootstrapper = SimpleBootstrapper(Action.LoadInitial),
                executorFactory = {
                    ManageStudentsExecutor(
                        mainDispatcher = mainDispatcher,
                        roomsRepository = roomsRepository,
                        mode = mode,
                    )
                },
                reducer = ManageStudentsReducer(),
            ) {}

    internal sealed interface Action {
        data object LoadInitial : Action
    }

    internal sealed interface Message {
        data class SetIsLoading(val isLoading: Boolean) : Message
        data class SetError(val kind: ManageStudentsErrorKind) : Message
        data object ClearError : Message
        data class SetRoomAndStudents(
            val room: Room,
            val editableStudents: ImmutableList<State.EditableStudent>,
        ) : Message

        data class SetEditableStudents(
            val editableStudents: ImmutableList<State.EditableStudent>,
        ) : Message
    }
}
```

- [ ] **Step 2: Создать `ManageStudentsReducer.kt`**

Файл `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/ManageStudentsReducer.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.impl.domain

import com.arkivanov.mvikotlin.core.store.Reducer
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory.Message

internal class ManageStudentsReducer : Reducer<State, Message> {

    override fun State.reduce(msg: Message): State = when (msg) {
        is Message.SetIsLoading -> copy(
            isLoading = msg.isLoading,
            isError = if (msg.isLoading) false else isError,
            errorKind = if (msg.isLoading) null else errorKind,
        )

        is Message.SetError -> copy(
            isLoading = false,
            isError = true,
            errorKind = msg.kind,
        )

        Message.ClearError -> copy(
            isError = false,
            errorKind = null,
        )

        is Message.SetRoomAndStudents -> copy(
            isLoading = false,
            isError = false,
            errorKind = null,
            room = msg.room,
            editableStudents = msg.editableStudents,
        )

        is Message.SetEditableStudents -> copy(
            editableStudents = msg.editableStudents,
        )
    }
}
```

- [ ] **Step 3: Создать `ManageStudentsExecutor.kt`**

Файл `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/ManageStudentsExecutor.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.impl.domain

import dev.nonoxy.common.utils.currentLocalDate
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Label
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.State
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsSuccessKind
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory.Action
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory.Message
import dev.nonoxy.feature.rooms.api.models.Room
import dev.nonoxy.feature.rooms.api.models.Student
import dev.nonoxy.feature.rooms.api.repository.RoomsRepository
import dev.nonoxy.residetrack.core.mvikotlin.BaseExecutor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, FormatStringsInDatetimeFormats::class)
internal class ManageStudentsExecutor(
    mainDispatcher: CoroutineDispatcher,
    private val roomsRepository: RoomsRepository,
    private val mode: ManageStudentsMode,
) : BaseExecutor<Intent, Action, State, Message, Label>(mainContext = mainDispatcher) {

    private val dateDisplayFormat = LocalDate.Format {
        byUnicodePattern("dd.MM.yyyy")
    }

    override suspend fun suspendExecuteAction(action: Action) {
        when (action) {
            Action.LoadInitial -> loadStudents()
        }
    }

    override suspend fun suspendExecuteIntent(intent: Intent) {
        when (intent) {
            Intent.LoadStudents -> loadStudents()
            Intent.OnAddStudent -> handleAddStudent()
            is Intent.OnRemoveStudent -> handleRemoveStudent(intent.studentId)
            is Intent.OnStreamNumberChange -> handleStreamNumberChange(intent.studentId, intent.value)
            is Intent.OnCheckInDateChange -> handleCheckInDateChange(intent.studentId, intent.value)
            is Intent.OnCheckOutDateChange -> handleCheckOutDateChange(intent.studentId, intent.value)
            is Intent.OnCheckInDateMillisChange -> handleCheckInDateMillisChange(intent.studentId, intent.millis)
            is Intent.OnCheckOutDateMillisChange -> handleCheckOutDateMillisChange(intent.studentId, intent.millis)
            Intent.OnSaveAndClose -> handleSaveAndClose()
            Intent.OnClose -> publish(Label.NavigateBack)
        }
    }

    private suspend fun loadStudents() {
        dispatch(Message.SetIsLoading(isLoading = true))
        try {
            when (mode) {
                is ManageStudentsMode.ExistingRoom ->
                    loadFromResult { roomsRepository.getRoomById(mode.roomId.toLong()) }
                ManageStudentsMode.DraftRoom ->
                    loadFromResult { roomsRepository.getDraftRoom() }
            }
        } catch (_: Exception) {
            dispatch(Message.SetError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
        }
    }

    private suspend fun loadFromResult(block: suspend () -> Result<Room?>) {
        block().onSuccess { room ->
            if (room == null) {
                val kind = when (mode) {
                    is ManageStudentsMode.ExistingRoom -> ManageStudentsErrorKind.RoomNotFound
                    ManageStudentsMode.DraftRoom -> ManageStudentsErrorKind.DraftRoomNotFound
                }
                dispatch(Message.SetError(kind = kind))
                return
            }

            val editableStudents = room.students.map { student ->
                State.EditableStudent(
                    id = Uuid.random().toString(),
                    studentId = student.id,
                    streamNumber = student.streamNumber.toString(),
                    checkInDate = dateDisplayFormat.format(student.checkInDate),
                    checkOutDate = dateDisplayFormat.format(student.checkOutDate),
                    checkInDateMillis = 1L,
                    checkOutDateMillis = 1L,
                    isNew = false,
                )
            }.toImmutableList()

            dispatch(
                Message.SetRoomAndStudents(
                    room = room,
                    editableStudents = editableStudents,
                )
            )
        }.onFailure {
            dispatch(Message.SetError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToLoadStudents))
        }
    }

    private fun handleAddStudent() {
        val newStudent = State.EditableStudent(
            id = Uuid.random().toString(),
            studentId = null,
            streamNumber = "",
            checkInDate = "",
            checkOutDate = "",
            checkInDateMillis = null,
            checkOutDateMillis = null,
            isNew = true,
        )
        val updated = (state().editableStudents + newStudent).toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleRemoveStudent(studentId: String) {
        val updated = state().editableStudents
            .filterNot { it.id == studentId }
            .toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleStreamNumberChange(studentId: String, value: String) {
        val filtered = value.filter { it.isDigit() }
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(streamNumber = filtered) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckInDateChange(studentId: String, value: String) {
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkInDate = value, checkInDateMillis = parseDateToMillis(value)) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckOutDateChange(studentId: String, value: String) {
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkOutDate = value, checkOutDateMillis = parseDateToMillis(value)) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckInDateMillisChange(studentId: String, millis: Long) {
        val dateString = formatDateFromMillis(millis)
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkInDate = dateString, checkInDateMillis = millis) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private fun handleCheckOutDateMillisChange(studentId: String, millis: Long) {
        val dateString = formatDateFromMillis(millis)
        val updated = state().editableStudents.map { st ->
            if (st.id == studentId) st.copy(checkOutDate = dateString, checkOutDateMillis = millis) else st
        }.toImmutableList()
        dispatch(Message.SetEditableStudents(editableStudents = updated))
    }

    private suspend fun handleSaveAndClose() {
        dispatch(Message.SetIsLoading(isLoading = true))

        val currentRoom = state().room
        if (currentRoom == null) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.RoomNotFound))
            return
        }

        val students = try {
            buildStudentsOrPublishError() ?: run {
                dispatch(Message.SetIsLoading(isLoading = false))
                return
            }
        } catch (_: Exception) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.InvalidDateFormat))
            return
        }

        val streamNumbers = students.map { it.streamNumber }
        if (streamNumbers.size != streamNumbers.distinct().size) {
            dispatch(Message.SetIsLoading(isLoading = false))
            publish(Label.ShowError(kind = ManageStudentsErrorKind.DuplicateStreamNumbers))
            return
        }

        when (mode) {
            is ManageStudentsMode.ExistingRoom -> saveExisting(currentRoom = currentRoom, students = students)
            ManageStudentsMode.DraftRoom -> saveDraft(currentRoom = currentRoom, students = students)
        }
    }

    /** Returns built list or null if a validation error was already published. */
    private suspend fun buildStudentsOrPublishError(): List<Student>? {
        val results = mutableListOf<Student>()
        for (editable in state().editableStudents) {
            if (editable.streamNumber.isBlank() ||
                editable.checkInDate.isBlank() ||
                editable.checkOutDate.isBlank()
            ) continue

            val streamNumber = editable.streamNumber.toIntOrNull()
            if (streamNumber == null) {
                publish(Label.ShowError(kind = ManageStudentsErrorKind.StreamNumberInvalid))
                return null
            }

            val checkInDate = dateDisplayFormat.parse(editable.checkInDate)
            val checkOutDate = dateDisplayFormat.parse(editable.checkOutDate)

            if (checkOutDate <= checkInDate) {
                publish(Label.ShowError(kind = ManageStudentsErrorKind.InvalidDateRange))
                return null
            }

            val currentDate = currentLocalDate
            val isNearOrExpired = checkOutDate.minus(currentDate).days <= 3

            results += Student(
                id = editable.studentId ?: 0L,
                streamNumber = streamNumber,
                checkInDate = checkInDate,
                checkOutDate = checkOutDate,
                isCheckOutDateNearOrExpired = isNearOrExpired,
            )
        }
        return results
    }

    private suspend fun saveExisting(currentRoom: Room, students: List<Student>) {
        roomsRepository.getRoomById((mode as ManageStudentsMode.ExistingRoom).roomId.toLong())
            .onSuccess { existingRoom ->
                if (existingRoom == null) {
                    dispatch(Message.SetIsLoading(isLoading = false))
                    publish(Label.ShowError(kind = ManageStudentsErrorKind.RoomNotFound))
                    return
                }
                val updated = existingRoom.copy(students = students)
                roomsRepository.saveRoom(updated)
                    .onSuccess {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowSuccess(kind = ManageStudentsSuccessKind.StudentsSaved))
                        publish(Label.NavigateBack)
                    }
                    .onFailure {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
                    }
            }
            .onFailure {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
            }
    }

    private suspend fun saveDraft(currentRoom: Room, students: List<Student>) {
        roomsRepository.getDraftRoom()
            .onSuccess { draftRoom ->
                if (draftRoom == null) {
                    dispatch(Message.SetIsLoading(isLoading = false))
                    publish(Label.ShowError(kind = ManageStudentsErrorKind.DraftRoomNotFound))
                    return
                }
                val updated = draftRoom.copy(students = students)
                roomsRepository.saveDraftRoom(updated)
                    .onSuccess {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowSuccess(kind = ManageStudentsSuccessKind.StudentsSaved))
                        publish(Label.NavigateBack)
                    }
                    .onFailure {
                        dispatch(Message.SetIsLoading(isLoading = false))
                        publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
                    }
            }
            .onFailure {
                dispatch(Message.SetIsLoading(isLoading = false))
                publish(Label.ShowError(kind = ManageStudentsErrorKind.FailedToSaveStudents))
            }
    }

    private fun formatDateFromMillis(millis: Long): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return dateDisplayFormat.format(localDate)
    }

    private fun parseDateToMillis(dateString: String): Long? = try {
        val localDate = dateDisplayFormat.parse(dateString)
        localDate.toEpochDays().seconds.inWholeSeconds
    } catch (_: Exception) {
        null
    }
}
```

Поведенческие отличия от старого ManageStudentsViewModel:
- Все `StringProvider`-вызовы выкинуты. Ошибки публикуются типизированно.
- Двойной publish `ShowSuccess(...)` + сразу `NavigateBack` сохранён (старый код их пушил подряд через `viewAction =`); MVIKotlin `publish` подряд работает аналогично.
- Старый `currentDate.minus(...)` остаётся идентичным.
- Старая верхняя try/catch вокруг `handleSaveAndClose` оборачивает теперь только парсинг (через `buildStudentsOrPublishError`) и публикует `InvalidDateFormat` — поведение сохранено.

- [ ] **Step 4: Проверка сборки**

Run: `./gradlew :shared:feature-manage-students:impl:assemble`
Expected: BUILD SUCCESSFUL — старый ViewModel ещё на месте, новый domain собран параллельно.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/domain/
git commit -m "Phase 5: feature-manage-students impl — add MVIKotlin domain (Executor + Reducer + parameterized StoreFactory)"
```

---

### Task 13: feature-manage-students presentation — ViewModel + UiState + UiLabel + mappers + DI

**Files:**
- Move: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/models/{UiRoom.kt,UiStudent.kt}` → `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/`
- Move: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/mappers/{UiRoomMapper.kt,UiStudentMapper.kt}` → `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/`
- Modify: оба перенесённых mapper'а (package) и оба перенесённых model'а (package)
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiManageStudentsState.kt`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiManageStudentsLabel.kt`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiEditableStudent.kt`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/UiManageStudentsStateMapper.kt`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/UiManageStudentsLabelMapper.kt`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt`
- Create: `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/di/FeatureManageStudentsPresentationModule.kt`
- Delete: `shared/feature-manage-students/presentation/src/commonMain/kotlin/.gitkeep`
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt` (старые импорты на UiRoomMapper/UiStudentMapper — поменять, старый VM пока живёт)
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsViewState.kt` (импорт UiRoom)

**Контекст.** Объёмная задача: помимо стандартного presentation-каркаса (ViewModel + UiState + UiLabel + mappers + DI), переносим `UiRoom`/`UiStudent` + их мапперы из `impl/ui/` в `presentation/`. Старый ViewModel в `impl/` остаётся живым — но его импорты на `UiRoomMapper`/`UiStudentMapper` нужно поправить на новый пакет, иначе он не соберётся.

`UiEditableStudent` — UI-зеркало `ManageStudentsStore.State.EditableStudent`. Можно было бы reuse, но KMMTemplate-стиль требует отдельного типа в presentation.

- [ ] **Step 1: Перенести UiRoom/UiStudent в presentation**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
mkdir -p shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/models/UiRoom.kt \
       shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiRoom.kt
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/models/UiStudent.kt \
       shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiStudent.kt
```

Поправить package в обоих файлах:

`shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiRoom.kt`:

Старое:
```kotlin
package dev.nonoxy.feature.manage_students.ui.models
```

Новое:
```kotlin
package dev.nonoxy.feature.manage_students.presentation.models
```

`UiStudent.kt` — аналогично.

Также убрать `internal` модификатор у `data class UiRoom` и `UiStudent`, чтобы UI мог их видеть из другого модуля. Стало:
```kotlin
package dev.nonoxy.feature.manage_students.presentation.models

import kotlinx.collections.immutable.ImmutableList

data class UiRoom(
    val id: Long,
    val floorNumber: String,
    val roomNumber: String,
    val bedsCount: String,
    val students: ImmutableList<UiStudent>,
)
```

```kotlin
package dev.nonoxy.feature.manage_students.presentation.models

data class UiStudent(
    val streamNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val isCheckOutDateNearOrExpired: Boolean,
)
```

- [ ] **Step 2: Перенести UiRoomMapper/UiStudentMapper в presentation**

```bash
mkdir -p shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/mappers/UiRoomMapper.kt \
       shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/UiRoomMapper.kt
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/mappers/UiStudentMapper.kt \
       shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/UiStudentMapper.kt
```

Поправить package и импорт UiRoom/UiStudent в обоих:

`UiRoomMapper.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.manage_students.presentation.models.UiRoom
import dev.nonoxy.feature.rooms.api.models.Room
import kotlinx.collections.immutable.toImmutableList

internal interface UiRoomMapper : Mapper<Room, UiRoom>

internal class UiRoomMapperImpl(
    private val studentMapper: UiStudentMapper,
) : UiRoomMapper {

    override fun map(item: Room): UiRoom = with(item) {
        UiRoom(
            id = id,
            floorNumber = floorNumber.toString(),
            roomNumber = roomNumber.toString(),
            bedsCount = bedsCount.toString(),
            students = students.let(studentMapper::map).toImmutableList(),
        )
    }
}
```

`UiStudentMapper.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.common.utils.mapper.Mapper
import dev.nonoxy.feature.manage_students.presentation.models.UiStudent
import dev.nonoxy.feature.rooms.api.models.Student

internal interface UiStudentMapper : Mapper<Student, UiStudent>

internal class UiStudentMapperImpl : UiStudentMapper {

    override fun map(item: Student): UiStudent = with(item) {
        UiStudent(
            streamNumber = streamNumber.toString(),
            checkInDate = checkInDate.toString(),
            checkOutDate = checkOutDate.toString(),
            isCheckOutDateNearOrExpired = isCheckOutDateNearOrExpired,
        )
    }
}
```

- [ ] **Step 3: Поправить старые импорты в impl ViewModel и State**

В `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt` (старый, в impl):

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.ui.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.ui.mappers.UiStudentMapper
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapper
```

В `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsViewState.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.ui.models.UiRoom
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.presentation.models.UiRoom
```

И в `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di/FeatureManageStudentsImplModule.kt` (старый DI):

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.ui.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.ui.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.manage_students.ui.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.ui.mappers.UiStudentMapperImpl
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapperImpl
```

**Кросс-фичовая зависимость:** `feature-manage-students/impl` теперь тащит классы из `feature-manage-students/presentation`. Это допустимо для convention plugins (kmpFeatureSetup auto-wiring). Но если возникнет циклическая (impl → presentation, а presentation → impl?) — её нет, presentation ничего не знает про impl. OK.

Однако impl-модулю надо явно объявить зависимость на presentation (он не подтягивается автоматически — посмотри `KmpFeatureSetupPlugin.kt`: `implModuleDependencies` для impl даёт `:shared:common` + api, без presentation).

Добавить в `shared/feature-manage-students/impl/build.gradle.kts` (временно — Task 17 уберёт):

```kotlin
commonMainDependencies {
    implementations(
        // ... текущие зависимости
        projects.shared.featureManageStudents.presentation, // TEMP for Task 13-16; remove in Task 17
    )
}
```

(Это потребуется, если detekt/build падает с unresolved UiRoomMapper.)

- [ ] **Step 4: Создать `UiEditableStudent.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiEditableStudent.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.models

data class UiEditableStudent(
    val id: String,
    val studentId: Long?,
    val streamNumber: String,
    val checkInDate: String,
    val checkOutDate: String,
    val checkInDateMillis: Long? = null,
    val checkOutDateMillis: Long? = null,
    val isNew: Boolean = false,
)
```

- [ ] **Step 5: Создать `UiManageStudentsState.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiManageStudentsState.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.models

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UiManageStudentsState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorKind: ManageStudentsErrorKind? = null,
    val room: UiRoom? = null,
    val editableStudents: ImmutableList<UiEditableStudent> = persistentListOf(),
)
```

- [ ] **Step 6: Создать `UiManageStudentsLabel.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/UiManageStudentsLabel.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.models

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsSuccessKind

sealed interface UiManageStudentsLabel {
    data object NavigateBack : UiManageStudentsLabel
    data class ShowError(val kind: ManageStudentsErrorKind) : UiManageStudentsLabel
    data class ShowSuccess(val kind: ManageStudentsSuccessKind) : UiManageStudentsLabel
}
```

- [ ] **Step 7: Создать `UiManageStudentsStateMapper.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/UiManageStudentsStateMapper.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.presentation.models.UiEditableStudent
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsState
import kotlinx.collections.immutable.toPersistentList

internal interface UiManageStudentsStateMapper {
    fun map(item: ManageStudentsStore.State): UiManageStudentsState
}

internal class UiManageStudentsStateMapperImpl(
    private val uiRoomMapper: UiRoomMapper,
) : UiManageStudentsStateMapper {

    override fun map(item: ManageStudentsStore.State): UiManageStudentsState = UiManageStudentsState(
        isLoading = item.isLoading,
        isError = item.isError,
        errorKind = item.errorKind,
        room = item.room?.let(uiRoomMapper::map),
        editableStudents = item.editableStudents.map { st ->
            UiEditableStudent(
                id = st.id,
                studentId = st.studentId,
                streamNumber = st.streamNumber,
                checkInDate = st.checkInDate,
                checkOutDate = st.checkOutDate,
                checkInDateMillis = st.checkInDateMillis,
                checkOutDateMillis = st.checkOutDateMillis,
                isNew = st.isNew,
            )
        }.toPersistentList(),
    )
}
```

- [ ] **Step 8: Создать `UiManageStudentsLabelMapper.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/mappers/UiManageStudentsLabelMapper.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.mappers

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsLabel

internal interface UiManageStudentsLabelMapper {
    fun map(item: ManageStudentsStore.Label): UiManageStudentsLabel
}

internal class UiManageStudentsLabelMapperImpl : UiManageStudentsLabelMapper {

    override fun map(item: ManageStudentsStore.Label): UiManageStudentsLabel = when (item) {
        ManageStudentsStore.Label.NavigateBack -> UiManageStudentsLabel.NavigateBack
        is ManageStudentsStore.Label.ShowError -> UiManageStudentsLabel.ShowError(kind = item.kind)
        is ManageStudentsStore.Label.ShowSuccess -> UiManageStudentsLabel.ShowSuccess(kind = item.kind)
    }
}
```

- [ ] **Step 9: Создать `ManageStudentsViewModel.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation

import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore.Intent
import dev.nonoxy.feature.manage_students.presentation.mappers.UiManageStudentsLabelMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiManageStudentsStateMapper
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsLabel
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsState
import dev.nonoxy.residetrack.core.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.mapNotNull

class ManageStudentsViewModel internal constructor(
    private val store: ManageStudentsStore,
    private val stateMapper: UiManageStudentsStateMapper,
    private val labelMapper: UiManageStudentsLabelMapper,
) : BaseViewModel<UiManageStudentsState, UiManageStudentsLabel>(initialState = UiManageStudentsState()) {

    init {
        bindAndStart {
            store.states.mapNotNull(stateMapper::map) bindTo ::acceptState
            store.labels.mapNotNull(labelMapper::map) bindTo ::acceptLabel
        }
    }

    fun onRetryLoadStudents() = store.accept(Intent.LoadStudents)

    fun onAddStudent() = store.accept(Intent.OnAddStudent)

    fun onRemoveStudent(studentId: String) = store.accept(Intent.OnRemoveStudent(studentId = studentId))

    fun onStreamNumberChange(studentId: String, value: String) =
        store.accept(Intent.OnStreamNumberChange(studentId = studentId, value = value))

    fun onCheckInDateChange(studentId: String, value: String) =
        store.accept(Intent.OnCheckInDateChange(studentId = studentId, value = value))

    fun onCheckOutDateChange(studentId: String, value: String) =
        store.accept(Intent.OnCheckOutDateChange(studentId = studentId, value = value))

    fun onCheckInDateMillisChange(studentId: String, millis: Long) =
        store.accept(Intent.OnCheckInDateMillisChange(studentId = studentId, millis = millis))

    fun onCheckOutDateMillisChange(studentId: String, millis: Long) =
        store.accept(Intent.OnCheckOutDateMillisChange(studentId = studentId, millis = millis))

    fun onSaveAndClose() = store.accept(Intent.OnSaveAndClose)

    fun onClose() = store.accept(Intent.OnClose)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
```

- [ ] **Step 10: Создать `FeatureManageStudentsPresentationModule.kt`**

Файл `shared/feature-manage-students/presentation/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/di/FeatureManageStudentsPresentationModule.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.presentation.di

import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.presentation.ManageStudentsViewModel
import dev.nonoxy.feature.manage_students.presentation.mappers.UiManageStudentsLabelMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiManageStudentsLabelMapperImpl
import dev.nonoxy.feature.manage_students.presentation.mappers.UiManageStudentsStateMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiManageStudentsStateMapperImpl
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapperImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManageStudentsPresentationModule = module {

    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf(::UiManageStudentsStateMapperImpl) bind UiManageStudentsStateMapper::class
    factoryOf<UiManageStudentsLabelMapper>(::UiManageStudentsLabelMapperImpl)

    viewModel { params ->
        val mode = params.get<ManageStudentsMode>()
        ManageStudentsViewModel(
            store = get { parametersOf(mode) },
            stateMapper = get(),
            labelMapper = get(),
        )
    }
}
```

Здесь Koin прокидывает `mode` от UI (`koinViewModel { parametersOf(mode) }`) через Koin params → ViewModel → опять `parametersOf(mode)` → Store-фабрика в impl-модуле (которая зарегистрирована аналогично `factory<ManageStudentsStore> { (mode: ManageStudentsMode) -> StoreFactory(...).create(mode) }` — это будет в Task 15, шаг 4).

- [ ] **Step 11: Удалить `.gitkeep`**

```bash
git rm shared/feature-manage-students/presentation/src/commonMain/kotlin/.gitkeep
```

- [ ] **Step 12: Проверка сборки**

Run: `./gradlew :shared:feature-manage-students:presentation:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL (старый `ManageStudentsViewModel` в impl собран, импорты UiRoomMapper переключены на presentation, Koin зарегистрирован дважды, но это не конфликт — оба модуля регистрируют одну и ту же `UiRoomMapper`, последняя регистрация выигрывает; в смежной части — старый DI в impl ещё ссылается на `StringProvider`, который мы пока не трогаем).

Если падает по `StringProvider` — он остался, его убираем в Task 16. Здесь он должен ещё резолвиться.

- [ ] **Step 13: Commit**

```bash
git add shared/feature-manage-students/presentation/ \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di/FeatureManageStudentsImplModule.kt \
        shared/feature-manage-students/impl/build.gradle.kts
git commit -m "Phase 5: feature-manage-students presentation — ViewModel, UiState, UiLabel, mappers (incl. moved UiRoom/UiStudent), DI"
```

---

### Task 14: feature-manage-students ui — Screen + views + composeResources move + Screen API + Message mapping

**Files:**
- Move: `shared/feature-manage-students/impl/src/commonMain/composeResources/` → `shared/feature-manage-students/ui/src/commonMain/composeResources/`
- Move: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/` → `shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/`
- Move: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt` → `shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/api/FeatureManageStudentsScreenApi.kt`
- Modify: все 9 файлов под `ui/` (ManageStudentsScreen + 8 views) — заменить старые типы, переключить на per-handler callbacks
- Create: `shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/ManageStudentsMessageMapping.kt`
- Delete: `shared/feature-manage-students/ui/src/commonMain/kotlin/.gitkeep`

**Контекст.** Зеркало Task 5, но больше файлов и нужны per-handler callbacks с пробросом `studentId` (т.к. список редактируемых студентов).

- [ ] **Step 1: Переместить compose-resources**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-manage-students/impl/src/commonMain/composeResources \
       shared/feature-manage-students/ui/src/commonMain/composeResources
```

- [ ] **Step 2: Переместить директорию `ui/` целиком**

```bash
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui \
       shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui
```

(`models/` и `mappers/` уже переехали в presentation в Task 13, остаются `ManageStudentsScreen.kt` + 8 views.)

- [ ] **Step 3: Переместить и переименовать navigation → Screen API**

```bash
mkdir -p shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/api
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt \
       shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/api/FeatureManageStudentsScreenApi.kt
rmdir shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation
```

- [ ] **Step 4: Переписать `FeatureManageStudentsScreenApi.kt`**

Полное содержимое:

```kotlin
package dev.nonoxy.feature.manage_students.ui.api

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import dev.nonoxy.core.navigation.ManageStudentsDraftRoomRoute
import dev.nonoxy.core.navigation.ManageStudentsExistingRoomRoute
import dev.nonoxy.core.navigation.bottomsheet.ModalBottomSheetConfiguration
import dev.nonoxy.core.navigation.bottomsheet.bottomSheet
import dev.nonoxy.core.navigation.navigateOnResumed
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.ui.ManageStudentsScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavController.navigateToManageStudentsExistingRoom(roomId: String) {
    navigateOnResumed(ManageStudentsExistingRoomRoute(roomId))
}

fun NavController.navigateToManageStudentsDraftRoom() {
    navigateOnResumed(ManageStudentsDraftRoomRoute)
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetManageStudentsExistingRoom(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<ManageStudentsExistingRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) { backStackEntry ->
        val roomId = backStackEntry.toRoute<ManageStudentsExistingRoomRoute>().roomId
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.ExistingRoom(roomId)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.bottomSheetManageStudentsDraftRoom(
    onNavigateBack: () -> Unit,
) {
    bottomSheet<ManageStudentsDraftRoomRoute>(
        configuration = ModalBottomSheetConfiguration(
            modifier = Modifier.statusBarsPadding().fillMaxWidth(),
        ),
    ) {
        ManageStudentsScreen(
            onNavigateBack = onNavigateBack,
            viewModel = koinViewModel { parametersOf(ManageStudentsMode.DraftRoom) },
        )
    }
}
```

- [ ] **Step 5: Создать `ManageStudentsMessageMapping.kt`**

Файл `shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/ManageStudentsMessageMapping.kt`:

```kotlin
package dev.nonoxy.feature.manage_students.ui

import dev.nonoxy.feature.manage_students.api.store.ManageStudentsErrorKind
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsSuccessKind
import org.jetbrains.compose.resources.getString
import residetrack.shared.feature_manage_students.ui.generated.resources.Res
import residetrack.shared.feature_manage_students.ui.generated.resources.error_draft_room_not_found
import residetrack.shared.feature_manage_students.ui.generated.resources.error_duplicate_stream_numbers
import residetrack.shared.feature_manage_students.ui.generated.resources.error_failed_to_load_students
import residetrack.shared.feature_manage_students.ui.generated.resources.error_failed_to_save_students
import residetrack.shared.feature_manage_students.ui.generated.resources.error_invalid_date_format
import residetrack.shared.feature_manage_students.ui.generated.resources.error_invalid_date_range
import residetrack.shared.feature_manage_students.ui.generated.resources.error_room_not_found
import residetrack.shared.feature_manage_students.ui.generated.resources.error_stream_number_invalid
import residetrack.shared.feature_manage_students.ui.generated.resources.students_saved_successfully

internal suspend fun ManageStudentsErrorKind.localizedSuspend(): String = when (this) {
    ManageStudentsErrorKind.FailedToLoadStudents -> getString(Res.string.error_failed_to_load_students)
    ManageStudentsErrorKind.FailedToSaveStudents -> getString(Res.string.error_failed_to_save_students)
    ManageStudentsErrorKind.RoomNotFound -> getString(Res.string.error_room_not_found)
    ManageStudentsErrorKind.DraftRoomNotFound -> getString(Res.string.error_draft_room_not_found)
    ManageStudentsErrorKind.StreamNumberInvalid -> getString(Res.string.error_stream_number_invalid)
    ManageStudentsErrorKind.InvalidDateRange -> getString(Res.string.error_invalid_date_range)
    ManageStudentsErrorKind.InvalidDateFormat -> getString(Res.string.error_invalid_date_format)
    ManageStudentsErrorKind.DuplicateStreamNumbers -> getString(Res.string.error_duplicate_stream_numbers)
}

internal suspend fun ManageStudentsSuccessKind.localizedSuspend(): String = when (this) {
    ManageStudentsSuccessKind.StudentsSaved -> getString(Res.string.students_saved_successfully)
}
```

- [ ] **Step 6: Переписать `ManageStudentsScreen.kt`**

Полное содержимое:

```kotlin
package dev.nonoxy.feature.manage_students.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nonoxy.feature.manage_students.presentation.ManageStudentsViewModel
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsLabel
import dev.nonoxy.feature.manage_students.ui.views.ManageStudentsContent
import dev.nonoxy.residetrack.common.ui.common.dialog.DialogScaffold
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackErrorSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.ResideTrackSnackbar
import dev.nonoxy.residetrack.common.ui.common.snackbar.SnackbarType
import dev.nonoxy.residetrack.common.ui.common.utils.CollectFlow
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ManageStudentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManageStudentsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbarType by rememberSaveable { mutableStateOf(SnackbarType.INFO) }

    viewModel.label.CollectFlow { label ->
        when (label) {
            UiManageStudentsLabel.NavigateBack -> onNavigateBack()
            is UiManageStudentsLabel.ShowError -> {
                currentSnackbarType = SnackbarType.ERROR
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localizedSuspend(),
                    withDismissAction = true,
                )
            }
            is UiManageStudentsLabel.ShowSuccess -> {
                currentSnackbarType = SnackbarType.INFO
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = label.kind.localizedSuspend(),
                    withDismissAction = true,
                )
            }
        }
    }

    DialogScaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    when (currentSnackbarType) {
                        SnackbarType.ERROR -> ResideTrackErrorSnackbar(snackbarData = snackbarData)
                        SnackbarType.INFO -> ResideTrackSnackbar(snackbarData = snackbarData)
                        else -> null
                    }
                },
            )
        },
    ) { paddingValues ->
        ManageStudentsContent(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            state = state,
            onRetryClick = viewModel::onRetryLoadStudents,
            onCloseClick = viewModel::onClose,
            onAddStudent = viewModel::onAddStudent,
            onRemoveStudent = viewModel::onRemoveStudent,
            onStreamNumberChange = viewModel::onStreamNumberChange,
            onCheckInDateChange = viewModel::onCheckInDateChange,
            onCheckOutDateChange = viewModel::onCheckOutDateChange,
            onCheckInDateMillisChange = viewModel::onCheckInDateMillisChange,
            onCheckOutDateMillisChange = viewModel::onCheckOutDateMillisChange,
            onSaveAndClose = viewModel::onSaveAndClose,
        )
    }
}
```

- [ ] **Step 7: Переписать `ManageStudentsContent.kt` под per-handler callbacks**

Полное содержимое:

```kotlin
package dev.nonoxy.feature.manage_students.ui.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nonoxy.feature.manage_students.presentation.models.UiManageStudentsState
import dev.nonoxy.residetrack.common.ui.common.state.ShowStateData

@Composable
internal fun ManageStudentsContent(
    state: UiManageStudentsState,
    onRetryClick: () -> Unit,
    onCloseClick: () -> Unit,
    onAddStudent: () -> Unit,
    onRemoveStudent: (String) -> Unit,
    onStreamNumberChange: (String, String) -> Unit,
    onCheckInDateChange: (String, String) -> Unit,
    onCheckOutDateChange: (String, String) -> Unit,
    onCheckInDateMillisChange: (String, Long) -> Unit,
    onCheckOutDateMillisChange: (String, Long) -> Unit,
    onSaveAndClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ShowStateData(
        modifier = modifier,
        state = state,
        isLoading = state.isLoading,
        isError = state.isError,
        onRetryClick = onRetryClick,
    ) { currentState ->
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            ManageStudentsTopBar(
                room = currentState.room,
                onCloseClick = onCloseClick,
            )

            ManageStudentsList(
                students = currentState.editableStudents,
                onRemoveStudent = onRemoveStudent,
                onStreamNumberChange = onStreamNumberChange,
                onCheckInDateChange = onCheckInDateChange,
                onCheckOutDateChange = onCheckOutDateChange,
                onCheckInDateMillisChange = onCheckInDateMillisChange,
                onCheckOutDateMillisChange = onCheckOutDateMillisChange,
                modifier = Modifier.weight(1f),
            )

            ManageStudentsBottomButtons(
                onAddStudent = onAddStudent,
                onSaveAndClose = onSaveAndClose,
            )
        }
    }
}
```

- [ ] **Step 8: Поправить остальные views — заменить onObtainEvent на per-handler callbacks, поправить импорт Res и UiRoom/UiStudent/UiEditableStudent**

Для каждого из файлов:
- `views/ManageStudentsTopBar.kt`
- `views/ManageStudentsList.kt`
- `views/ManageStudentsBottomButtons.kt`
- `views/EditableStudentCard.kt`
- `views/EmptyStudentsState.kt`
- `views/StudentCardContent.kt`
- `views/StudentCardHeader.kt`

Действия:
1. Все импорты `residetrack.shared.feature_manage_students.impl.generated.resources.*` → `residetrack.shared.feature_manage_students.ui.generated.resources.*`.
2. Все импорты `dev.nonoxy.feature.manage_students.ui.models.UiRoom` → `dev.nonoxy.feature.manage_students.presentation.models.UiRoom` (то же для `UiStudent`).
3. Все импорты `dev.nonoxy.feature.manage_students.presentation.models.EditableStudent` → `dev.nonoxy.feature.manage_students.presentation.models.UiEditableStudent`.
4. Параметр `onObtainEvent: (ManageStudentsEvent) -> Unit` разлагается на конкретные per-handler callbacks. Это требует ручной правки сигнатур каждого внутреннего composable.

Команда для массовой замены импортов:

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
find shared/feature-manage-students/ui/src/commonMain/kotlin -name "*.kt" -exec sed -i '' \
  -e 's|residetrack\.shared\.feature_manage_students\.impl\.generated\.resources|residetrack.shared.feature_manage_students.ui.generated.resources|g' \
  -e 's|dev\.nonoxy\.feature\.manage_students\.ui\.models\.UiRoom|dev.nonoxy.feature.manage_students.presentation.models.UiRoom|g' \
  -e 's|dev\.nonoxy\.feature\.manage_students\.ui\.models\.UiStudent|dev.nonoxy.feature.manage_students.presentation.models.UiStudent|g' \
  -e 's|dev\.nonoxy\.feature\.manage_students\.presentation\.models\.EditableStudent|dev.nonoxy.feature.manage_students.presentation.models.UiEditableStudent|g' \
  -e 's|: EditableStudent|: UiEditableStudent|g' \
  -e 's|EditableStudent(|UiEditableStudent(|g' \
  {} +
```

Затем по каждому файлу с `onObtainEvent` — ручная правка:

```bash
grep -n "onObtainEvent\|ManageStudentsEvent" shared/feature-manage-students/ui/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui/views/*.kt
```

Для каждого совпадения — заменить `onObtainEvent(ManageStudentsEvent.OnXxx(...))` на конкретный callback (например, `onStreamNumberChange(studentId, value)`), и параметр `onObtainEvent: (ManageStudentsEvent) -> Unit` в сигнатуре composable — на per-handler callbacks. Список соответствий:

| Старый Event | Новый callback в сигнатуре |
|---|---|
| `OnRemoveStudent(id)` | `onRemoveStudent: (String) -> Unit` |
| `OnStreamNumberChange(id, value)` | `onStreamNumberChange: (String, String) -> Unit` |
| `OnCheckInDateChange(id, value)` | `onCheckInDateChange: (String, String) -> Unit` |
| `OnCheckOutDateChange(id, value)` | `onCheckOutDateChange: (String, String) -> Unit` |
| `OnCheckInDateMillisChange(id, m)` | `onCheckInDateMillisChange: (String, Long) -> Unit` |
| `OnCheckOutDateMillisChange(id, m)` | `onCheckOutDateMillisChange: (String, Long) -> Unit` |
| `OnAddStudent` | `onAddStudent: () -> Unit` |
| `OnSaveAndClose` | `onSaveAndClose: () -> Unit` |
| `OnClose` | `onCloseClick: () -> Unit` (в TopBar) |
| `LoadStudents` (retry) | `onRetryClick: () -> Unit` (передаётся в `ShowStateData`) |

Импорт `dev.nonoxy.feature.manage_students.presentation.models.ManageStudentsEvent` удалить из всех файлов (он не существует — Event лежит в api как Intent, но в ui не нужен).

- [ ] **Step 9: Удалить `.gitkeep`**

```bash
git rm shared/feature-manage-students/ui/src/commonMain/kotlin/.gitkeep
```

- [ ] **Step 10: Проверка сборки**

Run: `./gradlew :shared:feature-manage-students:ui:assemble`
Expected: BUILD SUCCESSFUL. Если падает на views с `onObtainEvent` — пройдись ещё раз по `grep -n`.

Run: `./gradlew :android:app:assembleDevDebug`
Expected: BUILD SUCCESSFUL — старый `ManageStudentsViewModel` в `impl` ещё компилится (его `onObtainEvent`-обработчик внутренний, не торчит наружу), просто не используется UI.

- [ ] **Step 11: Commit**

```bash
git add shared/feature-manage-students/ui/ \
        shared/feature-manage-students/impl/src/commonMain/composeResources \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/ui \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation
git commit -m "Phase 5: feature-manage-students ui — Screen + views + composeResources move + Screen API + ErrorKind localization"
```

---

### Task 15: Wire feature-manage-students presentation+ui to :shared:main (Koin + NavHost) + параметризованный Store

**Files:**
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt`
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di/FeatureManageStudentsImplModule.kt` (старый DI — добавить ManageStudentsStore-фабрику параметризованную, переименовать старый ViewModel)
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt` (переименовать класс в OldManageStudentsViewModel)

**Контекст.** Зеркало Task 6, но с параметризованным Store-фабрикой.

- [ ] **Step 1: Переименовать старый класс в `OldManageStudentsViewModel`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ManageStudentsViewModel.kt \
       shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/OldManageStudentsViewModel.kt
```

В переименованном файле заменить:

Старое:
```kotlin
internal class ManageStudentsViewModel(
```

Новое:
```kotlin
internal class OldManageStudentsViewModel(
```

- [ ] **Step 2: Переписать `FeatureManageStudentsImplModule.kt`**

Полное содержимое:

```kotlin
package dev.nonoxy.feature.manage_students.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory
import dev.nonoxy.feature.manage_students.presentation.OldManageStudentsViewModel
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiRoomMapperImpl
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapper
import dev.nonoxy.feature.manage_students.presentation.mappers.UiStudentMapperImpl
import dev.nonoxy.feature.manage_students.utils.StringProvider
import dev.nonoxy.feature.manage_students.utils.StringProviderImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManageStudentsImplModule = module {

    // TODO Task 16: drop these duplicates once presentation module's DI fully takes over.
    factoryOf<UiStudentMapper>(::UiStudentMapperImpl)
    factoryOf(::UiRoomMapperImpl) bind UiRoomMapper::class
    factoryOf<StringProvider>(::StringProviderImpl)

    factory { (mode: ManageStudentsMode) ->
        ManageStudentsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create(mode = mode)
    } bind ManageStudentsStore::class

    // TODO Task 16: remove together with OldManageStudentsViewModel.
    viewModel { parameters ->
        OldManageStudentsViewModel(
            mode = parameters.get(),
            roomsRepository = get(),
            uiRoomMapper = get(),
            uiStudentMapper = get(),
            stringProvider = get(),
        )
    }
}
```

(Дубли `UiRoomMapper`/`UiStudentMapper`/`StringProvider` — нужны временно, чтобы старый `OldManageStudentsViewModel` ещё резолвился. Уберём в Task 16.)

- [ ] **Step 3: Зарегистрировать `featureManageStudentsPresentationModule` в Koin.kt**

В `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
...
            featureAddRoomImplModule,
            featureAddRoomPresentationModule,
            featureManageStudentsImplModule,
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
import dev.nonoxy.feature.manage_students.presentation.di.featureManageStudentsPresentationModule
...
            featureAddRoomImplModule,
            featureAddRoomPresentationModule,
            featureManageStudentsImplModule,
            featureManageStudentsPresentationModule,
```

- [ ] **Step 4: Переключить NavHost на новый Screen API**

В `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.presentation.navigation.bottomSheetManageStudentsExistingRoom
import dev.nonoxy.feature.manage_students.presentation.navigation.bottomSheetManageStudentsDraftRoom
import dev.nonoxy.feature.manage_students.presentation.navigation.navigateToManageStudentsExistingRoom
import dev.nonoxy.feature.manage_students.presentation.navigation.navigateToManageStudentsDraftRoom
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.ui.api.bottomSheetManageStudentsExistingRoom
import dev.nonoxy.feature.manage_students.ui.api.bottomSheetManageStudentsDraftRoom
import dev.nonoxy.feature.manage_students.ui.api.navigateToManageStudentsExistingRoom
import dev.nonoxy.feature.manage_students.ui.api.navigateToManageStudentsDraftRoom
```

- [ ] **Step 5: Проверка сборки + ручная сверка**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

Run приложение на эмуляторе → проверить полный цикл:
1. Открыть AddRoom → создать комнату → автопереход на Manage Students draft.
2. Добавить студента → заполнить поток, даты → «Сохранить» → snackbar успеха + возврат на Rooms.
3. На Rooms кликнуть по новой комнате → открывается Manage Students existing room с уже добавленным студентом.
4. Ошибочные сценарии: ввести буквы в номер потока (фильтр игнорирует), оставить пустые даты и нажать «Сохранить» → не сохраняет, snackbar об ошибке.

- [ ] **Step 6: Commit**

```bash
git add shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/ \
        shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di/FeatureManageStudentsImplModule.kt
git commit -m "Phase 5: wire feature-manage-students presentation+ui to :shared:main; rename OldManageStudentsViewModel"
```

---

### Task 16: Удалить мёртвый MVI-код feature-manage-students/impl + StringProvider

**Files:**
- Delete: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/OldManageStudentsViewModel.kt`
- Delete: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsAction.kt`
- Delete: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsEvent.kt`
- Delete: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsViewState.kt`
- Delete: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/utils/StringProvider.kt`
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di/FeatureManageStudentsImplModule.kt` (вычистить старые beans)

**Контекст.** После Task 15 NavHost полностью ушёл на новый Screen API, ViewModel идёт из presentation. Старый Old-ViewModel и его модели больше никому не нужны. `StringProvider` тоже — он использовался только OldManageStudentsViewModel.

- [ ] **Step 1: Удалить старые файлы**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git rm shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/OldManageStudentsViewModel.kt \
       shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsAction.kt \
       shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsEvent.kt \
       shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/models/ManageStudentsViewState.kt \
       shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/utils/StringProvider.kt
```

Папки `presentation/models/`, `presentation/`, `utils/` опустеют — git автоматически снимет их.

- [ ] **Step 2: Поправить `FeatureManageStudentsImplModule.kt` — финальное содержимое**

```kotlin
package dev.nonoxy.feature.manage_students.di

import dev.nonoxy.common.coroutines.CoroutineDispatchers
import dev.nonoxy.feature.manage_students.api.models.ManageStudentsMode
import dev.nonoxy.feature.manage_students.api.store.ManageStudentsStore
import dev.nonoxy.feature.manage_students.impl.domain.ManageStudentsStoreFactory
import org.koin.core.parameter.parametersOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureManageStudentsImplModule = module {

    factory { (mode: ManageStudentsMode) ->
        ManageStudentsStoreFactory(
            storeFactory = get(),
            mainDispatcher = get<CoroutineDispatchers>().main,
            roomsRepository = get(),
        ).create(mode = mode)
    } bind ManageStudentsStore::class
}
```

(UiRoomMapper / UiStudentMapper / StringProvider — выкинуты; единственный bean — параметризованная Store-фабрика.)

- [ ] **Step 3: Откатить временную dependency на presentation из `impl/build.gradle.kts`**

Если в Task 13, Step 3 добавлялась строка `projects.shared.featureManageStudents.presentation` — удалить её. Сейчас impl не зависит от presentation (Executor работает только с api + core + common).

Должно остаться **только** (если осталась `composeMultiplatformSetup` — Task 17 её уберёт; здесь не трогаем):

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
        projects.shared.commonUi,
        projects.shared.featureRooms.api,
        projects.shared.featureManageStudents.api,
    )
}

compose.resources {
    publicResClass = false
    generateResClass = auto
}
```

(Это исходное состояние — `composeMultiplatformSetup`, явные deps. Task 17 переведёт на `kmpFeatureSetup` и почистит. Если в Task 13 ничего не добавляли — этот шаг no-op.)

- [ ] **Step 4: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add shared/feature-manage-students/impl/
git commit -m "Phase 5: feature-manage-students impl — delete old MVI code (OldManageStudentsViewModel + models) + StringProvider"
```

---

### Task 17: feature-manage-students impl — репакет под `.impl.*` + переход на kmpFeatureSetup

**Files:**
- Move: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di/` → `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/di/`
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/di/FeatureManageStudentsImplModule.kt` (package)
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` (импорт)
- Modify: `shared/feature-manage-students/impl/build.gradle.kts` (полная замена)
- Delete: пустая директория `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/`

**Контекст.** Зеркало Task 8.

- [ ] **Step 1: Переместить `di/` под `impl/di/`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
mkdir -p shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl
git mv shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/di \
       shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/impl/di
```

- [ ] **Step 2: Поправить package в `FeatureManageStudentsImplModule.kt`**

Старое:
```kotlin
package dev.nonoxy.feature.manage_students.di
```

Новое:
```kotlin
package dev.nonoxy.feature.manage_students.impl.di
```

- [ ] **Step 3: Удалить пустую директорию `presentation/`**

```bash
find shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation -depth -type d -empty -delete 2>/dev/null || true
find shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/utils -depth -type d -empty -delete 2>/dev/null || true
```

Также удалить пустую файловую систему (если что-то осталось — `git status` покажет).

- [ ] **Step 4: Поправить импорт в `Koin.kt`**

В `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

Старое:
```kotlin
import dev.nonoxy.feature.manage_students.di.featureManageStudentsImplModule
```

Новое:
```kotlin
import dev.nonoxy.feature.manage_students.impl.di.featureManageStudentsImplModule
```

- [ ] **Step 5: Переписать `shared/feature-manage-students/impl/build.gradle.kts`**

Полное новое содержимое:

```kotlin
import extensions.androidLibraryConfig

plugins {
    alias(libs.plugins.conventionPlugin.kmpFeatureSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.feature.manage_students.impl"
}
```

Изменения относительно старого:
- `kmpLibrary` + `composeMultiplatformSetup` → один `kmpFeatureSetup`.
- Блок `compose.resources { ... }` удалён.
- Весь `commonMainDependencies` блок удалён — `kmpFeatureSetup` сам подтянет `:shared:common`, `:shared:feature-manage-students:api`, `:shared:core-domain`, `:shared:core-mvikotlin`. Из старого списка transitively нужны только Room/RoomsRepository — они приходят через `apis(projects.shared.featureRooms.api)` в `feature-manage-students/api/build.gradle.kts` (Task 11, Step 4).

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :shared:feature-manage-students:impl:assemble`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add shared/feature-manage-students/ \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt
git commit -m "Phase 5: feature-manage-students impl repackaged under .impl.* + switched to kmpFeatureSetup"
```

---

### Task 18: Удалить `shared/common/.../presentation/BaseViewModel.kt`

**Files:**
- Delete: `shared/common/src/commonMain/kotlin/dev/nonoxy/common/presentation/BaseViewModel.kt`

**Контекст.** Старый самописный `BaseViewModel<State, Event, Action>` был помечен на удаление в Phase 4 (см. `kmmtemplate-migration-design.md` секция 7: «Текущий `common/BaseViewModel` удаляется»). После Tasks 7 и 16 у него больше нет потребителей.

- [ ] **Step 1: Проверить отсутствие потребителей**

```bash
grep -rln "common.presentation.BaseViewModel" /Users/a.dobrov/StudioProjects/reside-track --include="*.kt" 2>/dev/null | grep -v build/
```

Expected: пусто (никаких совпадений). Если что-то всплыло — это потребитель, исправь сначала.

- [ ] **Step 2: Удалить файл**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git rm shared/common/src/commonMain/kotlin/dev/nonoxy/common/presentation/BaseViewModel.kt
```

Пустая директория `shared/common/src/commonMain/kotlin/dev/nonoxy/common/presentation/` автоматически снимется git.

- [ ] **Step 3: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add shared/common/
git commit -m "Phase 5: delete dev.nonoxy.common.presentation.BaseViewModel — no consumers after migration"
```

---

### Task 19: Полная Phase 5 верификация

**Files:** (нет изменений)

**Контекст.** Финальная контрольная точка — полный clean-build обеих платформ, detekt, ручная сверка всех 3 экранов.

- [ ] **Step 1: Clean build Android dev+prod**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
./gradlew clean :android:app:assembleDevDebug :android:app:assembleProdDebug
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: iOS framework link**

```bash
./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Detekt full sweep**

```bash
./gradlew detekt
```
Expected: BUILD SUCCESSFUL. Открыть `build/reports/detekt/detekt.html` — 0 code smells (как после Phase 4).

- [ ] **Step 4: Manual smoke test на Android (dev/debug)**

Запустить приложение → выполнить полный сценарий:

1. **Rooms screen опен.** Список комнат по этажам отображается. Если БД пустая — пустое состояние.
2. **«Добавить комнату».** Тап → AddRoom bottom-sheet открывается. Заголовок «Добавить новую комнату» виден.
3. **Валидация AddRoom.** Кнопка «Создать» неактивна. Ввести этаж = 1 → всё ещё неактивна. Ввести номер = 101 → неактивна. Ввести мест = 2 → активна.
4. **Создание комнаты.** Тап «Создать» → быстрый автопереход на Manage Students draft.
5. **Manage Students (draft).** Заголовок включает номер комнаты. Список студентов пуст. «Добавить слушателя» доступно.
6. **Добавление студента.** Тап «Добавить слушателя» → появляется карточка «Новый слушатель». Ввести поток = 123, даты заселения 01.09.2024 и выселения 30.06.2025.
7. **«Сохранить».** Snackbar успеха появляется, авто-возврат на Rooms. В Rooms видна новая комната с пометкой «1/2» (1 студент из 2 мест).
8. **Открытие existing room.** Тап по созданной комнате → Manage Students existing room. Существующий студент видим. Можно удалить (delete icon) и сохранить.
9. **Дубль-проверка.** AddRoom → ввести этаж = 1, номер = 101 (тот же) → «Создать» → snackbar «Комната 101 на этаже 1 уже существует».
10. **Невалидные даты.** Manage Students → ввести checkOut раньше checkIn → «Сохранить» → snackbar «Дата выселения должна быть позже даты заселения».
11. **Дублирующиеся потоки.** Manage Students → добавить второго студента с тем же номером потока 123 → «Сохранить» → snackbar «Найдены дублирующиеся номера потоков».

Все 11 пунктов должны пройти. Если что-то падает — фиксить и докоммитить.

- [ ] **Step 5: Зафиксировать состояние (commit опционален, только если были фиксы)**

```bash
git status  # должно быть clean
```

---

### Task 20: Обновить migration spec — отметить Фазу 5 завершённой

**Files:**
- Modify: `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`

**Контекст.** Финальный документ-апдейт по образцу Task 10 из Phase 4: меняем статус-строку и пункт списка фаз.

- [ ] **Step 1: Обновить статус-строку**

В файле `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`:

Старое (строка 178):
```
**Статус (2026-05-24):** Фазы 1–4 завершены; следующая — Фаза 5 (миграция feature-add-room и feature-manage-students).
```

Новое:
```
**Статус (2026-05-25):** Фазы 1–5 завершены; следующая — Фаза 6 (Compose Resources → moko-resources, удаление StringProvider — уже выполнено в Фазе 5, остаётся только перевод ресурсов).
```

(Уточнение про StringProvider: в Phase 5 он уже удалён, в Phase 6 остаётся только сам переезд compose-resources → moko.)

- [ ] **Step 2: Отметить пункт 5 как завершённый**

Старое (строка 205):
```
5. **Остальные фичи** — `feature-add-room`, `feature-manage-students`.
```

Новое:
```
5. **Остальные фичи** ✅ — `feature-add-room`, `feature-manage-students` → 4 модуля + MVIKotlin. `BaseViewModel`/`StringProvider` удалены.
```

- [ ] **Step 3: Commit**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git add docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md
git commit -m "docs: mark Phase 5 (remaining features) complete in migration spec"
```

- [ ] **Step 4: Финальная проверка состояния ветки**

```bash
git log --oneline main..HEAD
git status
```

Expected:
- Чистое рабочее дерево.
- Ветка `feature/add-students` ушла далеко вперёд от `main` (после Phase 5 — ещё ~25-30 коммитов).
- Все коммиты Phase 5 имеют префикс `Phase 5:` или `docs:` (для финальных двух).

---

## Контрольные команды (для копирования)

Полный verify-цикл, выполнять между крупными task-блоками или вручную при review:

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
./gradlew clean
./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug
./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64
./gradlew detekt
```

Smoke test на Android (dev/debug):
1. Открыть приложение — список комнат.
2. AddRoom → создать → автопереход в Manage Students draft.
3. Добавить студента → сохранить.
4. Открыть существующую комнату → редактировать студента → сохранить.
