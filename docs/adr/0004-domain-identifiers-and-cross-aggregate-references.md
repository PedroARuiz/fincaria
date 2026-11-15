# 0004. Domain entity identifiers and cross-aggregate references

## Status
Accepted

## Context

Milestone 2 introduced real persistence for `users` and a second bounded
context, `farms`. Two related questions came up:

- `User.id`/`Farm.id` are generated as random UUIDs, and the database
  column chosen for them is Postgres' native `uuid` type. This raised the
  question of whether the domain entities should type `id` as
  `java.util.UUID` (matching the column 1:1) or keep it as `String`.
- `Farm` needs to record which `User` owns it. This raised the question of
  whether `Farm` should hold a reference to the full `User` domain object
  (and, at the persistence level, a `@ManyToOne` JPA association to
  `UserJpaEntity`), or only to its identifier.

## Decision

- Domain entity ids (`User.id`, `Farm.id`) are typed as `String` in the
  domain layer, regardless of what type the underlying persistence column
  or JPA entity uses. The conversion between the domain's `String` and the
  adapter's native type (`UUID.fromString(...)`/`.toString()`) happens
  exclusively inside the persistence adapter (`UserRepositoryImpl`,
  `FarmRepositoryImpl`).
- `Farm` references its owning `User` solely by id (`userId: String`),
  never by holding a `User` object reference. This applies both to the
  domain model and to the JPA mapping: `FarmJpaEntity.userId` is a plain
  `UUID` column, with no `@ManyToOne`/`@JoinColumn` association. Referential
  integrity between `farms.user_id` and `users.id` is enforced at the
  database level, via a `FOREIGN KEY` constraint in the Flyway migration.

## Alternatives considered

- **Typing `id` as `java.util.UUID` in the domain**, mirroring the database
  column. Discarded because it would leak a persistence-level
  representation decision into the domain layer, conflicting with the
  domain/infrastructure separation already established in ADR-0002.
- **Modeling `Farm.user: User` as a direct object reference**, mirrored as
  a `@ManyToOne` association in `FarmJpaEntity`. Discarded because it would
  couple `farms`' domain and persistence packages to `users`' internal
  types, breaking the per-bounded-context independence from ADR-0002, and
  because navigable object graphs between aggregates introduce
  fetch-strategy/N+1 concerns better addressed deliberately, if ever, in a
  later milestone (N+1 queries are explicitly in scope for Milestone 7).

## Consequences

- Adapters are free to choose their own internal representation for an id
  without ever touching the domain model; conversion is explicit and
  localized to each persistence adapter.
- Cross-bounded-context dependencies stay minimal and one-directional:
  `farms` knows a `userId` exists, but nothing about `users`' domain or
  persistence code.
- Referential integrity between `farms` and `users` is enforced exclusively
  by the database, independent of whether any application code path
  remembered to check it.
- As more bounded contexts are added with relationships to existing ones,
  the same pattern (reference by id only, FK enforced at the database) is
  expected to apply unless a future ADR revisits it.
