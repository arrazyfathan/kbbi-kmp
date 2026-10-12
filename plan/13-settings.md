# 13 — Add settings and preferences

**Depends on:** 03, 04, 05, 07  
**Outcome:** Shared Settings reproduces source controls and persisted state.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Added shared UI preferences, app language/theme, haptics, bookmark layout, app-icon variants, notification toggles and reminder time/type contracts.
2. [x] Added persistent settings adapters for Android, iOS, desktop JVM, Browser JS and Browser Wasm. Theme applies from app startup; haptics are gated centrally; language preferences are applied through platform locale hooks. Changing language recreates shared resource lookups immediately, with navigation and splash state held outside the language-dependent composition (including iOS).
3. [x] Added Settings sections for appearance, interaction/language, diagnostics, notifications with unavailable states, data/history, and About. Clear-history confirmation calls the history-only use case; version, reporting toggles, reminder times and external links are wired.
4. [x] Added Privacy Policy and Terms screens using source copy and the source AboutLibraries license screen. The generated catalogue is bundled as a shared Compose resource for offline access on all targets, including iOS; it includes generated shared dependency entries with license metadata.
5. [x] Preserved source defaults: crash reporting on, analytics/performance off, campaign notifications off, reminders off.
6. [x] No notification permission is requested automatically. Reminder controls show their unavailable state until adapters are added by Plan 20; app-icon changes remain unavailable until Plan 23.

## Verification

- [x] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- [x] Person checks theme, language and bookmark layout persistence after restart/reload on supported targets; specifically confirm English → Indonesian → English changes immediately while remaining in Settings, then navigate to Home and legal screens and confirm the chosen language survives restart on each target.
- [x] Person checks that clearing history preserves bookmarks, notification-unavailable states are accurate, and English/Indonesian text is correct on each supported target.
- [x] Replace the static licenses disclosure with a generated dependency catalogue equivalent to the source AboutLibraries page.
- [x] Person checks Report a Bug opens the browser and Open Source Licenses displays its catalogue offline, expands library rows, and shows license details on supported targets.
- [x] Android, iOS Simulator framework, desktop JVM, Browser JS and Browser Wasm compilation passed.

## Catalogue maintenance

Regenerate the bundled catalogue after changing dependencies with `./gradlew --no-daemon :shared:exportLibraryDefinitions --console=plain` and include `shared/src/commonMain/composeResources/files/aboutlibraries.json` with the change.

The iOS external-link adapter uses UIKit’s options/completion-handler API; Settings waits for its result before displaying a failure message. Compilation passed on all five targets; the person confirmed browser launching and catalogue interaction passed during manual review on 2026-10-12.

## Manual acceptance

On 2026-10-12, the person confirmed all Plan 13 verification items passed: source UI/behavior comparison, preference persistence, immediate language switching and restart persistence, history clearing without bookmark loss, notification-unavailable states, localized copy, Report a Bug browser launching, and the offline expandable license catalogue. Device and simulator checks were performed manually by the person. Native reminder delivery and app-icon switching remain owned by Plans 20 and 23.
