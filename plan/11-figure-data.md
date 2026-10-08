# 11 — Add figure domain and API support

**Depends on:** 05, 06  
**Outcome:** A shared figure repository supports list, search, paging, and detail.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

**1:1 source parity:** Treat `/Users/macintosh/Personal/Android/Samples/kbbi` as the behavioral and visual source of truth. Port the complete relevant feature, including its screens, states, interactions, copy, assets, layout, typography, colors, motion, accessibility behavior, and edge cases. Do not leave simplified, placeholder, or intentionally different UI. On targets where an OS-specific API differs, use the native equivalent while preserving the same user-visible behavior. Verify against the source at equivalent screen sizes and with the same scenarios.**

## Implementation

1. Port source `feature/figure` models, repository contract, use cases, DTOs, and mappers into shared feature packages.
2. Implement `/api/v1/figure`, `/api/v1/figure/search`, and slug detail requests with multiplatform-safe path encoding.
3. Preserve source paging parameters, sort/order, empty-page semantics, and envelope error mapping.
4. Register Koin bindings and use the shared `HttpClient`; keep platform APIs out of common code.
5. Make image URLs and optional article fields robust to absent or malformed backend data.

## Verification

- [ ] Compare this plan’s delivered screens and user-visible behavior directly with the Android source at equivalent viewport sizes; record scenarios and resolve all differences. Any device or simulator interaction must be done manually by a person.

- MockEngine tests cover first/next/empty pages, query changes, detail, malformed payload, and failures.
- DI resolves the repository and use cases; all five targets compile.
