# 📚 Book Service

**Part of the Book Landing System - First Microservice**

This is the **Book Service** microservice responsible for managing all book-related operations.

## 🎯 Responsibilities

- ✅ CRUD operations on books (Create, Read, Update, Delete)
- ✅ Book data validation
- ✅ Book storage in PostgreSQL database
- ✅ REST API endpoints for book operations

## 🏗️ Architecture

The service follows **Clean Architecture** with three independent layers:

1. **Domain Layer** (`BookService.Domain`)
   - Business logic and entities
   - No external dependencies

2. **Infrastructure Layer** (`BookService.Infrastructure`)
   - Database access (Entity Framework Core + PostgreSQL)
   - External service integrations

3. **API Layer** (`BookService.API`)
   - REST Controllers
   - HTTP request/response handling
   - Swagger/OpenAPI documentation

## 📋 Learning Resources

👉 **Read**: [DEVELOPMENT.md](./DEVELOPMENT.md) for detailed explanations of:
- Architecture overview
- What each file does
- How data flows through the system
- Testing strategy
- Configuration explanation

## 🚀 Quick Start (Coming in Phase 1B)

```bash
# Build
dotnet build

# Run tests
dotnet test

# Run the service
dotnet run --project src/BookService.API

# Access Swagger UI
http://localhost:5000/swagger
```

## 📦 Current Status

**Phase 1A ✅ Complete:**
- Project structure created
- Configuration files ready
- Dependency injection setup
- Logging configured

**Phase 1B (Next):**
- Domain entities (Book.cs)
- Repository interfaces
- Business logic implementation

**Phase 1C (After 1B):**
- Database context
- Repository implementation
- Database migrations

**Phase 1D (After 1C):**
- REST Controllers
- DTOs
- API endpoints

**Phase 1E (After 1D):**
- Unit tests
- Repository tests
- Integration tests

**Phase 1F (After 1E):**
- Dockerfile
- docker-compose.yml
- GitHub Actions workflow

## 🗄️ Database

- **Type**: PostgreSQL
- **Connection**: Configured in `appsettings.json`
- **Default**: `localhost:5432/BookService`

## 📚 API Endpoints (Coming in Phase 1D)

```
GET    /api/books              - List all books
GET    /api/books/{id}         - Get book details
POST   /api/books              - Create a new book
PUT    /api/books/{id}         - Update a book
DELETE /api/books/{id}         - Delete a book
```

## 🔍 Swagger Documentation

Once running, access interactive API docs at:
```
http://localhost:5000/swagger
```

## 🧪 Testing

Three types of tests:
- **Unit Tests**: Business logic in isolation
- **Repository Tests**: Database operations with real PostgreSQL
- **Integration Tests**: Full API flow end-to-end

## 📝 Logging

- **Type**: Structured (JSON format)
- **Output**: Console and files in `logs/` directory
- **Tool**: Serilog

## 🐳 Docker (Coming in Phase 1F)

```bash
docker-compose up
```

Will start:
- Book Service API
- PostgreSQL database
- RabbitMQ (for future async messaging)

## 📖 Technology Stack

- **.NET**: 8.0 (latest LTS)
- **Framework**: ASP.NET Core
- **Database**: PostgreSQL with Entity Framework Core
- **Testing**: xUnit, Moq, Testcontainers
- **Logging**: Serilog (structured)
- **Documentation**: Swagger/OpenAPI
- **CI/CD**: GitHub Actions

---

**Next Step:** Proceed to Phase 1B - Domain Layer Implementation 🚀
