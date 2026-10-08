# 12 — Add figure screens

**Depends on:** 03, 04, 11  
**Outcome:** Figures are browsable and readable on every target.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port figure list, search field, loading shimmer, row imagery, empty/error/retry, and article detail.
2. Add shared ViewModels and state/actions for query, paging, detail loading, and retry.
3. Connect figure routes to main navigation and ensure back restores the list query and scroll position.
4. Adapt article layout to mobile, tablet, desktop, and browser widths; provide image fallback and accessibility descriptions.
5. Keep external article links behind the platform URL contract.

## Verification

- Figure list, search, pagination, detail, image failure, and retry work on each target.
- Repeated search changes cannot show a prior query's page.
