# Fincaria

Fincaria helps individual landowners register their agricultural plots by official cadastral reference and manage their crops with AI-powered agronomic guidance.

## Motivation

Families who own multiple agricultural farms often lack a centralized, reliable record of what they actually own — where each farm is located, its exact boundaries, and its official cadastral data. As a result, even basic questions such as which farms the family owns or how to get to them can be hard to answer.

Fincaria addresses this by letting families register each farm under its official cadastral reference, along with its exact geometry and location, giving them a single, reliable source of truth for their land. Beyond registration, the long-term vision is to keep a history of crops grown on each farm over time and use it — together with an AI-powered agronomist — to help forecast future harvests. **This part of the product is not implemented yet.**

## Vision

Beyond registering and locating farms, Fincaria is envisioned to grow along the following lines:

- **Crop history** — keeping a record of what has been grown on each farm over time.
- **Shared farms** — allowing several members of the same family to access and manage the same farms together, instead of each farm belonging to a single user.
- **AI-powered agronomist** — using that history, together with other data sources, to help plan and forecast future harvests.
- **Marketplace** — enabling farms to be listed for sale or lease within the platform.

None of these have been implemented yet; they describe the long-term direction of the project.

## Project status

Fincaria is in early development, built incrementally as a self-directed path into backend development. Milestones 0–6 are complete: a hexagonal Spring Boot service with `users`, `farms`, and `auth` bounded contexts, PostgreSQL persistence via Flyway, a full test suite (unit + integration, with an enforced coverage threshold), a versioned, documented REST API, JWT-based authentication protecting every endpoint except registration and login, and an observability & resilience layer (structured JSON logging, OpenTelemetry trace propagation, Prometheus metrics with Grafana dashboards, and a Resilience4j retry + circuit breaker around a simulated external dependency that degrades gracefully). See [ROADMAP.md](./ROADMAP.md) for the current milestone and what's planned next.

## Running locally

1. Start the local Postgres database: `docker compose up -d`.
2. Run the app: `./gradlew bootRun`.
3. The API is served at `http://localhost:8080/api/v1/...`; interactive
   docs are at `http://localhost:8080/swagger-ui/index.html` (raw OpenAPI
   spec at `/v3/api-docs`).

Run the test suite with `./gradlew check` (unit tests, Testcontainers-backed
integration tests, and a JaCoCo coverage check).

## Tech stack

### Implemented

- **Language & framework:** Kotlin, Spring Boot
- **Persistence:** PostgreSQL, Spring Data JPA / Hibernate, Flyway
- **Testing:** JUnit 5, MockK, Testcontainers, JaCoCo
- **API:** springdoc-openapi (OpenAPI/Swagger), URI versioning
- **Security:** Spring Security, JWT (jjwt), BCrypt password hashing

### Planned

Each item maps to a milestone in [ROADMAP.md](./ROADMAP.md):

- **Geospatial data:** PostGIS, once a migration actually needs geometry (follow-up to Milestone 2)
- **Observability & resilience:** Micrometer, Prometheus, Grafana, OpenTelemetry, Resilience4j — Milestone 6
- **Caching:** Redis — Milestone 7
- **Messaging:** Kafka or RabbitMQ — Milestone 8
- **Containerization & CI/CD:** Docker, GitHub Actions, GHCR, ktlint, detekt — Milestone 9
- **Infrastructure:** Terraform, Kubernetes / Fly.io / Render — Milestone 10
