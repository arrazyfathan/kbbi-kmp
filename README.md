# KBBI Multiplatform

KBBI Multiplatform is a Kotlin Multiplatform dictionary application for
Android and iOS. Its user interface and application logic are shared with
Compose Multiplatform, while platform-specific integrations remain in their
respective Android and iOS source sets.

## Migration

This project is the Kotlin Multiplatform migration of the original Android
KBBI application:

- Original repository: [arrazyfathan/kbbi](https://github.com/arrazyfathan/kbbi)
- Multiplatform targets: Android and iOS

The Android production features have been migrated into shared Kotlin and
Compose Multiplatform code. See [MIGRATION_STATE.md](./MIGRATION_STATE.md) for
the verified migration coverage, known warnings, and remaining test work.

## Features

- Search for Indonesian dictionary entries
- View word meanings and details
- Browse the bundled word catalog
- Save and remove bookmarked words
- Persist and manage search history
- Shared navigation and user interface on Android and iOS

## Technology

- Kotlin Multiplatform
- Compose Multiplatform and Material 3
- Navigation 3
- Koin dependency injection
- Ktor networking
- Room and bundled SQLite persistence
- Kotlinx Serialization and Coroutines

## Project Structure

- [`androidApp`](./androidApp) contains the Android application entry point
  and Android-specific configuration.
- [`iosApp`](./iosApp) contains the Xcode project and iOS application entry
  point.
- [`shared/src/commonMain`](./shared/src/commonMain) contains shared UI,
  navigation, domain, data, and dependency-injection code.
- [`shared/src/androidMain`](./shared/src/androidMain) contains Android
  platform implementations.
- [`shared/src/iosMain`](./shared/src/iosMain) contains iOS platform
  implementations.

## Running the Apps

### Android

Open the project in Android Studio and run the `androidApp` configuration, or
build a debug APK from the command line:

```shell
./gradlew :androidApp:assembleDebug
```

### iOS

Open [`iosApp/iosApp.xcodeproj`](./iosApp/iosApp.xcodeproj) in Xcode, select an
iOS simulator, and run the `iosApp` scheme.

To build the shared simulator framework directly:

```shell
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

## Tests

Run the shared Android and iOS tests with:

```shell
./gradlew :shared:allTests
```

The current suite primarily provides migration smoke coverage. Porting the
original repository's substantive unit and Room instrumentation tests remains
planned work.
