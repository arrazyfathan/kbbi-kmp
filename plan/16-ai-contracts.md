# 16 — Add AI word-study contracts

**Depends on:** 05, 06  
**Outcome:** Backend and custom-provider word-study generation share validated models and errors.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port `feature/wordstudy` models, provider catalog, repository contracts, and generation use cases.
2. Implement backend provider discovery/generation and custom chat-completion request/response mapping using shared Ktor.
3. Preserve source language selection, prompt rules, minimum result validation, and distinction between KBBI and AI-generated definitions.
4. Return typed errors for missing provider, invalid content, timeout, and provider response failures; preserve coroutine cancellation.
5. Keep API keys out of URL, logging, reporting, persisted study result, and diagnostic context.

## Verification

- MockEngine tests cover catalog, backend generation, custom generation, invalid JSON/content, and errors.
- The same request model compiles and executes on Android, iOS, JVM, JS, and Wasm.
