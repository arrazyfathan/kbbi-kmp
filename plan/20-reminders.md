# 20 — Add scheduled reminders

**Depends on:** 05, 07, 13, 14  
**Outcome:** Daily word, daily proverb, and bookmark review reminders have a working path on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port reminder models, time validation, deterministic daily selection, bookmark review selection, and scheduling state.
2. Android: implement WorkManager workers, notification channels, permission checks, and unique work reconciliation.
3. iOS: schedule local notifications with `UNUserNotificationCenter`; update/cancel requests when settings change.
4. Desktop: use an OS notification/scheduling adapter where reliable; provide an in-app due reminder on next launch otherwise.
5. Web: use HTTPS service-worker notifications where permission/support exist; provide an in-app due reminder otherwise.
6. Parse taps into typed word/proverb/bookmark routes and handle missing content gracefully.

## Verification

- Tests cover each reminder type, local-day rollover, time-zone change, permission denial, reschedule, and cancel.
- Notification tap or in-app card opens the intended route on each target.
