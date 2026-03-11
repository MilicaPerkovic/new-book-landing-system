# Phase 1B - Domain Layer Implementation ✅

## Overview

Phase 1B creates the **Domain Layer** - the heart of your business logic. This layer contains:
- Entities (Book.cs)
- Repositories Interface (IBookRepository.cs)
- Business Logic Service (BookService.cs)
- Supporting Types (Result.cs, BookStatus.cs)

**Goal**: Build a layer that knows NOTHING about databases, web frameworks, or HTTP. Pure business logic only.

---

## 📁 Files Created

### 1. **BookStatus.cs** - Enum for book states

```csharp
public enum BookStatus
{
    Draft = 0,           // Being created
    Published = 1,       // Visible to users
    Archived = 2,        // Hidden but preserved
    Discontinued = 3     // No longer available
}
```

**Why**: Track the lifecycle of a book through different states.

**Usage**:
```csharp
var publishedBooks = await repository.GetByStatusAsync(BookStatus.Published);
```

---

### 2. **Result.cs** - Standardized operation results

```csharp
public class Result<TData>
{
    public bool IsSuccess { get; private set; }
    public TData? Data { get; private set; }
    public string? ErrorMessage { get; private set; }
}
```

**Why**: Instead of throwing exceptions everywhere or returning null, we return a Result object. This makes error handling explicit and predictable.

**Traditional Approach (❌ Bad)**:
```csharp
try
{
    var book = bookService.CreateBook(data);  // Might throw exception
    if (book == null)                          // Might return null
    {
        // Handle it somehow?
    }
}
catch (Exception ex)
{
    // What now?
}
```

**New Approach (✅ Good)**:
```csharp
var result = await bookService.CreateBookAsync(data);
if (!result.IsSuccess)
{
    Console.WriteLine($"Error: {result.ErrorMessage}");
    return;
}
var createdBook = result.Data;
```

**Benefits**:
- ✅ No null reference exceptions
- ✅ Error handling is explicit
- ✅ Can chain operations easily
- ✅ Better for API responses

---

### 3. **Book.cs** - The Book Entity

This is the most important file. It represents "what is a book?" in your business.

#### **Key Design Principles**:

**A) Private Setters - Prevent Invalid State**
```csharp
public string Title { get; private set; } = string.Empty;
```

❌ Bad (without private setter):
```csharp
var book = new Book();
book.Title = null;  // ❌ Invalid state! Book has no title!
```

✅ Good (with private setter):
```csharp
book.Title = null;  // ❌ Compiler error! Can't set it!
```

**B) Factory Method - Force Validation**

```csharp
// Don't do this:
var book = new Book();  // ❌ Invalid state, no fields set

// Do this:
var book = Book.CreateNew("Title", "Author", "ISBN", "Desc", 19.99m);  // ✅ Validated
```

**C) Business Methods - Controlled State Changes**

```csharp
book.Publish();        // Changes status to Published, sets PublishedDate
book.Archive();        // Changes status to Archived
book.Discontinue();    // Changes status to Discontinued
```

Each method enforces business rules:
```csharp
public void Publish()
{
    if (Status == BookStatus.Published)
        throw new InvalidOperationException("Already published");
    
    Status = BookStatus.Published;
    PublishedDate = DateTime.UtcNow;
    UpdatedAt = DateTime.UtcNow;
}
```

**D) Validation - Keep Bad Data Out**

```csharp
private static void ValidateInput(string title, string author, string isbn, ...)
{
    if (string.IsNullOrWhiteSpace(title))
        throw new ArgumentException("Title cannot be empty");
    
    if (title.Length > 500)
        throw new ArgumentException("Title too long");
    
    // Validate ISBN format
    var isbnDigits = isbn.Replace("-", "");
    if (isbnDigits.Length != 13 || !isbnDigits.All(char.IsDigit))
        throw new ArgumentException("Invalid ISBN");
    
    if (price <= 0)
        throw new ArgumentException("Price must be positive");
}
```

**Why validation here?**
- Garbage in = Garbage out
- It's part of business logic (ISBN format is a business rule)
- Reusable (used in both Create and Update)
- Tested in unit tests

---

### 4. **IBookRepository.cs** - Database Contract

This interface defines what database operations we need:

```csharp
public interface IBookRepository
{
    Task<Book?> GetByIdAsync(Guid id);
    Task<IEnumerable<Book>> GetAllAsync();
    Task<IEnumerable<Book>> GetByStatusAsync(BookStatus status);
    Task<Book?> GetByIsbnAsync(string isbn);
    Task CreateAsync(Book book);
    Task UpdateAsync(Book book);
    Task DeleteAsync(Guid id);
    Task<bool> ExistsAsync(Guid id);
    Task<bool> IsbnExistsAsync(string isbn);
    Task<int> GetCountByStatusAsync(BookStatus status);
}
```

**Why Interface?**

**Testing Without Permission**:
```csharp
// In production: Real database
new BookService(new PostgreSqlBookRepository())

// In tests: Fake database
new BookService(new FakeBookRepository())  // ← No database needed!
```

**Swappable Implementation**:
```csharp
// Start with PostgreSQL
IBookRepository repo = new PostgreSqlRepository();

// Need to switch to MongoDB?
IBookRepository repo = new MongoDbRepository();  // Same code, different implementation!
```

---

### 5. **BookService.cs** - Business Logic

This is where the "smarts" happen. Coordinates between the entity and repository.

#### **Responsibilities**:

**A) Enforce Business Rules**
```csharp
// Check ISBN is unique before creating
var isbnExists = await _bookRepository.IsbnExistsAsync(isbn);
if (isbnExists)
    return Result<Book>.Failure("ISBN already exists");
```

**B) Coordinate Operations**
```csharp
var book = Book.CreateNew(...);           // Create entity
await _bookRepository.CreateAsync(book);  // Save to DB
return Result<Book>.Success(book);        // Return result
```

**C) Handle Errors Gracefully**
```csharp
try
{
    // Do work
}
catch (ArgumentException ex)
{
    return Result<Book>.Failure(ex.Message, "VALIDATION_ERROR");
}
catch (Exception ex)
{
    return Result<Book>.Failure($"Unexpected error: {ex.Message}");
}
```

**D) Provide Business Methods**
```csharp
await bookService.CreateBookAsync(...)
await bookService.UpdateBookAsync(...)
await bookService.DeleteBookAsync(...)
await bookService.PublishBookAsync(...)
await bookService.GetPublishedBooksAsync(...)
await bookService.GetStatisticsAsync(...)
```

#### **Example: Create Book Flow**

```csharp
// User calls
var result = await bookService.CreateBookAsync(
    title: "1984",
    author: "George Orwell",
    isbn: "978-0-451-52494-2",
    description: "Dystopian novel",
    price: 15.99m
);

// Service does:
1. Check if ISBN already exists
   ↓ if yes → return error
   
2. Create Book entity (validates all input)
   Book.CreateNew(...)
   ↓ if validation fails → return error
   
3. Save to database
   await repository.CreateAsync(book)
   ↓ if fails → return error
   
4. Return success with book
   Result<Book>.Success(book)
```

---

## 🔄 Data Flow

### Creation Flow
```
User Input
   ↓
BooksController (Phase 1D - not yet)
   ↓
BookService.CreateBookAsync()
   ├→ Check ISBN unique (IBookRepository)
   ├→ Create entity (Book.CreateNew)
   │   ├→ Validate all input
   │   ├→ Reject if invalid
   ├→ Save to database (IBookRepository)
   └→ Return Result
   ↓
Response to User
```

### Update Flow
```
BookService.UpdateBookAsync()
   ├→ Load existing book (IBookRepository)
   ├→ Call book.Update()
   │   ├→ Validate new input
   │   ├→ Update properties
   │   ├→ Update timestamps
   ├→ Save changes (IBookRepository)
   └→ Return Result
```

---

## 🧪 How This Enables Testing

### **Unit Test Example**

```csharp
// Create a fake repository (no database!)
public class FakeBookRepository : IBookRepository
{
    private List<Book> books = new();
    
    public Task<bool> IsbnExistsAsync(string isbn)
    {
        return Task.FromResult(
            books.Any(b => b.ISBN == isbn)
        );
    }
    
    // ... implement other methods
}

// Test the service
[Fact]
public async Task CreateBook_WithDuplicateIsbn_ReturnsFail()
{
    // Arrange
    var repo = new FakeBookRepository();
    repo.Add(Book.CreateNew("Book1", "Author", "978-0-451-52494-2", "...", 10m));
    var service = new BookService(repo);
    
    // Act
    var result = await service.CreateBookAsync(
        "Book2", "Author2", "978-0-451-52494-2", "Different book", 15m
    );
    
    // Assert
    Assert.False(result.IsSuccess);
    Assert.Contains("already exists", result.ErrorMessage);
}
```

**Why this is powerful:**
- ✅ No database needed
- ✅ Tests run in milliseconds
- ✅ No flakiness (no external dependencies)
- ✅ Simple to understand
- ✅ Fast feedback when developing

---

## 📊 Architecture Diagram

```
┌─────────────────────────────────────────┐
│         API Layer (Phase 1D)            │
│         BooksController                 │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      DOMAIN LAYER (Phase 1B) ← You here │
│                                         │
│  BookService                            │
│  ├─ CreateBookAsync()                   │
│  ├─ UpdateBookAsync()                   │
│  ├─ DeleteBookAsync()                   │
│  ├─ PublishBookAsync()                  │
│  └─ GetStatisticsAsync()                │
│                                         │
│  Book (Entity)                          │
│  ├─ Id, Title, Author, ISBN, ...       │
│  ├─ CreateNew() factory                 │
│  ├─ Publish() method                    │
│  └─ ValidateInput() validation          │
│                                         │
│  IBookRepository (Interface)            │
│  ├─ GetByIdAsync()                      │
│  ├─ CreateAsync()                       │
│  ├─ UpdateAsync()                       │
│  └─ DeleteAsync()                       │
│                                         │
│  Result<T> (Error handling)             │
│  └─ IsSuccess, Data, ErrorMessage       │
│                                         │
│  BookStatus (Enum)                      │
│  └─ Draft, Published, Archived, ...    │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│  INFRASTRUCTURE Layer (Phase 1C)        │
│  BookServiceDbContext                   │
│  BookRepository (implements interface)  │
│  → Talks to PostgreSQL                  │
└─────────────────────────────────────────┘
```

---

## 🧠 Key Concepts You Should Understand

### **1. Interfaces Define Contracts**
```csharp
// BookService doesn't care HOW repository saves
// It just knows it will save
await _bookRepository.CreateAsync(book);
```

### **2. Private Setters Protect Invariants**
```csharp
// Can't do this:
book.Title = null;  // ❌ Compiler error

// Can only do this:
book.Update("New Title", ...);  // ✅ Controlled change
```

### **3. Factory Methods Force Validation**
```csharp
// Bad state is impossible:
var book = Book.CreateNew(...);  // ✅ Always valid
```

### **4. Result Objects Replace Exceptions**
```csharp
// Predictable error handling:
var result = await service.CreateBookAsync(...);
if (!result.IsSuccess) { /* handle error */ }
```

### **5. Separation of Concerns**
```csharp
Entity (Book):       What is a book?
Repository:          Where do books come from?
Service:             What can we do with books?
```

---

## ✅ What We've Accomplished

✅ **Book Entity** - Represents a book with validation  
✅ **IBookRepository** - Defines database contract  
✅ **BookService** - Implements business logic  
✅ **Result Pattern** - Standardized error handling  
✅ **BookStatus Enum** - State management  

---

## 🎯 Best Practices Applied

| Practice | Why | Where Used |
|----------|-----|-----------|
| **Encapsulation** | Prevent invalid state | Book.cs with private setters |
| **Factory Method** | Force validation | Book.CreateNew() |
| **Repository Pattern** | Decouple logic from data access | IBookRepository + BookService |
| **Dependency Injection** | Easy testing and configurability | BookService(IBookRepository) |
| **Result Pattern** | Explicit error handling | Result<T> class |
| **Validation** | Garbage in ≠ Garbage out | Book.ValidateInput() |
| **Async/Await** | Non-blocking I/O | All repository calls |

---

## 🚀 What's Next? (Phase 1C)

**Phase 1C - Infrastructure Layer:**
1. BookServiceDbContext.cs (Entity Framework database setup)
2. BookRepository.cs (implements IBookRepository, actually talks to PostgreSQL)
3. Database migrations (create database schema)

Then infrastructure can save/load books from real database!

---

## 📚 Learning Path

You should now understand:

1. ✅ What an entity is (Book)
2. ✅ Why we use interfaces (IBookRepository)
3. ✅ What a service does (BookService - coordination + logic)
4. ✅ Why Result pattern is better than exceptions
5. ✅ How to design domain logic independent of database
6. ✅ How to write testable code

**Questions to test yourself:**

1. Why can't you set `Book.Title = null` directly?
2. Why does `BookService` need `IBookRepository` as a parameter?
3. What's the difference between `Book` entity and `BookService`?
4. How would you test `CreateBookAsync` with a fake repository?
5. Why is validation in the `Book` class, not in the service?

---

## 💡 Remember

> **Domain Layer = Pure Business Logic**
> 
> No database, no HTTP, no frameworks.
> Just: "What is a book? What can we do with books? What are the rules?"

**Is this ready for Phase 1C?** Let me know! 🚀
