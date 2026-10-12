# 14 — Complete navigation and external links

**Depends on:** 08, 09, 10, 12, 13  
**Outcome:** Every screen and external destination can be reached and restored.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. [x] Registered figure, figure detail, settings, legal, and stable reserved AI settings/update keys in shared Navigation3 serialization. Figure/settings/legal destinations are connected to their original UI actions. AI Settings UI is owned by Plan 18; update prompts/actions are owned by Plan 25. These reserved routes are rejected until their real UIs are delivered.
2. [x] Word detail saves a word identifier instead of the full meanings payload. Its route ViewModel restores the cached result first and looks up uncached words through the repository, with loading/error/retry handling. Figure detail already reloads by slug. Explicit serialized route names remain independent of Kotlin package names; bounded saved-stack decoding falls back to a valid root for obsolete or malformed state.
3. [x] Added one typed destination parser for browser hash URLs and native `kbbi://` links, including source word/proverb/bookmark destinations, figure/settings/legal routes, shared-text normalization, and reserved later-plan routes. Rejects control characters, malformed escaping, extra segments, credentials, queries, oversized links/arguments, and unavailable destinations.
4. [x] Preserved independent tab stacks and selected tabs on return. Added navigation-entry ViewModel ownership and duplicate-route prevention. Copied source navigation transition timings, offsets, fade behavior, tab indicator, labels, icon sizes/tints, press motion, and bottom-bar enter/exit motion.
5. [x] Browser URLs identify the active screen, and history stores validated identifier-only snapshots. Cold URL entry, refresh, back/forward, and hash changes restore the correct stack; invalid or unavailable routes fall back to Home without adding a history entry. Browser routing uses hashes to avoid requiring server route rewrites.
6. [x] Connected the shared incoming-request interface to validated navigation. Request identities are recorded in saved state and acknowledged to the host queue, preventing consumed requests from replaying during a UI refresh. Native intent filters, iOS URL callbacks/extensions, shortcuts, widgets, and notification producers remain owned by Plan 21; hosts must retain the same request ID when redelivering an event and remove acknowledged events.

## Verification

- [x] Person compares navigation, bottom-tab labels/indicator/press motion, transitions and back behavior directly with the Android source at equivalent viewport sizes.
- [x] Person opens Home → Words → detail → back, Bookmarks → saved detail → back, and Home → Proverb/Figures/Settings → detail/legal → back; checks tab selection, list position, and independent stacks.
- [x] Person checks English → Indonesian while on Settings and confirms navigation remains on Settings; checks screen restoration after background/process recreation on native targets.
- [x] Person cold-opens and refreshes browser `#/word/bahasa`, `#/figure/<existing-slug>`, `#/proverb/<existing-slug>`, `#/settings`, `#/privacy`, `#/terms`, and `#/licenses`; checks back/forward restores the previous tab and detail.
- [x] Person checks `#/unknown`, `#/word/%ZZ`, oversized arguments, and reserved `#/update` return to Home without crashes; opening the same available route does not duplicate it.
- [x] Person checks cached word detail opens offline after reload and uncached failures offer retry/back.
- [ ] Person checks native/shared incoming-request delivery and acknowledgement when Plan 21 supplies the platform adapters; repeats the same request identity to confirm it is consumed once.
- [x] Android, iOS Simulator framework, desktop JVM, Browser JS and Browser Wasm compilation passed on 2026-10-12 (including both web app shells). No agent device or browser interaction performed; the person confirmed the six manual checks above on 2026-10-12.

## Limits and later-plan ownership

The parser accepts at most 2,048 characters per link, 256 decoded characters per route argument, and 4,096 characters per shared-text request. Each tab retains its root plus up to 31 subsequent entries; browser and native saved-state decoding accepts at most 65,536 characters. All device and simulator verification remains manual. A successful build establishes compilation, not source parity or runtime acceptance.

Every implemented source destination is wired. Full AI Settings and update UI reachability stays with Plans 18 and 25, and native external entry acceptance stays with Plan 21.

## Manual acceptance

On 2026-10-12, the person confirmed all six navigation/source-comparison, route restoration, language-switching, browser routing/history, invalid-link and offline-detail checks above passed. Native incoming-request delivery and acknowledgement remain pending Plan 21.

Plan 18 implements `#/ai-settings`; its cold-open/refresh and back-navigation acceptance is now recorded in Plan 18 rather than as a reserved-route fallback. The prior Plan 14 person review predates this destination.
