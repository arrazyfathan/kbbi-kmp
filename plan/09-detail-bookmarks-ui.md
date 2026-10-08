# 09 — Complete detail, sharing, and bookmarks UI

**Depends on:** 03, 04, 06, 07, 08  
**Outcome:** Word detail and saved-word management match Android interactions.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port translation selection/provider labels, visitor count, AI/source badges, meanings, and all loading/error states.
2. Share common copy/share formatting; invoke OS sharing through task 05 adapters.
3. Port bookmark grid/list preference, swipe where appropriate, delete confirmation, and empty view.
4. Ensure save/delete updates detail, list, and future widgets immediately.
5. Preserve offline opening and cached meanings after unbookmarking.

## Verification

- Translated and original share/copy payloads are correct on all targets.
- Saving, deleting, switching layout, and offline opening behave consistently.
