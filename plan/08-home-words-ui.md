# 08 — Complete home and word-list UI

**Depends on:** 03, 04, 06, 07  
**Outcome:** Home and word list expose every source-app discovery path.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port top-word cards, suggestion modes, random word, search states, and word-list filtering.
2. Drive UI from immutable ViewModel state/action/event contracts; preserve query and focus across route/window changes.
3. Wire top words, suggestions, keyboard submit, pointer/touch selection, and random word to detail routes.
4. Cancel stale requests so old results cannot replace a newer search.
5. Add loading, empty, offline, and retry states with localized copy.

## Verification

- Typed search, suggestions, top words, random word, and word-list selection open the intended detail.
- Device/browser smoke covers no result, offline, retry, and back on each target.
