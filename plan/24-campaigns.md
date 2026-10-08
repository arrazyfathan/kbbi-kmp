# 24 — Add campaigns and update notifications

**Depends on:** 13, 14, 19, 20, 21  
**Outcome:** Editorial and update messages respect user choices and open the right content on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port campaign/update notification settings, payload parser, topic reconciliation, and action routing from Android.
2. Android: add Firebase Messaging service, topic workers, channels, permissions, and foreground/background behavior.
3. iOS: register APNs/FCM or the configured provider, map payloads to the shared parser, and reconcile subscriptions.
4. Web: add service-worker push and subscription management behind HTTPS and user permission; test JS and Wasm app shells.
5. Desktop and unsupported browsers: expose the same announcements/update items through an in-app feed on launch. Add a read-only backend feed contract if push is currently the only delivery path.
6. Ensure opt-out removes subscriptions, suppresses presentation, and does not override reporting/privacy preferences.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Payload contract tests cover word, proverb, bookmark, update, malformed, and duplicate messages.
- Permission denial and opt-out leave app startup/navigation functional on every target.
