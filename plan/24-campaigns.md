# 24 — Add campaigns and update notifications

**Depends on:** 13, 14, 19, 20, 21  
**Outcome:** Editorial and update messages respect user choices and open the right content on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port campaign/update notification settings, payload parser, topic reconciliation, and action routing from Android.
2. Android: add Firebase Messaging service, topic workers, channels, permissions, and foreground/background behavior.
3. iOS: register APNs/FCM or the configured provider, map payloads to the shared parser, and reconcile subscriptions.
4. Web: add service-worker push and subscription management behind HTTPS and user permission; test JS and Wasm app shells.
5. Desktop and unsupported browsers: expose the same announcements/update items through an in-app feed on launch. Add a read-only backend feed contract if push is currently the only delivery path.
6. Ensure opt-out removes subscriptions, suppresses presentation, and does not override reporting/privacy preferences.

## Verification

- Payload contract tests cover word, proverb, bookmark, update, malformed, and duplicate messages.
- Permission denial and opt-out leave app startup/navigation functional on every target.
