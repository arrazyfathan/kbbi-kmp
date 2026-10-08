# 18 — Add AI word-study UI

**Depends on:** 03, 04, 14, 16, 17  
**Outcome:** Users can configure providers and generate word-study content from detail on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port AI Settings provider list, backend/custom mode, add/edit form, model selection, secret visibility, connection test, validation, and privacy note.
2. Port detail's word-study card: generate, loading, structured result, error/retry, AI attribution, and configuration link.
3. Wire ViewModels to shared contracts, keeping credentials out of composable previews and saved navigation state.
4. Ensure changing a provider invalidates stale result state and a failed test does not delete saved configuration.
5. Adapt long result sections and editor fields to narrow mobile and wide desktop/browser windows.

## Verification

- Backend and custom mode each pass configure → test → generate → retry paths.
- Incomplete credentials route to settings; web reload asks for key again; all targets remain usable.
