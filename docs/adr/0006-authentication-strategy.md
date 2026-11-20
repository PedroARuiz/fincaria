# 0006. Authentication strategy: hand-rolled JWT over OAuth2/OIDC

## Status
Accepted

## Context

Milestone 5 needs authentication for the API: issuing credentials on login
and verifying them on every subsequent request. The roadmap lists two
options: hand-roll JWT issuance/validation, or integrate a real OIDC
provider (Keycloak via docker-compose) as an optional stretch, to learn
the industry-standard flow instead.

## Decision

Hand-rolled JWT, issued and validated by this same service:
- **jjwt** (0.12.x) for token building/parsing.
- **HMAC-SHA384 (HS384)**, symmetric signing: issuer and validator are the
  same service for now, so there's no need for asymmetric keys yet. HS384
  is pinned explicitly rather than HS256 — jjwt's `signWith(key)` actually
  auto-selects the strongest HMAC variant the key's length supports, and
  our secret is long enough for HS384, so it's a stronger digest at no
  extra engineering cost. Pinning it explicitly (instead of leaving it to
  jjwt's length-based auto-selection) keeps the algorithm a deliberate
  choice instead of an accidental side effect of how long the configured
  secret happens to be.
- The signing secret is externalized via the `JWT_SECRET` environment
  variable; `application.yml` only commits an explicitly-insecure
  dev/test fallback (long enough — at least 48 bytes — for HS384).
- A single short-lived access token (1 hour), no refresh token yet.
- Claims are limited to `sub` (userId), `iat`, and `exp` — no email, no
  roles, nothing beyond what's needed today.

## Alternatives considered

- **Keycloak/OIDC**: the industry-standard way to learn this flow, and
  still listed as a future stretch in the roadmap. Not chosen for this
  pass because it adds real operational weight (another container,
  client registration, JWKS/token introspection) that would obscure the
  JWT mechanics themselves — which is the actual learning goal of this
  milestone. Revisit once the mechanics are internalized.
- **Server-side sessions + cookies**: simpler mentally, but the roadmap
  explicitly wants token-based/stateless auth practiced, and sessions fit
  a browser-driven app more naturally than a JSON API consumed by varied
  clients.
- **Asymmetric signing (RS256) from the start**: unnecessary complexity
  while a single service both issues and validates tokens; worth
  revisiting if Milestone 11 ever splits this into multiple services.

## Consequences

- There is no token revocation: logging out is purely client-side
  (discarding the token). Refresh token rotation and login rate limiting
  are both tracked in `ROADMAP.md`'s future improvements backlog rather
  than solved here.
- Any real environment must supply `JWT_SECRET` itself; the committed
  default is deliberately insecure and only safe for local/test use.
- If the project ever needs standardized social/enterprise login, or
  splits into multiple services, this decision should be revisited in
  favor of a real OIDC provider — this ADR defers that, it doesn't rule
  it out.
