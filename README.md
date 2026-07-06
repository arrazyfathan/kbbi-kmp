<div align="center">

# KBBI Multiplatform

An Indonesian dictionary application built with Kotlin Multiplatform and Compose Multiplatform.

**Android · iOS · Web · Desktop**

[Features](#features) · [Architecture](#architecture) · [Running](#running-the-project) · [Development](#development)

</div>

---

## Overview

KBBI Multiplatform shares its user interface, navigation, domain logic, networking, and data layer across four application platforms. Platform-specific source sets provide native integrations where required.

| Platform | Target | Application module | Local persistence |
|---|---|---|---|
| Android | Android JVM | `androidApp` | Room and bundled SQLite |
| iOS | Kotlin/Native | `iosApp` | Room and bundled SQLite |
| Web | JavaScript and WebAssembly | `webApp` | In-memory storage |
| Desktop | JVM | `desktopApp` | Room and bundled SQLite |

This project is a Kotlin Multiplatform migration of the original [Android KBBI application](https://github.com/arrazyfathan/kbbi). See [MIGRATION_STATE.md](./MIGRATION_STATE.md) for migration coverage, known warnings, and remaining test work.

## Features

| Feature | Description |
|---|---|
| Dictionary search | Search for Indonesian words using the KBBI API |
| Word details | View meanings, origins, and structural information |
| Word catalog | Browse the bundled offline-capable word catalog |
| Bookmarks | Save and manage selected words |
| Search history | Review recently searched words |
| Shared navigation | Use one Navigation 3 flow across all platforms |
| Shared interface | Render a unified Material 3 interface with Compose Multiplatform |

## Technology

| Area | Technology |
|---|---|
| Language and core | [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) 2.4.0 |
| User interface | [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) 1.11.1 and Material 3 |
| Navigation | [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) |
| Dependency injection | [Koin](https://insert-koin.io/) 4.2.1 |
| Networking | [Ktor](https://ktor.io/) 3.5.0 |
| Persistence | [Room](https://developer.android.com/jetpack/androidx/releases/room) and bundled SQLite |
| Serialization | [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) |
| Configuration | [BuildKonfig](https://github.com/yshrsmz/BuildKonfig) |
| Code quality | [Ktlint](https://github.com/pinterest/ktlint) and [Detekt](https://github.com/detekt/detekt) |

### Platform Integrations

| Platform | Ktor engine | Platform implementation |
|---|---|---|
| Android | OkHttp | Android context, system bars, toast, clipboard, and Room builder |
| iOS | Darwin | UIKit integration, clipboard, and Room builder |
| Web | JavaScript | Browser runtime, clipboard, and in-memory data access |
| Desktop | CIO | AWT clipboard and JVM Room builder |

## Architecture

The project follows a shared, feature-oriented architecture. Most production code lives in `shared`, while application modules provide platform entry points and packaging.

```text
kbbi-kmp/
├── androidApp/             Android entry point and application configuration
├── desktopApp/             Desktop JVM entry point and native distributions
├── iosApp/                 Native Xcode project and iOS entry point
├── webApp/                 JavaScript and WebAssembly browser entry points
└── shared/
    └── src/
        ├── commonMain/     Shared UI, navigation, domain, data, and networking
        ├── androidMain/    Android integrations
        ├── iosMain/        iOS integrations
        ├── jvmMain/        Desktop integrations
        ├── sqliteMain/     Room implementation shared by native platforms
        └── webMain/        JavaScript and WebAssembly integrations
```

### Shared Boundaries

- `commonMain` contains the shared application and feature code.
- `sqliteMain` contains Room entities, converters, database definitions, and DAO adapters.
- Android, iOS, and desktop provide platform-specific database builders.
- Web uses the same repository contracts with an in-memory DAO because Room does not target JavaScript or WebAssembly.
- Koin assembles shared modules with the appropriate platform networking and persistence implementations.

## Running the Project

### Requirements

- Android Studio or IntelliJ IDEA with Kotlin Multiplatform support
- Xcode for the iOS application
- A compatible JDK for Gradle, Android, and desktop builds
- Node.js tooling managed by the Kotlin Gradle plugin for web builds

### Command Reference

| Platform | Action | Command |
|---|---|---|
| Android | Build debug APK | `./gradlew :androidApp:assembleDebug` |
| iOS | Build simulator framework | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` |
| Desktop | Run application | `./gradlew :desktopApp:run` |
| Desktop | Create native distribution | `./gradlew :desktopApp:createDistributable` |
| Web JS | Run development server | `./gradlew :webApp:jsBrowserDevelopmentRun` |
| Web JS | Build production distribution | `./gradlew :webApp:jsBrowserDistribution` |
| Web Wasm | Run development server | `./gradlew :webApp:wasmJsBrowserDevelopmentRun` |
| Web Wasm | Build production distribution | `./gradlew :webApp:wasmJsBrowserDistribution` |

### IDE Launching

**Android**

Open the project in Android Studio and run the `androidApp` configuration.

**iOS**

Open [`iosApp/iosApp.xcodeproj`](./iosApp/iosApp.xcodeproj) in Xcode, select an iOS simulator, and run the project.

## Development

### Tests

Run the shared multiplatform test suites:

```shell
./gradlew :shared:allTests
```

The current suite primarily provides migration smoke coverage. Porting the original repository's substantive unit and Room instrumentation tests remains planned work.

### Code Quality

Check formatting and static analysis:

```shell
./gradlew ktlintCheck detekt
```

Format Kotlin sources:

```shell
./gradlew ktlintFormat
```

Rules are configured in [.editorconfig](./.editorconfig) and [config/detekt/detekt.yml](./config/detekt/detekt.yml).

### Environment Configuration

Build-time configuration, including the API base URL, is managed through the ignored `local.properties` file and generated into shared code by BuildKonfig in [shared/build.gradle.kts](./shared/build.gradle.kts).

Create your local configuration from the committed example:

```shell
cp local.properties.example local.properties
```

Then edit `local.properties`:

```properties
BASE_URL=https://kbbi-api-green.vercel.app/
```

`local.properties` is ignored by Git, so developer-specific URLs stay out of the committed codebase. CI can also provide the same value with a `BASE_URL` environment variable. If neither value is set, the build falls back to the production API URL.

Example values:

| Environment | `BASE_URL` |
|---|---|
| Development | `http://localhost:3000/` or your staging API URL |
| Production | `https://kbbi-api-green.vercel.app/` |

Rebuild commands after changing `BASE_URL`:

| Platform | Command |
|---|---|
| Android | `./gradlew :androidApp:assembleDebug` |
| iOS | `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` or run the Xcode project |
| Desktop | `./gradlew :desktopApp:run` |
| Web JS | `./gradlew :webApp:jsBrowserDevelopmentRun` |
| Web Wasm | `./gradlew :webApp:wasmJsBrowserDevelopmentRun` |

Do not commit real secrets to `local.properties.example`. The base API URL is still visible in built apps and network traffic, so privileged API keys or private credentials must stay on a backend service.
