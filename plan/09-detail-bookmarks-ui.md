# 09 — Complete detail, sharing, and bookmarks UI

**Depends on:** 03, 04, 06, 07, 08  
**Outcome:** Word detail and saved-word management match Android interactions.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port translation selection/provider labels, visitor count, AI/source badges, meanings, and all loading/error states.
2. Share common copy/share formatting; invoke OS sharing through task 05 adapters.
3. Port bookmark grid/list preference, swipe where appropriate, delete confirmation, and empty view.
4. Ensure save/delete updates detail, list, and future widgets immediately.
5. Preserve offline opening and cached meanings after unbookmarking.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Translated and original share/copy payloads are correct on all targets.
- Saving, deleting, switching layout, and offline opening behave consistently.
