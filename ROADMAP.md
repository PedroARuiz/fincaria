# Fincaria — Backend Engineering Roadmap (Kotlin + Spring Boot)

## Purpose

This roadmap is a self-directed, course-like path to transition from an
Android/Kotlin background (9 years of experience) into backend development
with Kotlin + Spring Boot, up to a **mid/senior production-grade level**.
It is applied hands-on by building `fincaria`, a backend for registering
and managing agricultural land/farms in Spain, growing from a minimal
vertical slice into a service that reflects a real business environment
(persistence, security, observability, resilience, messaging,
containerization, cloud deployment).

**Working agreement:** every line of code and every repository decision in
this project is written and owned by the developer. The accompanying
mentor/reviewer role is to clarify concepts, help weigh decisions, answer
doubts, and review/suggest improvements on work already produced — not to
implement milestones directly.

No calendar pacing is assigned (no weeks/hours per milestone) — progress is
tracked purely by milestone and its dependencies.

## Roadmap overview

| # | Milestone | Focus | Status |
|---|-----------|-------|--------|
| 0 | Environment & project baseline | Tooling, repo hygiene, domain framing | ✅ Done |
| 1 | Spring Boot & hexagonal foundations | DI, ports & adapters, first vertical slice | ✅ Done |
| 2 | Persistence & data modeling | Relational modeling, JPA, migrations | ✅ Done |
| 3 | Testing strategy | Unit, integration, test pyramid, coverage | ✅ Done |
| 4 | API design & documentation | REST maturity, OpenAPI, pagination/versioning | 🚧 In progress |
| 5 | Security | AuthN/AuthZ, Spring Security, JWT/OAuth2 | ⬜ Not started |
| 6 | Observability & resilience | Logging, metrics, tracing, circuit breakers | ⬜ Not started |
| 7 | Caching & performance | Redis, query optimization, load testing | ⬜ Not started |
| 8 | Messaging & event-driven architecture | Kafka/RabbitMQ, domain events, outbox | ⬜ Not started |
| 9 | Containerization & CI/CD | Docker, pipelines, quality gates | ⬜ Not started |
| 10 | Cloud deployment & infrastructure | IaC, managed services, environments | ⬜ Not started |
| 11 | Microservices & advanced architecture | Service boundaries, DDD, sagas | ⬜ Not started |
| 12 | Professional polish & job readiness | System design, portfolio, interview prep | ⬜ Not started |

Target stack (adjust as needed while learning): Kotlin, Spring Boot,
PostgreSQL/PostGIS, Flyway, Testcontainers, MockK, ktlint, detekt, GitHub
Actions, Docker, Redis, Kafka or RabbitMQ.

---

## Milestone 0 — Environment & project baseline ✅ Done

**Completed:** git repo created and synced with `origin/main`; `README.md`
with the product vision; ADR-0001 documenting the foundational stack;
empty Gradle/Kotlin skeleton (`backend` module, JDK 21 toolchain,
`com.fincaria` package), verified with `./gradlew build` and
`./gradlew run`.

**Concepts:** JDK/toolchain management, Gradle fundamentals (contrasted
with Android/Gradle knowledge already held), IntelliJ for backend work, git
repo hygiene, ADR (Architecture Decision Record) practice.

**Build:**
- New git repo at the project root, `main` branch.
- `README.md` describing the product vision (land/farm registry → crop
  history → agronomic assistant → marketplace).
- `docs/adr/0001-*.md` — first ADR documenting the decision to start from
  scratch and the target stack.
- Empty Gradle Kotlin project skeleton (no Spring yet): practice
  `settings.gradle.kts` / `build.gradle.kts` basics, version catalogs
  (`libs.versions.toml`), and the Gradle wrapper.

**Definition of done:** repo pushed, README + first ADR readable, project
builds with `./gradlew build` with zero source files.

---

## Milestone 1 — Spring Boot & hexagonal foundations ✅ Done

**Completed:** Spring Boot wired in (web/validation/actuator starters, BOM via
`platform()`); ADR-0002 (hexagonal layering convention per bounded context)
and ADR-0003 (Spring wiring strategy for use cases/adapters); the `users`
bounded context (`domain`/`application`/`infrastructure`) with
`CreateUserUseCase`, an in-memory `UserRepository` adapter, and the
email-uniqueness business rule; `UserController` with request/response
DTOs and Bean Validation; a shared `GlobalExceptionHandler` mapping
validation and domain errors to HTTP status codes; an end-to-end
integration test (`MockMvcTester`) covering the success, validation-error
and conflict paths.

**Concepts:** Inversion of Control / Spring's `ApplicationContext`, beans
and bean scopes, auto-configuration, `@ConfigurationProperties`, profiles
(`application-{profile}.yml`), hexagonal/ports-and-adapters architecture
applied to a backend service, DTO vs. domain model separation, Bean
Validation, global exception handling.

**Build:**
- Add Spring Boot (`spring-boot-starter-web`, `-validation`, `-actuator`).
- First bounded context: `users` — `User` domain entity, `CreateUserUseCase`
  port + service, in-memory repository adapter (no DB yet, to isolate the
  architecture concept from persistence).
- `UserController` REST endpoint, request/response DTOs, `@Valid`.
- `@RestControllerAdvice` exception handler mapping domain exceptions → HTTP
  status codes.
- ADR documenting the hexagonal layering convention chosen (domain /
  application / infrastructure per bounded context).

**Definition of done:** `POST /api/users` works end-to-end against an
in-memory adapter, with a passing integration test (MockMvc/WebTestClient).

---

## Milestone 2 — Persistence & data modeling ✅ Done

**Completed:** `docker-compose.yml` with Postgres/PostGIS for local dev;
Flyway migrations for `users` and `farms`; `users` re-pointed from the
in-memory adapter to a real JPA adapter (`UserJpaEntity`,
`UserJpaRepository`); second bounded context `farms` (`Farm`,
`CadastralReference` value object with full format and checksum
validation, `CropType` enum, `CreateFarmUseCase`, `GetUserFarmsUseCase`,
JPA entity/repository/adapter, `FarmController`); `PostgreSQLTestContainer`
wiring Testcontainers via a single container shared across test classes
(chosen over one-per-class for faster test runs); integration tests for
both `UserController` and `FarmController`.

**Concepts:** relational modeling & normalization, SQL fundamentals, Spring
Data JPA / Hibernate mapping (entities, relationships, value objects via
`@Embeddable`/`@JvmInline` wrappers), transactions & isolation levels,
schema migrations (Flyway), geospatial data (PostGIS) for the
cadastral-reference/geometry concept, Testcontainers for realistic DB
tests.

**Build:**
- `docker-compose.yml` with PostgreSQL + PostGIS for local dev.
- Flyway migrations: `users`, `farms` tables.
- Second bounded context: `farms` — `Farm`, `CadastralReference` value
  object, `CropType` enum; `CreateFarmUseCase`, `GetUserFarmsUseCase`; JPA
  entities + Spring Data repositories + adapters implementing the domain
  repository ports.
- Replace the in-memory `users` adapter from Milestone 1 with a real JPA
  adapter.
- `AbstractIntegrationTest` base class wiring Testcontainers correctly (one
  container per test class, not shared via `companion object`).

**Definition of done:** farms can be created and listed per user against a
real Postgres/PostGIS container in tests and in local docker-compose.

---

## Milestone 3 — Testing strategy ✅ Done

**Completed:** unit tests with MockK for every use case service
(`CreateUserServiceTest`, `CreateFarmServiceTest`, `GetUserFarmsServiceTest`);
integration tests per controller covering success and domain error paths
(`UserControllerTest`, `FarmControllerTest`: 201/200, 400 validation, 409
conflict, and a data-integrity-violation 400 case); JaCoCo wired into the
Gradle build with an 85% minimum threshold enforced locally via
`./gradlew check` (CI enforcement deferred to Milestone 9); the
"a farm must have at least one crop type" rule built test-first
(`FarmTest`, `Farm.cropTypes: Set<CropType>` non-empty invariant), cascaded
through the command/service/persistence (`farm_crop_types` table)/DTOs/
controller layers. Also fixed a real Testcontainers bug found along the
way: `PostgreSQLTestContainer`'s shared container was being stopped and
recreated between test classes because of `@Container`'s per-class
lifecycle; it's now a true JVM-wide singleton started once in the
companion object's `init` block, which also surfaced and let us drop the
unnecessary PostGIS/amd64-emulation dependency in favor of plain
`postgres` (no migration uses geometry yet).

**Concepts:** test pyramid (unit vs. integration vs. end-to-end), JUnit 5,
MockK for idiomatic Kotlin mocking, `MockMvc`/`WebTestClient` for
controller tests, Testcontainers-backed integration tests, code coverage
(JaCoCo), mutation testing as an optional stretch (PIT), basics of TDD.

**Build:**
- Backfill/strengthen unit tests for every use case service (mock the
  outbound ports with MockK).
- Integration tests per controller, covering success + domain error paths
  (404/409/400 mapping).
- JaCoCo report wired into the Gradle build with a sensible minimum
  threshold enforced in CI.
- One feature (e.g., "a farm must have at least one crop type") built
  test-first to practice TDD explicitly.

**Definition of done:** `./gradlew test jacocoTestReport` runs clean with a
visible coverage report; CI fails the build below the threshold.

---

## Milestone 4 — API design & documentation 🚧 In progress

**Concepts:** REST resource design, pagination/filtering/sorting,
idempotency, API versioning strategies, HTTP semantics (status codes,
caching headers), OpenAPI/Swagger generation, contract-first vs.
code-first tradeoffs.

**Build:**
- Add `springdoc-openapi` to auto-generate and serve an OpenAPI spec +
  Swagger UI.
- Add pagination/sorting to `GET /api/users/{userId}/farms`.
- Introduce a second resource that needs filtering (e.g., farms by crop
  type) to practice query-parameter design.
- Decide and document (ADR) an API versioning strategy (e.g., URI
  versioning `/api/v1/...`) and apply it consistently.

**Definition of done:** Swagger UI reflects every endpoint accurately;
pagination/filtering is covered by integration tests.

---

## Milestone 5 — Security

**Concepts:** authentication vs. authorization, session-based vs.
token-based auth, Spring Security filter chain, password hashing, JWT
structure & validation, OAuth2/OIDC concepts, method-level authorization
(`@PreAuthorize`), OWASP Top 10 basics, secrets management (never commit
credentials).

**Build:**
- Add Spring Security; replace the open "create user" flow with real
  registration + login (hashed passwords).
- Issue and validate JWTs for authenticated requests.
- Authorization rule: a user can only see/manage **their own** farms.
- Optional stretch: integrate a real OIDC provider (Keycloak via
  docker-compose) instead of hand-rolled JWT issuance, to learn the
  industry-standard flow.

**Definition of done:** unauthenticated/forbidden requests are correctly
rejected (401/403) in integration tests; credentials are never logged or
committed.

---

## Milestone 6 — Observability & resilience

**Concepts:** structured logging & correlation/trace IDs, metrics
(Micrometer → Prometheus), distributed tracing (OpenTelemetry), Actuator
beyond health checks, resilience patterns (retries, timeouts, circuit
breakers via Resilience4j), graceful degradation.

**Build:**
- Structured JSON logging with a request/correlation ID propagated through
  the call stack.
- Micrometer + Prometheus endpoint exposed via Actuator; a minimal Grafana
  dashboard (docker-compose) visualizing request latency/error rate.
- Introduce one outbound dependency that can fail (e.g., a mocked
  "cadastral lookup" external API client) and wrap it with Resilience4j
  retry + circuit breaker, with tests simulating failures.

**Definition of done:** a simulated downstream failure degrades gracefully
(circuit opens, fallback returned) and is visible in logs/metrics.

---

## Milestone 7 — Caching & performance

**Concepts:** caching strategies & invalidation, Spring Cache abstraction,
Redis, N+1 query problems, database indexing, query plans, load/performance
testing.

**Build:**
- Add Redis via docker-compose; cache a read-heavy, rarely-changing
  endpoint (e.g., crop type catalog) with Spring Cache.
- Identify and fix at least one N+1 query using JPA fetch strategies or
  projections; show the before/after query count in a test or log.
- Run a basic load test (k6 or Gatling) against the farms listing endpoint
  and record baseline latency/throughput.

**Definition of done:** cache hit/miss is observable (metrics/logs); a
documented before/after performance comparison exists.

---

## Milestone 8 — Messaging & event-driven architecture

**Concepts:** synchronous vs. asynchronous communication, message brokers
(Kafka or RabbitMQ), domain events, eventual consistency, the outbox
pattern (avoiding dual-write problems), idempotent consumers.

**Build:**
- Add a broker via docker-compose (pick one: Kafka or RabbitMQ).
- Emit a domain event when a farm is created (`FarmRegistered`), published
  through an outbox table + relay rather than a direct dual write.
- Build one consumer reacting to that event (e.g., a "crop history"
  bounded context that initializes an empty production log when a farm is
  registered) — the first step toward a crop/production history feature.

**Definition of done:** creating a farm reliably results in a downstream
side effect via the broker, verified with an integration test using
Testcontainers for the broker.

---

## Milestone 9 — Containerization & CI/CD

**Concepts:** Docker fundamentals, multi-stage builds, image size/layer
caching, docker-compose for a full local stack, CI/CD pipeline design,
quality gates (lint/static analysis/tests/coverage/security scanning),
semantic versioning & release strategy.

**Build:**
- `Dockerfile` for the Spring Boot app (multi-stage: build with Gradle, run
  on a slim JRE image).
- Extend `docker-compose.yml` to run the full stack (app + Postgres + Redis
  + broker) with one command.
- GitHub Actions pipeline covering: ktlint, detekt, tests + coverage,
  dependency vulnerability scanning (e.g., OWASP Dependency-Check or
  `gradle-versions-plugin` + GitHub Dependabot), Docker image build, and
  (on tags) image publish to a registry (GHCR).

**Definition of done:** `docker compose up` runs the entire stack locally;
CI pipeline is green end-to-end and publishes a versioned image on tag
push.

---

## Milestone 10 — Cloud deployment & infrastructure

**Concepts:** managed database services, environment separation
(dev/staging/prod), secrets management in the cloud, Infrastructure as Code
basics (Terraform), basic Kubernetes concepts (or a simpler managed
container platform as a pragmatic alternative).

**Build:**
- Deploy the app to a real environment (pick a pragmatic target: a managed
  container platform such as Fly.io/Render, or basic Kubernetes if the
  broader learning goal justifies the extra complexity) backed by a managed
  Postgres (e.g., Neon/Supabase/RDS free tier).
- Externalize configuration/secrets via environment variables or a secrets
  manager — never hardcoded.
- Minimal Terraform (or equivalent) describing the managed database and
  deployment target, even if small, to practice IaC fundamentals.

**Definition of done:** the app is reachable over the internet, backed by a
managed database, with secrets never committed to the repo.

---

## Milestone 11 — Microservices & advanced architecture (stretch)

**Concepts:** when (and when not) to split a service, bounded-context
boundaries revisited with Domain-Driven Design vocabulary, API gateway /
BFF patterns, distributed transactions and the Saga pattern, multi-module
Gradle builds.

**Build:**
- Extract one bounded context (e.g., crop/production history or a future
  agronomic assistant) into its own deployable service communicating with
  the main app via the broker from Milestone 8, as a deliberate, documented
  (ADR) exercise in service decomposition — not because the domain strictly
  requires it yet.

**Definition of done:** two independently deployable services collaborate
correctly through async messaging, each with its own CI pipeline.

---

## Milestone 12 — Professional polish & job readiness

**Concepts:** system design interview patterns, code review etiquette,
trunk-based development practices, writing a portfolio-grade README,
architecture diagrams, recording a demo, preparing for backend-focused
technical interviews.

**Build:**
- Polished `README.md` with architecture diagram(s), setup instructions,
  and a short demo (GIF/video).
- A written case study (could live in `docs/`) walking through 2-3 of the
  hardest technical decisions made across the roadmap (e.g., the outbox
  pattern, the security model, the caching strategy) — this becomes
  interview material.
- Mock system-design exercise using fincaria itself as the subject (e.g.,
  "how would you scale farm search to 10M farms?").

**Definition of done:** the repo is presentable as a portfolio piece; every
architectural decision can be justified from first principles, not just
"because the roadmap said so".
