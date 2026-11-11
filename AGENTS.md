# Project development guide

These instructions apply throughout this repository. Follow the existing implementation when extending a feature; build scripts and source code are authoritative if older documentation disagrees.

## Architecture

KBBI is an Indonesian dictionary built with Kotlin Multiplatform and Compose Multiplatform for Android, iOS, desktop JVM, JavaScript, and WebAssembly. Share UI and application behavior in `shared`; keep application hosts focused on startup, lifecycle, OS integrations, and packaging.

- Organize shared code by feature, with `data`, `domain`, and `presentation` layers where applicable. Smaller platform-oriented features may keep a flatter layout; do not create empty layers just for symmetry.
- Put business models, repository interfaces, and use cases in `domain`. Keep them independent of concrete network, persistence, and native implementations.
- Put repository implementations, remote/local data sources, DTOs, entities, and mappers in `data`. Map transport and storage models at these boundaries.
- Put Compose screens, UI models, state, actions, events, and ViewModels in `presentation`. Follow the existing state/action/event pattern: expose read-only state, update immutable state through the ViewModel, and handle one-time effects through the existing event flow.
- Use the existing `AppResult`, `DataError`, `UiText`, and `CapabilityResult` contracts rather than introducing parallel error systems.
- Use Koin constructor injection. Feature data modules register implementations; `di/AppModule.kt` assembles shared, feature, network, database, observability, platform, use-case, and ViewModel modules through `initKoin`.
- Use Ktor for networking, Kotlinx Serialization for payloads, Room with bundled SQLite on Android/iOS/JVM, and browser storage adapters on web.
- Extend the existing Navigation 3 destinations, navigator, and incoming-launch handling. Keep external-entry parsing and shared navigation behavior consistent across hosts.
- Keep `widgetCore` small and independent of Compose UI and the main application runtime. It contains serializable widget snapshot contracts used across native widget integrations.

## Folder structure

```text
androidApp/                     Android host, manifest, resources, widgets, notifications
desktopApp/                     Desktop JVM host, launch handling, distribution assets
iosApp/
  iosApp/                       Swift/SwiftUI host and native coordinators
  WidgetExtension/              WidgetKit extension
  ShareExtension/               Native share extension
  Configuration/                Xcode configuration, including shared version inputs
  iosApp.xcodeproj/              Xcode targets and build settings
webApp/                         JS/Wasm browser host and web assets
widgetCore/src/
  commonMain/                   Shared widget snapshot models and serialization
  commonTest/                   Widget contract tests
shared/
  src/
    commonMain/
      kotlin/com/arrazyfathan/kbbi/
        core/                   Shared infrastructure, contracts, design system, utilities
        di/                     Application dependency assembly
        navigation/             Shared destinations, navigation, incoming actions
        feature/                Feature code grouped by responsibility
      composeResources/         Localized strings, images, fonts, bundled files
    sqliteMain/                 Room database, entities, converters, DAO adapters
    androidMain/                Android actuals and native adapters
    iosMain/                    iOS actuals and Kotlin/Native adapters
    jvmMain/                    Desktop actuals and JVM adapters
    webMain/                    Browser adapters shared by JS and Wasm
    commonTest/                 Shared behavior and contract tests
    jvmTest/                    JVM-specific tests, including persistence coverage
    jsTest/                     Browser-specific tests
    iosTest/                    iOS-specific tests
  schemas/                      Configured Room schema output (created on export)
gradle/libs.versions.toml        Central dependency/plugin versions and SDK levels
version.properties              Application release version and build number
config/detekt/detekt.yml         Static-analysis rules
.editorconfig                   Formatting rules
.github/workflows/ci.yml         Build and delivery checks
docs/release.md                  Signing, release configuration, artifact instructions
MIGRATION_STATE.md              Migration coverage and known gaps
plan/                           Feature plans and acceptance criteria
```

Features currently include home/search, words, details, bookmarks, proverbs, figures, settings, AI word study, reminders, campaigns, widgets, and app updates. Place additions beside the feature they extend; reserve `core` for code shared across features.

## Versioning and build configuration

- `version.properties` is the application version source of truth. `VERSION_MAJOR.VERSION_MINOR.VERSION_PATCH` supplies Android `versionName`, shared `BuildKonfig.APP_VERSION`, desktop package version, and iOS `MARKETING_VERSION`. `VERSION_CODE` supplies Android `versionCode` and iOS `CURRENT_PROJECT_VERSION`.
- Increase `VERSION_CODE` for every Android production release. Keep iOS build numbers valid for the release being uploaded. Do not bump application versions for routine changes unless preparing a release or explicitly requested.
- For the next release, breaking API/UI changes increment major, `feat` changes increment minor, and otherwise increment patch. Reset lower components when increasing major/minor. The highest-impact change in the release determines the bump.
- Manage dependency and plugin versions through `gradle/libs.versions.toml`; use catalog aliases in build scripts. Manage the Gradle distribution through the checked-in wrapper. Do not duplicate versions in feature code.
- Distinguish runtime/toolchain settings from bytecode targets: the checked-in daemon criteria select Amazon JDK 21, while Android compilation targets JVM 17 and CI currently provisions JDK 17. Inspect these files before changing Java requirements.
- Preserve Android application ID `com.arrazyfathan.kbbi.kmp` and existing platform bundle identities unless an identity migration is explicitly part of the task.
- Configure `BASE_URL` in ignored `local.properties`, starting from `local.properties.example`. It is required during Gradle configuration. `-PappEnvironment=production` selects the shared production environment value.
- After dependency changes, refresh and commit the bundled license catalog with `./gradlew --no-daemon :shared:exportLibraryDefinitions`.
- Keep signing material, provider credentials, Firebase configuration, and machine-specific paths out of commits. Use the release inputs documented in `docs/release.md`.

## Native implementations by platform

Define portable interfaces and data contracts in `commonMain`. Use constructor-injected implementations for native services and the existing `expect`/`actual` boundaries for platform factories, modules, and utilities. Do not import Android, UIKit, AWT, or browser APIs into common code.

| Source set / host | Responsibilities |
| --- | --- |
| `androidMain` / `androidApp` | Android context and lifecycle, permissions, speech, clipboard, icons, update installation, Room builder; host integrations include WorkManager reminders, FCM, Glance widgets, intents, and application startup. Ktor uses OkHttp. |
| `iosMain` / `iosApp` | Kotlin/Native adapters, UIKit integration, permissions, speech, preferences, Room builder; Swift handles the native app, notification coordination, shortcuts, WidgetKit, and sharing extensions. Ktor uses Darwin. |
| `jvmMain` / `desktopApp` | AWT/desktop services, preferences, launch requests, Room builder, desktop window startup, native packaging. Ktor uses CIO. |
| `webMain` / `webApp` | Browser permissions and capabilities, clipboard, speech, browser navigation, storage, JS/Wasm startup. Ktor uses the JS engine. Room is unavailable; current bindings use `LocalStorageWordDao` and `BrowserProverbDao`. |
| `sqliteMain` | Room implementation shared by Android, iOS, and desktop; platform source sets construct the database. |

- The source-set graph is `commonMain → sqliteMain → androidMain/iosMain/jvmMain` and `commonMain → webMain → jsMain/wasmJsMain`. iOS device and simulator targets depend on `iosMain`.
- When changing an `expect` declaration, update every applicable `actual`. When adding a service contract, supply platform bindings and a deliberate unsupported implementation where necessary.
- Represent permission denial, unavailability, and failures through the existing capability result types. Keep the UI functional when a platform does not support a service.
- Bind native services in the existing `platformModule`/`databaseModule` or host initialization path. Keep OS lifecycle ownership and cleanup explicit; release microphone sessions, observers, and callbacks when cancelled or disposed.
- Keep widget snapshot serialization, deep links, request identities, and acknowledgement behavior compatible across shared code and native extensions.
- Preserve persisted data when changing schemas or storage keys. Update Room versions, migrations, and exported schemas as needed; verify browser storage compatibility separately.
- Verify both JS and Wasm compatibility for changes in `webMain`; browser-specific code must not assume JVM APIs.

## Code conventions

- Use package root `com.arrazyfathan.kbbi` and mirror the package structure in folders. Keep feature code within its feature and follow neighboring files' organization.
- Use `PascalCase` for types and Compose UI functions, `camelCase` for ordinary functions/properties, and `UPPER_SNAKE_CASE` for constants. Preserve existing serialized values and public identifiers when renaming.
- Prefer descriptive responsibility suffixes such as `ViewModel`, `Screen`, `Route`, `UseCase`, `Repository`, `RemoteDataSource`, `Dto`, and `Entity`. Follow existing platform suffixes such as `.android.kt`, `.ios.kt`, `.jvm.kt`, and `.web.kt` where used; not every platform file uses a suffix.
- Prefer immutable models and read-only public APIs. Keep mutable flows private and expose read-only state. Use state updates that preserve concurrent changes.
- Keep composables focused on rendering and dispatching actions. Reuse the shared theme, components, and resources; keep user-facing strings in `composeResources` and update supported translations together.
- Use lifecycle-owned coroutine scopes such as `viewModelScope`. Keep blocking native work off the UI thread, follow existing dispatcher injection, and rethrow `CancellationException` before mapping failures.
- Keep DTO/entity mapping and native exceptions out of UI code. Reuse shared error-to-UI mapping and logging/observability contracts. Do not log credentials or sensitive user content.
- Add meaningful regression coverage for behavior changes using the existing Kotlin test, coroutine-test, and Ktor MockEngine infrastructure. Put portable tests in `commonTest` and platform-specific behavior in the relevant target tests.
- Make focused changes. Avoid unrelated rewrites, blanket suppressions, generated-file edits, and bulk reformatting outside the task.

## Code formatting and verification

`.editorconfig` defines UTF-8, LF line endings, a final newline, and four-space indentation. Kotlin declaration/call-site trailing commas are enabled. Ktlint permits PascalCase `@Composable` functions and disables its standard filename rule. Detekt has additional naming and complexity rules; consult `config/detekt/detekt.yml` rather than relying on tool defaults.

```sh
# Apply Kotlin formatting, then check formatting and static analysis
./gradlew ktlintFormat
./gradlew ktlintCheck detekt

# Shared JVM and widget contract tests
./gradlew :shared:jvmTest :widgetCore:jvmTest

# Full shared multiplatform test aggregate, when target tooling is available
./gradlew :shared:allTests

# Android tests and compilation
./gradlew :androidApp:testDebugUnitTest :androidApp:assembleDebug

# iOS simulator framework compilation
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64

# Both browser production targets
./gradlew :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution

# Desktop compilation
./gradlew :desktopApp:classes

# Whitespace verification
git diff --check
```

Run checks appropriate to the changed code and report failures or unavailable tooling accurately. Documentation-only changes need content/link and whitespace checks, not application builds. For Swift, follow surrounding Xcode formatting and the repository's four-space indentation; Ktlint does not format Swift. Native iOS host/extension changes also require the relevant Xcode build, not only Kotlin framework compilation. Review `.github/workflows/ci.yml` and `docs/release.md` for delivery checks and host-specific desktop packaging.

## Conventional Commit Messages

The following rules adapt the supplied [Conventional Commit Messages cheatsheet by Bengt Brodersen](https://gist.github.com/qoomon/5dfcdf8eec66a051ecd85625518cfd13), under the MIT license reproduced below. They follow the [Conventional Commits specification](https://www.conventionalcommits.org/).

### Format

```text
<type>(<optional scope>)<optional !>: <description>

<optional body>

<optional footer>
```

```sh
git commit -m "<type>(<optional scope>): <description>" \
    -m "<optional body>" \
    -m "<optional footer>"
```

Separate subject, body, and footer with blank lines. Initial commit: `chore: init`. Preserve default Git merge subjects (`Merge branch '<branch name>'`) and revert subjects (`Revert "<reverted commit subject line>"`).

### Types

| Type | Use for |
| --- | --- |
| `feat` | Add, adjust, or remove API/UI features. |
| `fix` | Fix an API/UI bug introduced by a preceding feature. |
| `refactor` | Rewrite or restructure code without changing API/UI behavior. |
| `perf` | A refactor specifically improving performance. |
| `style` | Whitespace, formatting, or other code-style changes without behavior changes. |
| `test` | Add missing tests or correct existing tests. |
| `docs` | Documentation-only changes. |
| `build` | Build tools, dependencies, project versions, or other build components. |
| `ops` | Infrastructure, deployment, CI/CD, backups, monitoring, or recovery. |
| `chore` | Maintenance such as initialization or `.gitignore` changes. |

### Scope, description, and body

- Scope is optional and provides context. Prefer the affected module, platform, or feature: `shared`, `android`, `ios`, `desktop`, `web`, `widgets`, `home`, `bookmarks`, `wordstudy`, or `release`. These are examples, not an enforced allowlist. Do not use issue identifiers as scopes.
- Description is mandatory. Use imperative present tense: `add`, `change`, or `fix`; not `added` or `changes`. Think “this commit will …”. Start with a lowercase letter and do not end with a period.
- Body is optional. Explain the motivation and how behavior changes from the previous implementation. Use imperative present tense for change instructions.
- Footer is optional except for breaking changes. Issue references belong here, for example `Closes #123` or `Fixes JIRA-456`.

### Breaking changes

A breaking change must include `!` immediately before the subject colon, for example `feat(api)!: remove status endpoint`. Include a footer beginning with `BREAKING CHANGE:` and explain the impact and required migration. For one line, put a space after the marker; for a multiline explanation, put two newlines after it.

```text
feat(storage)!: replace bookmark export format

Use versioned snapshots for bookmark exports

BREAKING CHANGE: migrate existing exports before importing them with this version
```

Apply the major/minor/patch release rules in the versioning section above.

### Examples

```text
feat: add email notifications on new direct messages
feat(home): add voice search
fix(bookmarks): prevent duplicate saved words
fix(api): correct request body checksum calculation
perf: decrease memory footprint when loading the word catalog
build: update dependencies
build(release): bump version to 1.3.0
refactor: extract word suggestion matching
style: remove empty line
test(web): cover unavailable browser storage
docs: add project development instructions
ops(ci): verify browser production bundles
chore: update gitignore
```

### Optional hook enforcement

[git-conventional-commits](https://github.com/qoomon/git-conventional-commits) can validate messages through a local `commit-msg` hook and help determine versions/changelogs. A server-side `pre-receive` hook can reject invalid incoming commits on a server you control; it does not enforce local commits merely by being placed in this checkout.

Hooks are not installed by this document. Any validator added later must accept every type listed above, optional scopes, the `!` breaking-change marker, and the default merge/revert formats. The abbreviated regex in the supplied cheatsheet omits some of those valid cases and should not be copied unchanged.

### Cheatsheet license

MIT License

Copyright (c) [2026] [Bengt brodersen]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Project to-do lists (Herdr)

<!-- over9k:todo -->
Each project has a to-do list the user sees in Herdr's To-do panel and edits there
too. Use the `todo` command (it picks the project from the git repo you run it in):
`todo` lists it, `todo add <text>` adds, `todo done <n>` ticks, `todo rm <n>` deletes,
`todo all` shows every project's. While request logging is on for that project
(`todo log` says; in Claude Code a note on each message says so), add each thing
the user asks for in a project (a change, a fix, a feature), even mid-task, as soon
as you read it, and tick it once it's done and checked; leave it open if it's only
partly done, and say so. When they change, narrow or drop a request, correct its
item instead of adding another: `todo edit <n> <new text>` rewords it, `todo rm <n>`
removes it. When they say to stop or start logging, run `todo log off` / `todo log on`
(that project only). Also add an item when the user asks you to note something for
later, or when you leave follow-up work undone. Keep items short, one line each.
<!-- /over9k:todo -->
