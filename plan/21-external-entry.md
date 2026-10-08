# 21 — Add external OS entry points

**Depends on:** 05, 14  
**Outcome:** Share text, deep links, shortcuts, and notification/widget launches enter the correct shared route.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port source external-intent parser, shortcut actions, widget and notification launch request types into a shared validated parser.
2. Android: add `VIEW`, `SEND`, and `PROCESS_TEXT` intent filters, launcher shortcuts, singleTop handling, and cold-start routing.
3. iOS: add URL handling and suitable share/shortcut extension or App Intent integration.
4. Desktop: handle app URL/file launch arguments and expose quick-search/open commands.
5. Web: add PWA manifest shortcuts, share target where supported, and URL-based routing with browser history.
6. Consume each launch request once, including when the app was already running.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Tests cover valid, malformed, repeated, and oversized input.
- Warm and cold launches from each supported OS entry point reach the intended screen without duplicate navigation.
