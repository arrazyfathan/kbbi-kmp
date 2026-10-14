# 20 — Add scheduled reminders

**Depends on:** 05, 07, 13, 14  
**Outcome:** Daily word, daily proverb, and bookmark review reminders have a working path on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port reminder models, time validation, deterministic daily selection, bookmark review selection, and scheduling state.
2. Android: implement WorkManager workers, notification channels, permission checks, and unique work reconciliation.
3. iOS: schedule local notifications with `UNUserNotificationCenter`; update/cancel requests when settings change.
4. Desktop: use an OS notification/scheduling adapter where reliable; provide an in-app due reminder on next launch otherwise.
5. Web: use HTTPS service-worker notifications where permission/support exist; provide an in-app due reminder otherwise.
6. Parse taps into typed word/proverb/bookmark routes and handle missing content gracefully.

## Current implementation status — 2026-10-14

- Shared deterministic selection, localized reminder content, reminder scheduling contract, and cached-proverb lookup are implemented. Browser proverb pages now persist for subsequent reminder selection.
- Android uses WorkManager one-shot rescheduling, notification channels, Android 13+ permission requests, and typed notification links.
- iOS schedules up to 14 days of localized local notifications and forwards notification taps into shared navigation.
- Desktop posts reminders through a system tray when one is available. Browser JS/Wasm posts browser notifications while running and permission is granted.
- User confirmed that the remaining delivery paths and person-led acceptance checks are complete, including persistent fallback coverage, desktop/browser tap routing and service-worker behavior, iOS schedule refresh, Android reboot/time-zone reconciliation, and source/device acceptance.

Plan 20 is **complete** based on the user's confirmation of the remaining verification gates, in addition to the recorded builds and shared reminder tests.

## Verification

- [x] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Person confirmed completion; any device or simulator interaction was performed manually.
- [x] Shared tests cover stable same-day word selection, all three reminder content types, empty-content handling, localized bookmark counts, and reminder-time validation.
- [x] Verify adapter behavior for local-day rollover, time-zone change, permission denial, reschedule, and cancel. Person confirmed completion.
- [x] Verify notification tap or in-app card opens the intended route on each target. Person confirmed completion.
