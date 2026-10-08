# 23 — Add app icon choices and branding

**Depends on:** 03, 05, 13  
**Outcome:** Theme/icon selection applies the closest supported brand treatment on each platform.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port source alternate icon IDs, labels, and assets; map each choice to a common preference.
2. Android: configure launcher activity aliases and switch them without losing launch shortcuts.
3. iOS: add alternate icon sets and call the supported app-icon API; reconcile saved state after OS rejection.
4. Desktop: provide branded package icons at build time and apply selected palette inside the running app.
5. Web: package PWA icons and update in-app theme/brand treatment; show when installed PWA icon changes require reinstall.
6. Keep UI selection aligned with the icon actually applied by the OS.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Every offered choice has an asset and correct label.
- Android/iOS icon changes survive restart and launch; desktop/web preference does not claim an unsupported runtime icon change.
