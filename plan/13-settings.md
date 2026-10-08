# 13 — Add settings and preferences

**Depends on:** 03, 04, 05, 07  
**Outcome:** Shared Settings reproduces source controls and persisted state.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port `UiPreferences`, `NotificationSettings`, reminder time/type, bookmark layout, and app-icon choice contracts.
2. Add platform persistence for non-secret preferences; apply language, theme, and haptics at startup.
3. Port settings sections, clear-history confirmation, version display, notification toggles, reporting toggles, and unavailable-capability states.
4. Port Privacy Policy, Terms, and open-source licenses views and connect external links.
5. Preserve source defaults: crash reporting on, analytics/performance off, campaign notifications off, reminders off.
6. Keep permission prompts tied to a user action and refresh state after OS settings change.

## Verification

- Changing theme/language/layout survives restart where persistence is supported.
- Clear history preserves bookmarks; denied permissions give accurate state; all text appears in English and Indonesian.
