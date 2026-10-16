# 23 — Add app icon choices and branding

**Depends on:** 03, 05, 13  
**Outcome:** The seven source icon choices stay aligned with the icon actually shown by each platform.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.

## Implementation

- [x] Keep the seven stable identifiers (`default`, `royal_ocean`, `golden_sunset`, `golden_coral_energy`, `deep_forest_energy`, `neon_violet`, `blaze_orange`) shared between settings and platform adapters. Confirm the choice before applying it, save only after success, report errors, and read back the OS selection after a rejected native request. Theme selection remains independent.
- [x] Android: register seven launcher aliases against the existing main activity, keep shortcut metadata on each alias, use package-ID-correct component names, and switch atomically on API 33+ with rollback and verified state on older versions.
- [x] iOS: package six named alternate app icon sets in the host app's asset catalog, declare them through the Debug/Release asset-catalog settings, and use UIKit's alternate-icon selector while confirming the actual selected name before saving. Extensions receive no alternate-icon settings.
- [x] Desktop: set default DMG/MSI/DEB package icons and update the running Compose window icon from the saved selection. Package branding remains the shipped default.
- [x] Browser JS/Wasm: ship all seven favicon choices, update the active tab icon and persist the preference, add default and dedicated maskable PWA assets, and version the service-worker cache. A pinned/installed PWA retains its packaged manifest icon.
- [x] Localize platform-specific descriptions and confirmation/error feedback in English and Indonesian; expose the applied selection in the existing accessible radio picker.

## Verification

- [x] Compare the Settings picker, confirmation, copy, assets, and scenarios directly with the Android source at equivalent viewport sizes. Any device or simulator interaction must be performed manually by a person. The user confirmed all Plan 23 verification scenarios passed on 2026-10-16.
- [x] Confirm all seven identifiers have a localized label and preview, and that the settings store updates only after the adapter reports success.
- [x] Build the Android debug APK, Desktop distributable, JS and Wasm production distributions, iOS device and Simulator Kotlin targets, and the Xcode iOS Simulator host. Inspect the generated host Info.plist for all six alternate icon names.
- [x] On Android, change all seven icons, confirm exactly one launcher alias remains enabled, and verify shortcuts, deep links, app relaunch, and launcher refresh. The user confirmed all Plan 23 verification scenarios passed on 2026-10-16.
- [x] On iPhone and iPad, exercise all seven choices, accept and reject the iOS prompt, reopen the app, and verify the selected icon and extensions. The user confirmed all Plan 23 verification scenarios passed on 2026-10-16.
- [x] On Desktop, change icons and reopen the app; in JS and Wasm, change tab icons and reload. Confirm the packaged Desktop/PWA identity stays fixed and matches its explanation. The user confirmed all Plan 23 verification scenarios passed on 2026-10-16.

## Implementation progress — 2026-10-16

The shared picker now applies through platform adapters instead of treating its saved preference as proof that the OS changed the icon. Android launcher aliases retain the existing shortcut resource. iOS alternate assets are configured only on the host target. Desktop's live window and browser tab use the saved choice, while their packaged app/PWA identity stays fixed. The user confirmed all manual source comparison and runtime acceptance scenarios passed on Android, iPhone/iPad, Desktop, browser JS, and browser Wasm on 2026-10-16; device and simulator operation was performed by the user.

Automated build evidence: `:androidApp:assembleDebug`, `:desktopApp:createDistributable`, `:webApp:jsBrowserDistribution`, `:webApp:wasmJsBrowserDistribution`, `:shared:compileKotlinIosSimulatorArm64`, and `:shared:compileKotlinIosArm64` passed. `xcodebuild` also succeeded; `actool` compiled all six alternate sets and the generated host `Info.plist` contains the six matching `CFBundleAlternateIcons` entries for iPhone and iPad. The macOS Desktop bundle contains `KBBI.icns`. Both browser distributions contain all seven favicons and the maskable PWA asset. Browser bundles retain existing webpack notices for optional Node `os`/`path` modules and large bundle sizes. On 2026-10-16 the user confirmed the manual source comparison and runtime acceptance scenarios passed on Android, iPhone/iPad, Desktop, browser JS, and browser Wasm. No agent device or Simulator operation was performed.
