# Phase 3: Навигация — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Привести модуль `:shared:core-navigation` reside-track к полному соответствию KMMTemplate: `Screen` становится marker-интерфейсом с top-level `@Serializable` route-объектами, навигационные утилиты и bottom-sheet-инфраструктура причёсываются под эталон.

**Architecture:** Чисто механический рефакторинг навигации без изменения поведения приложения. `sealed interface Screen` с вложенными route → `interface Screen` (marker) + top-level `@Serializable` объекты/классы. Пакет `bottom_sheet` → `bottomsheet`. `NavigationUtils.kt` и bottom-sheet-файлы приводятся к KMMTemplate. Добавляется `NavigationSharedTransitionUtils.kt`. Все consumer-модули (`:shared:main` + 3 feature-модуля) обновляются под новые имена.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Navigation 2.9.2, kotlinx.serialization, Gradle convention plugins.

---

## Контекст и границы фазы

**Что делаем (спека, разделы 11 и 14, пункт 3):**
- `Screen` (sealed interface с вложенными route) → marker-интерфейс `Screen` + top-level `@Serializable` route-объекты/классы в `core-navigation`.
- Bottom-sheet-инфраструктура сохраняется, причёсывается под стиль KMMTemplate.
- `navigateOnResumed` / `popBackStackOnResumed` и весь `NavigationUtils.kt` приводятся к KMMTemplate.
- Модуль `:shared:core-navigation` доводится до полной парности с KMMTemplate `core-navigation` (включая `NavigationSharedTransitionUtils.kt` — см. memory «KMMTemplate full compliance»: эталон копируется целиком).

**Чего НЕ делаем в этой фазе (важно):**
- Screen API (`composableXxxScreen` / `navigateToXxxScreen`) **остаётся в текущих impl-модулях** в пакете `presentation/navigation/`. Спека (раздел 11) говорит, что он переезжает в `ui/api/`, но модуль `ui` не существует — он создаётся при 4-модульном split фичи в Фазах 4–5. Перенос файлов в `ui/api/` произойдёт там. В Фазе 3 мы только меняем содержимое этих файлов (ссылки на route).
- Переименование пакетов `dev.nonoxy.*` → `dev.nonoxy.residetrack.*` — это Фаза 8. В Фазе 3 префикс пакета остаётся `dev.nonoxy.core.navigation`.

**Решение пользователя:** мёртвый route `RoomDetail(roomNumber: Long)` (объявлен, нигде не зарегистрирован, нет переходов) — **удаляется**, не переносится.

**Допустимое расхождение с KMMTemplate:** `ModalBottomSheetConfiguration.defaultContainerColor` остаётся `{ ResideTrackTheme.colors.background }` (в KMMTemplate `{ Color.White }`). Причина — сохранение поведения приложения (спека, раздел 4: «поведение не меняется»). Из-за этого `:shared:core-navigation` сохраняет зависимость `projects.shared.commonUi`, которой нет в KMMTemplate.

**Контрольная точка после фазы:** `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug` зелёный, `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64` зелёный, `./gradlew detekt` зелёный.

## Целевые имена route

| Текущий (вложенный) | Новый (top-level) |
|---|---|
| `Screen.Rooms` | `RoomsRoute` |
| `Screen.RoomDetail(roomNumber: Long)` | — удаляется |
| `Screen.AddRoom` | `AddRoomRoute` |
| `Screen.ManageStudentsExistingRoom(roomId: String)` | `ManageStudentsExistingRoomRoute(roomId: String)` |
| `Screen.ManageStudentsDraftRoom` | `ManageStudentsDraftRoomRoute` |

## Карта файлов

**Изменяются в `:shared:core-navigation`:**
- `build.gradle.kts` — зависимости под KMMTemplate (Task 1)
- `src/commonMain/kotlin/dev/nonoxy/core/navigation/Screen.kt` — marker-интерфейс + route (Task 3)
- `src/commonMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.kt` — паритет с KMMTemplate (Task 4)
- `src/androidMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.android.kt` — удаляется (Task 4)
- `src/iosMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.ios.kt` — удаляется (Task 4)
- `src/commonMain/kotlin/dev/nonoxy/core/navigation/NavigationSharedTransitionUtils.kt` — создаётся (Task 5)
- `src/{common,android,ios}Main/.../bottom_sheet/` → `bottomsheet/` — переименование пакета (Task 2)
- bottom-sheet-файлы — причёсывание под KMMTemplate (Tasks 4, 6)
- `src/androidMain/AndroidManifest.xml` — удаляется (Task 7)

**Изменяются consumer-файлы:**
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt` (Tasks 2, 3)
- `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/navigation/FeatureRoomsNavigation.kt` (Task 3)
- `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation/FeatureAddRoomNavigation.kt` (Tasks 2, 3)
- `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt` (Tasks 2, 3)
- `shared/main/build.gradle.kts` — чистка зависимости (Task 7)
- `gradle/libs.versions.toml` — чистка catalog (Task 7)
- `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` — статус (Task 8)

---

### Task 1: `core-navigation/build.gradle.kts` — паритет зависимостей с KMMTemplate

**Files:**
- Modify: `shared/core-navigation/build.gradle.kts`

**Контекст.** Текущий файл держит `compose.multiplatform.navigation`, `compose.navigation.material`, `androidx.lifecycle.runtimeCompose`, `compose.multiplatform.material3`, `projects.shared.common`, `projects.shared.commonUi` — все в `implementations`. KMMTemplate `core-navigation` держит `material3` в `implementations` и `navigation` + `backhandler` в `apis`. Расхождения reside-track:
- `compose.navigation.material` (`material-navigation`, Material-2-навигация) — **не используется** ни в одном `.kt`-файле (reside-track написал собственный material3-навигатор). Удаляем.
- `projects.shared.common` — **не используется** в `core-navigation` (ни одного импорта `dev.nonoxy.common.*`). Удаляем.
- `androidx.lifecycle.runtimeCompose` — `NavigationUtils.kt` использует `collectAsStateWithLifecycle`/`Lifecycle`, но в KMMTemplate они доступны транзитивно через `navigation-compose` (та же версия 2.9.2). Удаляем явную зависимость.
- `compose.multiplatform.backhandler` — добавляем в `apis` (нужно в Task 4: `ModalBottomSheetHost` переходит на общий `androidx.compose.ui.backhandler.BackHandler`).
- `projects.shared.commonUi` — **оставляем** (нужно для `ResideTrackTheme` в `ModalBottomSheetConfiguration`).

- [ ] **Step 1: Переписать `shared/core-navigation/build.gradle.kts`**

Полное новое содержимое файла:

```kotlin
import extensions.androidLibraryConfig
import extensions.apis
import extensions.commonMainDependencies
import extensions.implementations

plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
    alias(libs.plugins.conventionPlugin.jsonSerialization)
    alias(libs.plugins.conventionPlugin.composeMultiplatformSetup)
}

androidLibraryConfig {
    namespace = "dev.nonoxy.core.navigation"
}

commonMainDependencies {
    implementations(
        libs.compose.multiplatform.material3,
        projects.shared.commonUi,
    )

    apis(
        libs.compose.multiplatform.navigation,
        libs.compose.multiplatform.backhandler,
    )
}
```

- [ ] **Step 2: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

**Блокер:** если компиляция `NavigationUtils.kt` падает с `unresolved reference: collectAsStateWithLifecycle` или `Lifecycle` — значит `lifecycle-runtime-compose` не приходит транзитивно (паритет convention-плагинов из Фазы 1 неполный). ОСТАНОВИТЬСЯ и сообщить пользователю, не добавлять зависимость наугад.

- [ ] **Step 3: Commit**

```bash
git add shared/core-navigation/build.gradle.kts
git commit -m "Phase 3: align core-navigation build.gradle.kts with KMMTemplate"
```

(pre-commit hook прогонит detekt; если упадёт — починить и повторить коммит.)

---

### Task 2: Переименование пакета `bottom_sheet` → `bottomsheet`

**Files:**
- Rename: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottom_sheet/` → `bottomsheet/`
- Rename: `shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/bottom_sheet/` → `bottomsheet/`
- Rename: `shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/bottom_sheet/` → `bottomsheet/`
- Modify (package/import): 8 файлов в `bottomsheet/` + 3 consumer-файла

**Контекст.** KMMTemplate использует пакет `...core.navigation.bottomsheet` (без подчёркивания). reside-track использует `bottom_sheet`. Строка `bottom_sheet` встречается в коде только в `package`/`import`-объявлениях (camelCase `BottomSheet*` в идентификаторах не затрагивается), поэтому сплошная замена безопасна.

- [ ] **Step 1: Переместить директории через `git mv`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git mv shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottom_sheet shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet
git mv shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/bottom_sheet shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/bottomsheet
git mv shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/bottom_sheet shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/bottomsheet
```

- [ ] **Step 2: Заменить `bottom_sheet` → `bottomsheet` во всех затронутых файлах**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
sed -i '' 's/bottom_sheet/bottomsheet/g' \
  shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetConfiguration.kt \
  shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetHost.kt \
  shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetLayout.kt \
  shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavGraphBuilder.kt \
  shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigator.kt \
  shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigatorDestinationBuilder.kt \
  shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigator.android.kt \
  shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigator.ios.kt \
  shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt \
  shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation/FeatureAddRoomNavigation.kt \
  shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt
```

- [ ] **Step 3: Проверить, что старые директории `bottom_sheet/` исчезли**

Run: `find shared/core-navigation/src -type d -name bottom_sheet`
Expected: пусто (ничего не выведено). Если директория осталась из-за неотслеживаемого `.DS_Store` — удалить: `rm -rf <путь>/bottom_sheet`.

- [ ] **Step 4: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add -A shared/core-navigation shared/main shared/feature-add-room shared/feature-manage-students
git commit -m "Phase 3: rename bottom_sheet package to bottomsheet (KMMTemplate parity)"
```

---

### Task 3: `Screen` — marker-интерфейс + top-level route-объекты

**Files:**
- Modify: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/Screen.kt`
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/navigation/ResideTrackNavHost.kt`
- Modify: `shared/feature-rooms/impl/src/commonMain/kotlin/dev/nonoxy/feature/rooms/presentation/navigation/FeatureRoomsNavigation.kt`
- Modify: `shared/feature-add-room/impl/src/commonMain/kotlin/dev/nonoxy/feature/add_room/presentation/navigation/FeatureAddRoomNavigation.kt`
- Modify: `shared/feature-manage-students/impl/src/commonMain/kotlin/dev/nonoxy/feature/manage_students/presentation/navigation/FeatureManageStudentsNavigation.kt`

- [ ] **Step 1: Переписать `Screen.kt`**

Полное новое содержимое (`RoomDetail` удалён по решению пользователя):

```kotlin
package dev.nonoxy.core.navigation

import kotlinx.serialization.Serializable

interface Screen

@Serializable
data object RoomsRoute : Screen

@Serializable
data object AddRoomRoute : Screen

@Serializable
data class ManageStudentsExistingRoomRoute(val roomId: String) : Screen

@Serializable
data object ManageStudentsDraftRoomRoute : Screen
```

- [ ] **Step 2: Обновить `ResideTrackNavHost.kt`**

Заменить импорт:

```kotlin
import dev.nonoxy.core.navigation.Screen
```

на:

```kotlin
import dev.nonoxy.core.navigation.RoomsRoute
```

Заменить:

```kotlin
            startDestination = Screen.Rooms
```

на:

```kotlin
            startDestination = RoomsRoute
```

- [ ] **Step 3: Переписать `FeatureRoomsNavigation.kt`**

Полное новое содержимое:

```kotlin
package dev.nonoxy.feature.rooms.presentation.navigation

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

- [ ] **Step 4: Обновить `FeatureAddRoomNavigation.kt`**

Заменить импорт:

```kotlin
import dev.nonoxy.core.navigation.Screen
```

на:

```kotlin
import dev.nonoxy.core.navigation.AddRoomRoute
```

Заменить `navigate(Screen.AddRoom)` на `navigate(AddRoomRoute)`:

```kotlin
fun NavController.navigateToAddRoomScreen() {
    navigate(AddRoomRoute) {
        launchSingleTop = true
    }
}
```

Заменить `bottomSheet<Screen.AddRoom>(` на `bottomSheet<AddRoomRoute>(`:

```kotlin
    bottomSheet<AddRoomRoute>(
```

- [ ] **Step 5: Обновить `FeatureManageStudentsNavigation.kt`**

Заменить импорт:

```kotlin
import dev.nonoxy.core.navigation.Screen
```

на:

```kotlin
import dev.nonoxy.core.navigation.ManageStudentsDraftRoomRoute
import dev.nonoxy.core.navigation.ManageStudentsExistingRoomRoute
```

Заменить тело `navigateToManageStudentsExistingRoom`:

```kotlin
fun NavController.navigateToManageStudentsExistingRoom(roomId: String) {
    navigate(ManageStudentsExistingRoomRoute(roomId)) {
        launchSingleTop = true
    }
}
```

Заменить тело `navigateToManageStudentsDraftRoom`:

```kotlin
fun NavController.navigateToManageStudentsDraftRoom() {
    navigate(ManageStudentsDraftRoomRoute) {
        launchSingleTop = true
    }
}
```

В `bottomSheetManageStudentsExistingRoom` заменить `bottomSheet<Screen.ManageStudentsExistingRoom>(` на `bottomSheet<ManageStudentsExistingRoomRoute>(` и строку извлечения route:

```kotlin
        val roomId = backStackEntry.toRoute<ManageStudentsExistingRoomRoute>().roomId
```

В `bottomSheetManageStudentsDraftRoom` заменить `bottomSheet<Screen.ManageStudentsDraftRoom>(` на `bottomSheet<ManageStudentsDraftRoomRoute>(`.

- [ ] **Step 6: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Проверить, что `Screen.` больше нигде не используется как вложенный route**

Run: `grep -rn "Screen\." --include="*.kt" shared`
Expected: пусто (ничего не выведено).

- [ ] **Step 8: Commit**

```bash
git add shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/Screen.kt shared/main shared/feature-rooms shared/feature-add-room shared/feature-manage-students
git commit -m "Phase 3: convert Screen to marker interface with top-level route objects"
```

---

### Task 4: `NavigationUtils.kt` — паритет с KMMTemplate, удаление `BackHandler` expect/actual

**Files:**
- Modify: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.kt`
- Delete: `shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.android.kt`
- Delete: `shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.ios.kt`
- Modify: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetHost.kt`

**Контекст.** В reside-track `NavigationUtils.kt` объявляет `expect fun BackHandler(...)` с actual-реализациями в `NavigationUtils.android.kt` (`androidx.activity.compose.BackHandler`) и `NavigationUtils.ios.kt` (`androidx.compose.ui.backhandler.BackHandler`). KMMTemplate этого expect/actual не имеет — `ModalBottomSheetHost` использует общий мультиплатформенный `androidx.compose.ui.backhandler.BackHandler` напрямую. Зависимость `compose.multiplatform.backhandler` добавлена в Task 1. Также `NavigationUtils.kt` приводим к KMMTemplate побайтно (мелкие отличия: `popBackStackOnResumed` через `::popBackStack`, `LaunchedParamsEffect` через `rememberUpdatedState`).

- [ ] **Step 1: Переписать `NavigationUtils.kt`**

Полное новое содержимое (паритет с KMMTemplate, без `expect fun BackHandler`):

```kotlin
package dev.nonoxy.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import androidx.savedstate.read

const val DEFAULT_RESULT_KEY = "RESULT"

val NavController.currentRoute: String?
    get() = currentDestination?.route

fun NavController.popBackStackOnResumed() {
    runOnResumed(::popBackStack)
}

fun NavController.runOnResumed(block: () -> Unit) {
    if (currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return

    block()
}

fun <T> NavController.popBackStackOnResumed(
    key: String = DEFAULT_RESULT_KEY,
    value: T? = null
) {
    runOnResumed {
        popBackStack(
            key = key,
            value = value,
        )
    }
}

fun <T> NavController.popBackStack(
    key: String = DEFAULT_RESULT_KEY,
    value: T? = null
) {
    if (value != null) {
        putNavigationResultToPreviousBackStackEntry(key, value)
    }

    popBackStack()
}

fun <T> NavController.putNavigationResultToPreviousBackStackEntry(
    key: String,
    value: T,
) {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(key, value)
}

fun <T> NavController.putNavigationResultToCurrentBackStackEntry(
    key: String,
    value: T,
) {
    currentBackStackEntry
        ?.savedStateHandle
        ?.set(key, value)
}

@Composable
fun <T> NavBackStackEntry.CheckNavigationResult(
    key: String,
    initialValue: T? = null,
    onResult: (T) -> Unit = {},
) {
    savedStateHandle
        .getStateFlow(key, initialValue)
        .collectAsStateWithLifecycle()
        .value
        ?.also(onResult)
        ?.let {
            savedStateHandle[key] = null
            savedStateHandle.remove<T>(key)
        }
}

inline fun <reified T : Any> NavController.navigateOnResumed(
    noinline builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigateOnResumed(route = T::class, builder = builder)
}

fun <T : Any> NavController.navigateOnResumed(
    route: T,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    runOnResumed {
        navigate(route, navOptions(builder))
    }
}

@Composable
fun <T> NavController.LaunchedParamsEffect(
    key: String,
    clear: Boolean = true,
    onResult: (T) -> Unit = {}
) {
    val params = get<T>(key)
    val currentOnResult by rememberUpdatedState(onResult)

    LaunchedEffect(key1 = params) {
        if (params != null) {
            if (clear) clear<T>(key)

            currentOnResult(params)
        }
    }
}

fun <T> NavController.get(key: String): T? = currentBackStackEntry?.savedStateHandle?.get(key)

fun <T> NavController.set(key: String, value: T) {
    currentBackStackEntry?.savedStateHandle?.set(key, value)
}

fun <T> NavController.clear(key: String): T? {
    currentBackStackEntry?.savedStateHandle?.set(key, null)
    return currentBackStackEntry?.savedStateHandle?.remove<T>(key)
}

fun NavBackStackEntry.getStringArg(key: String): String? = arguments?.read { getStringOrNull(key) }

fun NavBackStackEntry.getIntArg(key: String): Int? = arguments?.read { getIntOrNull(key) }

fun NavBackStackEntry.getLongArg(key: String): Long? = arguments?.read { getLongOrNull(key) }

fun NavBackStackEntry.getBooleanArg(key: String) = arguments?.read { getBooleanOrNull(key) }
```

- [ ] **Step 2: Удалить платформенные `NavigationUtils`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git rm shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.android.kt
git rm shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/NavigationUtils.ios.kt
```

- [ ] **Step 3: Переписать `bottomsheet/ModalBottomSheetHost.kt`**

Полное новое содержимое (паритет с KMMTemplate — общий `androidx.compose.ui.backhandler.BackHandler`, `@Suppress("ModifierMissing")`, `ExperimentalComposeUiApi`):

```kotlin
package dev.nonoxy.core.navigation.bottomsheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.compose.LocalOwnersProvider
import kotlinx.coroutines.launch

@Suppress("ModifierMissing")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ModalBottomSheetHost(
    modalBottomSheetNavigator: ModalBottomSheetNavigator
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    val bottomSheetBackStack by modalBottomSheetNavigator.backStack.collectAsState()
    val transitionInProgress by modalBottomSheetNavigator.transitionInProgress.collectAsState()

    val currentEntry = bottomSheetBackStack.lastOrNull()
    val configuration =
        (currentEntry?.destination as? ModalBottomSheetNavigator.Destination)?.configuration

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = configuration.getSkipPartiallyExpanded(),
        confirmValueChange = configuration.getConfirmValueChange()
    )

    LaunchedEffect(bottomSheetBackStack.size) {
        if (bottomSheetBackStack.isNotEmpty()) {
            if (!sheetState.isVisible) {
                sheetState.show()
            }
        }
    }

    if (bottomSheetBackStack.isNotEmpty()) {

        if (configuration.getProperties().shouldDismissOnBackPress) {
            BackHandler {
                scope.launch {
                    sheetState.hide()
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                currentEntry?.let { entry ->
                    modalBottomSheetNavigator.dismiss(entry)
                }
            },
            sheetState = sheetState,
            modifier = configuration.getModifier(),
            sheetMaxWidth = configuration.getSheetMaxWidth(),
            shape = configuration.getShape(),
            properties = configuration.getProperties(),
            containerColor = configuration.getContainerColor(),
            contentColor = configuration.getContentColor(),
            tonalElevation = configuration.getTonalElevation(),
            scrimColor = configuration.getScrimColor(),
            dragHandle = configuration.getDragHandle(),
            contentWindowInsets = configuration.getContentWindowInsets(),
        ) {
            AnimatedContent(
                targetState = currentEntry,
                transitionSpec = configuration.getContentTransition(),
                label = "BottomSheetContentAnimation"
            ) { entry ->
                entry?.let { backStackEntry ->
                    DisposableEffect(backStackEntry) {
                        onDispose {
                            modalBottomSheetNavigator.onTransitionComplete(backStackEntry)
                        }
                    }

                    backStackEntry.LocalOwnersProvider(saveableStateHolder) {
                        val destination =
                            backStackEntry.destination as ModalBottomSheetNavigator.Destination
                        destination.content(backStackEntry)
                    }
                }
            }
        }
    }

    LaunchedEffect(transitionInProgress, currentEntry) {
        transitionInProgress.forEach { entry ->
            if (entry != currentEntry && !bottomSheetBackStack.contains(entry)) {
                modalBottomSheetNavigator.onTransitionComplete(entry)
            }
        }
    }
}
```

- [ ] **Step 4: Проверить, что `dev.nonoxy.core.navigation.BackHandler` больше не используется**

Run: `grep -rn "navigation.BackHandler\|expect fun BackHandler\|actual fun BackHandler" --include="*.kt" shared`
Expected: пусто.

- [ ] **Step 5: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add -A shared/core-navigation
git commit -m "Phase 3: align NavigationUtils with KMMTemplate, drop BackHandler expect/actual"
```

---

### Task 5: Добавить `NavigationSharedTransitionUtils.kt`

**Files:**
- Create: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/NavigationSharedTransitionUtils.kt`

**Контекст.** KMMTemplate `core-navigation` содержит `NavigationSharedTransitionUtils.kt` (хелперы shared-element-переходов). reside-track его не имеет. По принципу полного соответствия эталону (memory «KMMTemplate full compliance» — копировать целиком, не выкидывать «как не в объёме») файл добавляется. Функция `kmmTemplateSharedElement` переименована в `resideTrackSharedElement` (имя проекта в идентификаторе — как `ResideTrackTheme` вместо `KmmTemplateTheme`). Остальное — побайтно из KMMTemplate.

- [ ] **Step 1: Создать `NavigationSharedTransitionUtils.kt`**

Полное содержимое:

```kotlin
@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.nonoxy.core.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize
import androidx.compose.animation.SharedTransitionScope.PlaceHolderSize.Companion.contentSize
import androidx.compose.animation.SharedTransitionScope.SharedContentState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Suppress("CompositionLocalAllowlist")
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope> {
    error("CompositionLocal LocalSharedTransitionScope was not provided")
}

fun Modifier.resideTrackSharedElement(
    sharedContentState: SharedContentState,
    animatedVisibilityScope: AnimatedVisibilityScope,
    boundsTransform: BoundsTransform = DefaultBoundsTransform,
    placeHolderSize: PlaceHolderSize = contentSize,
    renderInOverlayDuringTransition: Boolean = true,
    zIndexInOverlay: Float = 0f,
    clipInOverlayDuringTransition: OverlayClip = ParentClip,
): Modifier = composed {
    with(LocalSharedTransitionScope.current) {
        this@resideTrackSharedElement
            .sharedElement(
                sharedContentState = sharedContentState,
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = boundsTransform,
                placeHolderSize = placeHolderSize,
                renderInOverlayDuringTransition = renderInOverlayDuringTransition,
                zIndexInOverlay = zIndexInOverlay,
                clipInOverlayDuringTransition = clipInOverlayDuringTransition
            )
    }
}

@Composable
fun rememberSharedContentState(
    key: String,
    sharedTransitionScope: SharedTransitionScope = LocalSharedTransitionScope.current,
): SharedContentState = with(sharedTransitionScope) { rememberSharedContentState(key = key) }

private val DefaultSpring =
    spring(stiffness = StiffnessMediumLow, visibilityThreshold = Rect.VisibilityThreshold)

@ExperimentalSharedTransitionApi
private val ParentClip: OverlayClip =
    object : OverlayClip {
        override fun getClipPath(
            sharedContentState: SharedContentState,
            bounds: Rect,
            layoutDirection: LayoutDirection,
            density: Density,
        ): Path? {
            return sharedContentState.parentSharedContentState?.clipPathInOverlay
        }
    }

private val DefaultBoundsTransform = BoundsTransform { _, _ ->
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = StiffnessMediumLow,
        visibilityThreshold = Rect.VisibilityThreshold,
    )
}
```

- [ ] **Step 2: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

**Примечание.** `DefaultSpring` — приватное `val`, не используется в самом файле (так же и в эталоне KMMTemplate). detekt-правило `UnusedPrivateProperty` для reside-track настроено как в Фазе 1; если detekt при коммите упадёт на `DefaultSpring` — добавить к нему `@Suppress("UnusedPrivateProperty")` (точечно, так как файл копируется из эталона как есть).

- [ ] **Step 3: Commit**

```bash
git add shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/NavigationSharedTransitionUtils.kt
git commit -m "Phase 3: add NavigationSharedTransitionUtils (KMMTemplate parity)"
```

---

### Task 6: Причёсывание bottom-sheet-навигатора под KMMTemplate

**Files:**
- Modify: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigator.kt`
- Modify: `shared/core-navigation/src/androidMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigator.android.kt`
- Modify: `shared/core-navigation/src/iosMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetNavigator.ios.kt`
- Modify: `shared/core-navigation/src/commonMain/kotlin/dev/nonoxy/core/navigation/bottomsheet/ModalBottomSheetLayout.kt`

**Контекст.** Мелкие отличия от KMMTemplate: квалификация `ModalBottomSheetNavigator.Destination` в заголовке класса вместо импорта, видимость `NAME` (`internal` → public), передача `NAME` в конструктор `Navigator` на Android, `@SuppressLint("RestrictedApi")` на Android, KDoc в `ModalBottomSheetLayout`.

- [ ] **Step 1: `ModalBottomSheetNavigator.kt`**

Удалить строку импорта:

```kotlin
import dev.nonoxy.core.navigation.bottomsheet.ModalBottomSheetNavigator.Destination
```

Заменить заголовок класса:

```kotlin
expect class ModalBottomSheetNavigator() : Navigator<Destination> {
```

на:

```kotlin
expect class ModalBottomSheetNavigator() : Navigator<ModalBottomSheetNavigator.Destination> {
```

Заменить в companion object:

```kotlin
    companion object {
        internal val NAME: String
    }
```

на:

```kotlin
    companion object {
        val NAME: String
    }
```

- [ ] **Step 2: `ModalBottomSheetNavigator.android.kt`**

Добавить импорт (первой строкой среди импортов, по алфавиту):

```kotlin
import android.annotation.SuppressLint
```

Удалить строку импорта:

```kotlin
import dev.nonoxy.core.navigation.bottomsheet.ModalBottomSheetNavigator.Destination
```

Заменить аннотацию + заголовок класса:

```kotlin
@Navigator.Name(ModalBottomSheetNavigator.NAME)
actual class ModalBottomSheetNavigator actual constructor() : Navigator<Destination>() {
```

на:

```kotlin
@SuppressLint("RestrictedApi")
@Navigator.Name(ModalBottomSheetNavigator.NAME)
actual class ModalBottomSheetNavigator actual constructor() : Navigator<ModalBottomSheetNavigator.Destination>(NAME) {
```

Заменить companion object:

```kotlin
    public actual companion object {
        internal actual const val NAME = "modalBottomSheet"
    }
```

на:

```kotlin
    actual companion object {
        actual const val NAME = "modalBottomSheet"
    }
```

- [ ] **Step 3: `ModalBottomSheetNavigator.ios.kt`**

Удалить строку импорта:

```kotlin
import dev.nonoxy.core.navigation.bottomsheet.ModalBottomSheetNavigator.Destination
```

Заменить заголовок класса:

```kotlin
actual class ModalBottomSheetNavigator actual constructor() : Navigator<Destination>(NAME) {
```

на:

```kotlin
actual class ModalBottomSheetNavigator actual constructor() : Navigator<ModalBottomSheetNavigator.Destination>(NAME) {
```

Заменить companion object:

```kotlin
    actual companion object {
        internal actual const val NAME = "modalBottomSheet"
    }
```

на:

```kotlin
    actual companion object {
        actual const val NAME = "modalBottomSheet"
    }
```

- [ ] **Step 4: `ModalBottomSheetLayout.kt`**

Удалить строку импорта:

```kotlin
import androidx.navigation.compose.NavHost
```

В KDoc заменить строку:

```kotlin
 * Use with a [NavHost] to manage bottom sheet destinations.
```

на:

```kotlin
 * Use with a [androidx.navigation.NavHost] to manage bottom sheet destinations.
```

- [ ] **Step 5: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add shared/core-navigation
git commit -m "Phase 3: polish bottom-sheet navigator to KMMTemplate style"
```

---

### Task 7: Чистка зависимости `compose-navigation-material` и пустого манифеста

**Files:**
- Modify: `shared/main/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Delete: `shared/core-navigation/src/androidMain/AndroidManifest.xml`

**Контекст.** `compose.navigation.material` (`org.jetbrains.compose.material:material-navigation`) после Task 1 остаётся объявленной только в `shared/main/build.gradle.kts` и нигде в `.kt` не используется (reside-track использует собственный material3-навигатор). Удаляем из `main`, затем из version-каталога. Пустой `core-navigation/src/androidMain/AndroidManifest.xml` (`<manifest></manifest>`) удаляем — в KMMTemplate `core-navigation` манифеста нет, новые модули reside-track из Фазы 2 (`core-presentation`, `core-mvikotlin`, `core-domain`, `common-resources`) тоже без манифеста и собираются.

- [ ] **Step 1: Удалить `libs.compose.navigation.material` из `shared/main/build.gradle.kts`**

В блоке `implementations(...)` удалить строку:

```kotlin
        libs.compose.navigation.material,
```

- [ ] **Step 2: Удалить записи из `gradle/libs.versions.toml`**

В секции `[versions]` удалить строку:

```toml
compose-navigation-material = "1.8.0-beta05"
```

В секции `[libraries]` удалить строку:

```toml
compose-navigation-material = { module = "org.jetbrains.compose.material:material-navigation", version.ref = "compose-navigation-material" }
```

- [ ] **Step 3: Удалить пустой манифест `core-navigation`**

```bash
cd /Users/a.dobrov/StudioProjects/reside-track
git rm shared/core-navigation/src/androidMain/AndroidManifest.xml
```

- [ ] **Step 4: Проверить, что `compose.navigation.material` / `compose-navigation-material` больше нигде нет**

Run: `grep -rn "navigation.material\|navigation-material" --include="*.kts" --include="*.toml" shared gradle`
Expected: пусто.

- [ ] **Step 5: Проверка сборки**

Run: `./gradlew :android:app:assembleDevDebug :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add shared/main/build.gradle.kts gradle/libs.versions.toml
git add -A shared/core-navigation
git commit -m "Phase 3: drop unused compose-navigation-material dependency and empty manifest"
```

---

### Task 8: Контрольная точка Фазы 3

**Files:**
- Modify: `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`

- [ ] **Step 1: Полная сборка Android (оба флейвора)**

Run: `./gradlew :android:app:assembleDevDebug :android:app:assembleProdDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Сборка iOS-фреймворка**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: detekt**

Run: `./gradlew detekt`
Expected: BUILD SUCCESSFUL.

**ВАЖНО (memory-гочи Фазы 2):** detekt на reside-track однажды отрапортовал exit code 0, фактически провалившись. Проверять НЕ только exit code — просмотреть вывод на наличие строк `detekt finished with ... issues` / `Analysis failed`. Если есть findings — починить и перезапустить.

- [ ] **Step 4: Ручная проверка флоу навигации**

Запустить приложение (Android) и проверить, что поведение не изменилось:
1. Открывается список комнат (стартовый экран).
2. Кнопка добавления комнаты открывает bottom-sheet «Add Room».
3. Из «Add Room» открывается bottom-sheet «Manage Students» (draft room).
4. Тап по комнате открывает bottom-sheet «Manage Students» (existing room).
5. Кнопка «назад» / свайп закрывают bottom-sheet'ы.

Использовать skill `verify` или `run` для запуска приложения.

- [ ] **Step 5: Обновить статус в спеке миграции**

В файле `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`, раздел 14:

Заменить:

```
**Статус (2026-05-20):** Фазы 1–2 завершены; следующая — Фаза 3 (навигация).
```

на:

```
**Статус (2026-05-21):** Фазы 1–3 завершены; следующая — Фаза 4 (пилот feature-rooms).
```

Заменить строку пункта 3:

```
3. **Навигация** — `Screen` marker-интерфейс + route-объекты.
```

на:

```
3. **Навигация** ✅ — `Screen` marker-интерфейс + top-level route-объекты,
   `core-navigation` доведён до парности с KMMTemplate.
```

- [ ] **Step 6: Commit**

```bash
git add docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md
git commit -m "Phase 3: mark navigation phase complete in migration spec"
```

---

## Self-Review (выполнено при написании плана)

**Покрытие спеки (раздел 11 «Навигация»):**
- `Screen` sealed → marker-интерфейс + top-level `@Serializable` route → Task 3. ✅
- Screen API (`composableXxxScreen`/`navigateToXxxScreen`) — остаётся в impl-модулях, переезд в `ui/api/` отложен до Фаз 4–5 (модуль `ui` ещё не существует). Зафиксировано в разделе «Границы фазы». ✅
- Bottom-sheet-инфраструктура причёсывается под стиль → Tasks 2, 4, 6. ✅
- `navigateOnResumed` / `popBackStackOnResumed` из KMMTemplate → Task 4 (`NavigationUtils.kt`). ✅
- Полный паритет `core-navigation` с KMMTemplate (`NavigationSharedTransitionUtils.kt`) → Task 5. ✅

**Согласованность имён:** route-объекты `RoomsRoute` / `AddRoomRoute` / `ManageStudentsExistingRoomRoute` / `ManageStudentsDraftRoomRoute` используются единообразно в Tasks 3; пакет `bottomsheet` — в Tasks 2, 4, 6.

**Без плейсхолдеров:** все шаги содержат конкретный код/команды.

**Контрольные точки:** сборка обоих флейворов + iOS + detekt проверяются после каждой задачи (per-task build) и финально в Task 8.
