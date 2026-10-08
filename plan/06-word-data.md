# 06 — Complete dictionary search and translation data

**Depends on:** 02, 05  
**Outcome:** Shared word contracts reproduce the Android source's current behavior.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Compare source `feature/home` domain/data with KMP; add missing top words, suggestions, did-you-mean, translation, visitor count, history clearing, and request fields.
2. Keep endpoint-specific routes and envelope validation; map malformed responses to `DataError`, not empty success.
3. Preserve remote-first lookup, cached fallback, saved state, and the difference between `entries.json` index and definitions.
4. Keep DTOs, domain models, and entities separate; add missing mappers and Koin bindings.
5. Normalize input in use cases and cancel superseded searches.

## Verification

- MockEngine tests cover valid, empty, malformed, not-found, failure, and cancellation responses.
- Use-case tests cover suggestions, translations, visitor metadata, and history; all targets compile.
