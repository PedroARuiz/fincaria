# 0008. Event serialization: plain JSON with additive versioning

## Status
Accepted

## Context

Milestone 8 publishes domain events (`FarmRegistered`) to Kafka through the
transactional outbox. This requires fixing a serialization format for the
event payload and a strategy for evolving that contract over time.

The payload already travels as JSON: the outbox step serializes an explicit,
primitive-typed representation into the `payload` column and the relay
publishes it unchanged. This ADR makes that choice deliberate, weighing it
against the industrial-standard alternative (Avro backed by a Schema Registry)
and, more importantly, writing down how the event contract is allowed to
evolve.

## Decision

Plain JSON, with a disciplined versioning rule:

- The payload has an **explicit representation in the adapter**, decoupled from
  the domain class, so the wire contract never changes by accident when a
  domain field is renamed. The event type travels in a Kafka header and the
  outbox `event_type` column, not only inside the body.
- **Versioning rule: additive, backward- and forward-compatible changes only.**
  New fields must be optional / have a default; existing fields are never
  renamed, removed, or retyped. An old consumer ignores unknown new fields; a
  new consumer tolerates the absence of fields it expects.
- An unavoidable **incompatible** change is published as a new versioned event
  type (e.g. `FarmRegistered.v2`), with v1 and v2 coexisting until every
  consumer has migrated.
- A **contract test** pins the JSON shape of each event, so an incompatible
  change fails in CI — the lightweight equivalent of what a Schema Registry
  would enforce.

## Alternatives considered

- **Avro + Schema Registry**: the industrial standard for serious Kafka
  estates. The registry validates compatibility when a new schema version is
  registered (turning a contract break into a registration/deploy failure
  instead of a runtime one), and the binary encoding is compact. Rejected for
  now: it adds a Schema Registry to the stack (Apache Kafka 4.x in KRaft mode
  bundles none, so it would mean running Confluent's or another separately),
  the payload stops being human-readable, and it couples the project to the
  Confluent ecosystem. Its core value — policing contracts across
  independently-deployed services and teams — does not exist yet in a
  single-deployment monolith. Worth revisiting when a bounded context is
  extracted into its own service (Milestone 11).
- **JSON Schema or Protobuf, each with a registry**: same fundamental
  trade-off — schema validation in exchange for extra infrastructure.

## Consequences

- Contract compatibility is guaranteed by discipline (pull-request review)
  plus the contract test, not by an external registry. This is an accepted,
  explicit trade-off for a single-deployment codebase.
- The `event_type` header/column is already shaped to carry a version suffix,
  so introducing `FarmRegistered.v2` later needs no structural change.
- The migration path to a registry stays open: the move would be to formalize
  the already-explicit payload as a registered schema, not to rework how
  events are produced or consumed.
