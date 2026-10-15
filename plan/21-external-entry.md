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

## Implementation progress — 2026-10-15

The shared launch queue and semantic dispatch are implemented. Android accepts KBBI links, `SEND`, `PROCESS_TEXT`, reminder/widget payloads and four launcher shortcuts. iOS registers `kbbi://`, handles foreground/background/cold shortcut delivery, exposes four App Intents, and includes a plain-text Share Extension with lookup, retry, extension-local result caching, and App Group handoff for the next app opening. Desktop accepts bounded CLI/search/file/URI arguments, forwards subsequent launches to the running process, handles macOS open-URL events, and writes `kbbi://` registration into its packaged app bundle. Browser JS and Wasm builds include a PWA manifest, shortcuts, share target, query cleanup, and a same-origin shell/runtime cache.

Build verification is recorded in `plan/parity-inventory.md`. The Android, iOS Kotlin/Native, JVM desktop, JS, and Wasm compilation/package checks pass. Source comparison and all real device/simulator/browser interaction checks remain unchecked until performed manually by a person. The App Group identifier is `group.com.arrazyfathan.kbbi`; the Apple Developer team must enable that group for both iOS bundle identifiers before device signing. Native Windows/Linux URL associations remain deferred; their portable CLI entry points are present.
