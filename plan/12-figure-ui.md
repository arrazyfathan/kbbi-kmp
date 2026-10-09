# 12 — Add figure screens

**Depends on:** 03, 04, 11  
**Outcome:** Figures are browsable and readable on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port figure list, search field, loading shimmer, row imagery, empty/error/retry, and article detail.
2. Add shared ViewModels and state/actions for query, paging, detail loading, and retry.
3. Connect figure routes to main navigation and ensure back restores the list query and scroll position.
4. Adapt article layout to mobile, tablet, desktop, and browser widths; provide image fallback and accessibility descriptions.
5. Keep external article links behind the platform URL contract.

## Verification

- [x] Person manually compared the Explore-to-Figure path, list loading and paging, search while scrolling, empty/error/retry states, image loading/fallback, detail article layout, source URL opening, and back restoration against the Android source at equivalent viewport sizes. Device and simulator interaction was performed by a person; review passed.

- [x] Shared figure list/detail screens, debounced search, paged loading/retry states, portrait fallback, external URL adapter, and navigation are implemented. Search changes cancel the previous paging flow.
- [x] Android, iOS Simulator framework, desktop JVM, Browser JS, and Browser Wasm compilation passed.
- [x] Person manually checked figure list, search, paging, detail, failed images, retry, external URL opening, and back restoration on each supported target; review passed.
