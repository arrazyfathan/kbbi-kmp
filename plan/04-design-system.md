# 04 — Match the visual and interaction system

**Depends on:** 03  
**Outcome:** Existing and new screens consistently match Android themes and components.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Ported the six source `AppTheme` palettes into the shared domain and Material color scheme. `KBBITheme` accepts a theme and defaults to Royal Ocean; settings persistence remains in task 13.
2. Kept the source typography and bundled font families, and aligned primary controls/status feedback to Material color roles. Added reusable `AppPrimaryButton` and applied it to word detail copy and proverb retry actions.
3. Added contrasting `onPrimary`/`onSecondary` colors for each palette. Light gold/orange/coral secondary accents use dark foregrounds where white fails text contrast.
4. Routed existing primary/secondary accents in home, word list, bookmarks, splash, detail, and proverb screens through `MaterialTheme.colorScheme`.
5. Added a centered 840 dp maximum shared content width for wide windows and hand cursor feedback on the shared primary button. Existing status/navigation bar and IME insets and text-field keyboard actions were retained.

## Verification

- [x] All five targets build after the shared theme and responsive-width changes. Command: `./gradlew --no-daemon :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 :desktopApp:compileKotlin :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution --console=plain`; result: **BUILD SUCCESSFUL**, 148 tasks, 37 executed.
- [x] You manually compared home, detail, words, bookmarks, and proverb appearance with the source app on devices and confirmed the review passed on 2026-10-08.
- [x] You manually reviewed narrow/wide windows, safe areas, IME, keyboard traversal, touch targets, and contrast on the relevant targets and confirmed the review passed on 2026-10-08.

The JS and Wasm webpack builds retain the baseline critical-dependency and bundle-size warnings. Device and simulator acceptance is manual-only under the repository-wide plan policy.
