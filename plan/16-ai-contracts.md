# 16 — Add AI word-study contracts

**Depends on:** 05, 06  
**Outcome:** Backend and custom-provider word-study generation share validated models and errors.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port `feature/wordstudy` models, provider catalog, repository contracts, and generation use cases.
2. Implement backend provider discovery/generation and custom chat-completion request/response mapping using shared Ktor.
3. Preserve source language selection, prompt rules, minimum result validation, and distinction between KBBI and AI-generated definitions.
4. Return typed errors for missing provider, invalid content, timeout, and provider response failures; preserve coroutine cancellation.
5. Keep API keys out of URL, logging, reporting, persisted study result, and diagnostic context.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- MockEngine tests cover catalog, backend generation, custom generation, invalid JSON/content, and errors.
- The same request model compiles and executes on Android, iOS, JVM, JS, and Wasm.
