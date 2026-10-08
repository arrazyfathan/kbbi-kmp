# 14 — Complete navigation and external links

**Depends on:** 08, 09, 10, 12, 13  
**Outcome:** Every screen and external destination can be reached and restored.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Add figure, figure detail, settings, legal, AI settings, and update route keys to shared Navigation3 configuration.
2. Keep stable route identifiers in saved state; reload large word/figure payloads from repositories.
3. Parse browser URLs and native deep links through one typed destination parser; reject malformed or oversized inputs.
4. Preserve bottom-tab stacks, back behavior, transitions, and selected tab after returning from detail.
5. Connect share/external intents only after route handling is ready; avoid replaying a consumed launch request.

## Verification

- Every source screen is reachable from its intended UI action.
- Cold start, browser refresh, back/forward, invalid link, and repeated link open correct routes without crashes.
