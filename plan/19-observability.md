# 19 — Add observability and privacy gates

**Depends on:** 05, 06, 13, 14, 16  
**Outcome:** Analytics, crash, and network reporting mirror Android policy without leaking dictionary or AI content.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Ported source typed `AnalyticsEvent` names/enum parameters, all screen names, crash operations, reporting preferences, reporter and coordinator contracts.
2. [x] Wired available search, suggestion/top/random/voice input, word-list results, content navigation, bookmark mutations, translation, proverb results, app sharing and AI study results at action/result boundaries. Navigation screen reporting is outside locale recreation and deduplicates repeated route observations. Future reminder/widget/update/external producers remain with their owning plans.
3. [x] Retained crash preference on by default and analytics/performance off. Reporters read current preferences on every call; preference setters synchronize native collection immediately. Performance additionally requires the production environment and a release build.
4. [x] Added Android Firebase SDK and Swift Firebase adapters, with explicit unavailable fallback when this project's Firebase configuration is absent. Desktop/browser reporters are explicit no-ops. Settings preserves source row dimensions, icons and controls, and shows localized unavailable/production-only descriptions and disabled semantics where applicable.
5. [x] Per the person's 2026-10-13 request, restored full local development network logging with Ktor `LogLevel.ALL`: original URLs, headers, request bodies and response bodies are printed without endpoint filters, redaction or formatter sanitization. The existing development/Debug console gate remains. Raw network output goes directly to the local console. Remote performance URLs remain sanitized; crash keys use enum/boolean/version allowlists and nonfatal reports carry operation and exception type only.
6. [x] Ktor request hooks close metrics after success, HTTP failure, transport failure and cancellation, including custom-provider requests. Trace stop is idempotent; response metadata is bounded to approved MIME types and nonnegative payload sizes. Cancellation, transport outages/timeouts and expected validation errors are not recorded as crashes.

## Native configuration

SDK adapters are compiled into the apps, but no source-project Firebase configuration is copied. Native Settings shows reporting unavailable until a matching target configuration is bundled. Desktop and browser intentionally have no reporting backend.

- **Android:** register `com.arrazyfathan.kbbi.kmp` in Firebase and inject its matching `androidApp/google-services.json` (ignored by Git). The person chose this separate application ID on 2026-10-13 so the KMP and original Android apps can be installed together. The Google Services and Crashlytics Gradle plugins apply only when that file exists. Use `-PappEnvironment=production` for production metadata; performance still requires a release build.
- **iOS:** inject this project's `iosApp/iosApp/GoogleService-Info.plist` (ignored by Git). Xcode links Firebase Analytics, Crashlytics and Performance via the pinned Firebase Apple 13.0.1 Swift package. Set `KBBI_ENVIRONMENT=production` for a production release; development/Debug remains ineligible for performance. Automatic screen and raw URL instrumentation are disabled; the shared typed events/manual metrics provide reporting.
- **Delivery:** Plan 26 owns CI configuration injection and Crashlytics mapping/dSYM delivery. Live collection and symbolication must be checked by a person with the target Firebase project.

## Verification

- [x] Person compares Settings diagnostics rows, spacing/icons/type, switch feedback, saved preferences and English/Indonesian capability text directly with the Android source at equivalent viewport sizes.
- [x] With target Firebase configuration, person enables analytics, opens each available route and exercises search/bookmark/translation/proverb/AI/app-share actions; checks source event names/parameters and one event per action/result or screen change, including locale change and back navigation.
- [x] Person switches analytics, crash and performance preferences off while the app is running and confirms collection stops without restart; checks saved choices after restart, crash defaults on and analytics/performance default off.
- [x] Person checks performance unavailable in development/Debug and eligible only in a configured production release; checks completed, failed and cancelled requests produce sanitized metrics.
- [x] Person checks full request/response details appear in the local development console for dictionary, translation and AI requests; checks dictionary terms, definitions/translations, visitor IDs, API keys and provider editor content remain absent from remote diagnostic payloads; verifies native unavailable and desktop/browser no-op states without configured reporting.
- [x] Focused fake reporter/MockEngine tests cover exact names/parameters, defaults/opt-out/capability and production gates, repeated screen observations, URL and diagnostic sanitization, trace closure/idempotence and result-boundary events. **28 focused JVM tests passed on 2026-10-13**.
- [x] Android APK, iOS framework and Xcode host app, Desktop JVM, Browser JS and Wasm compile; iOS/JS/Wasm test sources compile without running device or Simulator tests. All builds passed on 2026-10-13.

Build/test log: `/tmp/kbbi-plan19-final-build.log`. Xcode host build log: `/tmp/kbbi-plan19-xcode.log`. The person confirmed all five manual observability verification checks passed on 2026-10-13, including source UI comparison, live events, preference opt-out/persistence, performance gates and local/remote logging behavior. These results are person-reported; an agent build or unit test does not establish device acceptance.

Native SDK policy references: [Analytics collection](https://firebase.google.com/docs/analytics/ios/configure-data-collection), [Crashlytics collection and unsent reports](https://firebase.google.com/docs/crashlytics/android/customize-crash-reports), and [Performance instrumentation controls](https://firebase.google.com/docs/perf-mon/disable-sdk).

## Xcode package resolution follow-up (2026-10-13)

After the person reported missing Firebase package products in a normal IDE build, Firebase was resolved into Xcode's default package cache and `-ObjC` was added to both target configurations. A standard generic Simulator Xcode build then passed, with the person's iOS configuration plist packaged and its bundle ID matching the app. This verifies compilation and resource packaging; live SDK behavior remains a manual person check.

For Xcode package resolution, the command from the repository root is:

```bash
xcodebuild -resolvePackageDependencies -project iosApp/iosApp.xcodeproj -scheme iosApp
```

Resolution log: `/tmp/kbbi-firebase-default-resolution.log`. Default-cache build log: `/tmp/kbbi-firebase-default-build.log`. The Kotlin framework script intentionally runs every build; Gradle handles incremental work.

### Android Studio package-service recovery

The Android Studio build was reproduced separately: its DerivedData lives under `~/Library/Caches/Google/AndroidStudio2026.2.1/DerivedData/`, rather than Xcode's default directory. Resolving all 13 dependencies and compiling against that cache succeeded, but the running IDE still failed. Its `.xcactivitylog` reported `Communication with the service was interrupted` for all package repositories, followed by the three missing Firebase products. This matches the package-service failure reported in [JetBrains KMT-2926](https://youtrack.jetbrains.com/issue/KMT-2926).

After resolving the IDE cache, closing and reopening the kbbi-kmp project reset the native build service. Android Studio's **Build 'iosApp'** then completed successfully for iPhone 18 Pro Max / iOS 27.0 Simulator / Debug on 2026-10-13. The app was compiled without launching a Simulator app; person runtime checks were subsequently confirmed complete on 2026-10-13.

Recovery if this service failure recurs: close and reopen the project before rebuilding. If package downloads are missing, resolve using the IDE's actual DerivedData directory, then reopen the project. A successful command-line build alone does not verify Android Studio's native pipeline.

IDE-cache resolution/build logs: `/tmp/kbbi-firebase-androidstudio-resolution.log`, `/tmp/kbbi-firebase-androidstudio-build.log`. Successful IDE host build: `10FC5174-E799-4768-B924-C7D76FC0A5CF.xcactivitylog` in the IDE DerivedData `Logs/Build` directory.

## Local network logging rollback (2026-10-13)

Restored unfiltered Ktor request/response logging at the person's request. `HttpClientFactory` installs `Logging` at `LogLevel.ALL`; `NetworkLogFormatter` retains the person's custom timestamp, request/response sections, headers, pretty JSON bodies and cURL output; `AppLogger.network(message)` prints that formatted output directly to the local development console. Values are not redacted. Removed duplicate metadata-only console logging from the performance hook. No tests or device checks were run for this rollback.
