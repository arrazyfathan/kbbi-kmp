# 10 — Complete proverbs and splash

**Depends on:** 03, 04, 07  
**Outcome:** Proverb browsing and startup follow the Android source behavior.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Compare proverb list paging, meaning detail, search, cache fallback, and retry with the source ViewModel and repository.
2. Port missing list/item layouts and localized empty, loading, and error states.
3. Reconcile splash timing, animation, initial route, and loading overlay with Android.
4. Ensure startup does not duplicate navigation or start network work before platform DI is ready.
5. Keep saved UI state small and reload page/detail content after restoration.

## Verification

- Proverb list/detail work online and from cache; failed pages can retry.
- Cold start, back, and process/window recreation land on the intended route on all targets.
