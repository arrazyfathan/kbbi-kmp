# 18 — Add AI word-study UI

**Depends on:** 03, 04, 14, 16, 17  
**Outcome:** Users can configure providers and generate word-study content from detail on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

- [x] Port the source AI Settings provider list, backend/custom mode, add/edit form, model selection, secret visibility, connection test, validation, and privacy note.
- [x] Port detail's word-study card: generate, loading, structured result, error/retry, attribution, and configuration navigation. Source dimensions, colors, icons, copy, fonts, list/chip layouts, and app-bar collapse behavior are retained.
- [x] Wire the shared ViewModels and Koin; secrets stay in transient editor/request state, outside previews and saved navigation state. Native targets use transient platform messages; Desktop/browser show visible transient feedback.
- [x] Cancel and clear stale study state on provider/configuration, word/source, or output-language changes. A failed connection test preserves saved configuration and keys.
- [x] Use the source scrollable form and wrapping model/result sections within the existing app width constraint; apply keyboard insets once and avoid duplicate bottom-safe-area padding.
- [x] Add Home → Explore → AI with the source menu cards, Settings button and animated morph toggle (source springs, float motion, rotations, blur pulse and back-to-collapse behavior). Implement the existing `/ai-settings` route and detail configuration navigation.
- [x] Explain browser key re-entry after reload/close using a session-storage capability exposed by the configuration contract.

Integration fixes while exposing Plan 17 storage: credential-manager initialization and operations now run off the UI thread. iOS Keychain query dictionaries retain their CoreFoundation values and replace existing key data with `SecItemUpdate`, preserving the previous key if an update fails.

## Verification

- [x] Focused JVM tests cover backend catalog/default/model selection, custom provider validation/save, incomplete mode selection, a failed connection test preserving saved credentials, study generation/retry, provider/language invalidation, pending-request cancellation, settings navigation for incomplete credentials, and AI source mapping. **11 tests passed**.
- [x] Android debug APK, iOS Simulator framework and Xcode host app (generic Simulator destination), Desktop Kotlin, shared/browser JS and Wasm, and common tests compiled on iOS/JS/Wasm. No device or Simulator test binary was executed.
- [x] Person compared Home Explore → AI, AI Settings, and detail's study card directly with the Android source at equivalent viewport sizes, including spacing/icons/type, app-bar collapse, Explore toggle morph/press/float/blur motion and back-to-collapse, model chips, editor rows, key visibility, message feedback, loading/error/result states, and English/Indonesian text.
- [x] Person selected a backend provider/model → generated from a word detail → checked provider attribution → retried after an error; custom provider add/edit → test → save → select mode/model → generate → retry also passed.
- [x] Person switched provider/model/key or output language after generating and during an in-flight request, confirmed old study content clears, and confirmed a failed connection test preserves the saved provider/key.
- [x] Person confirmed missing credentials open AI Settings, native restart restores keys, browser reload requires key re-entry, and backend mode remains usable without custom credentials.
- [x] Person checked long results and provider/model text on narrow/wide windows, scrolled all editor controls above the keyboard, and checked Home/detail/AI Settings back navigation and browser `#/ai-settings` cold-open/refresh.

The person confirmed all AI word-study verification across Plans 16–18 passed on 2026-10-12. The browser same-origin proxy and its security tests remain deferred to Plan 26; browser custom providers were verified with providers that permit direct browser requests.

Build/test log: `/tmp/kbbi-plan18-final-build.log`. iOS host build: `/tmp/kbbi-plan18-xcode.log`. The person separately confirmed the live-provider, device, and visual acceptance above. Browser custom providers were checked with providers that permit direct browser requests; the same-origin CORS proxy and its security tests remain open in Plan 17 pending the hosting runtime in Plan 26.
