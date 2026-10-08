# 22 — Add widgets and glanceable surfaces

**Depends on:** 07, 14, 20, 21  
**Outcome:** Word of day, saved word, and quick search are accessible outside or at the front of the app.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Share deterministic daily-item selection and small widget snapshot models without exposing Room entities to extensions.
2. Android: port Glance word-of-day, saved-word, quick-search widgets and refresh workers/receivers.
3. iOS: add WidgetKit counterparts using App Group snapshots and deep links; refresh after bookmark changes and day rollover.
4. Desktop/web: place the three actions in a discoverable app entry area or PWA shortcuts when OS widgets are unavailable.
5. Handle no bookmarks, offline word lookup, removed saved words, and stale widget taps.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Widget/surface content matches current local data and refreshes at next local day and after bookmark edits.
- Each tap opens search or the intended word; empty states never link to an invalid detail.
