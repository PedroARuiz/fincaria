# 0005. API versioning strategy

## Status
Accepted

## Context

Milestone 4 grows the REST API surface (pagination, filtering, a second
documented resource) and introduces OpenAPI/Swagger documentation. Up to
this point every endpoint (`/api/users`, `/api/users/{userId}/farms`) has
been unversioned: any future breaking change to a request/response shape
would have no way to coexist with clients built against the previous
contract. A versioning policy needs to be chosen and applied consistently
before the documented surface grows further.

## Decision

Every REST endpoint is prefixed with a URI version segment:
`/api/v1/...` (e.g. `/api/v1/users`, `/api/v1/users/{userId}/farms`).
This applies retroactively to the endpoints that already exist, not only
to new ones, so the policy is consistent from the start rather than mixed.

A new major version (`/api/v2/...`) is only introduced for a breaking
change to an existing endpoint's contract (e.g. removing/renaming a field,
changing a status code's meaning). Additive, backward-compatible changes
(new optional fields, new endpoints, new query parameters) ship under the
existing version.

## Alternatives considered

- **Custom header** (`X-API-Version: 1`): keeps the resource URL itself
  "clean", but is less discoverable — it doesn't show up in Swagger UI
  paths, curl history, or server logs the way a URI segment does, which
  matters while this is still a learning project with a lot of manual
  exploration.
- **Content negotiation / media-type versioning**
  (`Accept: application/vnd.fincaria.v1+json`): the most "RESTful" option
  in theory, but brings disproportionate ceremony (custom media types,
  content negotiation configuration) for the current size and maturity of
  the API.
- **Query parameter** (`?version=1`): trivial to add, but non-idiomatic
  and easy to lose across caches/proxies/load balancers, which silently
  breaks the guarantee a versioning scheme is supposed to provide.

## Consequences

- Every existing controller route and its integration tests need to be
  updated to the `/api/v1/...` prefix as part of applying this decision.
- OpenAPI/Swagger UI reflects the version naturally, since it's part of
  the path — no extra documentation effort needed to communicate it.
- A `/v2` prefix is expected the next time a breaking change is needed;
  how `v1` and `v2` controllers/routes coexist in code (e.g. separate
  controller classes vs. shared ones) is deliberately left open and will
  be decided with that future change, when the actual shape of the
  breaking change is known.
