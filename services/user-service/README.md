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

## Application Service Layer (Part 2)

### Architecture Overview
The service implements a layered architecture:
1. **gRPC Handler Layer** (`UserGrpcService`) - API boundary for inter-service communication
2. **Application Service Layer** (`UserApplicationService`) - Business orchestration and use cases
3. **Domain Layer** - Core business logic, entities, and exceptions
4. **Infrastructure Layer** - Database, security, and external integrations

### Exception Hierarchy
Domain-specific exceptions in `si.um.feri.userservice.domain.exception`:
- `DomainException` (base)
  - `DuplicateEmailException` - Email already registered
  - `InvalidCredentialsException` - Authentication failure
  - `UserNotFoundException` - User does not exist

### DTOs and Mapping
Transfer objects in `si.um.feri.userservice.application.dto`:
- `UserDTO` - User profile data (id, email, firstName, lastName, createdAt)
- `RegisterUserDTO` - Registration request (email, password, firstName, lastName)
- `AuthenticateUserDTO` - Login request (email, password)
- `UpdateProfileDTO` - Profile update request (firstName, lastName)

Mapper in `si.um.feri.userservice.application.mapper.UserMapper`:
- Converts between domain entities and DTOs
- Handles null safety and immutability

### Application Service: UserApplicationService
Location: `src/main/java/si/um/feri/userservice/application/service/UserApplicationService.java`

**Responsibilities:**
- Register new users (validates email uniqueness, hashes password via BCrypt)
- Authenticate users (credentials validation, token generation)
- Retrieve user by ID with proper error handling
- Update user profile information

**Methods:**
```java
// Register user with email uniqueness validation
UserDTO register(RegisterUserDTO dto)

// Authenticate and return token
String authenticate(AuthenticateUserDTO dto)

// Retrieve user by ID
UserDTO getById(String userId)

// Update user profile
UserDTO updateProfile(String userId, UpdateProfileDTO dto)
```

### Security Components
- **PasswordHasher** - BCrypt hashing with salt for secure storage
- **TokenService** - JWT token generation and validation with expiry
- Uses Java 17 records for immutable DTO structures

### Testing
Unit tests cover all application service methods:
- `UserApplicationServiceTest` (8 tests) - Registration, authentication, retrieval, updates
- `PasswordHasherTest` (2 tests) - Hash and verify operations
- `SimpleTokenServiceTest` (5 tests) - JWT generation and validation
- Integration tests verify end-to-end flows

**Run tests:**
```bash
mvn test
```

### Test Coverage
- Registration: Valid registration, duplicate email rejection, error handling
- Authentication: Successful login, invalid credentials, user not found
- User retrieval: Successful lookup, user not found handling
- Profile updates: Valid updates, invalid user handling
- Security: Password hashing, token generation, token validation
