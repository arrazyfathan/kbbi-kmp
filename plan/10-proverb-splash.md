# 10 — Complete proverbs and splash

**Depends on:** 03, 04, 07  
**Outcome:** Proverb browsing and startup follow the Android source behavior.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Compare proverb list paging, meaning detail, search, cache fallback, and retry with the source ViewModel and repository.
2. Port missing list/item layouts and localized empty, loading, and error states.
3. Reconcile splash timing, animation, initial route, and loading overlay with Android.
4. Ensure startup does not duplicate navigation or start network work before platform DI is ready.
5. Keep saved UI state small and reload page/detail content after restoration.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Proverb list/detail work online and from cache; failed pages can retry.
- Cold start, back, and process/window recreation land on the intended route on all targets.
