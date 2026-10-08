# 04 — Match the visual and interaction system

**Depends on:** 03  
**Outcome:** Existing and new screens consistently match Android themes and components.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Ported the six source `AppTheme` palettes into the shared domain and Material color scheme. `KBBITheme` accepts a theme and defaults to Royal Ocean; settings persistence remains in task 13.
2. Kept the source typography and bundled font families, and aligned primary controls/status feedback to Material color roles. Added reusable `AppPrimaryButton` and applied it to word detail copy and proverb retry actions.
3. Matched the Android source color roles exactly: source `onPrimary` and `onSecondary` are white for all six palettes. Keep any accessibility improvements as a separate approved design change rather than silently changing source appearance.
4. Routed existing primary/secondary accents in home, word list, bookmarks, splash, detail, and proverb screens through `MaterialTheme.colorScheme`.
5. Added a centered 840 dp maximum shared content width for wide windows and hand cursor feedback on the shared primary button. Existing status/navigation bar and IME insets and text-field keyboard actions were retained.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- [x] All five targets build after the shared theme and responsive-width changes. Command: `./gradlew --no-daemon :androidApp:assembleDebug :shared:linkDebugFrameworkIosSimulatorArm64 :desktopApp:compileKotlin :webApp:jsBrowserDistribution :webApp:wasmJsBrowserDistribution --console=plain`; result: **BUILD SUCCESSFUL**, 148 tasks, 37 executed.
- [x] You manually compared home, detail, words, bookmarks, and proverb appearance with the source app on devices and confirmed the review passed on 2026-10-08.
- [x] You manually reviewed narrow/wide windows, safe areas, IME, keyboard traversal, touch targets, and contrast on the relevant targets and confirmed the review passed on 2026-10-08.

The JS and Wasm webpack builds retain the baseline critical-dependency and bundle-size warnings. Device and simulator acceptance is manual-only under the repository-wide plan policy.
