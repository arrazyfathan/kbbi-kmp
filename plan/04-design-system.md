# 04 — Match the visual and interaction system

**Depends on:** 03  
**Outcome:** Existing and new screens consistently match Android themes and components.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port source `AppTheme` palettes and associated typography to the shared Compose theme.
2. Compare buttons, fields, cards, top bars, bottom tabs, sheets, dialogs, loading, errors, and transitions with source screens.
3. Add reusable shared components; connect selected theme and haptics through settings later in task 13.
4. Adapt safe areas, IME, pointer hover, focus, desktop window widths, and browser resize.
5. Check accessibility semantics, contrast, touch targets, and keyboard traversal.

## Verification

- Home, detail, words, bookmarks, and proverb show the correct source states.
- Narrow and wide windows remain usable without clipped controls or lost focus.
