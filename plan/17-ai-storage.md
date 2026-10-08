# 17 — Add secure AI configuration storage

**Depends on:** 05, 13, 16  
**Outcome:** Provider preferences persist safely on native targets; web keys last one session.

**Device testing:** Device and simulator checks must be performed manually by a person, not by an agent. Agents may build and prepare reproducible steps, but must leave device verification for a human.

## Implementation

1. Port provider add/edit/remove, active mode, model selection, and configuration validation from Android.
2. Use Android Keystore, iOS Keychain, and a supported OS credential store per desktop package; persist non-secret metadata separately.
3. On browser JS/Wasm, retain custom API keys in memory only; clear them on close/reload and explain re-entry in the UI.
4. Add a stateless same-origin forwarding endpoint for browser providers whose CORS policy blocks direct requests. Send credentials per request; do not store, log, or return them.
5. Restrict proxy targets to HTTPS public hosts, reject local/private addresses and redirects to them, cap payload/time/rate, and deploy it alongside web assets.
6. Keep backend-selected provider mode usable when custom credentials are absent.

## Verification

- Native keys survive restart and are absent from ordinary preference files/logs; browser keys do not survive reload.
- Proxy tests cover valid request, blocked target, redirect, oversized body, timeout, and no credential logging.
