# QUICKSTART - Second Microservice (User Service)

## Goal
This guide gives you a complete start-to-finish process to run, test, verify, and submit Microservice 2.

Service path:
- services/user-service

Task target:
- Naloga 3 (Mikrostoritev 2)

---

## 1. What You Will Prove

By following this guide, you will have evidence for all required points:
1. User-service is implemented in a different technology than book-service.
2. gRPC is implemented and callable.
3. Data is stored in a database.
4. Logs are visible during runtime.
5. Unit tests are implemented and passing.
6. Dockerfile builds and runs.
7. GitHub Actions runs tests on push.

---

## 2. Prerequisites

Install or confirm:
1. Java 17
2. Maven (or use Docker-only path if Maven is not installed)
3. Docker
4. Optional: grpcurl for manual gRPC testing

Quick checks:

```bash
java -version
mvn -version
docker --version
grpcurl --version
```

If `mvn` is missing, you can still complete verification with Docker, but CI and local test flow are easier with Maven.

### 2.1 macOS Quick Fix (Based on Common Missing Tools)

If you see `command not found: mvn` or `command not found: grpcurl`, install tools with Homebrew:

```bash
brew update
brew install maven grpcurl
```

Re-check:

```bash
java -version
mvn -version
docker --version
grpcurl --version
```

Notes:
1. Java 21 is fine for running this project locally, while source compatibility target remains Java 17.
2. If you do not want to install grpcurl globally, you can run it via Docker:

```bash
docker run --rm fullstorydev/grpcurl -version
```

---

## 3. Start PostgreSQL

From project root, start database container:

```bash
docker rm -f users-db 2>/dev/null || true
docker run --name users-db \
	-e POSTGRES_PASSWORD=postgres \
	-e POSTGRES_USER=postgres \
	-e POSTGRES_DB=users_db \
	-p 5432:5432 \
	-d postgres:16-alpine
```

Verify DB is running:

```bash
docker ps --filter name=users-db
```

Expected:
1. Container `users-db` is up.
2. Port `5432` is exposed.

---

## 4. Configure Environment Variables (Optional)

Defaults are already configured in application settings, but you can set explicit values:

```bash
export DB_URL='jdbc:postgresql://localhost:5432/users_db'
export DB_USERNAME='postgres'
export DB_PASSWORD='postgres'
export GRPC_PORT='9090'
export JWT_SECRET='change-this-secret-key-to-at-least-32-characters'
```

Note:
- JWT secret must be at least 32 characters.

---

## 5. Run Service Locally (Source Mode)

Go to service directory and start app:

```bash
cd services/user-service
mvn spring-boot:run
```

Expected startup evidence in logs:
1. Flyway migration succeeds.
2. gRPC server starts on port `9090`.
3. No fatal startup exception.

Stop service with `Ctrl + C` when done.

---

## 6. Run Tests (Source Mode)

From `services/user-service`:

```bash
mvn -B test
```

Expected:
1. Build exits with success.
2. All tests pass.

If you get `mvn: command not found`:
1. Install Maven and rerun.
2. Or use Docker verification path below and confirm tests through GitHub Actions after push.

---

## 7. Validate gRPC Endpoints Manually

Keep service running on `localhost:9090`.

List service and methods:

```bash
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext localhost:9090 list user.v1.UserService
```

Expected methods:
1. RegisterUser
2. AuthenticateUser
3. GetUserById
4. UpdateUserProfile
5. ValidateToken

Register user:

```bash
grpcurl -plaintext \
	-d '{"email":"author1@example.com","password":"StrongPassword123","full_name":"Author One","role":"AUTHOR"}' \
	localhost:9090 user.v1.UserService/RegisterUser
```

Authenticate user:

```bash
grpcurl -plaintext \
	-d '{"email":"author1@example.com","password":"StrongPassword123"}' \
	localhost:9090 user.v1.UserService/AuthenticateUser
```

Validate token (replace token value):

```bash
grpcurl -plaintext \
	-d '{"token":"PASTE_ACCESS_TOKEN_HERE"}' \
	localhost:9090 user.v1.UserService/ValidateToken
```

Important for proto field names:
1. Use `full_name` in grpcurl JSON, not `fullName`.

---

## 8. Run with Docker (No Local Java/Maven Needed)

From `services/user-service` build image:

```bash
docker build -t user-service:local .
```

Run container and connect to host PostgreSQL:

```bash
docker run --rm \
	-p 9090:9090 \
	-e DB_URL='jdbc:postgresql://host.docker.internal:5432/users_db' \
	-e DB_USERNAME='postgres' \
	-e DB_PASSWORD='postgres' \
	-e JWT_SECRET='change-this-secret-key-to-at-least-32-characters' \
	user-service:local
```

Expected:
1. Container starts.
2. Flyway migration executes.
3. gRPC is reachable at `localhost:9090`.

---

## 9. GitHub Actions Verification

Workflow file:
- .github/workflows/user-service.yml

Push changes:

```bash
git add .
git commit -m "chore(user-service): final quickstart and verification"
git push origin YOUR_BRANCH
```

Expected on GitHub:
1. Workflow runs on push.
2. `services/user-service` tests execute.
3. Workflow is green when tests pass.

---

## 10. Final Submission Checklist

Before deadline, confirm all are done:
1. Service starts (source or Docker).
2. Database migration creates users table.
3. Unit tests pass.
4. gRPC methods are callable.
5. Runtime logs are visible.
6. Docker image builds and runs.
7. GitHub Actions passes on push.

---

## 11. Evidence to Show in Demo/Defense

Prepare screenshots or terminal outputs for:
1. Successful startup logs (Flyway + gRPC port).
2. `mvn -B test` success or CI success page.
3. grpcurl calls for RegisterUser and AuthenticateUser.
4. Docker build and run success.
5. Green GitHub Actions run.

---

## 12. Troubleshooting

1. `mvn: command not found`
- Install Maven or use Docker path and verify tests via CI.

2. gRPC returns invalid argument
- Check JSON field names against proto (`full_name`, `user_id`).

3. DB connection failure
- Ensure `users-db` container is running and credentials are correct.

4. JWT errors
- Ensure `JWT_SECRET` length is at least 32 characters.

5. Port conflict on `9090`
- Change `GRPC_PORT` and call grpcurl on new port.
