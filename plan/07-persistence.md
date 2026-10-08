# 07 — Harden local word and proverb persistence

**Depends on:** 06  
**Outcome:** Bookmarks, caches, and histories have the same semantics through each backend.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Compare Android Room schema, converters, DAOs, and cache policies against KMP.
2. Keep Room on Android/iOS/JVM and browser storage behind shared interfaces; surface quota/corrupt-data failures.
3. Ensure deleting a bookmark preserves a cached definition; trim/clear history without affecting saved words.
4. Preserve proverb page/detail fallback, stable ordering, and offline reading.
5. Add migrations for schema changes made within KMP; a fresh original-Android-to-KMP install is acceptable.

## Verification

- Storage tests cover insert, update, delete, fallback, ordering, corrupted data, and failed writes.
- Saved words and cached proverb details reopen offline on each target.
