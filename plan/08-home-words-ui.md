# 08 — Complete home and word-list UI

**Depends on:** 03, 04, 06, 07  
**Outcome:** Home and word list expose every source-app discovery path.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Port top-word chips, suggestion modes, search states, and word-list filtering. Random-word entry is initiated by the source shortcut and is wired with external entry points in Plan 21.
2. [x] Drive UI from immutable ViewModel state/action/event contracts; preserve query and focus across route/window changes.
3. [x] Wire top words, suggestions, keyboard submit, pointer/touch selection, and random word to detail routes.
4. [x] Cancel stale requests so old results cannot replace a newer search.
5. [x] Add loading, empty, offline, and retry states with localized copy.
6. [ ] Port and compare the complete Android home and word-list UI, including all source animations, gestures, styling, content, and interactions; remove any simplified or placeholder treatment.
7. [ ] Complete source Home control behavior: microphone/voice input, animated search-to-voice control, and the expanded Explore menu with source destinations. These depend on later voice, figure, settings, AI, navigation, and external-entry work.

## Verification

- [x] Person visually compared Home and word-list screens with the Android source at equivalent viewport sizes, including suggestion placement; user confirmed the appearance matches on 2026-10-08.
- [ ] Complete source behavior comparison, including voice search and Explore destinations; those capabilities depend on later plans. Device and simulator interactions remain manual-only.

- [x] JVM tests cover suggestions, did-you-mean/retry, detail navigation events, random selection, stale search cancellation, word-list filtering, and word selection.
- [x] JVM and Browser JS test suites pass; Android, iOS simulator framework, desktop JVM, Browser JS, and Browser Wasm compile tasks pass.
- [ ] Person manually verifies typed search, suggestions, top words, random word, and word-list selection open the intended detail, and checks no result, offline, retry, and back behavior on each target.
