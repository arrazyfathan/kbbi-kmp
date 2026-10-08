# 11 — Add figure domain and API support

**Depends on:** 05, 06  
**Outcome:** A shared figure repository supports list, search, paging, and detail.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port source `feature/figure` models, repository contract, use cases, DTOs, and mappers into shared feature packages.
2. Implement `/api/v1/figure`, `/api/v1/figure/search`, and slug detail requests with multiplatform-safe path encoding.
3. Preserve source paging parameters, sort/order, empty-page semantics, and envelope error mapping.
4. Register Koin bindings and use the shared `HttpClient`; keep platform APIs out of common code.
5. Make image URLs and optional article fields robust to absent or malformed backend data.

## Verification

- MockEngine tests cover first/next/empty pages, query changes, detail, malformed payload, and failures.
- DI resolves the repository and use cases; all five targets compile.
