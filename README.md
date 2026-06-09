# KBBI Multiplatform

KBBI Multiplatform is a Kotlin Multiplatform dictionary application for Android and iOS. Its user interface and application logic are shared with Compose Multiplatform, while platform-specific integrations remain in their respective Android and iOS source sets.

## Migration

This project is the Kotlin Multiplatform migration of the original Android KBBI application:
- Original repository: [arrazyfathan/kbbi](https://github.com/arrazyfathan/kbbi)
- Multiplatform targets: Android and iOS

The Android production features have been migrated into shared Kotlin and Compose Multiplatform code. See [MIGRATION_STATE.md](./MIGRATION_STATE.md) for the verified migration coverage, known warnings, and remaining test work.

---

## Features

- **Indonesian Dictionary Search**: Search for words in the KBBI database.
- **Word Details**: View word meanings, origins, and structural details.
- **Word Catalog**: Browse the bundled, offline-capable dictionary catalog.
- **Bookmarks**: Save and manage bookmarked words.
- **Search History**: Persist and view past search records.
- **Shared UI & Navigation**: Fully unified user interface and flow using Navigation 3.

---

## Technology Stack

- **Core**: [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) (2.4.0)
- **UI & Layout**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) (1.11.1) with Material 3
- **Navigation**: [Navigation 3](https://developer.android.com/jetpack/compose/navigation) for multiplatform routing
- **Dependency Injection**: [Koin](https://insert-koin.io/) (4.2.1)
- **Networking**: [Ktor](https://ktor.io/) (3.5.0) with OkHttp (Android) & Darwin (iOS) engines
- **Persistence**: [Room](https://developer.android.com/jetpack/androidx/releases/room) with bundled SQLite driver
- **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Code Style & Analysis**: [Ktlint](https://github.com/pinterest/ktlint) and [Detekt](https://github.com/detekt/detekt)
- **Environment Management**: [BuildKonfig](https://github.com/yshrsmz/BuildKonfig)

---

## Project Architecture & Data Flow

The codebase follows a Clean Architecture design, dividing components into shared and platform-specific implementations:

```
kbbi-kmp
├── androidApp/             # Android application wrapper (MainActivity, BaseApplication)
├── iosApp/                 # iOS native Xcode project (App entry point, UIViewController wrapper)
└── shared/                 # Shared logic & UI module
    └── src/
        ├── commonMain/     # Shared UI components, view models, domain logic, and Ktor APIs
        ├── androidMain/    # Android-specific database builders & system bar controllers
        └── iosMain/        # iOS-specific database builders & ViewController integration
```

### Shared Configurations & DI
- **Dependency Injection**: All dependency modules are declared in `commonMain` under Koin configuration, but the platform-specific dependencies (such as Ktor engines and Room database builders) are configured in `androidMain` and `iosMain` using Koin's `platformModule`.
- **Database (Room)**: The database builder is declared via platform-specific builders (`getDatabaseBuilder()`) and initialized with the KSP-generated `WordDatabaseConstructor`.

---

## Running the Applications

### Android

Open the project in Android Studio and run the `androidApp` configuration, or build a debug APK from the command line:

```shell
./gradlew :androidApp:assembleDebug
```

### iOS

Open [`iosApp/iosApp.xcodeproj`](./iosApp/iosApp.xcodeproj) in Xcode, select an iOS simulator target, and run the project.

To build the shared iOS framework from the command line:
```shell
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

---

## Testing

Run the shared Android, iOS, and common test suites with:

```shell
./gradlew :shared:allTests
```

The current suite primarily provides migration smoke coverage. Porting the original repository's substantive unit and Room instrumentation tests remains planned work.

---

## Code Style & Formatting

The project enforces code formatting and quality analysis using **Ktlint** and **Detekt**.

- **Check Code Quality**:
  ```shell
  ./gradlew ktlintCheck detekt
  ```
- **Auto-Format Code**:
  ```shell
  ./gradlew ktlintFormat
  ```

Linter rules are globally configured in [.editorconfig](./.editorconfig) and [config/detekt/detekt.yml](./config/detekt/detekt.yml).

---

## Environment Management (Dev/Prod)

Environment properties (such as the base API URL) are managed via **BuildKonfig** inside the shared module.

- Default configurations are declared directly in [shared/build.gradle.kts](./shared/build.gradle.kts).
- **Local Settings**: Specify local keys in `local.properties` (which is excluded from Git tracking).
- **CI/CD Pipelines**: Pass production keys via environment variables, which are read by the Gradle build script.
