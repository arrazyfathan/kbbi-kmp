# 10 — Complete proverbs and splash

**Depends on:** 03, 04, 07  
**Outcome:** Proverb browsing and startup follow the Android source behavior.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Compare proverb list paging, meaning detail, search, cache fallback, and retry with the source ViewModel and repository.
2. [x] Port missing list/item layouts and localized empty, loading, and error states; restore the source AI-generated meaning notice through cache, match the focused/unfocused gradient search treatment and icon spacing, preserve the app-bar gradient, and apply bottom safe-area clearance to the list rather than the whole screen.
3. [x] Reconcile splash timing, animation, initial route, and loading overlay with Android; match transparent system bars, navigation contrast handling, keyboard resize, and bottom navigation height to the Android source.
4. [x] Keep startup behind the splash so navigation and paging work begin after the already initialized platform DI graph; avoid duplicate detail reload on configuration changes.
5. [x] Save only the search query and selected proverb slug; reload the list/detail content after restoration.

## Verification

- [x] Person compared this plan’s delivered screens and user-visible behavior with the Android source at equivalent viewport sizes and confirmed the review passed (2026-10-09).
- [x] Android and iOS simulator target compilation passes: `:androidApp:compileDebugKotlin :shared:compileKotlinIosSimulatorArm64`.

- [x] Person confirmed proverb list/detail behavior online and from cache, including retry after failed pages (2026-10-09).
- [x] Person confirmed cold start, back, and process/window recreation land on the intended route on all targets (2026-10-09).
