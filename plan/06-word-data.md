# 06 — Complete dictionary search and translation data

**Depends on:** 02, 05  
**Outcome:** Shared word contracts reproduce the Android source's current behavior.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Compare source `feature/home` domain/data with KMP; add top words, suggestions/did-you-mean, translation, visitor metadata, history clearing, and request fields.
2. [x] Keep endpoint-specific routes and envelope validation; map malformed responses to `DataError`, not empty success.
3. [x] Preserve remote-first word lookup, cached fallback, saved state, and the separate `entries.json` word index and API definitions.
4. [x] Keep DTOs, domain models, and entities separate; add mappers and Koin bindings.
5. [x] Trim search/translation inputs; superseded searches continue to be cancelled by the home ViewModel.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- [x] MockEngine tests cover valid, empty, malformed, not-found, failure, and cancellation responses (`:shared:jvmTest`, 2026-10-08).
- [x] Use-case tests cover suggestions, translations, visitor metadata, and history (`:shared:jvmTest`, 2026-10-08).
- [x] Android debug APK, iOS simulator framework source, desktop JVM, Browser JS, and Browser Wasm compile (2026-10-08).
- [ ] Device and simulator behavior is manually verified by a person.
