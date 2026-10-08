# 02 — Establish the five-target build baseline

**Depends on:** 01  
**Outcome:** Reproducible build/run commands and a known-failure baseline for every registered target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Toolchain and configuration

Baseline observed on 2026-10-07:

| Component | Version or setting |
|---|---|
| JDK | 17.0.15 |
| Gradle wrapper | 9.8.0 |
| Kotlin | 2.4.20 |
| Compose Multiplatform | 1.12.1 |
| Android compile/target/min SDK | 37 / 37 / 24 |
| Android Gradle Plugin | 9.4.1 |
| Xcode | 27.0 (27A266a) |
| iOS Simulator runtime | 27.0 |
| Node.js | 26.10.0 |

The shared Gradle module requires `BASE_URL` in the repository-root `local.properties`. The value is generated into shared code by BuildKonfig. The current build does not read `BASE_URL` from the process environment and does not have a default. Start from the committed example, then provide a reachable API URL:

```shell
cp local.properties.example local.properties
# Edit local.properties and set BASE_URL to your development or staging API.
```

`local.properties` is ignored by Git. Keep credentials out of this file's committed example and out of command output. CI must provision the property before Gradle configuration. The example currently points to the project's public API endpoint; change it if that endpoint is not appropriate for your environment.

## Build commands and results

Gradle configuration succeeded:

```shell
./gradlew --no-daemon help --console=plain
```

All five targets compiled/built successfully in one Gradle invocation:

```shell
./gradlew --no-daemon \
  :androidApp:assembleDebug \
  :shared:linkDebugFrameworkIosSimulatorArm64 \
  :desktopApp:compileKotlin \
  :webApp:jsBrowserDistribution \
  :webApp:wasmJsBrowserDistribution \
  --console=plain
```

Result: **BUILD SUCCESSFUL** (148 tasks; 32 executed). The first web build attempts found stale generated Yarn lock files in the ignored `kotlin-js-store/` cache. These were refreshed with `./gradlew --no-daemon kotlinUpgradeYarnLock` and `./gradlew --no-daemon kotlinWasmUpgradeYarnLock`; the combined build then passed. No generated lock file is tracked.

The iOS host application also built successfully:

```shell
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp -configuration Debug \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath /tmp/kbbi-kmp-derived \
  CODE_SIGNING_ALLOWED=NO build
```

Result: **BUILD SUCCEEDED**. The Xcode build emitted an AppIntents metadata warning because the app does not link the AppIntents framework.

## Run and smoke baseline

| Target | Run/build command | Observed result |
|---|---|---|
| Android | `./gradlew :androidApp:assembleDebug`; install `androidApp/build/outputs/apk/debug/androidApp-debug.apk` on an API 36 emulator and open `com.arrazyfathan.kbbi` | Pass: app started and Koin initialized; searched “makan”, opened definitions, and Android system Back returned to the search/home UI. |
| iOS | Build with the Xcode command above; `xcrun simctl install booted /tmp/kbbi-kmp-derived/Build/Products/Debug-iphonesimulator/kbbi-kmp.app`; `xcrun simctl launch booted com.arrazyfathan.kbbi` | Pass: startup, home, search for “makan”, and definition detail. Back navigation did not return from the detail screen: no back control was exposed and simulator system Back did not change the screen. Record as an existing baseline issue for navigation work. |
| Desktop JVM | `./gradlew :desktopApp:run` | Gradle run task completed and the Java app process remained running. The native window was not accessible to the available UI automation, so visible startup/search/navigation are unverified. |
| Browser JS | `./gradlew :webApp:jsBrowserDistribution`; serve `webApp/build/dist` locally and open `/js/productionExecutable/index.html` | Pass over local HTTP: home loaded, “makan” search returned a detail with definitions. HTTPS deployment behavior, routing and service-worker behavior were not tested. |
| Browser Wasm | `./gradlew :webApp:wasmJsBrowserDistribution`; serve `webApp/build/dist` locally and open `/wasmJs/productionExecutable/index.html` | Pass over local HTTP: home loaded, recent search opened “makan” detail with definitions. HTTPS deployment behavior, routing and service-worker behavior were not tested. |

For repeatable local web smoke checks:

```shell
python3 -m http.server 8765 --bind 127.0.0.1 --directory webApp/build/dist
```

Then open `http://127.0.0.1:8765/js/productionExecutable/index.html` or `http://127.0.0.1:8765/wasmJs/productionExecutable/index.html`. This is a local development check, not an HTTPS release check.


Historical note: the initial task 02 device smoke entries were collected by an agent before the manual-only device-testing policy was added. A person must repeat and record those checks before treating them as manually verified acceptance evidence.

## Known baseline limitations

- iOS detail navigation did not expose or respond to a Back action in the tested flow.
- Desktop JVM process launch succeeded, but visual interaction with its window was not available in the automation environment.
- Browser builds and smoke checks succeeded locally; deployed HTTPS, browser storage/session boundaries, routing on reload, and service worker behavior remain unverified.
- The production JS bundle (8.06 MiB), Wasm-related assets (5.78 and 8.24 MiB), Gradle deprecation/configuration warnings, and JS critical-dependency warning were reported during the successful build. They did not fail compilation.

## Verification

- [x] Gradle configuration and all five target build commands recorded.
- [x] Android and iOS launch/search/detail flows exercised; results and the iOS navigation failure recorded.
- [x] Browser JS and Wasm local production bundles launched and exercised.
- [ ] Desktop window interaction and deployed HTTPS browser smoke remain unverified.
