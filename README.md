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

Fincaria is in early development. The repository has just been set up; no application code exists yet. See [ROADMAP.md](./ROADMAP.md) for the current milestone and what's planned next.

## Tech stack

This is the target stack for the project. Not everything is implemented yet — see [ROADMAP.md](./ROADMAP.md) for the current status of each milestone.

- **Language & framework:** Kotlin, Spring Boot
- **Persistence:** PostgreSQL, PostGIS, Spring Data JPA / Hibernate, Flyway
- **Testing:** JUnit 5, MockK, Testcontainers, JaCoCo
- **API:** springdoc-openapi (OpenAPI/Swagger)
- **Security:** Spring Security, JWT, OAuth2/OIDC (Keycloak)
- **Observability & resilience:** Micrometer, Prometheus, Grafana, OpenTelemetry, Resilience4j
- **Caching:** Redis
- **Messaging:** Kafka or RabbitMQ
- **Containerization & CI/CD:** Docker, Docker Compose, GitHub Actions, GHCR
- **Infrastructure:** Terraform, Kubernetes / Fly.io / Render
- **Code quality:** ktlint, detekt
