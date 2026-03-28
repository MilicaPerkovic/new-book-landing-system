# READMETHIRDMS - Task 4 Plan (Microservice 3)

## 1. Goal
Deliver the **third microservice** for the Book Landing System in a **new technology stack** that showcases:
- Reactive programming model end-to-end
- Message-broker integration (ActiveMQ Artemis)
- Persistent storage for its own data
- Structured logging + metrics hooks
- Automated tests (unit + reactive integration)
- Docker packaging and GitHub Actions workflow (tests on `push`)
- OpenAPI/Swagger exposure for REST consumers

## 2. Selected Microservice: `order-service`
**Business responsibility**
- Accept preorder requests for books
- Track lifecycle (`PENDING`, `CONFIRMED`, `CANCELLED`, `FULFILLED`)
- Emit domain events (`order.placed`, `order.status-changed`) toward Notification/Analytics services
- Read book snapshots from Book Service (via REST) if needed for validation (future stretch)

This choice ties directly to course requirements because order events naturally flow through a broker and trigger downstream processing.

## 3. Technology & Rationale
| Concern | Choice | Why |
| --- | --- | --- |
| Framework | **Quarkus 3 (Mutiny)** | Reactive-first programming model, different technology than ASP.NET + Spring Boot |
| Language | Java 21 (can add Kotlin extensions later) | Native support in Quarkus, easy on-boarding |
| API | REST via `quarkus-rest` + `SmallRye OpenAPI` | Generates Swagger UI automatically |
| Persistence | PostgreSQL 15 via **Hibernate Reactive + Panache** | Keeps relational consistency, fully non-blocking |
| Messaging | **ActiveMQ Artemis** via `SmallRye Reactive Messaging` (`outgoing` channel) | Required broker, easy to run in Docker |
| Validation | `jakarta.validation` | Declarative input checks |
| Logging | JSON logs via `quarkus-logging-json` + MDC request-id | Structured logs for grading |
| Testing | JUnit 5 + REST Assured + `quarkus-test-h2` (or `Db2`), plus reactive messaging tests | Covers business rules + transport |
| CI | GitHub Actions workflow `order-service-ci.yml` | Runs `mvnw test` inside service |

## 4. Functional Scope (MVP)
### Must have
1. Create preorder for a published book
2. Fetch order by id
3. List orders filtered by `bookId` or `userId`
4. Update status (`confirm`, `cancel`, `fulfill`)
5. Emit `order.placed` event to ActiveMQ topic `order.events`

### Should have
1. Emit `order.status-changed` events
2. Basic rate limiting (Quarkus `io.quarkus.vertx.http`) or guard rails on endpoints
3. Optimistic concurrency/versioning on order documents

### Nice to have (stretch)
1. Subscribe to `book.published` events to warm cache/reference data
2. Saga-style compensation hooks
3. GraphQL projection endpoint

## 5. Data Model (PostgreSQL schema)
| Column | Type | Notes |
| --- | --- | --- |
| id | UUID (PK) | Generated in service |
| book_id | UUID | Book identifier (foreign key reference by contract only) |
| user_id | UUID | Customer placing preorder |
| quantity | INT | Defaults to 1 |
| price_snapshot | NUMERIC(10,2) | Price captured at order time |
| status | TEXT | Enum: `PENDING`, `CONFIRMED`, `CANCELLED`, `FULFILLED` |
| created_at | TIMESTAMP WITH TZ | Auto-set |
| updated_at | TIMESTAMP WITH TZ | Auto-set |
| version | INT | For optimistic locking |

`Flyway` migration `V1__create_orders_table.sql` will provision this schema.

## 6. REST + OpenAPI Contract (initial)
| Method | Path | Description |
| --- | --- | --- |
| POST | `/api/orders` | Create preorder (returns 202 + order payload) |
| GET | `/api/orders/{id}` | Fetch by id |
| GET | `/api/orders` | Filter by `bookId`, `userId`, `status` |
| POST | `/api/orders/{id}/confirm` | Transition to CONFIRMED |
| POST | `/api/orders/{id}/cancel` | Transition to CANCELLED |
| POST | `/api/orders/{id}/fulfill` | Transition to FULFILLED |
| GET | `/api/orders/{id}/events` (optional) | Stream emitted events for debugging |

Swagger UI automatically available at `/q/swagger-ui` once `smallrye-openapi` extension is installed.

## 7. Messaging Topology
- **Outgoing**: `order.placed` (payload: order id, book id, price, timestamp)
- **Outgoing**: `order.status-changed` (payload: id, old status, new status)
- **Protocol**: JMS (ActiveMQ Artemis) using SmallRye Reactive Messaging (`@Outgoing("order-events")`)
- **Delivery guarantee**: at-least-once (broker-managed, explicit `ack`) with idempotent consumers recommended
- **Testing**: Use `@QuarkusTest` + embedded Artemis or Testcontainers to assert message emission

## 8. Non-Functional Requirements Checklist
- Reactive HTTP stack (no blocking drivers)
- Proper request/trace logging with correlation id
- Health endpoints (`/q/health`, `/q/health/live`, `/q/health/ready`)
- Config profiles: `dev`, `test`, `prod`
- Secrets/config via `.env` + Quarkus `application-prod.properties`
- Container image based on `quay.io/quarkus/quarkus-distroless-image:2.0`

## 9. Phase Plan ("Parts")
| Part | Focus | Deliverables | Done Criteria |
| --- | --- | --- | --- |
| **Part 0** | Project scaffold | Quarkus app skeleton, modules, Maven wrapper | `mvnw quarkus:dev` starts, health endpoint OK |
| **Part 1** | Domain + Persistence | Order entity, Panache repository, Flyway migration, reactive datasource config | Save/find order works in unit test |
| **Part 2** | Application services | Use cases for create/list/status-change, validation rules, custom exceptions | Unit tests cover business rules + optimistic locking |
| **Part 3** | REST API + OpenAPI | Resource classes (Mutiny), DTOs, mapping, OpenAPI annotations | REST endpoints callable, swagger available |
| **Part 4** | Messaging | Configure ActiveMQ channel, produce events, add integration test with Testcontainers Artemis | `order.placed` shows in broker logs/tests |
| **Part 5** | Logging & Observability | JSON logging, request-id filter, metrics (`micrometer-registry-prometheus`) | Logs structured, `/q/metrics` exposes counters |
| **Part 6** | Testing suite | Unit + repository + REST + messaging tests, coverage config | `mvnw test` green locally |
| **Part 7** | Docker & Compose | Multi-stage Dockerfile, local compose with Postgres + Artemis + service | `docker compose up order-service` works |
| **Part 8** | GitHub Actions | `.github/workflows/order-service-ci.yml`, caching, test artifact | Pipeline runs on push, fails on red tests |
| **Part 9** | Documentation | `services/order-service/README.md`, runbook, API samples, checklist vs assignment | Reviewer can follow README to run/test |

## 10. Timeline (1-week sprint)
1. **Day 1-2**: Parts 0-2 (scaffold, domain, persistence)
2. **Day 3**: Part 3 (REST) + begin Part 4 (messaging)
3. **Day 4**: Finish Part 4, start Part 5
4. **Day 5**: Part 6 (testing)
5. **Day 6**: Part 7 (Docker) + Part 8 (CI)
6. **Day 7**: Part 9 (docs) + buffer + demo review

## 11. Definition of Done
Task 4 is complete when all statements hold:
1. `order-service` runs on Quarkus, fully reactive stack, different tech than first two services.
2. PostgreSQL schema owns its data; Flyway migration executed on startup.
3. ActiveMQ Artemis receives emitted events for order lifecycle actions.
4. Structured logs show meaningful context (request-id, duration, user/book ids).
5. Test suite (unit + integration + messaging) runs via `mvnw test` and in CI workflow.
6. Docker image builds locally, container passes health checks, Compose file wires DB + broker + service.
7. OpenAPI docs accessible, README explains how to run/test, GitHub Actions workflow green on push.

## 12. Next Step
Start **Part 0**: scaffold Quarkus reactive project, add essential extensions (`q rest`, `hibernate-reactive-panache`, `smallrye-openapi`, `smallrye-reactive-messaging-activemq`, `flyway`, `jdbc-postgresql` for migration). Once dev mode runs, proceed to Part 1.

## Phase Execution Log

### Part 0 – Project Scaffold ✅ (27 Mar 2026)
- Generated a fresh Quarkus 3 application (`si.um.feri.orders:order-service`) and relocated it under `services/order-service` so it matches the mono-repo layout.
- Added the baseline extensions we will rely on later (`resteasy-reactive-jackson`, `hibernate-reactive-panache`, `reactive-pg-client`, `smallrye-openapi`, `smallrye-reactive-messaging`, `flyway`, `quarkus-logging-json`, `jdbc-postgresql`).
- Verified the skeleton builds via `./mvnw package -DskipTests`, which ensures `quarkus:dev` can boot once we wire configs.
- Outcome: Part 0 is DONE; moving on to **Part 1 – Domain + Persistence** next.

### Part 1 – Domain + Persistence ✅ (28 Mar 2026)
- Modeled the aggregate with `OrderEntity` + `OrderStatus`, including optimistic locking, timestamp hooks, and lifecycle guard rails.
- Introduced `OrderRepository` (reactive Panache) and a Flyway migration that provisions schema `orders`, indexes, and constraints defined in the architecture doc.
- Wired datasource/Flyway configuration via `application.properties` so the service boots against PostgreSQL with migrations executed at startup.
- Added Testcontainers-backed `OrderRepositoryTest` to persist+fetch an order using PostgreSQL 15, giving us a regression harness for future use cases.
