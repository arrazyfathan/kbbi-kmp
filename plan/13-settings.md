# 13 — Add settings and preferences

**Depends on:** 03, 04, 05, 07  
**Outcome:** Shared Settings reproduces source controls and persisted state.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port `UiPreferences`, `NotificationSettings`, reminder time/type, bookmark layout, and app-icon choice contracts.
2. Add platform persistence for non-secret preferences; apply language, theme, and haptics at startup.
3. Port settings sections, clear-history confirmation, version display, notification toggles, reporting toggles, and unavailable-capability states.
4. Port Privacy Policy, Terms, and open-source licenses views and connect external links.
5. Preserve source defaults: crash reporting on, analytics/performance off, campaign notifications off, reminders off.
6. Keep permission prompts tied to a user action and refresh state after OS settings change.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Changing theme/language/layout survives restart where persistence is supported.
- Clear history preserves bookmarks; denied permissions give accurate state; all text appears in English and Indonesian.
