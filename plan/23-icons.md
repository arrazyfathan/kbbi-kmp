# 23 — Add app icon choices and branding

**Depends on:** 03, 05, 13  
**Outcome:** Theme/icon selection applies the closest supported brand treatment on each platform.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port source alternate icon IDs, labels, and assets; map each choice to a common preference.
2. Android: configure launcher activity aliases and switch them without losing launch shortcuts.
3. iOS: add alternate icon sets and call the supported app-icon API; reconcile saved state after OS rejection.
4. Desktop: provide branded package icons at build time and apply selected palette inside the running app.
5. Web: package PWA icons and update in-app theme/brand treatment; show when installed PWA icon changes require reinstall.
6. Keep UI selection aligned with the icon actually applied by the OS.

## Verification

- Every offered choice has an asset and correct label.
- Android/iOS icon changes survive restart and launch; desktop/web preference does not claim an unsupported runtime icon change.
