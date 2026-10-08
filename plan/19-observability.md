# 19 — Add observability and privacy gates

**Depends on:** 05, 06, 13, 14, 16  
**Outcome:** Analytics, crash, and network reporting mirror Android policy without leaking dictionary or AI content.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port typed `AnalyticsEvent`, screen names, crash operations, reporting preferences, and coordinator contracts.
2. Connect events at action/result and navigation boundaries, avoiding duplicates from recomposition or multiple collectors.
3. Keep crash reporting enabled by default; analytics and performance default off. Enforce source production/release gate for performance where applicable.
4. Add Android/iOS adapters and suitable desktop/web adapters or explicit no-op reporters; report capability honestly in Settings.
5. Sanitize network URLs and diagnostics: exclude search terms, definitions, translations, visitor IDs, and API keys.
6. Close network traces on success, failure, and cancellation; never report expected validation errors as crashes.

## Verification

- Fake reporter tests check names, parameters, gates, duplicate prevention, and URL sanitization.
- Turning a preference off stops collection without requiring restart; no private content appears in logs.
