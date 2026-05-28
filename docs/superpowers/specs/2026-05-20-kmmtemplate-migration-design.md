# reside-track → KMMTemplate: миграция архитектуры

**Дата:** 2026-05-20
**Статус:** дизайн одобрен, ожидает плана реализации

## 1. Контекст

reside-track — Kotlin Multiplatform приложение (Android + iOS, Compose Multiplatform)
для учёта расселения студентов по комнатам. Хранилище локальное (Room), сети нет.

Текущая архитектура:
- самописный MVI: `BaseViewModel<State, Event, Action>` в `shared:common`,
  `obtainEvent()`, прямое присваивание `viewState`/`viewAction`;
- feature-модули разбиты на 2 части — `api` / `impl` (вся логика, presentation и ui свалены в `impl`);
- ресурсы — Compose Resources; в `feature-manage-students` есть костыль `StringProvider`
  для доступа к строкам вне Compose;
- единый app-модуль `composeApp` (Android-приложение + сборка iOS-фреймворка);
- пакеты `dev.nonoxy.*` (без имени приложения).

KMMTemplate (`/Users/a.dobrov/StudioProjects/KMMTemplate`) — эталонный шаблон.

## 2. Цель

Привести reside-track в полное соответствие архитектуре и стилю кода KMMTemplate
**без изменения пользовательского поведения приложения**.

## 3. Зафиксированные решения

- Одна спека на всю миграцию; исполнение — упорядоченными фазами с проверкой сборки
  между фазами.
- Baseline до миграции закоммичен: `78f2a06 Add manage students feature`.
- Версии (Kotlin/AGP/Compose/Koin и др.) бампаются до уровня KMMTemplate по ходу миграции.
- Полное соответствие KMMTemplate; решения KMMTemplate принимаются как есть, своего
  не выдумываем.
- При исполнении git worktree не используется — работа в текущем checkout.
- Сборка и detekt проверяются между фазами.

## 4. Не-цели и границы

- `core-network` **не добавляется** — в приложении нет сети (YAGNI).
- Room (`core-database`) **сохраняется** — в KMMTemplate БД нет, прямого аналога не
  существует; модуль оформляется под стиль KMMTemplate.
- Bottom-sheet навигация **сохраняется** — это потребность reside-track (модальные
  экраны AddRoom/ManageStudents); причёсывается под стиль KMMTemplate.
- Поведение приложения (экраны, флоу, бизнес-логика) не меняется.

## 5. Целевая структура модулей

```
:android:app                       ← было composeApp (тонкое Android-приложение)
:shared:main                       ← новый (App() + ResideTrackNavHost + Koin.kt)

:shared:common
:shared:common-ui                  ← было design-system (тема, компоненты, без строк)
:shared:common-resources           ← новый (moko-resources: строки, иконки)
:shared:core-domain                ← новый (Json, exceptions)
:shared:core-mvikotlin             ← новый (BaseExecutor, StoreFactory DI)
:shared:core-presentation          ← новый (BaseViewModel<State, Label>)
:shared:core-navigation
:shared:core-database              ← Room, оставляем

:shared:feature-rooms:{api,impl,presentation,ui}
:shared:feature-add-room:{api,impl,presentation,ui}
:shared:feature-manage-students:{api,impl,presentation,ui}
```

Дополнительно:
- `module-templates/` в корне (FreeMarker-шаблоны генерации модулей) заменяет
  модуль `:shared:template-module`;
- `iosApp/` остаётся, переключается на `:shared:main`;
- старая корневая папка `core/` (если пустая/устаревшая) удаляется.

## 6. build-logic

- `buildLogic` → `build-logic` (нейминг KMMTemplate).
- Convention plugins по образцу KMMTemplate:
  - `kmp-library` — базовый KMP-модуль;
  - `android-library` — Android-only модуль;
  - `compose-multiplatform-setup` — модуль с Compose;
  - `kmp-feature-setup` — feature-модуль: авто-разводка зависимостей по типу модуля
    (api/impl/presentation/ui), Compose-compiler для api/presentation;
  - `json-serialization` — модуль с сериализацией.
- `gradle/libs.versions.toml` переписывается по образцу KMMTemplate: версии бампаются
  (Kotlin 2.3.10, AGP 9.0.0, Compose 1.10.1, Koin 4.1.1 и т.д.) + новые библиотеки:
  MVIKotlin, moko-resources, moko-mvvm.

## 7. Core-модули

| Модуль | Содержимое |
|---|---|
| `core-mvikotlin` (новый) | `BaseExecutor`, `coreMVIKotlinModule` (LoggingStoreFactory + Napier) |
| `core-presentation` (новый) | `BaseViewModel<State, Label>` (биндинг Store, `OneTimeEvent`, iOS-обёртки через moko-mvvm). Текущий `common/BaseViewModel` удаляется |
| `core-domain` (новый) | `Json`, exceptions — минимально |
| `common` | `Mapper`, `CoroutineDispatchers`, `coRunCatching`, extensions — привести к виду KMMTemplate |
| `common-ui` | бывший `design-system`: тема + компоненты, без строк |
| `common-resources` (новый) | moko-resources: строки + иконки |
| `core-database` | Room — оставляем, оформляем под стиль (нейминг, convention plugin, DI) |

## 8. MVI: самописный → MVIKotlin

Соответствие сущностей:

| reside-track сейчас | → KMMTemplate |
|---|---|
| `XxxEvent` (через `obtainEvent`) | `Store.Intent` |
| `XxxAction` (side-effects) | `Store.Label` |
| `XxxViewState` | `Store.State` (api) + `UiXxxState` (presentation) |
| `handleXxx()` в ViewModel | `XxxExecutor` (impl/domain) |
| прямое присваивание state | `Message` (внутри `StoreFactory`) + `XxxReducer` |
| `BaseViewModel<S,E,A>` | `XxxViewModel : BaseViewModel<UiXxxState, UiXxxLabel>` + мапперы State/Label |

Поток: UI вызывает `viewModel.onXxx()` → `store.accept(Intent)` → `Executor` →
`dispatch(Message)` → `Reducer` обновляет `State` / `publish(Label)` → мапперы →
`UiState`/`UiLabel` → UI.

## 9. 4-модульный split фичи

На примере `feature-rooms` (остальные аналогично):

- **api** — `RoomsStore` (`Intent`/`State`/`Label`) + публичные доменные модели
  (`Room`, `Student`, интерфейс `RoomsRepository`).
- **impl** — `domain/` (`RoomsExecutor`, `RoomsReducer`, `RoomsStoreFactory` + `Message`),
  `data/` (`RoomsRepositoryImpl`, мапперы domain↔db, привязка к Room DAO),
  `di/FeatureRoomsImplModule.kt`.
- **presentation** — `RoomsViewModel`, `models/` (`UiRoomsState`, `UiRoomsLabel`),
  `mappers/` (State/Label мапперы), `di/FeatureRoomsPresentationModule.kt`.
- **ui** — `RoomsScreen`, `views/`, `api/FeatureRoomsScreenApi.kt`
  (`composableRoomsScreen` / `navigateToRoomsScreen`).

Зависимости: `ui → presentation → api`, `impl → api`. DI: модуль `impl` (Store) +
модуль `presentation` (ViewModel + мапперы).

Особенность `feature-manage-students`: ViewModel создаётся с параметром
`ManageStudentsMode` (ExistingRoom/DraftRoom) — параметризованная фабрика Koin
сохраняется.

## 10. Ресурсы

Compose Resources → moko-resources. Строки фич и `design-system` переносятся в
`common-resources/src/commonMain/moko-resources/base/strings.xml`, использование через
`MR.strings.*`. Костыль `StringProvider` в `feature-manage-students` удаляется — moko
`StringDesc` работает вне Compose (ровно та задача, ради которой `StringProvider`
вводился).

## 11. Навигация

- `Screen` (sealed interface с вложенными route) → marker-интерфейс `Screen` +
  top-level `@Serializable` route-объекты/классы в `core-navigation`.
- Screen API (`composableXxxScreen` / `navigateToXxxScreen`) переезжает в `ui/api/`
  каждой фичи.
- Bottom-sheet-инфраструктура сохраняется в `core-navigation`, причёсывается под стиль.
- Добавляются `navigateOnResumed` / `popBackStackOnResumed` из KMMTemplate.

## 12. App-модуль

`composeApp` разделяется на:
- `:android:app` — Android-приложение **по полному образцу KMMTemplate `android:app`**:
  product flavors `dev`/`prod`, версионирование `AppVersion`, `buildConfig = true`,
  подпись через keystore, переименование APK/AAB, классы `Activity` / `Application` /
  `di/AppModule`;
- `:shared:main` — `App()` composable, `ResideTrackNavHost`, агрегация всех
  common/core/feature-модулей через reflection-хелперы `build.gradle.kts` (как в
  KMMTemplate `shared:main`), `Koin.kt` с `initKoin`, сборка iOS-фреймворка.

`iosApp/` переключается на потребление `:shared:main`.

## 13. Пакеты

`dev.nonoxy.*` → `dev.nonoxy.residetrack.*` финальным механическим шагом. Примеры:
`dev.nonoxy.feature.rooms.*` → `dev.nonoxy.residetrack.feature.rooms.*`;
`dev.nonoxy.core.design.*` → `dev.nonoxy.residetrack.common.ui.*`;
`dev.nonoxy.common.*` → `dev.nonoxy.residetrack.common.*`.

## 14. Фазы исполнения

Каждая фаза завершается контрольной точкой: проект собирается, detekt проходит.

**Статус (2026-05-28):** Фазы 1–6 завершены; следующая — Фаза 7 (App-модуль: `composeApp` → `:android:app` + `:shared:main`).

1. **build-logic + версии** ✅ — `build-logic`, convention plugins, `libs.versions.toml`,
   бамп версий, новые библиотеки. Сюда же вынужденно вошёл структурный split
   `composeApp` → `:android:app` + `:shared:main` (AGP 9 запрещает совмещённый
   KMP + application модуль).
2. **Core-модули + доводка app-слоя** ✅:
   - core-модули: `core-mvikotlin`, `core-presentation`, `core-domain`, `common`,
     `common-ui`, `common-resources`, `core-database` под стиль;
   - **`:android:app` до полной парности с KMMTemplate `android:app`**: product flavors
     `dev`/`prod` (`flavorDimensions "environment"`), версионирование через `AppVersion`
     (скопировать `utils/AppVersion.kt` и `valueSource/GitCommitCountValueSource.kt` в
     build-logic), `buildFeatures { buildConfig = true }`, подпись через
     `keystores/debug.keystore.jks` + property `DEBUG_STORE_PASSWORD`/`DEBUG_KEY_ALIAS`,
     `applicationIdSuffix`/`versionNameSuffix` для debug, переименование APK/AAB через
     `androidComponents { onVariants }`, классы `Application` + `di/AppModule`.
     В Фазе 1 `:android:app` создан намеренно тонким (без flavors/AppVersion/keystore) —
     здесь доводится до эталона;
   - **`:shared:main` до парности с KMMTemplate**: агрегация common/core/feature-модулей
     через reflection-хелперы в `build.gradle.kts` вместо явного списка зависимостей
     (в Фазе 1 оставлен явный список);
   - **Koin до паттерна KMMTemplate**: `initKoin(appDeclaration)` в
     `:shared:main/di/Koin.kt` + класс `Application` (Android) вместо
     `KoinMultiplatformApplication` внутри `App()` (в Фазе 1 оставлен старый паттерн).
3. **Навигация** ✅ — `Screen` marker-интерфейс + top-level route-объекты,
   `core-navigation` доведён до парности с KMMTemplate.
4. **Пилот** ✅ — `feature-rooms` → 4 модуля + MVIKotlin.
5. **Остальные фичи** ✅ — `feature-add-room`, `feature-manage-students` → 4 модуля + MVIKotlin. `BaseViewModel`/`StringProvider` удалены.
6. **Ресурсы** ✅ — Compose Resources → moko-resources. Все strings/plurals/images/fonts централизованы в `:shared:common-resources` под `moko-resources/`. `StringProvider` удалён ещё в Фазе 5.
7. **App-модуль** — `composeApp` → `:android:app` + `:shared:main`.
8. **Пакеты** — переименование `dev.nonoxy.*` → `dev.nonoxy.residetrack.*`.

## 15. Риски

- **Бамп AGP 8→9 и Kotlin 2.1→2.3** — отдельная поверхность поломок; версии бампаются
  по ходу, поломки ловятся на контрольных точках сборки между фазами.
- **Большой объём в одной спеке** — митигируется фазовым исполнением и проверкой
  сборки после каждой фазы.
- **Миграция ресурсов на moko-resources** — затрагивает все строки; проверяется
  отображение текста после фазы 6.
- **Переименование пакетов** — механическое, но затрагивает все файлы; делается
  последним шагом, отдельной фазой.

## 16. Верификация

- После каждой фазы: проект собирается (Android-таск сборки) и detekt проходит.
- После миграции каждой фичи: поведение экрана сверяется вручную (флоу, навигация,
  валидация полей).
- Эталон сверяется напрямую с файлами KMMTemplate — при сомнении смотреть, как
  сделано там.
