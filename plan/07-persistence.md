# 07 — Harden local word and proverb persistence

**Depends on:** 06  
**Outcome:** Bookmarks, caches, and histories have the same semantics through each backend.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. [x] Compare Android Room schema, converters, DAOs, and cache policies against KMP.
2. [x] Keep Room on Android/iOS/JVM and browser storage behind shared interfaces; expose quota/corrupt-data issues from browser storage.
3. [x] Ensure deleting a bookmark preserves a cached definition; trim/clear history without affecting saved words.
4. [x] Preserve proverb page/detail fallback, stable ordering, and offline reading.
5. [x] Keep KMP schema migrations explicit; the existing 9→10 and 10→11 migrations cover the current KMP schema.

## Verification

- [x] SQLite and browser-storage tests cover insert, update, unbookmark, fallback, ordering, corrupted data, and failed writes (`:shared:jvmTest :shared:jsTest`, 2026-10-08).
- [ ] Saved words and cached proverb details reopen offline on each target (manual human verification pending).
- [x] Android debug APK, iOS simulator framework, desktop JVM, Browser JS, and Browser Wasm compile (2026-10-08).
