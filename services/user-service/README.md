# User Service

Java Spring Boot microservice for user registration, authentication, profile management, and token validation via gRPC.

## Tech Stack
- Java 17
- Spring Boot 3
- gRPC
- PostgreSQL
- Flyway
- JUnit + Mockito

## Run locally
1. Ensure PostgreSQL is running.
2. Set env vars if needed:
   - `DB_URL` (default `jdbc:postgresql://localhost:5432/users_db`)
   - `DB_USERNAME` (default `postgres`)
   - `DB_PASSWORD` (default `postgres`)
   - `GRPC_PORT` (default `9090`)
3. Start app:
   - `mvn spring-boot:run`

## Run tests
- `mvn test`

## Build Docker image
From `services/user-service`:
- `docker build -t user-service:local .`
- `docker run --rm -p 9090:9090 user-service:local`

## gRPC methods
- `RegisterUser`
- `AuthenticateUser`
- `GetUserById`
- `UpdateUserProfile`
- `ValidateToken`

Proto file:
- `src/main/proto/user_service.proto`
