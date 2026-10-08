# 06 — Complete dictionary search and translation data

**Depends on:** 02, 05  
**Outcome:** Shared word contracts reproduce the Android source's current behavior.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. [x] Compare source `feature/home` domain/data with KMP; add top words, suggestions/did-you-mean, translation, visitor metadata, history clearing, and request fields.
2. [x] Keep endpoint-specific routes and envelope validation; map malformed responses to `DataError`, not empty success.
3. [x] Preserve remote-first word lookup, cached fallback, saved state, and the separate `entries.json` word index and API definitions.
4. [x] Keep DTOs, domain models, and entities separate; add mappers and Koin bindings.
5. [x] Trim search/translation inputs; superseded searches continue to be cancelled by the home ViewModel.

## Verification

- [x] MockEngine tests cover valid, empty, malformed, not-found, failure, and cancellation responses (`:shared:jvmTest`, 2026-10-08).
- [x] Use-case tests cover suggestions, translations, visitor metadata, and history (`:shared:jvmTest`, 2026-10-08).
- [x] Android debug APK, iOS simulator framework source, desktop JVM, Browser JS, and Browser Wasm compile (2026-10-08).
- [ ] Device and simulator behavior is manually verified by a person.
