# 18 — Add AI word-study UI

**Depends on:** 03, 04, 14, 16, 17  
**Outcome:** Users can configure providers and generate word-study content from detail on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port AI Settings provider list, backend/custom mode, add/edit form, model selection, secret visibility, connection test, validation, and privacy note.
2. Port detail's word-study card: generate, loading, structured result, error/retry, AI attribution, and configuration link.
3. Wire ViewModels to shared contracts, keeping credentials out of composable previews and saved navigation state.
4. Ensure changing a provider invalidates stale result state and a failed test does not delete saved configuration.
5. Adapt long result sections and editor fields to narrow mobile and wide desktop/browser windows.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- Backend and custom mode each pass configure → test → generate → retry paths.
- Incomplete credentials route to settings; web reload asks for key again; all targets remain usable.
