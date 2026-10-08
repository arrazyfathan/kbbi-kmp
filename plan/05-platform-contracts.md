# 05 — Define platform capability boundaries

**Depends on:** 02  
**Outcome:** Common code can request OS behavior without importing OS APIs.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Define focused interfaces for share text, open URL, locale, haptics, speech, notification permission, alternate icon, and incoming launch request.
2. Return explicit success, denied, unavailable, or failed outcomes where the UI must react.
3. Implement adapters in Android, iOS, JVM, and web source sets or app shells. Avoid Android context and browser globals in `commonMain`.
4. Register one implementation of each contract per target in Koin and verify lifecycle cleanup.
5. Define an in-app path for unsupported OS features; capability checks must control visibility and error text.

### Delivery status

- [x] Common capability contracts and explicit success/denied/unavailable/failed results are defined.
- [x] Android, iOS, JVM desktop, and browser adapters are registered in each target's Koin platform module.
- [x] Unsupported capabilities return `Unavailable`; incoming launch requests currently emit no requests until task 21 adds platform intent handling.
- [x] Platform adapters hold no long-lived listeners or scopes that require cleanup.
- [x] Android declares the `VIBRATE` permission for its haptics adapter.
- [x] Add and run common contract tests for denied and unavailable outcomes on the JVM target (2026-10-08).
- [ ] Resolve the adapter graph from Koin at runtime on each target (JVM, Android host, and Browser JS passed; Wasm runner failed; iOS simulator runtime remains for manual review).
- [ ] Manually verify device and simulator behavior; this is for a person, not an agent.

Share success means the platform share UI was opened; it does not mean the user completed sharing. JVM and browser text sharing remain unavailable and should use an in-app copy path.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- [x] All registered target builds compile: Android debug APK, iOS simulator framework, desktop JVM, browser JS, and browser Wasm (2026-10-08).
- [ ] Koin resolves the adapter graph at runtime on each target (JVM, Android host, and Browser JS passed; Wasm runner failed; iOS simulator runtime remains for manual review).
- [x] Common contract tests cover unavailable and denied outcomes without platform SDKs on JVM (`:shared:jvmTest`, passed 2026-10-08).
- [ ] Manual device and simulator acceptance by a person.
