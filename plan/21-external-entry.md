# 21 — Add external OS entry points

**Depends on:** 05, 14  
**Outcome:** Share text, deep links, shortcuts, and notification/widget launches enter the correct shared route.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port source external-intent parser, shortcut actions, widget and notification launch request types into a shared validated parser.
2. Android: add `VIEW`, `SEND`, and `PROCESS_TEXT` intent filters, launcher shortcuts, singleTop handling, and cold-start routing.
3. iOS: add URL handling and suitable share/shortcut extension or App Intent integration.
4. Desktop: handle app URL/file launch arguments and expose quick-search/open commands.
5. Web: add PWA manifest shortcuts, share target where supported, and URL-based routing with browser history.
6. Consume each launch request once, including when the app was already running.

## Verification

- Tests cover valid, malformed, repeated, and oversized input.
- Warm and cold launches from each supported OS entry point reach the intended screen without duplicate navigation.
