# Phase 1C - Infrastructure Layer Implementation ✅

## Overview

Phase 1C creates the **Infrastructure Layer** - the connection between your domain logic (what we built in Phase 1B) and the actual PostgreSQL database.

**Goal**: Implement database persistence with Entity Framework Core while keeping domain logic database-agnostic.

---

## 🏗️ Architecture: How It All Connects

```
┌─────────────────────────────────────────────────┐
│         API Layer (BooksController)             │
│         REST Endpoints                          │
└────────────────────┬────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────┐
│         Domain Layer (BookService)              │
│         Business Logic                          │
└────────────────────┬────────────────────────────┘
                     │ depends on IBookRepository
                     ↓
┌─────────────────────────────────────────────────┐
│  INFRASTRUCTURE LAYER (Phase 1C) ← YOU HERE     │
│                                                  │
│  BookServiceDbContext                           │
│  ├─ Maps Book entity to database table         │
│  ├─ Configures columns, constraints, indexes  │
│  └─ Entity Framework Core setup                │
│                                                  │
│  BookRepository                                 │
│  ├─ Implements IBookRepository                 │
│  ├─ Converts C# to SQL                         │
│  └─ Executes queries on PostgreSQL             │
│                                                  │
│  DesignTimeDbContextFactory                    │
│  └─ Enables EF Core migrations                 │
│                                                  │
│  InfrastructureServiceCollectionExtensions     │
│  └─ Dependency Injection setup                 │
└────────────────────┬────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────┐
│  PostgreSQL Database                            │
│  ├─ Books table                                │
│  ├─ Indexes (ISBN, Status, CreatedAt)         │
│  └─ Constraints (unique, not null, etc.)      │
└─────────────────────────────────────────────────┘
```

---

## 📁 Files Created

### 1. **BookServiceDbContext.cs** (~220 lines)

**What it is:**
- Entity Framework Core's representation of the database
- Maps C# objects to database tables
- Configures columns, types, constraints, indexes

**How it works:**
```csharp
// In C#
var book = new Book { Title = "1984", Author = "Orwell", ... };
dbContext.Books.Add(book);
await dbContext.SaveChangesAsync();

// Entity Framework converts to SQL:
INSERT INTO Books (id, title, author, isbn, ...) 
VALUES ('123e4567...', '1984', 'Orwell', ...)
```

**Key methods:**

| Method | Purpose |
|--------|---------|
| `DbSet<Book> Books` | Represents the "Books" table |
| `OnModelCreating()` | Configure table/column mappings |
| `SaveChangesAsync()` | Commit changes to database |

**Configuration done in OnModelCreating():**
```csharp
entity.ToTable("Books", "public");              // Table name
entity.HasKey(b => b.Id);                      // Primary key
entity.Property(b => b.ISBN).HasMaxLength(20); // Column constraints
entity.HasIndex(b => b.ISBN).IsUnique();       // Unique constraint
entity.HasIndex(b => b.Status);                // Index for fast filtering
```

**Database Schema Generated:**
```sql
CREATE TABLE Books (
    id UUID PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(300) NOT NULL,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    status INTEGER NOT NULL DEFAULT 0,
    image_url VARCHAR(1000),
    published_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_books_isbn_unique ON Books(isbn);
CREATE INDEX idx_books_status ON Books(status);
CREATE INDEX idx_books_created_at ON Books(created_at);
```

---

### 2. **BookRepository.cs** (~340 lines)

**What it is:**
- Implements the `IBookRepository` interface from domain layer
- Translates business operations to SQL queries
- Uses Entity Framework Core to execute queries

**Relationship:**
```
Domain:           IBookRepository (interface)
                         ↑
                         │ implements
                         │
Infrastructure:   BookRepository (concrete class)
                         ↓
Database:         PostgreSQL (actual data)
```

**How it works:**

```csharp
// Service calls:
await repository.GetByIdAsync(bookId)

// Repository converts to:
var book = await _dbContext.Books
    .FirstOrDefaultAsync(b => b.Id == bookId);

// Entity Framework executes:
SELECT * FROM Books WHERE id = @id
```

**Methods implemented:**

| Method | SQL Generated | Use Case |
|--------|---------------|----------|
| `GetByIdAsync(id)` | SELECT WHERE id | Get one book |
| `GetAllAsync()` | SELECT | Get all books |
| `GetByStatusAsync(status)` | SELECT WHERE status | Filter by status |
| `GetByIsbnAsync(isbn)` | SELECT WHERE isbn | Search by ISBN |
| `CreateAsync(book)` | INSERT | Create new book |
| `UpdateAsync(book)` | UPDATE | Modify book |
| `DeleteAsync(id)` | DELETE | Remove book |
| `ExistsAsync(id)` | SELECT 1 | Check existence |
| `IsbnExistsAsync(isbn)` | SELECT 1 WHERE isbn | Check uniqueness |

**Error Handling:**
```csharp
try
{
    await _dbContext.SaveChangesAsync();
}
catch (DbUpdateException ex)
{
    // Database constraint violation (e.g., duplicate ISBN)
    _logger.LogError(ex, "Database constraint violation");
    throw;
}
```

**Logging:**
```csharp
_logger.LogInformation("Creating new book: {Title} by {Author}", book.Title, book.Author);
_logger.LogInformation("Retrieved {BookCount} books", books.Count);
```

---

### 3. **DesignTimeDbContextFactory.cs** (~60 lines)

**What it is:**
- Factory for creating DbContext at design-time
- Needed by Entity Framework Core CLI tools
- Enables `dotnet ef` commands to work

**Why needed:**

When running:
```bash
dotnet ef migrations add CreateInitial
dotnet ef database update
```

The `dotnet ef` tool needs a DbContext instance, but:
- ❌ No ASP.NET Core host is running
- ❌ No dependency injection container exists
- ❌ Can't use Program.cs services

**Solution:** Factory that creates DbContext independently

**How it works:**
```csharp
// EF Core CLI calls this when needed
public BookServiceDbContext CreateDbContext(string[] args)
{
    // 1. Load configuration from appsettings.json
    var configuration = new ConfigurationBuilder()
        .SetBasePath(Directory.GetCurrentDirectory())
        .AddJsonFile("appsettings.json")
        .Build();

    // 2. Get connection string
    var connectionString = configuration.GetConnectionString("DefaultConnection");

    // 3. Create DbContext options
    var options = new DbContextOptionsBuilder<BookServiceDbContext>()
        .UseNpgsql(connectionString)
        .Options;

    // 4. Return context
    return new BookServiceDbContext(options);
}
```

**Used by:**
```bash
# These commands use DesignTimeDbContextFactory automatically:
dotnet ef migrations add CreateInitial
dotnet ef database update
dotnet ef migrations list
dotnet ef database drop
```

---

### 4. **InfrastructureServiceCollectionExtensions.cs** (~100 lines)

**What it is:**
- Extension method for dependency injection setup
- Registers all infrastructure services
- Follows clean architecture patterns

**Clean code pattern:**
```csharp
// Instead of this in Program.cs:
services.AddDbContext<BookServiceDbContext>(...);
services.AddScoped<IBookRepository, BookRepository>();
services.AddScoped<BookServiceDbContext>();
// ... more setup ...

// We do this:
services.AddInfrastructure(configuration);
```

**What gets registered:**

```csharp
public static IServiceCollection AddInfrastructure(
    this IServiceCollection services,
    IConfiguration configuration)
{
    // 1. DbContext + PostgreSQL
    services.AddDbContext<BookServiceDbContext>(options =>
    {
        options.UseNpgsql(
            connectionString,
            npgsqlOptions =>
            {
                // Retry failed queries up to 3 times
                npgsqlOptions.EnableRetryOnFailure(maxRetryCount: 3);
            }
        );
    });

    // 2. Repository
    services.AddScoped<IBookRepository, BookRepository>();

    // 3. Migration support
    services.AddScoped<BookServiceDbContext>();

    return services;
}
```

**Dependency lifetimes:**

| Service | Lifetime | Why |
|---------|----------|-----|
| DbContext | Scoped | One per HTTP request, tracks entities |
| IBookRepository | Scoped | Depends on DbContext |

**InitializeDatabaseAsync() method:**

Called at startup to:
1. Apply migrations
2. Create database if needed
3. Seed data (if added later)

```csharp
// In Program.cs:
var app = builder.Build();
await app.Services.InitializeDatabaseAsync();
```

---

## 🔄 Complete Data Flow: Create Book Example

### **Step-by-step:**

```
1. User submits form: POST /api/books
   { "title": "1984", "author": "Orwell", ... }
   
2. BooksController receives request (Phase 1D)
   calls: await bookService.CreateBookAsync(...)
   
3. BookService (Domain Layer)
   ├─ Validates ISBN is unique
   ├─ Creates Book entity (with validation)
   └─ calls: await repository.CreateAsync(book)
   
4. BookRepository (Infrastructure Layer)
   └─ calls: _dbContext.Books.Add(book)
   └─ calls: await _dbContext.SaveChangesAsync()
   
5. Entity Framework Core
   └─ Converts Book object to SQL
   └─ Generates: INSERT INTO Books (id, title, author, ...) VALUES (...)
   
6. PostgreSQL Database
   └─ Executes INSERT
   └─ Stores in Books table
   └─ Returns success
   
7. Response flows back:
   Repository → Service → Controller → User
   { "id": "123e4567...", "title": "1984", ... }
```

---

## 🗄️ Database Schema

### **Books Table**

```
Column            | Type              | Constraints
------------------+-------------------+---------------------------
id                | UUID              | PRIMARY KEY
title             | VARCHAR(500)      | NOT NULL
author            | VARCHAR(300)      | NOT NULL
isbn              | VARCHAR(20)       | NOT NULL, UNIQUE
description       | TEXT              | NOT NULL
price             | NUMERIC(10,2)     | NOT NULL
status            | INTEGER           | NOT NULL, DEFAULT 0
image_url         | VARCHAR(1000)     | NULL
published_date    | TIMESTAMP         | NULL
created_at        | TIMESTAMP         | DEFAULT CURRENT_TIMESTAMP
updated_at        | TIMESTAMP         | DEFAULT CURRENT_TIMESTAMP
```

### **Indexes for Performance**

| Index Name | On Column | Type | Purpose |
|------------|-----------|------|---------|
| `idx_books_isbn_unique` | isbn | UNIQUE | Enforce uniqueness, fast lookup |
| `idx_books_status` | status | Regular | Fast filtering (published books) |
| `idx_books_created_at` | created_at | Regular | Fast sorting (newest first) |

---

## 📌 Entity Framework Core Concepts

### **LINQ to SQL**

Entity Framework converts C# LINQ to SQL automatically:

```csharp
// C#
var publishedBooks = await _dbContext.Books
    .Where(b => b.Status == BookStatus.Published)
    .OrderByDescending(b => b.CreatedAt)
    .ToListAsync();

// Becomes SQL:
SELECT * FROM Books 
WHERE status = 1 
ORDER BY created_at DESC
```

### **Change Tracking**

Entity Framework tracks which entities changed:

```csharp
var book = await dbContext.Books.FirstAsync();
book.Update("New Title", ...);

// EF Core knows it changed!
await dbContext.SaveChangesAsync();

// Generates:
UPDATE Books SET title = @title, updated_at = NOW() WHERE id = @id
```

### **Async Operations**

All database calls are async (non-blocking):

```csharp
// Async - doesn't block thread
var book = await repository.GetByIdAsync(id);

// Sync - blocks thread (❌ don't use)
var book = repository.GetByIdAsync(id).Result;
```

---

## 🔐 Update Program.cs

The updated Program.cs now includes:

```csharp
// 1. Import infrastructure namespace
using BookService.Infrastructure.Extensions;
using BookService.Domain.Services;

// 2. Register infrastructure services
builder.Services.AddInfrastructure(builder.Configuration);

// 3. Register domain service
builder.Services.AddScoped<BookService>();

// 4. Initialize database at startup
await app.Services.InitializeDatabaseAsync();
```

---

## 🚀 Next Steps: Create Initial Migration

After Phase 1C, create the first migration:

```bash
cd /Users/micko/Desktop/itaproject/new-book-landing-system/services/book-service

# Create migration
dotnet ef migrations add CreateInitial \
    --project src/BookService.Infrastructure \
    --startup-project src/BookService.API

# Apply to database
dotnet ef database update \
    --project src/BookService.Infrastructure \
    --startup-project src/BookService.API
```

This creates:
- `Migrations/` folder
- Initial migration file
- PostgreSQL database schema

---

## 📊 Design Patterns Used

### **Repository Pattern**
- Abstracts database access behind interface
- Easy to test with fake repositories
- Can swap implementations

### **Dependency Injection**
- Services registered in container
- Injected where needed
- Makes testing easy

### **Extension Methods**
- Cleaner code (`AddInfrastructure()`)
- Reusable setup
- Follows conventions

### **Factory Pattern**
- DesignTimeDbContextFactory
- Creates objects when needed
- Handles complex setup

---

## 🧪 How This Enables Testing

### **Unit Tests (don't need database)**

```csharp
[Fact]
public async Task CreateBook_WithDuplicateISBN_ReturnsFail()
{
    // Arrange - fake repository
    var mockRepo = new Mock<IBookRepository>();
    mockRepo.Setup(r => r.IsbnExistsAsync("ISBN"))
        .ReturnsAsync(true);
    
    var service = new BookService(mockRepo.Object);

    // Act
    var result = await service.CreateBookAsync("Title", "Author", "ISBN", "Desc", 10m);

    // Assert
    Assert.False(result.IsSuccess);
}
```

### **Repository Tests (use real database in container)**

```csharp
[Fact]
public async Task CreateBook_SavesToDatabase()
{
    // Arrange - real PostgreSQL in Docker container
    using var container = new PostgreSqlContainer()
        .WithDatabase(dbName: "test_db")
        .Build();
    
    await container.StartAsync();
    
    var options = new DbContextOptionsBuilder<BookServiceDbContext>()
        .UseNpgsql(container.GetConnectionString())
        .Options;
    
    using var dbContext = new BookServiceDbContext(options);
    var repo = new BookRepository(dbContext, _logger);

    // Act
    var book = Book.CreateNew("Title", "Author", "ISBN", "Desc", 10m);
    await repo.CreateAsync(book);
    
    // Assert
    var retrieved = await repo.GetByIdAsync(book.Id);
    Assert.NotNull(retrieved);
}
```

---

## ✅ What We've Accomplished

✅ **BookServiceDbContext** - EF Core database mapping  
✅ **BookRepository** - Database implementation  
✅ **DesignTimeDbContextFactory** - Enables migrations  
✅ **InfrastructureServiceCollectionExtensions** - DI setup  
✅ **Updated Program.cs** - Infrastructure registration  

---

## 🎯 Architecture Summary

```
Domain Layer:
├─ Book (entity)
├─ IBookRepository (interface)
├─ BookService (business logic)
└─ Result<T> (error handling)

Infrastructure Layer:
├─ BookServiceDbContext (EF Core mapping)
├─ BookRepository (implementation)
├─ DesignTimeDbContextFactory (migrations)
└─ InfrastructureServiceCollectionExtensions (DI)

API Layer:
└─ BooksController (Phase 1D - not yet)

Database Layer:
└─ PostgreSQL (Books table)
```

---

## 📚 Key Concepts Mastered

| Concept | What | Why |
|---------|------|-----|
| **DbContext** | Bridge to database | Maps objects to tables |
| **Repository** | Data access layer | Abstracts database details |
| **Migration** | Schema version control | Track database changes |
| **Async/Await** | Non-blocking I/O | Scalable web service |
| **DI** | Service registration | Easy testing |
| **LINQ** | Query language | C# SQL generation |

---

## 🚀 What's Next? (Phase 1D)

**Phase 1D - API Layer:**
1. Create BooksController
2. Create DTOs (Data Transfer Objects)
3. Add validation middleware
4. Create API endpoints
5. Implement error mapping

Then we'll have a working REST API! 🎉

---

## 💡 Summary

> **Infrastructure Layer = Bridge Between Logic and Database**
> 
> - Domain doesn't know how data is stored
> - Database doesn't know business logic
> - Infrastructure handles the conversion
> - Clean, testable, maintainable!

---

Ready for Phase 1D (API Layer)? 🚀
