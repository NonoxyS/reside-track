# Review Checklist

LLM reviewer answers each item **yes / no + cited line**. "Yes" = violation found.

## Architecture / Layers → `mobile-architecture.mdc#presentation-layer-discipline`

- [ ] Domain type reaches a `@Composable` directly (not via `Ui*` model)?
- [ ] `ImmutableList` / `ImmutableMap` used in domain model or `Store.State` (not only in `Ui*`)?
- [ ] Mapper calls a Repository or runs `suspend` code?
- [ ] Snackbar/label string resolved inside `@Composable` instead of in `Ui*LabelMapper`?
- [ ] `when(domainEnum) -> MR.strings.x` mapping inside a `@Composable`?
- [ ] `StringResource` used as a constructor field in a UI-enum (not a lazy getter)?
- [ ] `Store.Label` / `UiXLabel` / `UiXLabelMapper(Impl)` missing or removed from a feature?
- [ ] `store.dispose()` missing from `BaseViewModel.onCleared()`?

## DI / Koin → `mobile-architecture.mdc#koin-di`

- [ ] `factoryOf(::Impl) { bind<I>() }` or `singleOf(::Impl) { bind<I>() }` used (impl leaks into graph)?
- [ ] Parameterless binding uses `single<I> { Impl() }` instead of `singleOf<I>(::Impl)`?
- [ ] Dependency resolved via `KoinPlatform.getKoin().get()` inside a class body?

## Compose / Design System → `mobile-compose.mdc`

- [ ] `MaterialTheme.colorScheme.*` or `MaterialTheme.typography.*` accessed in `feature-*/ui/`?
- [ ] Raw dp literal (`16.dp`, `8.dp`, …) instead of a DS spacing token from `Dimens.kt`/`Sizes.kt`?
- [ ] String literal in `Text("…")` instead of `stringResource(MR.strings.x)`?
- [ ] `Color(0x…)` / `Color(0xFF…)` literal instead of DS token?
- [ ] UI-emitting `@Composable` missing `modifier: Modifier = Modifier` parameter? (Exception: root-screen composables that call `koinViewModel<T>()`.)
- [ ] `DialogScaffold` used in a full-screen destination (not a bottom sheet)?
- [ ] Scrollable content under `FloatingBottomBar` hardcodes clearance instead of using `LocalFloatingBottomBarInset.current`?

## Navigation → `mobile-architecture.mdc#compose-navigation`

- [ ] Route object placed in `:shared:core-navigation` instead of `Feature<X>ScreenApi.kt`?
- [ ] `navigateOnResumed` called from a modal sheet (use direct `navController.navigate` from sheets)?
- [ ] `ScreenApi.kt` missing `navigateTo<X>Screen(...)` extension or wrong declaration order?

## Error Handling → `mobile-error-handling.mdc`

- [ ] `runCatching { }` or bare `try/catch` in a `suspend` function instead of `coRunCatching`?
- [ ] Repository throws to caller instead of returning `Result.failure(...)`?
- [ ] Missing `Napier.e(throwable, message)` in catchBlock?
- [ ] `println(...)` used instead of `Napier.*`?

## Data Layer → `mobile-data-layer.mdc`

- [ ] `Dispatchers.IO` / `Dispatchers.Default` referenced directly in `commonMain` code?
- [ ] DB entity type used outside `core-database` or leaking into domain models?

## Code Quality → `mobile-code-rules.mdc`

- [ ] `!!` used without a preceding null-filter or exhaustive check?
- [ ] Comment that describes *what* the code does (not *why*)?
- [ ] KDoc downgraded to inline `//` on a declaration?
- [ ] Version literal in a `build.gradle.kts` instead of catalog alias?
- [ ] Auto-wired dependency redeclared in a feature module's `build.gradle.kts`?
