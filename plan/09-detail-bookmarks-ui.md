# 09 — Complete detail, sharing, and bookmarks UI

**Depends on:** 03, 04, 06, 07, 08  
**Outcome:** Word detail and saved-word management match Android interactions.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Port translation selection/provider labels, visitor count, AI/source badges, meanings, and translation loading/error states.
2. [x] Share common copy/share formatting; invoke OS sharing through task 05 adapters.
3. [x] Port the persistent bookmark grid/list preference, list swipe-to-delete, grid long-press delete, confirmation, and empty view.
4. [x] Save/delete updates the observed bookmark state immediately; carry AI-generated metadata into saved words.
5. [x] Unbookmarking retains cached word meanings for offline lookup.

## Verification

- [x] Person manually compared the detail and bookmarks screens against the Android source and confirmed they match, including navigating to Bookmarks after fixing the shared list-view vector resource crash (2026-10-09). Any further device or simulator interaction must be done manually by a person.

- [x] Person manually checked translated and original share/copy payloads and the native share sheet on supported targets; confirmed all are okay (2026-10-09).
- [x] Person manually checked saving, deleting, layout persistence, and offline opening after unbookmarking; confirmed all are okay (2026-10-09).
- [x] Android APK, iOS simulator framework, desktop JVM, Browser JS, and Browser Wasm compile tasks pass.
