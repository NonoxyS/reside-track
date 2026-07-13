# reside-track

Rental management KMM app (Android + iOS) — room check-in/out tracking, upcoming checkouts, notifications.

## Build Commands

```shell
./gradlew :android:app:assembleDevDebug                        # Android debug APK (dev flavor)
./gradlew :android:app:lintDevDebug                            # Android Lint
./gradlew detekt                                               # Detekt static analysis
./gradlew :shared:feature-rooms:impl:testAndroidHostTest       # commonTest on Android host JVM
```

iOS: open `iosApp/iosApp.xcodeproj` in Xcode.

## Stack

- Kotlin 2.3.10, Compose Multiplatform 1.10.1 (material3 1.9.0), AGP 9.0.0, Java 17.
- Gradle 9.4.1.
- Android: compileSdk/targetSdk 36, minSdk 26. Flavors: `dev` / `prod`.
- Room 2.8.4 + sqliteBundled 2.6.2 (local DB in `:shared:core-database`).
- Coroutines 1.10.2, kotlinx.serialization 1.10.0, kotlinx-datetime 0.7.1, immutableCollections 0.4.0.
- Koin 4.1.1. Per-module Koin module functions; `:shared:main/di/MainModule.kt` aggregates includes.
- MVIKotlin 4.3.0 (BaseExecutor + `coreMVIKotlinModule` in `:shared:core-mvikotlin`, LoggingStoreFactory via Napier).
- Napier 2.7.1.
- moko-mvvm 0.16.1 (CFlow/CStateFlow for iOS contract on `BaseViewModel`).
- moko-resources 0.26.0 in `:shared:common-resources` (generated `MR` in `dev.nonoxy.residetrack.common.resources`).
- moko-permissions 0.20.1.
- compose-navigation 2.9.2 + `Screen` marker interface + `@Serializable` route objects + per-feature `ScreenApi`.
- Multi-module project (25 modules) under `android/` and `shared/`. `build-logic` composite build with `kmp-library`, `kmp-feature-setup`, `compose-multiplatform-setup`, `json-serialization` convention plugins.

## Architecture

25 modules under `android/` and `shared/`. KMP layout per module: `src/{commonMain,commonTest,androidMain,iosMain}/kotlin/...`. Targets: Android + iOS only.

| Module | Type | Responsibility |
|---|---|---|
| `:android:app` | Android-only | `com.android.application` entry. `AndroidApp`, `AppActivity`, manifest. Package `dev.nonoxy.residetrack`. |
| `:shared:main` | KMP shell | `App.kt`, `TabContainerNavHost`, `iosMain/MainViewController.kt`. Koin composition root (`initKoin()` + `MainModule`). Targets: Android + iOS. |
| `:shared:common` | KMP | `coRunCatching`, `ResultExtensions`, `OneTimeEvent`, `CoroutineDispatchers`, `StringConverter`, `commonModule`. |
| `:shared:common-ui` | KMP + Compose | Design system: `ResideTrackTheme` (colors/typography/shapes/dimens/sizes), `ShowStateData`, `ResideTrackLoader`, `ResideTrackButton`, `ResideTrackSnackbar`, `LoadingState`, `ErrorLoadingState`. |
| `:shared:common-resources` | KMP + moko-resources | Shared strings/fonts/images. `MR` in `dev.nonoxy.residetrack.common.resources`. |
| `:shared:core-domain` | KMP | App-wide pure domain models. |
| `:shared:core-mvikotlin` | KMP | `BaseExecutor`, `coreMVIKotlinModule` (binds `StoreFactory` → `LoggingStoreFactory(DefaultStoreFactory())` via Napier). |
| `:shared:core-presentation` | KMP | `BaseViewModel<S,L>`, `BaseIosViewModel`. |
| `:shared:core-navigation` | KMP + Compose | `Screen` marker, `NavigationUtils` (`navigateOnResumed`, `popBackStackOnResumed`, `CheckNavigationResult`), `FloatingBottomBar`, `LocalFloatingBottomBarInset`, route helpers. |
| `:shared:core-database` | KMP | Room 2.8.4 + sqliteBundled — DB entities, DAOs, `ResideTrackDatabase`. |
| `:shared:core-initializer` | KMP | `AppInitializer` (priority-level init chain), room seeding from `MR.files`. |
| `:shared:core-rooms` | KMP | `RoomsRepository`, `DraftRoomStorage` — rooms CRUD and draft state. |
| `:shared:core-backup` | KMP | DB-level export/import (backup/restore). |
| `:shared:core-notifications` | KMP | Local push notifications for checkout reminders (scheduled via WorkManager on Android). |
| `:shared:feature-splash:*` | KMP | Splash screen (presentation + ui only — no Store). |
| `:shared:feature-rooms:*` | KMP | Room list with check-in/out state. api/impl/presentation/ui. |
| `:shared:feature-add-room:*` | KMP | Add new room (bottom sheet). api/impl/presentation/ui. |
| `:shared:feature-room-editor:*` | KMP | Edit/delete room (full-screen). api/impl/presentation/ui. |
| `:shared:feature-upcoming:*` | KMP | Upcoming checkouts bucketed by date (overdue/today-tomorrow/this-week). api/impl/presentation/ui. |
| `:shared:template-module` | KMP | Blank module template. |

### Dependency invariants

- `:shared:feature-A:impl` never depends on `:shared:feature-B:*`. Cross-feature reuse hoists to a `:shared:core-*` module.
- `:shared:feature-X:api` exports only contracts (Store, Intent/State/Label, public domain models). Depends only on `:shared:core-domain` + stdlib + MVIKotlin core.
- `:shared:feature-X:impl` depends on its own `:api` + relevant `:shared:core-*` modules.
- `:shared:feature-X:presentation` depends on its `:api` + `:shared:core-presentation`, NOT on `:impl`.
- `:shared:feature-X:ui` depends on its `:presentation` + `:shared:common-ui` + `:shared:common-resources`.
- `:shared:core-*` modules are single (no api/impl split).
- `:shared:main` is the sole Composition Root — only module pulling `:*:impl` into the link graph.

## Rules (`.claude/rules/`)

| File | Description |
| --- | --- |
| `mobile-overview.mdc` | Stack, module layout, naming conventions |
| `mobile-architecture.mdc` | MVIKotlin Store/Executor/Reducer, `BaseViewModel<State, Label>`, Koin DI, Compose Navigation, dependency invariants |
| `mobile-compose.mdc` | Design system, recomposition, composable rules, `LocalFloatingBottomBarInset` |
| `mobile-data-layer.mdc` | Room DB, repositories, DTO/domain split, `CoroutineDispatchers` |
| `mobile-error-handling.mdc` | `coRunCatching`, `Result<T>`, Napier logging |
| `mobile-resources.mdc` | moko-resources 0.26.x — `MR.strings/fonts/images`, `StringConverter` |
| `mobile-code-rules.mdc` | Access modifiers, comments, NPE-safety |
| `review-checklist.md` | Machine-readable LLM review checklist |

## Key Gotchas

- In suspend code use `coRunCatching { ... }` from `common/utils/CoroutineUtils.kt` — NOT `runCatching` (swallows `CancellationException`) and NOT bare `try/catch`. Plain `runCatching` only in non-suspend paths.
- `CoroutineDispatchers` is the only way to obtain dispatchers in `commonMain` — inject it. Never reference `Dispatchers.IO/Default` directly in `commonMain`.
- `ImmutableList`/`ImmutableMap` only in `Ui*State` models (converted in the state mapper). Domain models and `Store.State` use plain `List`/`Map`.
- `StringResource` in UI-enum models: use **lazy getter** `val title get() = when(this) { ... MR.strings.x }`, NOT a constructor field. Constructor-field MR access triggers static-init on K/N in unit tests → `FileFailedToInitializeException`.
- Snackbar/one-shot strings: resolve in presentation `Ui*LabelMapper` (inject `StringConverter`), not in `@Composable`. `Ui*Label.ShowX(message: String)` carries the ready string. UI only calls `label.message`.
- Presentation module that needs `MR` must manually add `projects.shared.commonResources` in its `build.gradle.kts` — `kmpFeatureSetup` auto-wires common-resources only to `:ui` modules.
- `DialogScaffold` is only for bottom sheets (wrap-content scaffold). For full-screen destinations use `Box(Modifier.fillMaxSize())` + `ShowStateData` — `DialogScaffold` collapses inner `weight(1f)` boxes to 0.
- `navigateOnResumed` / `runOnResumed` silently drop calls from modal sheets — a `FloatingWindow` backstack entry never reaches `RESUMED`. Navigate directly from sheets (`navController.navigate(route) { ... }`); keep `navigateOnResumed` only for screen-to-screen navigation.
- `BaseViewModel.onCleared()` must call `store.dispose()` — Store does not auto-dispose.
- `FloatingBottomBar` floats over content. Scrollable content under it must add `LocalFloatingBottomBarInset.current` to bottom `contentPadding`. Never hardcode the clearance height.
- `MaterialTheme.colorScheme.*` / `MaterialTheme.typography.*` forbidden in feature UI — always use `ResideTrackTheme.colors.*` / `.typography.*`.
- Material components (AlertDialog, DatePicker, TextButton…) do not inherit brand colors automatically — set each slot color explicitly from `ResideTrackTheme.colors`. Exception: `LocalTextSelectionColors` is provided globally in `Theme.kt` (covers cursor + selection handle).
- Koin binding anti-pattern: `factoryOf(::Impl) { bind<I>() }` registers `Impl` as primary key and `I` as secondary → impl leaks into the graph. Use `factoryOf<I>(::Impl)` (no-param impl) or `factory<I> { new(::Impl) }` (has params).
- `Store.Label`, `UiXLabel`, `UiXLabelMapper(Impl)` are always present in every feature — even if the sealed interface is empty. Never remove "empty" scaffolding; it prevents boilerplate churn when the first Label is added.
- `Napier.base(DebugAntilog(...))` is called once per platform entry, BEFORE `initKoin(...)`.
- All deps go through `gradle/libs.versions.toml`. No version literals in `build.gradle.kts`.
- `kmpFeatureSetup` auto-wires by submodule name — name MUST be one of `api`/`impl`/`presentation`/`ui`, otherwise build fails. Do NOT redeclare auto-wired deps (see `mobile-architecture.mdc#build-conventions`).
- Git hooks in `.githooks/`. Enable: `git config core.hooksPath .githooks`.
- CI (`.github/workflows/`): PRs to `develop` run Detekt + Android Lint (`:android:app:lintDevDebug`) + iOS checks.
- Detekt baseline: `linters/detekt/baseline.xml` — regenerate with `./gradlew detektBaseline`.
