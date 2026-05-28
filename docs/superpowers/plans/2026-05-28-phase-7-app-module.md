# Phase 7 — App-module parity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Привести `:android:app` и `:shared:main` к полной парности с KMMTemplate — repackage под `ui/`, edge-to-edge, network security config per flavor, перенос Koin init на Swift, iOS framework `isStatic = true`.

**Architecture:** Pure structural alignment. Никакой бизнес-логики не трогаем. Каждая таска — изолированный коммит, после каждой проект должен собираться. Атомарные группы (move + import update; Koin init relocation) объединены в один коммит, чтобы build не падал между шагами.

**Tech Stack:** Kotlin Multiplatform 2.3.x, Compose Multiplatform, AGP 9, Koin, Napier, Kotlin/Native ObjC interop (Swift).

**Spec:** `docs/superpowers/specs/2026-05-28-phase-7-app-module-design.md`

---

## File Structure

**Создаются:**
- `android/app/src/dev/res/xml/network_security_config.xml` — dev flavor network security (system + user CAs)
- `android/app/src/prod/res/xml/network_security_config.xml` — prod flavor network security (system only)
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/ui/App.kt` — new home for `App()` composable
- `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/ui/MainViewController.kt` — new home for iOS entry point

**Модифицируются:**
- `android/app/src/main/AndroidManifest.xml` — добавить `networkSecurityConfig`, убрать `configChanges`
- `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt` — добавить `enableEdgeToEdge()`, обновить импорт `App`
- `android/app/build.gradle.kts` — закомментированный release signing TODO-блок
- `shared/main/build.gradle.kts` — iOS framework `isStatic = true`
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt` — убрать default param `appDeclaration: KoinAppDeclaration = {}`
- `iosApp/iosApp/iOSApp.swift` — Koin init в `init()`
- `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md` — Phase 7 ✅

**Удаляются:**
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/App.kt` (старое расположение)
- `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt` (старое расположение)
- `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/Platform.kt`
- `shared/main/src/androidMain/kotlin/dev/nonoxy/residetrack/Platform.android.kt`
- `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/Platform.ios.kt`

**Memory artifacts (после merge):**
- `memory/phase-7-decisions.md` (new)
- `MEMORY.md` (add index line)

---

### Task 1: Network security config per flavor

**Files:**
- Create: `android/app/src/dev/res/xml/network_security_config.xml`
- Create: `android/app/src/prod/res/xml/network_security_config.xml`

- [ ] **Step 1: Create dev flavor network security config**

Write `android/app/src/dev/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

- [ ] **Step 2: Create prod flavor network security config**

Write `android/app/src/prod/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

- [ ] **Step 3: Commit**

```bash
git add android/app/src/dev/res/xml/network_security_config.xml android/app/src/prod/res/xml/network_security_config.xml
git commit -m "Phase 7: add per-flavor network security configs (dev allows user CAs, prod system-only)"
```

---

### Task 2: AndroidManifest — networkSecurityConfig + drop configChanges

**Files:**
- Modify: `android/app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Update AndroidManifest.xml**

Replace contents of `android/app/src/main/AndroidManifest.xml` with:

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
        android:networkSecurityConfig="@xml/network_security_config"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">
        <activity
            android:exported="true"
            android:name=".app.MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 2: Verify build**

Run: `./gradlew :android:app:processDevDebugManifest`
Expected: BUILD SUCCESSFUL (manifest references `@xml/network_security_config` which resolves via dev flavor res).

- [ ] **Step 3: Commit**

```bash
git add android/app/src/main/AndroidManifest.xml
git commit -m "Phase 7: wire networkSecurityConfig in AndroidManifest, drop configChanges attr"
```

---

### Task 3: MainActivity — enableEdgeToEdge

**Files:**
- Modify: `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt`

- [ ] **Step 1: Add enableEdgeToEdge() call**

Replace contents of `MainActivity.kt`:

```kotlin
package dev.nonoxy.residetrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.nonoxy.residetrack.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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

Note: `import dev.nonoxy.residetrack.App` остаётся прежним — App.kt ещё не переехал. Поменяем в Task 7.

- [ ] **Step 2: Verify compile**

Run: `./gradlew :android:app:compileDevDebugKotlin`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Commit**

```bash
git add android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt
git commit -m "Phase 7: enable edge-to-edge in MainActivity"
```

---

### Task 4: android/app/build.gradle.kts — release signing TODO block

**Files:**
- Modify: `android/app/build.gradle.kts`

- [ ] **Step 1: Add commented-out release signing block**

Find `signingConfigs {` block. Replace it with:

```kotlin
    signingConfigs {
        // TODO: Create release keystore and uncomment this
        // register("release").configure {
        //     file("$rootDir/signing.properties").let { file ->
        //         if (!file.canRead()) error("signing.properties file read error")
        //
        //         val properties = Properties().apply {
        //             file.inputStream().use { stream -> load(stream) }
        //         }
        //
        //         storeFile = file("$rootDir/keystores/release.keystore.jks")
        //         storePassword = properties.getProperty("keystorePassword")
        //         keyAlias = properties.getProperty("keyAlias")
        //         keyPassword = properties.getProperty("keyPassword")
        //     }
        // }

        named("debug").configure {
            val DEBUG_STORE_PASSWORD: String by project
            val DEBUG_KEY_ALIAS: String by project

            storeFile = file("$rootDir/keystores/debug.keystore.jks")
            storePassword = DEBUG_STORE_PASSWORD
            keyAlias = DEBUG_KEY_ALIAS
            keyPassword = DEBUG_STORE_PASSWORD
        }
    }
```

- [ ] **Step 2: Verify Gradle script syntax**

Run: `./gradlew :android:app:help`
Expected: BUILD SUCCESSFUL (script parses cleanly).

- [ ] **Step 3: Commit**

```bash
git add android/app/build.gradle.kts
git commit -m "Phase 7: add commented-out release signing TODO block (matches KMMTemplate)"
```

---

### Task 5: shared/main build.gradle.kts — iOS framework isStatic = true

**Files:**
- Modify: `shared/main/build.gradle.kts`

- [ ] **Step 1: Flip isStatic flag**

Find:

```kotlin
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false
        }
```

Replace `isStatic = false` with `isStatic = true`.

- [ ] **Step 2: Verify iOS framework links**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. Если линк-фаза падает на duplicate symbols — фиксировать как риск в спеке и откатить.

- [ ] **Step 3: Commit**

```bash
git add shared/main/build.gradle.kts
git commit -m "Phase 7: iOS framework isStatic=true (KMMTemplate default)"
```

---

### Task 6: Move App.kt to ui/ subpackage + update MainActivity import (atomic)

**Files:**
- Create: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/ui/App.kt`
- Delete: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/App.kt`
- Modify: `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt`
- Modify: `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt`

- [ ] **Step 1: Write new App.kt under ui/**

Create `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/ui/App.kt`:

```kotlin
package dev.nonoxy.residetrack.ui

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

- [ ] **Step 2: Delete old App.kt**

```bash
git rm shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/App.kt
```

- [ ] **Step 3: Update MainActivity import**

In `android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt` replace:

```kotlin
import dev.nonoxy.residetrack.App
```

with:

```kotlin
import dev.nonoxy.residetrack.ui.App
```

- [ ] **Step 4: Update MainViewController import**

In `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt` add/replace import line so that file references `dev.nonoxy.residetrack.ui.App`. (Текущий MainViewController использует `App()` без явного импорта — он лежит в том же пакете `dev.nonoxy.residetrack`. После переезда App в `.ui` нужен explicit import.)

Full updated MainViewController.kt (still at old path — переедет в Task 7):

```kotlin
package dev.nonoxy.residetrack

import androidx.compose.ui.window.ComposeUIViewController
import dev.nonoxy.residetrack.di.initKoin
import dev.nonoxy.residetrack.ui.App
import platform.UIKit.UIViewController

private var koinStarted = false

fun MainViewController(): UIViewController {
    if (!koinStarted) {
        koinStarted = true
        initKoin()
    }
    return ComposeUIViewController { App() }
}
```

- [ ] **Step 5: Verify build**

Run: `./gradlew :android:app:compileDevDebugKotlin :shared:main:compileKotlinIosSimulatorArm64`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 6: Commit**

```bash
git add shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/ui/App.kt \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/App.kt \
        android/app/src/main/kotlin/dev/nonoxy/residetrack/app/MainActivity.kt \
        shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt
git commit -m "Phase 7: move App.kt under dev.nonoxy.residetrack.ui (KMMTemplate parity)"
```

---

### Task 7: iOS Koin init relocation — Swift entry point (atomic, multi-file)

**Files:**
- Create: `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/ui/MainViewController.kt`
- Delete: `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt`
- Modify: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`
- Modify: `iosApp/iosApp/iOSApp.swift`

**Что и зачем:** атомарно убираем Koin bootstrap из Kotlin'а и переносим вызов в Swift entry point. Шаги между файлами выполняются как один коммит, чтобы iOS app собирался на каждой границе истории.

- [ ] **Step 1: Write new MainViewController.kt under ui/ (one-liner, без bootstrap)**

Create `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/ui/MainViewController.kt`:

```kotlin
package dev.nonoxy.residetrack.ui

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App() }
```

- [ ] **Step 2: Delete old MainViewController.kt**

```bash
git rm shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt
```

- [ ] **Step 3: Tighten initKoin signature — drop default param**

Replace the signature line in `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt`:

```kotlin
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
```

with:

```kotlin
fun initKoin(appDeclaration: KoinAppDeclaration) {
```

- [ ] **Step 4: Update iOSApp.swift — call Koin init from Swift**

Replace `iosApp/iosApp/iOSApp.swift` with:

```swift
import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoin(appDeclaration: { _ in })
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

Note: `doInitKoin` — это auto-generated ObjC-name для `initKoin` (Kotlin/Native префиксует `init*` функции `do`, потому что `init` зарезервирован в ObjC). Если после `linkDebugFrameworkIosSimulatorArm64` сгенерированный `ComposeApp.h` содержит `initKoin:` без префикса — заменить на `KoinKt.initKoin(appDeclaration: { _ in })`. Сценарий-fallback должен сработать одним из двух вариантов; третий не понадобится.

- [ ] **Step 5: Verify Kotlin compile + iOS framework link**

Run: `./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64`
Expected: BUILD SUCCESSFUL. Команда генерирует `ComposeApp.h` — заглянуть в него и подтвердить, что есть `+ (void)doInitKoinWithAppDeclaration:(void (^)(KoinApplication *))appDeclaration ...` либо `+ (void)initKoinWithAppDeclaration:...`. Соответственно поправить Swift, если выбран не тот вариант.

Run the Swift compile via Xcode CLI (если доступно) или просто проверить рунтайм на симуляторе вручную.

- [ ] **Step 6: Manual iOS smoke check**

Open `iosApp/iosApp.xcodeproj` в Xcode (или через CLI: `xed iosApp/iosApp.xcodeproj`), build & run на iOS Simulator. Ожидаемо: запускается, открывается экран комнат, никакого crash из-за `Koin not started`. Если экран открылся — Koin успешно стартовал из Swift.

- [ ] **Step 7: Commit**

```bash
git add shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/ui/MainViewController.kt \
        shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/MainViewController.kt \
        shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/di/Koin.kt \
        iosApp/iosApp/iOSApp.swift
git commit -m "Phase 7: move Koin init from MainViewController to iOSApp.swift; MainViewController becomes one-liner"
```

---

### Task 8: Delete unused Platform.* boilerplate

**Files:**
- Delete: `shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/Platform.kt`
- Delete: `shared/main/src/androidMain/kotlin/dev/nonoxy/residetrack/Platform.android.kt`
- Delete: `shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/Platform.ios.kt`

- [ ] **Step 1: Verify Platform symbols truly unused**

Run:

```bash
grep -rn "dev\.nonoxy\.residetrack\.Platform\|dev\.nonoxy\.residetrack\.getPlatform\|dev\.nonoxy\.residetrack\.AndroidPlatform\|dev\.nonoxy\.residetrack\.IOSPlatform" --include="*.kt" --include="*.swift" /Users/a.dobrov/StudioProjects/reside-track
```

Expected: только сами Platform-файлы. Если найден внешний consumer — STOP, переоценить решение перед удалением.

- [ ] **Step 2: Delete files**

```bash
git rm shared/main/src/commonMain/kotlin/dev/nonoxy/residetrack/Platform.kt \
       shared/main/src/androidMain/kotlin/dev/nonoxy/residetrack/Platform.android.kt \
       shared/main/src/iosMain/kotlin/dev/nonoxy/residetrack/Platform.ios.kt
```

- [ ] **Step 3: Verify build**

Run: `./gradlew :shared:main:compileKotlinIosSimulatorArm64 :shared:main:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git commit -m "Phase 7: delete unused Platform expect/actual boilerplate from :shared:main"
```

---

### Task 9: Full verification

**Files:** none (gate task)

- [ ] **Step 1: Run Android dev assembly**

```bash
./gradlew :android:app:assembleDevDebug
```
Expected: BUILD SUCCESSFUL. APK named `app-dev-debug-<version>.apk` produced.

- [ ] **Step 2: Run Android prod assembly**

```bash
./gradlew :android:app:assembleProdDebug
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Run iOS framework link**

```bash
./gradlew :shared:main:linkDebugFrameworkIosSimulatorArm64
```
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Run detekt**

```bash
./gradlew detekt
```
Expected: BUILD SUCCESSFUL, no violations.

- [ ] **Step 5: Manual smoke (Android dev + prod)**

Install both flavors на эмулятор, открыть экран комнат, добавить комнату, открыть manage-students. Edge-to-edge корректен (контент под статусбаром, инсеты в Compose отрабатывают).

- [ ] **Step 6: Manual smoke (iOS simulator)**

Build & run iosApp. Экран комнат открывается → Koin запустился из Swift. Если CRASH `KoinApplication has not been started` — вернуться к Task 7 Step 4 и проверить mangled-name.

---

### Task 10: Mark Phase 7 complete in migration spec

**Files:**
- Modify: `docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md`

- [ ] **Step 1: Update status line**

Find:

```
**Статус (2026-05-28):** Фазы 1–6 завершены; следующая — Фаза 7 (App-модуль: `composeApp` → `:android:app` + `:shared:main`).
```

Replace with:

```
**Статус (2026-05-28):** Фазы 1–7 завершены; следующая — Фаза 8 (Пакеты: `dev.nonoxy.*` → `dev.nonoxy.residetrack.*`).
```

- [ ] **Step 2: Mark Phase 7 ✅ in phases list**

Find:

```
7. **App-модуль** — `composeApp` → `:android:app` + `:shared:main`.
```

Replace with:

```
7. **App-модуль** ✅ — `:android:app` и `:shared:main` доведены до парности с KMMTemplate: file-moves под `ui/`, удалён неиспользуемый Platform-boilerplate, edge-to-edge, network security config per flavor, Koin init вынесен в `iOSApp.swift`, iOS framework `isStatic = true`.
```

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/specs/2026-05-20-kmmtemplate-migration-design.md
git commit -m "docs: mark Phase 7 (app-module parity) complete in migration spec"
```

---

### Task 11: Memory artifacts

**Files:**
- Create: `/Users/a.dobrov/.claude/projects/-Users-a-dobrov-StudioProjects-reside-track/memory/phase-7-decisions.md`
- Modify: `/Users/a.dobrov/.claude/projects/-Users-a-dobrov-StudioProjects-reside-track/memory/MEMORY.md`

- [ ] **Step 1: Write phase-7-decisions memory**

Create `phase-7-decisions.md`:

```markdown
---
name: phase-7-decisions
description: Phase 7 ✅ — :android:app + :shared:main parity with KMMTemplate (file moves, edge-to-edge, network security config, Swift Koin init, isStatic=true)
metadata:
  type: project
---

Phase 7 (2026-05-28) — структурная парность app-модулей с KMMTemplate.

**Решения:**
- App.kt и MainViewController.kt репакованы под `dev.nonoxy.residetrack.ui` (как в KMMTemplate `dev.nonoxy.kmmtemplate.ui`).
- `Platform.kt` / `Platform.android.kt` / `Platform.ios.kt` в `:shared:main` удалены — unused KMP boilerplate. Однотипный `getPlatform()` в `:shared:common` остался (отдельный пакет, используется).
- `navigation/ResideTrackNavHost.kt` оставлен отдельным файлом — у нас 4 экрана + bottom-sheet navigator, inline в App.kt раздувает.
- iOS framework `isStatic = true` (KMMTemplate-default, меньше .ipa, быстрее старт).
- Koin init вынесен из `MainViewController.kt` в `iosApp/iosApp/iOSApp.swift` через `KoinKt.doInitKoin(appDeclaration: { _ in })`. Имя `doInitKoin` — Kotlin/Native ObjC name-mangling для `init*` функций. `initKoin` в Koin.kt теперь без default param.
- `enableEdgeToEdge()` в MainActivity.
- `android:configChanges=…` снят с активити — KMMTemplate не имеет, лишний debt.
- Network security config per flavor: dev допускает user CAs (charles/прокси), prod — только system CAs.
- Release signing TODO-блок добавлен закомментированным в `android/app/build.gradle.kts` — заготовка для будущего keystore.
- `moko.resources` plugin в `:shared:main` не подключали — MR живёт в `:shared:common-resources`.

См. [[kmmtemplate-migration]] и [[kmmtemplate-full-compliance]].
```

- [ ] **Step 2: Append MEMORY.md index line**

Append at end of `MEMORY.md`:

```
- [Phase 7 decisions](phase-7-decisions.md) — Фаза 7 ✅ (`:android:app` + `:shared:main` parity, file-moves под `ui/`, edge-to-edge, network security config per flavor, Swift Koin init, iOS `isStatic=true`)
```

(memory files — non-git, не коммитятся).

---

## Self-Review

**Spec coverage:**

| Spec item | Task |
|---|---|
| A1 enableEdgeToEdge | T3 |
| A2 AndroidManifest networkSecurityConfig + drop configChanges | T2 |
| A3 dev/prod network_security_config.xml | T1 |
| A4 build.gradle.kts release signing TODO | T4 |
| B5 App.kt → ui/ | T6 |
| B6 MainViewController.kt → ui/ | T7 |
| B7 Delete Platform.* | T8 |
| B8 NavHost оставить отдельно | соблюдено (не трогаем файл) |
| B9 MainActivity import update | T6 |
| C10 isStatic = true | T5 |
| C11 Koin.kt remove default param | T7 |
| C12 moko.resources не подключаем | соблюдено (build.gradle.kts не трогаем по этому пункту) |
| C13 changeGitHooksDir пропускаем | соблюдено |
| C14 extra deps оставить | соблюдено (build.gradle.kts не трогаем по этому пункту) |
| D15 MainViewController one-liner | T7 |
| D16 KoinKt.doInitKoin в Swift | T7 |
| Верификация | T9 |
| Памятка по итогам (spec обновление, memory, single commit) | T10 + T11 (single-commit-per-phase предпочтение spec'а отвергнуто в пользу bite-sized tasks; каждая таска = отдельный коммит, потом ничего не сквошим) |

**Placeholder scan:** нет TBD/TODO в шагах, весь код приведён полностью.

**Type consistency:** `initKoin(appDeclaration: KoinAppDeclaration)` без default — согласовано между T7 Step 3 и Swift Step 4. `dev.nonoxy.residetrack.ui.App` — согласовано между T6 (создание файла) и T6 (импорт в MainActivity).

**Отклонение от спеки:** в спеке предполагался single squash-commit за всю фазу. План делит её на 10 atomic commits — это упрощает review, не ломает build на каждой границе, и согласуется с Phases 5/6 стилем. После merge можно при желании squash'нуть, но не обязательно.
