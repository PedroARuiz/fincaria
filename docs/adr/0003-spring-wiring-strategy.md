# 0003. Spring wiring strategy for use cases and adapters

## Status
Accepted

## Context

Milestone 1 introduced the first use case (`CreateUserUseCase`) and its
adapters, following the layering convention from ADR-0002. ADR-0002
established that `domain` has no framework dependency, but left open
whether `application` (use-case services) and `infrastructure` (adapters)
wire into Spring via stereotype annotations or via explicit, framework-free
configuration.

## Decision

`application` services and `infrastructure` adapters are wired into Spring
directly through its own stereotype annotations (`@Service`,
`@Repository`/`@Component`), relying on component scanning — rather than
keeping them annotation-free and wiring them manually through
`@Configuration` classes with `@Bean` factory methods.

## Alternatives considered

- **Framework-free application layer with manual `@Bean` wiring** (the
  approach recommended in Tom Hombergs' *Get Your Hands Dirty on Clean
  Architecture*, referenced in ADR-0002): keeps `application` free of any
  Spring import, at the cost of an extra `@Configuration` class with a
  `@Bean` method per use case. Discarded for now to keep the ceremony per
  use case lower while learning the pattern; the pragmatic approach is also
  what the large majority of real Spring Boot codebases do in practice.

## Consequences

- `application` and `infrastructure` gain a light compile-time dependency
  on Spring annotations, in exchange for zero manual wiring per use case —
  Spring's component scan discovers and constructs the beans automatically.
- `domain` remains the only layer with zero framework dependency, as
  already stated in ADR-0002.
- If a stricter framework-free `application` layer becomes valuable later
  (e.g. when extracting a bounded context into its own service in
  Milestone 11), this decision can be revisited with a new ADR.
