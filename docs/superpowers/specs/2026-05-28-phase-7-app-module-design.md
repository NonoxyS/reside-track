# Phase 7 — App-модуль: parity с KMMTemplate

**Дата:** 2026-05-28
**Статус:** approved, готово к плану
**Контекст:** см. `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` §14, фаза 7.

## Цель

Довести `:android:app` и `:shared:main` до полного соответствия эталону KMMTemplate. Структурный split `composeApp → :android:app + :shared:main` выполнен ещё в Phase 2; здесь — gap-filling и repackage, чтобы ничего не осталось «как было раньше».

## Скоуп

### A. `:android:app`

1. **MainActivity.kt** — добавить `enableEdgeToEdge()` перед `super.onCreate()`.
2. **AndroidManifest.xml**:
   - добавить `android:networkSecurityConfig="@xml/network_security_config"` в `<application>`;
   - убрать `android:configChanges="..."` с `<activity>` (KMMTemplate его не имеет).
3. **Новые файлы под flavor-specific res:**
   - `android/app/src/dev/res/xml/network_security_config.xml` —
     `cleartextTrafficPermitted="false"`, `<certificates src="system" />` + `<certificates src="user" />` (dev допускает user CAs для прокси/charles);
   - `android/app/src/prod/res/xml/network_security_config.xml` —
     `cleartextTrafficPermitted="false"`, только `<certificates src="system" />`.
4. **android/app/build.gradle.kts** — добавить закомментированный release signing TODO-блок (как в KMMTemplate `signingConfigs`).

### B. `:shared:main` — repackage

5. `commonMain/.../App.kt` → `commonMain/.../ui/App.kt`. Пакет: `dev.nonoxy.residetrack.ui`.
6. `iosMain/.../MainViewController.kt` → `iosMain/.../ui/MainViewController.kt`. Пакет: `dev.nonoxy.residetrack.ui`.
7. **Удалить** неиспользуемый KMP boilerplate:
   - `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/Platform.kt`
   - `shared/main/src/androidMain/kotlin/dev/nonoxy/residetrack/Platform.android.kt`
   - `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/Platform.ios.kt`
8. `navigation/ResideTrackNavHost.kt` оставляем отдельным файлом (4 экрана + bottom-sheet navigator — inline в App.kt раздувает).
9. MainActivity.kt — обновить импорт `dev.nonoxy.residetrack.App` → `dev.nonoxy.residetrack.ui.App`.

### C. `:shared:main` — config

10. **build.gradle.kts** — iOS framework `isStatic = false` → `isStatic = true` (KMMTemplate-default, меньше .ipa, быстрее старт).
11. **di/Koin.kt** — убрать default value `appDeclaration: KoinAppDeclaration = {}` → `appDeclaration: KoinAppDeclaration` (обязательный параметр, как в KMMTemplate).
12. Не подключаем `alias(libs.plugins.moko.resources)` в `:shared:main` — MR живёт в `:shared:common-resources`. KMMTemplate подключает по историческим причинам; нам не нужно.
13. `changeGitHooksDir` task — пропускаем (cosmetic, registration закомментирована и у KMMTemplate).
14. Лишние commonMain deps (navigation, lifecycle, koin-compose) — **оставляем**, нужны для NavHost и Koin compose в `App()` / `ResideTrackNavHost`.

### D. iOS Koin init — перенос в Swift

15. `MainViewController.kt` — стрипаем bootstrap, становится one-liner:
    ```kotlin
    fun MainViewController() = ComposeUIViewController { App() }
    ```
    Удаляем `koinStarted` флаг и `initKoin()` вызов.
16. `iosApp/iosApp/iOSApp.swift` — добавляем Koin init в `init()`:
    ```swift
    @main
    struct iOSApp: App {
        init() {
            KoinKt.doInitKoin(appDeclaration: { _ in })
        }
        var body: some Scene {
            WindowGroup { ContentView() }
        }
    }
    ```
    Имя `doInitKoin` — стандартный Kotlin/Native name-mangling для функций `do*` (collides with Swift reserved `init`). Если генерируется как `initKoin`, используем его.

### E. Out of scope

- Package rename `dev.nonoxy.*` → `dev.nonoxy.residetrack.*` — отдельная Phase 8.
- `KmmTemplateTheme` → `ResideTrackTheme` уже сделан в более ранней фазе.
- ContentView.swift `.ignoresSafeArea(.keyboard)` — намеренное отличие, не трогаем.

## Верификация

После всех изменений:

- `./gradlew :android:app:assembleDevDebug` — green
- `./gradlew :android:app:assembleProdDebug` — green
- `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64` — green
- `./gradlew detekt` — green
- iOS: запуск симулятора через `iosApp`, экран комнат открывается (доказательство: Koin теперь стартует из Swift, а не из MainViewController).
- Android: запуск devDebug + prodDebug на эмуляторе, edge-to-edge корректен, экран комнат открывается.

## Риски

- **Перенос Koin init в Swift** — единственное место, где можно споткнуться. Если KoinKt экспортирует имя `doInitKoin` (mangled) — использовать его; иначе `initKoin`. Проверить генерируемый header `ComposeApp.h` после линка фреймворка.
- **`isStatic = true`** — потенциально новые ошибки линковки на iOS (duplicate symbols в multi-framework setup). У нас один фреймворк → должно пройти.
- **Удаление `Platform.kt`** — проверить, что нигде не используется `getPlatform()` (по grep — не должно).

## Памятка по итогам

После завершения:
- обновить `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` (Phase 7 → ✅);
- зафиксировать решения в `memory/phase-7-decisions.md` + добавить в `MEMORY.md`;
- single commit за всю фазу: `Phase 7: align :android:app + :shared:main with KMMTemplate (file moves, edge-to-edge, network security config, Swift Koin init, isStatic=true)`.
