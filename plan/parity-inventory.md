# Live Android-to-KMP parity inventory

Audit baseline: Android source `7183b268cd260bcfb78830b00ef680ab3aa3e1ff` and KMP target `65eee5ab0fd3ed721ab241010ee9b520b92a41a1`, inspected 2026-10-07. Both working trees were clean. Source: `/Users/macintosh/Personal/Android/Samples/kbbi`; target: this repository.

This is a code inventory, not a full runtime acceptance report. `present` means a corresponding implementation is visible in the inspected KMP tree; it does not assert behavioral parity. Task 02's build and core entry-path evidence is recorded below; feature-level parity still needs target-specific verification. Shared UI and logic are intended for each target, but platform-specific behavior still needs target-level confirmation.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Status key

- **Implementation:** `present`, `partial`, or `missing` in the inspected KMP revision.
- **Platform:** `unverified`, `failed`, `verified`, or `n/a`. `unverified` is the initial state; attach command/scenario, result, date, and revision when changed.
- Each row has one primary roadmap owner. Later tasks update this file and attach evidence before claiming completion.

## Feature and behavior inventory

Feature and core entry paths are package-relative to `com/arrazyfathan/kbbi` within the named module's `src/main/java` tree. `app/src/main/...` paths are relative to the Android app module.

| ID | Android source entry point / behavior | KMP counterpart and data dependencies | Implementation | Primary owner | Android | iOS | JVM desktop | Browser JS | Browser Wasm |
|---|---|---|---|---|---|---|---|---|---|
| F01 | `feature/home/presentation/home/HomeScreen.kt`, `HomeViewModel.kt`: search, recent history, suggestions, top words | `shared/.../feature/home`, word API, catalog, history DAO | partial | 08 | unverified | unverified | unverified | unverified | unverified |
| F02 | `feature/home/domain/usecase/*`, `feature/home/data/WordRepository.kt` and remote sources: lookup, meaning, translation, suggestions, top words | Shared word models/use cases/repository; Ktor and local DAO | partial | 06 | unverified | unverified | unverified | unverified | unverified |
| F03 | `feature/words/presentation/words/WordListScreen.kt`, `WordViewModel.kt`: searchable word catalog | `shared/.../feature/words`; bundled `entries.json` catalog | partial | 08 | unverified | unverified | unverified | unverified | unverified |
| F04 | `feature/detail/presentation/detail/DetailScreen.kt`, `DetailViewModel.kt`: word meanings, save state, share/copy | Shared detail screen and word repository; clipboard adapter exists per target | partial | 09 | unverified | unverified | unverified | unverified | unverified |
| F05 | `feature/bookmark/presentation/bookmark/BookmarksScreen.kt`, `BookmarksViewModel.kt`: observe, open, remove saved words | Shared bookmarks UI/use cases and platform DAO | partial | 09 | unverified | unverified | unverified | unverified | unverified |
| F06 | `feature/proverb/presentation/proverb/ProverbScreen.kt`, `ProverbViewModel.kt` and repository: paged proverbs and detail | Shared proverb screen/repository/cache and network | partial | 10 | unverified | unverified | unverified | unverified | unverified |
| F07 | `feature/splash/presentation/splash/SplashScreen.kt`: startup and initial destination | Shared splash route/screen and navigation state | partial | 10 | unverified | unverified | unverified | unverified | unverified |
| F08 | `feature/figure/presentation/figure/FigureScreen.kt`, `FigureDetailScreen.kt` and ViewModels: list, search, paging, detail | No figure feature found under target `shared/src` | missing | 11 | unverified | unverified | unverified | unverified | unverified |
| F09 | `feature/settings/presentation/settings/SettingsScreen.kt`, `SettingsViewModel.kt`: preferences, theme, reminders and app options | No settings feature found under target `shared/src`; target theme primitives exist | partial | 13 | unverified | unverified | unverified | unverified | unverified |
| F10 | `feature/wordstudy/domain/usecase/GenerateWordStudyUseCase.kt`, data remote sources: backend/custom-provider contracts, generation and errors | No AI word-study contracts or data source found in target | missing | 16 | unverified | unverified | unverified | unverified | unverified |
| F11 | `feature/wordstudy/data/AiConfigurationDataStore.kt`: provider configuration persistence and secret handling | No AI configuration storage found in target; roadmap default is native persistence and session-only browser keys | missing | 17 | unverified | unverified | unverified | unverified | unverified |
| F12 | `feature/wordstudy/presentation/ai/AiSettingsScreen.kt`, `AiSettingsViewModel.kt` and detail integration: AI settings and generated study UI | No AI settings or generation UI found in target | missing | 18 | unverified | unverified | unverified | unverified | unverified |
| F13 | `app/src/main/.../navigation`: app destinations, back stack and transitions | Shared Navigation3 destinations/state; platform entry points still need runtime review | partial | 14 | unverified | unverified | unverified | unverified | unverified |
| F14 | `app/src/main/.../intent` and shortcut request: incoming links, shares and shortcuts | No external intent/shortcut adapters found in target apps | missing | 21 | unverified | unverified | unverified | unverified | unverified |
| F15 | Search voice input and microphone permission flow in home presentation | No voice recognition implementation found in target | missing | 15 | unverified | unverified | unverified | unverified | unverified |
| F16 | `core/presentation/designsystem`: theme, typography, icons, components and haptics | Shared theme/type/icon/components; platform haptic parity not confirmed | partial | 04 | unverified | unverified | unverified | unverified | unverified |
| F17 | `app/src/main/res`, core design-system resources, localized strings and assets | Shared Compose resources include strings, fonts, images and animations; completeness not reconciled | partial | 03 | unverified | unverified | unverified | unverified | unverified |
| F18 | `feature/home/data` and `feature/proverb/data`: Room cache, bookmarks, history and migrations | Shared Room/SQLite DAO for native targets; in-memory/browser DAO implementation | partial | 07 | unverified | unverified | unverified | unverified | unverified |
| F19 | `app/src/main/.../notifications/DailyReminderWorker.kt`, `WorkManagerReminderScheduler.kt`: daily reminder scheduling, permission and delivery | No reminder scheduler or notification adapter found in target apps | missing | 20 | unverified | unverified | unverified | unverified | unverified |
| F20 | `app/src/main/.../notifications/EditorialMessagingService.kt`, topic workers: editorial campaigns, subscriptions and messaging | No campaign, push messaging, or in-app campaign path found in target apps | missing | 24 | unverified | unverified | unverified | unverified | unverified |
| F21 | `app/src/main/.../widgets`: word of day, saved word, quick search and refresh | No widget implementation found in target apps | missing | 22 | unverified | unverified | unverified | unverified | unverified |
| F22 | Android app icon manager and icon resources | Target Android has launcher icons; no cross-platform icon preference capability found | partial | 23 | unverified | unverified | unverified | unverified | unverified |
| F23 | `core/observability`: analytics, crash/performance reporting, privacy preferences | No observability/reporting implementation found under target `shared/src` or app modules | missing | 19 | unverified | unverified | unverified | unverified | unverified |
| F24 | `core/app-update`: release check, download, validation, install prompt | No app-update implementation found in target modules | missing | 25 | unverified | unverified | unverified | unverified | unverified |
| F25 | `app/src/main/AndroidManifest.xml`, app/core manifests, build flavors and DI | Target Android manifest, platform entry points and shared Koin exist; other packaging/configuration needs review | partial | 05 | unverified | unverified | unverified | unverified | unverified |
| F26 | `core/data`, feature data modules: API DTOs, repositories, errors, caching and network policy | Shared Ktor word/proverb sources and result/error types; figure and AI data absent | partial | 06 | unverified | unverified | unverified | unverified | unverified |
| F27 | `core/presentation` and feature screens: loading, errors, empty states and recovery | Shared loading/error UI and feature screens; failure-state behavior not yet reconciled | partial | 04 | unverified | unverified | unverified | unverified | unverified |
| F28 | Unit, instrumented, migration, UI and integration tests across source modules | Target has common smoke/unit tests, including `NetworkLogFormatterTest`; source word mapper/use-case fakes and Room migration/instrumentation coverage are not present under matching tests | partial | 26 | unverified | unverified | unverified | unverified | unverified |
| F29 | Gradle modules, CI workflows, Firebase/release configuration and delivery scripts | Target Gradle app modules and iOS project exist; CI and release parity not yet reconciled | partial | 02 | unverified | unverified | unverified | unverified | unverified |

## Platform acceptance scenarios

For every applicable feature row, record results for normal use, cold start, offline behavior, and relevant API/storage failures. For permission-gated behavior, test both grant and denial. For persisted state, test restart; for browser state, test reload and session boundaries. Use `n/a` only with a reason. Add a compact evidence note below or link a dated, revision-specific artifact; do not infer verification from source presence or compilation alone.

## Coverage reconciliation

The source repository contains production modules for `app`, `core` (app update, data, DI, domain, logging, observability, presentation design system/UI, utilities), and features `bookmark`, `detail`, `figure`, `home`, `proverb`, `settings`, `splash`, `words`, and `wordstudy`. Its app layer also owns external intents/navigation, notifications/workers/messaging, shortcuts, widgets, app startup, and delivery configuration. Resources, Android manifests, unit/instrumentation/UI/integration tests, and release artifacts/configuration are included in the rows above. The source inventory was restricted to source and configuration trees; generated `build/` outputs and bundled release binaries were not treated as production capabilities.

The target tree inspected contains shared home, words, detail, bookmarks, proverb, and splash code; shared navigation, design system, core network/error/logging, native SQLite persistence and a browser DAO; and Android/iOS/JVM/web entry points. Figure, settings, voice, AI, reminders, external entry adapters, widgets, campaigns, reporting, and app update have no corresponding target feature implementation at this revision and are owned by their roadmap tasks. Task 02 verifies the core startup/search/detail path on Android, iOS, JS, and Wasm, with the limitations recorded in the evidence log; feature-level parity remains to be checked by each owning task.

## Evidence log

| Date | Revision | Target | Scenario or command | Result |
|---|---|---|---|---|
| 2026-10-07 | source `7183b268`; target `65eee5a` | source and target trees | Reconciled source modules, app-owned capabilities, target source sets and roadmap owners | Static coverage and ownership checked |
| 2026-10-07 | target worktree after task 01 | Android | Gradle configuration, debug APK build, startup/Koin, search “makan”, detail and system Back | Build and core flow passed |
| 2026-10-07 | target worktree after task 01 | iOS Simulator | Gradle framework link, Xcode Debug build, startup/Koin, search “makan”, detail and Back | Builds and flow to detail passed; Back did not return from detail |
| 2026-10-07 | target worktree after task 01 | Desktop JVM | JVM compile and `:desktopApp:run` | Compile passed; process launched, but window interaction unavailable |
| 2026-10-07 | target worktree after task 01 | Browser JS | Production distribution and local HTTP startup/search/detail | Passed locally; HTTPS deployment checks remain open |
| 2026-10-07 | target worktree after task 01 | Browser Wasm | Production distribution and local HTTP startup/recent-search/detail | Passed locally; HTTPS deployment checks remain open |
| 2026-10-07 | target worktree after task 02 | Shared resources | Android APK, iOS simulator framework, desktop JVM, Browser JS, and Browser Wasm builds after adding Indonesian resources | All target builds passed; this is compile evidence, not manual device acceptance |
