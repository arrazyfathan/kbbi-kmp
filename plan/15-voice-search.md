# 15 — Add voice search

**Depends on:** 05, 08, 13  
**Outcome:** Voice input uses native recognition where available and leaves typed search usable everywhere.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port source voice-search states, normalization, partial results, cancellation, permission errors, and bottom sheet.
2. Implement Android speech recognition and iOS Speech framework adapters with microphone/speech permissions.
3. Implement a documented JVM desktop recognition path, with explicit unavailable response if no compatible engine/service exists.
4. Use browser speech recognition where exposed; capability-check JS and Wasm, and leave the typed field active where unavailable.
5. Feed final recognized text through the ordinary search use case; never log recognized content.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Tests cover partial/final result, empty result, cancellation, permission denial, and unavailable capability.
- On each target the voice control either recognizes and searches or clearly reports unavailability without blocking typed search.
