# READMESECONDMS - Task 3 Plan (Microservice 2)

## Goal
Implement the second microservice for the Book Landing System, in a different technology than Microservice 1, with:
- planned business functionality
- gRPC support
- database persistence
- logging
- unit tests
- Dockerfile
- GitHub Actions workflow running unit tests on push

## Recommended Choice for Microservice 2
Because `book-service` is already in C#/.NET, implement `user-service` in Java (Spring Boot) so each service uses a different technology.

## Scope for User Service (MVP for grading)
Implement these core features first:
1. User registration
2. User login (basic token/JWT)
3. Get user profile
4. Update user profile
5. Role support (`AUTHOR`, `PUBLISHER`, `ADMIN`, `READER`)

Optional if time permits:
1. Refresh token
2. Session revoke/logout
3. Password reset flow

## gRPC Contract (required)
Use gRPC for service-to-service communication.

Initial gRPC endpoints:
1. `RegisterUser`
2. `AuthenticateUser`
3. `GetUserById`
4. `UpdateUserProfile`
5. `ValidateToken`

Create proto file in:
- `services/user-service/src/main/proto/user_service.proto`

## Database Choice
Use PostgreSQL (fits structured user/account data).

Tables/entities:
1. `users` (id, email, password_hash, full_name, role, created_at, updated_at)
2. `sessions` (optional MVP+, token_id, user_id, expires_at, revoked)

## Logging Requirements
Implement structured logs for:
1. service startup
2. incoming gRPC requests
3. validation errors
4. authentication failures
5. DB errors

Do not log sensitive values (passwords, full tokens).

## Testing Requirements
Minimum for assignment:
1. Unit tests for service/business layer
2. Unit tests for validators/mappers
3. gRPC handler tests (happy path + error path)

Target: >= 80% coverage in service layer.

## Docker Requirements
Create `services/user-service/Dockerfile` with multi-stage build:
1. build stage (Gradle/Maven)
2. runtime stage (JRE)

Expose gRPC port (example `9090`).

## GitHub Actions Requirement
Create workflow:
- file: `.github/workflows/user-service-ci.yml`
- trigger: `push`
- job: run unit tests for `services/user-service`

Minimal workflow steps:
1. checkout
2. setup JDK
3. cache Maven/Gradle dependencies
4. run tests
5. (optional) publish test report artifact

## Implementation Plan by Parts

### Part 0 - Project setup (Foundation)
Deliverables:
1. Create Java service skeleton in `services/user-service`
2. Add build tool files (`pom.xml` or `build.gradle`)
3. Add base package structure (`domain`, `application`, `infrastructure`, `grpc`)
4. Add app configuration for local/dev

Done criteria:
- service compiles
- app starts locally

---

### Part 1 - Data model and persistence
Deliverables:
1. `User` entity + role enum
2. repository interface + implementation (Spring Data)
3. DB migration scripts (Flyway/Liquibase)

Done criteria:
- table created automatically
- basic save/find works in test

---

### Part 2 - Business logic
Deliverables:
1. registration use case
2. login/auth use case
3. profile read/update use cases
4. password hashing service

Done criteria:
- unit tests pass for main business rules
- duplicate email and invalid credentials handled

---

### Part 3 - gRPC API layer
Deliverables:
1. `user_service.proto`
2. generated classes setup
3. gRPC server implementation for required methods
4. request validation + mapped error responses

Done criteria:
- gRPC methods callable from test client
- success and failure responses verified

---

### Part 4 - Logging and observability
Deliverables:
1. request/response logging interceptor
2. error logging strategy
3. correlation/request id support (if possible)

Done criteria:
- logs are readable and useful for debugging
- no sensitive data leakage

---

### Part 5 - Testing package
Deliverables:
1. unit tests for service layer
2. unit tests for validator/helper classes
3. gRPC endpoint tests
4. coverage report config

Done criteria:
- all tests green locally
- meaningful assertions, not only smoke tests

---

### Part 6 - Dockerization
Deliverables:
1. production-ready `Dockerfile`
2. `.dockerignore`
3. optional local compose snippet for user-service + postgres

Done criteria:
- docker image builds
- container starts and connects to DB

---

### Part 7 - GitHub Actions CI
Deliverables:
1. `.github/workflows/user-service-ci.yml`
2. test run on each push
3. CI badge/update docs (optional)

Done criteria:
- workflow succeeds on push
- failed tests correctly fail pipeline

---

### Part 8 - Documentation and submission checklist
Deliverables:
1. `services/user-service/README.md` with run/test/build instructions
2. API/gRPC examples
3. final checklist aligned with assignment requirements

Done criteria:
- evaluator can run project quickly
- all assignment bullets traceable in docs

## Suggested Execution Order (what we do together)
1. Part 0
2. Part 1
3. Part 2
4. Part 3
5. Part 5
6. Part 4
7. Part 6
8. Part 7
9. Part 8

Reason: first make it work, then test deeply, then package and automate.

## Definition of Done for Task 3
Task is complete when all are true:
1. second microservice implemented in different technology than first
2. gRPC implemented and working
3. data persisted in DB
4. logs available during runtime
5. unit tests implemented and passing
6. Dockerfile present and buildable
7. GitHub Actions runs tests on push

## Milestone Timeline (to 24. 3. 2026)
1. Day 1-2: Part 0-1
2. Day 3-4: Part 2-3
3. Day 5: Part 5
4. Day 6: Part 4 + Part 6
5. Day 7: Part 7-8 + final cleanup

## Next Step
Start with Part 0 now.
After Part 0 is done and verified, continue to Part 1.
