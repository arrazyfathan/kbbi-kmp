# 26 — Restore delivery and verify parity

**Status:** In progress (2026-10-22). Cross-target CI build gates, release preparation documentation, and Android APK download/verification/install implementation are in place. Artifact publishing, web hosting and proxy configuration, CI matrix execution, and human source/device acceptance remain open.

**Depends on:** 01–25  
**Outcome:** The five runtime targets are buildable, tested, and ready for their respective release processes.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

**Android identity decision (2026-10-13):** The person chose `com.arrazyfathan.kbbi.kmp` as the Android application ID for an independent installation alongside the original app. Preserve this ID when porting source build metadata. Kotlin packages and the Android namespace remain `com.arrazyfathan.kbbi`; Firebase configuration must match the KMP application ID.

1. Port source development/production metadata, versioning, Android signing/distribution, shrink rules, and release notes without copying secrets.
2. Add iOS archive/export configuration, desktop DMG/MSI/DEB packaging, and web HTTPS hosting for assets, service worker, and stateless AI proxy.
3. Configure CI gates for common tests, Android debug build/tests, iOS simulator framework and Xcode build, JVM package/launch smoke, JS production bundle, and Wasm production bundle.
4. Port meaningful source unit tests into `commonTest` or platform test suites; add DI resolution, navigation, persistence, API contract, and OS-adapter tests.
5. Run a manual feature matrix on each target: startup, search, word/proverb/figure detail, bookmarks, settings, AI, offline/error, deep link, notification, and update.
6. Reconcile every task against the task 01 checklist; record support limits and external configuration requirements.
7. Review release artifacts and permissions; verify no signing keys, API keys, service account files, or personal local paths entered tracked files.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- [x] Add CI gates for shared/Android unit tests, Android debug and minified release builds, DMG/MSI/DEB desktop packaging on native runners, a virtual-display Linux desktop launch smoke, JS and Wasm production bundles, iOS Simulator framework, and unsigned Xcode Simulator app build (`.github/workflows/ci.yml`). Workflow execution is pending.
- [ ] All build and focused test gates pass, with device/browser smoke results attached to the checklist.
- Every Android source capability is marked implemented with evidence, mapped to a native equivalent, or documented with an in-app path where the OS lacks the capability.
- Android, iOS, desktop, JS, and Wasm launch from their packaged artifacts.

## Implementation progress — 2026-10-22

- Added a CI workflow that uses the committed public API endpoint example, runs shared JVM and Android unit tests, builds Android debug and minified unsigned production APKs, packages DMG/MSI/DEB on native runners, runs a Linux desktop launch smoke in Xvfb, produces JS/Wasm production bundles, and builds the iOS Simulator framework and unsigned Xcode app. CI rejects tracked Firebase configuration and common signing credential files. The workflow uploads review artifacts and does not publish them.
- Android release signing now reads all four keystore inputs from Gradle properties or environment variables, rejects partial configuration, enables R8/resource shrinking, and builds an unsigned review APK when signing material is absent. Target-specific ProGuard rules preserve crash line information without copying Android-only source rules.
- Added [release preparation guidance](../docs/release.md) for versioning, Android identity/signing inputs, desktop host-specific installers, iOS signing, HTTPS hosting requirements, the deferred same-origin AI proxy, and release review.
- Local Linux/browser build command `./gradlew --no-daemon :shared:jvmTest :androidApp:testDebugUnitTest :androidApp:assembleDebug :desktopApp:packageDeb :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution --console=plain` completed successfully. The Android unit-test task is `NO-SOURCE`; `packageDeb` is skipped on this macOS host and still needs its Ubuntu CI run. JS and Wasm production distributions completed, but JS webpack reported unresolved Node `os`/`path` module warnings and both bundles reported size warnings; browser runtime acceptance remains open.
- `./gradlew --no-daemon :shared:linkDebugFrameworkIosSimulatorArm64 --console=plain` and an unsigned generic iOS Simulator `xcodebuild ... build` both succeeded. The local Xcode retry needed access to compiler and SwiftPM caches outside the workspace. No Simulator launch was performed.
- After adding the Android update adapter, `./gradlew --no-daemon :androidApp:assembleDebug --console=plain` and the final `:androidApp:compileDebugKotlin` passed. The update-download receiver, installer activity, FileProvider paths, and `REQUEST_INSTALL_PACKAGES` declaration are included in the merged Android build.
- `./gradlew --no-daemon :androidApp:assembleRelease -PappEnvironment=production --console=plain` passed with R8 and resource shrinking enabled and no signing inputs, producing an unsigned production review APK.
- `./gradlew --no-daemon :desktopApp:packageDmg --console=plain` passed and produced `desktopApp/build/compose/binaries/main/dmg/KBBI-1.2.0.dmg`. Linux DEB and Windows MSI remain for native CI runners.
- `:androidApp:ktlintCheck` still fails on existing style violations in other Android app files; the check report contains no findings for the new updater files. The CI workflow does not claim a lint gate.
- The workflow YAML parsed and `git diff --check` passed. The GitHub Actions matrix has not run yet; DMG/MSI/DEB package and uploaded artifact paths remain to be confirmed by CI.
- Android APK delivery now uses DownloadManager and validates package identity, expected version, version-code increase, and signer before presenting the system installer. The human install and permission scenarios remain open.
- Production credentials, Apple signing/provisioning, Firebase configuration, a web hosting origin, and a proxy runtime are intentionally not supplied in this checkout. Release publishing and deployment remain unconfigured.
- The plan 01 feature matrix still contains partial implementations and unverified runtime rows. Source-comparison and device/browser acceptance must be completed by a person; no agent device or simulator interaction was performed.
