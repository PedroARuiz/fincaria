# 0007. Messaging broker: Kafka over RabbitMQ

## Status
Accepted

## Context

Milestone 8 introduces asynchronous communication and an event-driven
architecture: a domain event is emitted when a farm is registered and a
downstream consumer reacts to it. This requires choosing a message broker,
and the roadmap explicitly frames it as a pick between Kafka and RabbitMQ.

The choice shapes the rest of the milestone (outbox table + relay +
consumer) and the path toward extracting a bounded context into its own
deployable service in Milestone 11. It also matters beyond the immediate
technical case: this project doubles as a portfolio piece, so the broker's
relevance to backend hiring is a deliberate input to the decision.

## Decision

Apache Kafka, running in **KRaft** mode (no ZooKeeper):

- Kafka's model is a **persistent, immutable, partitioned event log**
  rather than a transient delivery queue. Consuming does not remove events;
  each consumer tracks its own offset and can replay the log. This fits
  bounded contexts that evolve into independent services, and it fits the
  future "crop history" feature, which is intrinsically a time-ordered
  sequence of events — a natural event log.
- **KRaft** is chosen over the legacy ZooKeeper topology to keep the local
  docker-compose stack and the Testcontainers-backed integration tests
  lighter and simpler to start.
- The decision is aligned with the project's portfolio/employability goal:
  Kafka is the de-facto standard for event streaming and appears far more
  often in backend job postings than RabbitMQ.

## Alternatives considered

- **RabbitMQ**: simpler to stand up and sufficient for today's technical
  case (one event, one consumer). Its traditional broker model (smart
  broker routing transient messages that are deleted once acknowledged)
  does not fit the reproducible event log or the project's event-driven
  direction as well, and it carries less learning/CV value for this
  milestone. Not ruled out for genuine task-queue / work-distribution use
  cases, but not the right fit here.
- **No broker (database trigger or direct call)**: discussed before this
  ADR. A trigger would work for the specific case of inserting an empty
  downstream row atomically, but it breaks bounded-context isolation
  (one context reaching into another's tables beneath the architecture),
  cannot cross the network/database boundary required for the Milestone 11
  service split, and re-couples the side effect to the farm-creation
  transaction's latency and failure. Rejected.

## Consequences

- A steeper learning curve and more operational weight (partitions,
  offsets, consumer groups) than a traditional queue — accepted
  deliberately as part of the learning goal of this milestone.
- The broker and consumption pattern established here become reusable
  infrastructure for the Milestone 11 service decomposition.
- The event's serialization format (e.g. JSON vs. Avro + Schema Registry)
  and the consumer's idempotency strategy are decided in later steps of
  this milestone, not in this ADR.
