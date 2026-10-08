# 05 — Define platform capability boundaries

**Depends on:** 02  
**Outcome:** Common code can request OS behavior without importing OS APIs.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Define focused interfaces for share text, open URL, locale, haptics, speech, notification permission, alternate icon, and incoming launch request.
2. Return explicit success, denied, unavailable, or failed outcomes where the UI must react.
3. Implement adapters in Android, iOS, JVM, and web source sets or app shells. Avoid Android context and browser globals in `commonMain`.
4. Register one implementation of each contract per target in Koin and verify lifecycle cleanup.
5. Define an in-app path for unsupported OS features; capability checks must control visibility and error text.

## Verification

- All registered targets compile and Koin resolves the adapter graph.
- Contract tests cover unavailable and denied outcomes without platform SDKs.
