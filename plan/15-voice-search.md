# 15 — Add voice search

**Depends on:** 05, 08, 13  
**Outcome:** Voice input uses native recognition where available and leaves typed search usable everywhere.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port source voice-search states, normalization, partial results, cancellation, permission errors, and bottom sheet.
2. Implement Android speech recognition and iOS Speech framework adapters with microphone/speech permissions.
3. Implement a documented JVM desktop recognition path, with explicit unavailable response if no compatible engine/service exists.
4. Use browser speech recognition where exposed; capability-check JS and Wasm, and leave the typed field active where unavailable.
5. Feed final recognized text through the ordinary search use case; never log recognized content.

## Verification

- Tests cover partial/final result, empty result, cancellation, permission denial, and unavailable capability.
- On each target the voice control either recognizes and searches or clearly reports unavailability without blocking typed search.
