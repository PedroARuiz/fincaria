# 0002. Hexagonal layering convention per bounded context

## Status
Accepted

## Context

With Spring Boot now wired into the project, every bounded context needs a
consistent internal package structure before any domain code is written.
Business logic must stay decoupled from frameworks and infrastructure
details (HTTP, persistence, messaging), remain testable without a running
Spring context, and allow infrastructure adapters to be swapped (e.g. an
in-memory repository replaced by a JPA one) without touching business logic.

This decision is also an explicit learning goal: internalizing the
ports-and-adapters pattern as commonly applied to Spring Boot services,
building on — but using different vocabulary than — the author's prior
experience with Clean Architecture layering (domain/data/presentation) on
Android.

## Decision

- Every bounded context is organized under `com.fincaria.<context>`, with
  three sibling packages: `domain`, `application`, `infrastructure`.
- `domain` contains only entities and value objects, together with the
  business rules intrinsic to them. It has no notion of "use case" and no
  dependency on any framework.
- `application` contains the use-case layer, split into:
  - `port.in` — inbound port interfaces, one per use case (e.g.
    `CreateUserUseCase`).
  - `port.out` — outbound port interfaces that use cases depend on (e.g.
    `UserRepository`).
  - `service` — use-case implementations (e.g. `CreateUserService`), each
    implementing one `port.in` interface and depending on one or more
    `port.out` interfaces.
- `infrastructure` contains the adapters, grouped first by direction and
  then by technology:
  - `infrastructure.in.*` — inbound adapters (e.g. `in.web` for REST
    controllers and DTOs).
  - `infrastructure.out.*` — outbound adapters (e.g. `out.persistence` for
    repository implementations).

Example for the `users` bounded context:
```
com.fincaria.users
  domain
  application
    port.in
    port.out
    service
  infrastructure
    in.web
    out.persistence
```

## Alternatives considered

- **Ports location**: keeping the ports inside `domain` was considered,
  closer to the Android Clean Architecture convention the author is used to
  (where repository interfaces live in the domain layer). Discarded in
  favor of keeping ports in `application`, which leaves `domain` limited to
  pure business rules and matches a convention more specifically associated
  with Spring Boot hexagonal architecture literature.
- **Adapter grouping**: grouping `infrastructure` packages by technology
  only (`infrastructure.web`, `infrastructure.persistence`), without an
  explicit `in`/`out` split, was considered. Discarded because some
  technologies (e.g. messaging, planned for a later milestone) have both
  inbound and outbound adapters, which would otherwise live ambiguously in
  the same package.

## Consequences

- Business logic (`domain` + `application`) can be unit-tested without a
  running Spring context, a database, or any framework dependency — use
  cases are tested by mocking `port.out` interfaces directly.
- Infrastructure adapters are swappable without touching business logic:
  the in-memory `UserRepository` adapter planned for this milestone can be
  replaced by a JPA adapter in Milestone 2 with no change to
  `domain`/`application`.
- The convention adds structural overhead — even a single, simple use case
  requires several files spread across three packages instead of one. This
  is accepted as the cost of the learning goal and of staying consistent as
  more bounded contexts are added.
- Because the convention differs from the domain/data/presentation layering
  the author is used to on Android, some initial friction is expected until
  the new vocabulary (ports, adapters, `in`/`out`) becomes second nature.
