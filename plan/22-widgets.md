# 22 — Add widgets and glanceable surfaces

**Depends on:** 07, 14, 20, 21  
**Outcome:** Word of day, saved word, and quick search are accessible outside or at the front of the app.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Share deterministic daily-item selection and small widget snapshot models without exposing Room entities to extensions.
2. Android: port Glance word-of-day, saved-word, quick-search widgets and refresh workers/receivers.
3. iOS: add WidgetKit counterparts using App Group snapshots and deep links; refresh after bookmark changes and day rollover.
4. Desktop/web: place the three actions in a discoverable app entry area or PWA shortcuts when OS widgets are unavailable.
5. Handle no bookmarks, offline word lookup, removed saved words, and stale widget taps.

## Verification

- Widget/surface content matches current local data and refreshes at next local day and after bookmark edits.
- Each tap opens search or the intended word; empty states never link to an invalid detail.
