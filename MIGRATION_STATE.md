# KBBI KMP Migration State

Last verified: 2026-06-09

Source project: `/Users/macintosh/personal/Android/samples/kbbi`

## Current Status

The Android application production code has been migrated to shared Kotlin and
Compose Multiplatform code for Android and iOS. The iOS application compiles,
links, and builds through Xcode.

The migration is functionally complete, but it is not a literal 100% migration
of every source-project file.

## iOS Fixes

- Aligned Kotlin `2.3.20`, Compose Multiplatform `1.11.1`, KSP `2.3.4`,
  Room `2.8.4`, SQLite `2.6.2`, Koin `4.2.0`, and lifecycle
  `2.11.0-beta01`.
- Removed dependency substitutions that replaced the native Compose runtime and
  broke `androidx.compose.runtime` resolution on iOS.
- Initialized Koin before the iOS Compose controller renders.
- Kept Room database creation platform-specific and used the generated
  `WordDatabaseConstructor`.

## Verified

- `:shared:compileKotlinIosSimulatorArm64`
- `:shared:linkDebugFrameworkIosSimulatorArm64`
- `:shared:allTests`
- `:androidApp:assembleDebug`
- Xcode Debug build for a generic iOS Simulator destination

## Migration Coverage

Migrated:

- All production Kotlin files from the Android project
- Home, detail, bookmarks, word list, and splash flows
- Navigation, Koin DI, Ktor networking, Room persistence, and logging
- Dictionary catalog, fonts, shared strings, icons, and screen images
- Android and iOS platform implementations

Not migrated:

- The original substantive unit tests:
  - `NetworkLogFormatterTest`
  - `WordMappersTest`
  - `FakeWordRepository`
  - `WordUseCasesTest`
- The original Android Room instrumentation test
- Five Lottie/raw animation files (`empty.json`, `loading.json`,
  `loading_search.json`, `reading.json`, and `swipeblue.json`). The migrated UI
  does not reference them.

The current KMP tests are only smoke tests. Porting the original tests to
`commonTest` is the main remaining migration work.

## Known Warnings

- Material Icons Extended is deprecated and pinned to Compose `1.7.3`.
- `LocalClipboardManager` is deprecated in favor of `LocalClipboard`.
- The Xcode simulator link reports that bundled SQLite's ICU object was built
  for iOS Simulator `18.5` while the app deployment target is `18.2`. The build
  succeeds, but deployment-target compatibility should be checked before
  supporting simulator runtimes older than `18.5`.
