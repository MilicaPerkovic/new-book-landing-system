# 📚 Book Service Development Guide

## Overview
This guide explains the architecture and implementation of the **Book Service** microservice for the Book Landing System. The Book Service is responsible for managing all book-related operations (CRUD - Create, Read, Update, Delete).

---

## 🏗️ Architecture & Layers

### Why Layered Architecture?

We split the code into **three independent layers**. This makes the code:
- **Testable** - Each layer can be tested separately
- **Maintainable** - Changes in one layer don't break others
- **Scalable** - Easy to swap implementations (e.g., change database)
- **Reusable** - Domain logic can be used in different places

```
┌─────────────────────────────────────────┐
│  API Layer (BookService.API)            │
│  - REST Controllers                     │
│  - HTTP Request/Response Handling       │
│  - Swagger Documentation                │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Domain Layer (BookService.Domain)      │
│  - Business Logic                       │
│  - Entities (Book.cs)                   │
│  - Interfaces (IBookRepository.cs)      │
│  - NO database, NO web frameworks       │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  Infrastructure Layer                   │
│  (BookService.Infrastructure)           │
│  - Database Access (PostgreSQL)         │
│  - Repositories Implementation          │
│  - Logging Configuration                │
│  - External Service Integration         │
└─────────────────────────────────────────┘
```

---

## 📁 Phase 1A: What We Just Created

### 1. **Folder Structure**
```
book-service/
├── src/
│   ├── BookService.API/           ← ASP.NET Core Web API
│   ├── BookService.Domain/        ← Business Logic
│   └── BookService.Infrastructure/ ← Database & External Services
├── tests/
│   ├── BookService.UnitTests/      ← Test business logic
│   ├── BookService.RepositoryTests/ ← Test database operations
│   └── BookService.IntegrationTests/ ← Test API endpoints
├── .github/workflows/              ← GitHub Actions CI/CD
└── docs/                           ← Documentation
```

### 2. **Project Files (.csproj)**

Each folder has a `.csproj` file - this is like a **recipe** for building the project.

**BookService.API.csproj:**
```csharp
// Includes packages needed for the web API:
- Serilog (logging)
- Entity Framework Core (database)
- Swashbuckle (Swagger/OpenAPI)
- FluentValidation (input validation)
```

**BookService.Domain.csproj:**
```csharp
// Pure business logic - NO external dependencies
// Just the framework (.NET 8)
```

**BookService.Infrastructure.csproj:**
```csharp
// Database and technical implementation
- Entity Framework Core
- Npgsql (PostgreSQL driver)
- Serilog (logging)
```

**Test Projects:**
```csharp
// Testing libraries
- xUnit (testing framework)
- Moq (mocking/faking dependencies)
- Testcontainers (Docker containers for tests)
- WebApplicationFactory (API testing)
```

---

## ⚙️ Configuration Files

### **appsettings.json**
This file contains **configuration settings** for the application.

```json
{
  "Logging": { ... },              // How to handle logs
  "ConnectionStrings": {            // Database connection info
    "DefaultConnection": "Host=localhost;Port=5432;..."
  },
  "AllowedHosts": "*",              // Which hosts can call this API
  "Serilog": { ... }                // Advanced logging configuration
}
```

### **appsettings.Development.json**
Settings **only for development** (overrides main settings).

---

## 🚀 Program.cs Explained

This is the **entry point** of the application. It's like the "main" function.

### **Section 1: Logging Setup**
```csharp
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .WriteTo.Console()
    .WriteTo.File("logs/bookservice-.txt")
```
**What it does:** Sets up structured logging so we can see what's happening.

**Why:** When things break, we need to know what went wrong. Logs are our debugging tool.

### **Section 2: Dependency Injection**
```csharp
builder.Services.AddControllers();
builder.Services.AddSwaggerGen();
builder.Services.AddCors();
```

**What it does:** Registers services that will be available throughout the application.

**Why:** Instead of creating objects manually, ASP.NET Core handles it for us. This makes testing easier.

**Example:** When a controller needs a `BookService`, .NET automatically provides it.

### **Section 3: Database (TODO)**
```csharp
// builder.Services.AddDbContext<BookServiceDbContext>(...);
```
**Currently commented out.** In Phase 2, we'll uncomment this to connect to PostgreSQL.

### **Section 4: HTTP Pipeline (Middleware)**
```csharp
app.UseHttpsRedirection();
app.UseCors("AllowAll");
app.MapControllers();
```

**What it does:** Configures how HTTP requests are processed.

**Pipeline flow:**
1. Request comes in
2. HTTPS redirect (if needed)
3. CORS check (allow frontend to call this)
4. Route to the right controller
5. Send response back

---

## 📊 Three Types of Tests (Why We Need All Three)

### **1. Unit Tests** (`BookService.UnitTests`)
- Test **isolated business logic**
- Mock all external dependencies (database, APIs)
- **Fast** (~milliseconds per test)
- **Example:** Test that `BookService.ValidateISBN()` correctly validates ISBNs

```csharp
[Fact]
public void ValidateISBN_WithValidISBN_ReturnsTrue()
{
    var service = new BookService();
    var result = service.ValidateISBN("978-3-16-148410-0");
    Assert.True(result);
}
```

### **2. Repository Tests** (`BookService.RepositoryTests`)
- Test **database operations**
- Uses **real PostgreSQL** in Docker (Testcontainers)
- **Medium speed** (seconds per test)
- **Example:** Test that we can save a book to the database

```csharp
[Fact]
public async Task CreateBook_WithValidBook_SavesToDatabase()
{
    var book = new Book { Title = "Test Book", Author = "Test Author" };
    await repository.CreateAsync(book);
    var retrieved = await repository.GetByIdAsync(book.Id);
    Assert.NotNull(retrieved);
}
```

### **3. Integration Tests** (`BookService.IntegrationTests`)
- Test **complete flow**: HTTP Request → API → Database → Response
- Uses **real API** and **real database**
- **Slowest** (seconds-minutes per test)
- **Example:** Test that `POST /api/books` creates a book

```csharp
[Fact]
public async Task CreateBook_ViaAPI_ReturnsCreatedBook()
{
    var response = await _client.PostAsJsonAsync("/api/books", 
        new { title = "Test", author = "Author" });
    
    Assert.Equal(HttpStatusCode.Created, response.StatusCode);
}
```

### **Why All Three?**
| Type | Speed | Tests | Best For |
|------|-------|-------|----------|
| Unit | ⚡⚡⚡ Fast | Business logic in isolation | Core logic |
| Repository | ⚡⚡ Medium | Database operations | Data access |
| Integration | ⚡ Slow | Full API flow | User scenarios |

---

## 🔄 Data Flow Example

### **User creates a book:**

```
1. Frontend sends:
   POST /api/books
   { "title": "Harry Potter", "author": "J.K. Rowling" }

2. API Gateway routes to Book Service

3. BooksController receives the request
   (defined in future phase)

4. Controller calls BookService.CreateBookAsync()
   (defined in Domain layer)

5. BookService validates the data
   - Check title is not empty
   - Check ISBN format
   - etc.

6. BookService calls IBookRepository.CreateAsync()
   (interface defined in Domain)

7. BookRepository (Infrastructure layer) saves to PostgreSQL
   UPDATE books SET title='...', author='...'

8. Database returns the new book with ID

9. Response flows back up the chain:
   Repository → Service → Controller → API Gateway → Frontend

10. Frontend receives:
    { "id": 123, "title": "Harry Potter", "author": "J.K. Rowling" }
```

---

## 🔧 What's Next? (Phase 1B)

### **Phase 1B: Domain Layer (Book Entity)**
1. Create `Book.cs` - represents a book
2. Create `IBookRepository.cs` - interface for database operations
3. Create `BookService.cs` - business logic

### **Phase 1C: Infrastructure Layer (Database)**
1. Create `BookServiceDbContext.cs` - database context
2. Create `BookRepository.cs` - implements IBookRepository
3. Create migrations for database schema

### **Phase 1D: API Layer (REST Endpoints)**
1. Create `BooksController.cs` - HTTP endpoints
2. Create DTOs (Data Transfer Objects)
3. Add validation

### **Phase 1E: Testing**
1. Write unit tests
2. Write repository tests
3. Write integration tests

### **Phase 1F: DevOps**
1. Dockerfile
2. docker-compose.yml
3. GitHub Actions workflow

---

## 📚 References & Keywords

### **Key Terms:**

- **ORM (Object-Relational Mapping):** Entity Framework converts C# objects to database tables
- **DI (Dependency Injection):** .NET automatically provides objects that classes need
- **CORS:** Security mechanism allowing frontend to call this backend
- **REST:** API style using HTTP methods (GET, POST, PUT, DELETE)
- **Microservice:** Independent, focused service with one responsibility
- **Structured Logging:** Logs in JSON format for easy searching

### **Packages We're Using:**

- **Entity Framework Core:** Database access (ORM)
- **Npgsql:** PostgreSQL driver for .NET
- **Serilog:** Advanced logging
- **Swashbuckle:** Auto-generates Swagger docs
- **xUnit:** Testing framework
- **Moq:** Mocking library
- **Testcontainers:** Docker containers for tests

---

## 📋 File Descriptions

| File | Purpose |
|------|---------|
| `BookService.API.csproj` | Project definition for API layer |
| `BookService.Domain.csproj` | Project definition for business logic |
| `BookService.Infrastructure.csproj` | Project definition for data access |
| `appsettings.json` | Configuration (database URL, logging, etc.) |
| `appsettings.Development.json` | Dev-specific configuration |
| `Program.cs` | Startup code - registers services, configures middleware |

---

## ✅ Phase 1A Checklist

- ✅ Folder structure created
- ✅ .csproj files created (API, Domain, Infrastructure, Tests)
- ✅ appsettings.json created
- ✅ Program.cs created with comments
- ✅ This documentation created

---

## 🎯 Ready for Phase 1B?

The foundation is set! The next step is to create the **Domain Layer** with:
- `Book.cs` entity
- `IBookRepository.cs` interface
- `BookService.cs` business logic

Let me know when you're ready! 🚀
