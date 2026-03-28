# Phase 1D - API Layer Implementation ✅

## Overview

Phase 1D creates the **API Layer** - the REST endpoints that expose your business logic to the world. This is what the frontend and other services will call.

**Goal**: Create a professional REST API that's documented, validated, error-handled, and production-ready.

---

## 🏗️ Architecture: Complete Stack

```
┌──────────────────────────────────────┐
│  Client/Frontend Application         │
│  (React, Mobile, etc.)              │
└────────────────┬─────────────────────┘
                 │ HTTP/JSON
┌────────────────▼─────────────────────┐
│  API LAYER (Phase 1D) ← YOU HERE    │
│                                      │
│ BooksController                     │
│ ├─ GET    /api/books               │
│ ├─ GET    /api/books/{id}          │
│ ├─ POST   /api/books               │
│ ├─ PUT    /api/books/{id}          │
│ ├─ DELETE /api/books/{id}          │
│ └─ POST   /api/books/{id}/publish  │
│                                      │
│ DTOs (Data Transfer Objects)       │
│ ├─ CreateBookRequest               │
│ ├─ UpdateBookRequest               │
│ └─ BookResponse                    │
│                                      │
│ Validators (FluentValidation)      │
│ ├─ CreateBookRequestValidator      │
│ └─ UpdateBookRequestValidator      │
│                                      │
│ Middleware (GlobalExceptionHandler)│
│ └─ Catches & formats exceptions    │
└────────────────┬─────────────────────┘
                 │
┌────────────────▼─────────────────────┐
│  Domain Layer (Phase 1B)            │
│  BookService, Book entity           │
└────────────────┬─────────────────────┘
                 │
┌────────────────▼─────────────────────┐
│  Infrastructure Layer (Phase 1C)    │
│  BookRepository, DbContext          │
└────────────────┬─────────────────────┘
                 │
┌────────────────▼─────────────────────┐
│  PostgreSQL Database                │
└──────────────────────────────────────┘
```

---

## 📁 Files Created

### 1. **BookDtos.cs** (~150 lines)

Data Transfer Objects for API communication.

**What are DTOs?**
```
Entity (Domain) ≠ DTO (API)

Book Entity:
- Id, Title, Author, ISBN, Description, Price
- Status (enum), ImageUrl
- PublishedDate, CreatedAt, UpdatedAt
- Business methods: Publish(), Archive()
- Validation

CreateBookRequest DTO:
- Title, Author, ISBN, Description, Price, ImageUrl
- Purpose: Tell API what to create
- No Id (generated), no timestamps

BookResponse DTO:
- All public data user should see
- Status as string "Published" not enum
- Timestamps for audit trail
- No business methods
```

**Types included:**

| DTO | Purpose | Usage |
|-----|---------|-------|
| `CreateBookRequest` | POST request body | Create new book |
| `UpdateBookRequest` | PUT request body | Update book |
| `BookResponse` | GET response body | Return book data |
| `BookStatisticsResponse` | Statistics response | Dashboard |
| `ErrorResponse` | Error response | All errors |

---

### 2. **BookValidators.cs** (~140 lines)

FluentValidation validators for request validation.

**What is FluentValidation?**

Traditional validation (❌ Bad):
```csharp
if (string.IsNullOrEmpty(request.Title)) 
    throw new ValidationException("Title required");
if (request.Title.Length > 500) 
    throw new ValidationException("Title too long");
if (request.Price <= 0) 
    throw new ValidationException("Price invalid");
// ... more checks scattered throughout
```

FluentValidation (✅ Good):
```csharp
RuleFor(x => x.Title)
    .NotEmpty().WithMessage("Title required")
    .MaximumLength(500).WithMessage("Title too long");

RuleFor(x => x.Price)
    .GreaterThan(0).WithMessage("Price must be positive");
```

**Validators included:**

| Validator | Validates |
|-----------|-----------|
| `CreateBookRequestValidator` | POST request body |
| `UpdateBookRequestValidator` | PUT request body |

**Validation rules:**

```csharp
Title:
  - Not empty
  - Max 500 chars
  - Min 1 char

Author:
  - Not empty
  - Max 300 chars

ISBN:
  - Not empty
  - Must be valid ISBN-13 format (13 digits)
  - Custom validation: BeValidISBN()

Description:
  - Not empty
  - Max 5000 chars
  - Min 10 chars

Price:
  - Greater than 0
  - Less than or equal to 10000

ImageUrl (optional):
  - Must be valid URL (http/https)
  - Max 1000 chars
  - Custom validation: BeValidUrl()
```

**Custom validators:**

```csharp
private static bool BeValidISBN(string isbn)
{
    // Remove hyphens: "978-0-451-52494-2" → "9780451524942"
    var isbnDigits = isbn.Replace("-", "");
    
    // Check is 13 digits
    return isbnDigits.Length == 13 && isbnDigits.All(char.IsDigit);
}

private static bool BeValidUrl(string? url)
{
    if (string.IsNullOrWhiteSpace(url))
        return true; // Optional field
    
    // Try to parse as URL
    return Uri.TryCreate(url, UriKind.Absolute, out var result)
        && (result.Scheme == Uri.UriSchemeHttp 
            || result.Scheme == Uri.UriSchemeHttps);
}
```

---

### 3. **GlobalExceptionMiddleware.cs** (~80 lines)

Global error handler for all unhandled exceptions.

**What is Middleware?**

Middleware = code that runs on EVERY request/response

```
Request Pipeline:
HTTP Request
    ↓
Auth Middleware
    ↓
CORS Middleware
    ↓
GlobalExceptionMiddleware ← We are here
    ↓
Controller (BooksController)
    ↓
Domain Layer (BookService)
    ↓
Infrastructure (Repository)
    ↓
Database
    ↓
Response back through same pipeline
```

**What it does:**

```
WITHOUT middleware:
try { await controller.CreateBook() }
catch { /* Generic 500 error */ }

WITH middleware:
ArgumentException → 400 Bad Request
KeyNotFoundException → 404 Not Found
InvalidOperationException → 409 Conflict
Other → 500 Internal Server Error
```

**Exception mapping:**

```csharp
exception switch
{
    // 400 Bad Request - Invalid input
    ArgumentException => { Status = 400, ErrorCode = "VALIDATION_ERROR" },
    
    // 404 Not Found
    KeyNotFoundException => { Status = 404, ErrorCode = "NOT_FOUND" },
    
    // 409 Conflict - Invalid operation
    InvalidOperationException => { Status = 409, ErrorCode = "INVALID_OPERATION" },
    
    // 500 Internal Server Error - Unexpected
    _ => { Status = 500, ErrorCode = "INTERNAL_ERROR" }
}
```

**Benefits:**

✅ Consistent error responses  
✅ Proper HTTP status codes  
✅ Centralized error logging  
✅ No try-catch in every controller method  

---

### 4. **BooksController.cs** (~500 lines)

The main REST API controller with all endpoints.

**Controller responsibilities:**

```
✅ Receive HTTP requests
✅ Parse query/body parameters
✅ Call domain service
✅ Map domain models to DTOs
✅ Return HTTP responses

❌ Business logic (that's service)
❌ Database access (that's repository)
❌ Complex validation (that's validators)
```

**Endpoints implemented:**

#### **GET Endpoints**

| Endpoint | Returns | Purpose |
|----------|---------|---------|
| `GET /api/books` | `BookResponse[]` | All books |
| `GET /api/books/{id}` | `BookResponse` | One book |
| `GET /api/books/published` | `BookResponse[]` | Published only |
| `GET /api/books/status/{status}` | `BookResponse[]` | By status |
| `GET /api/books/statistics` | `BookStatisticsResponse` | Dashboard stats |

#### **POST (Create)**

| Endpoint | Request | Response |
|----------|---------|----------|
| `POST /api/books` | `CreateBookRequest` | 201 Created + `BookResponse` |

#### **PUT (Update)**

| Endpoint | Request | Response |
|----------|---------|----------|
| `PUT /api/books/{id}` | `UpdateBookRequest` | 200 OK + `BookResponse` |

#### **DELETE**

| Endpoint | Response |
|----------|----------|
| `DELETE /api/books/{id}` | 204 No Content |

#### **State Changes**

| Endpoint | Purpose |
|----------|---------|
| `POST /api/books/{id}/publish` | Publish book |
| `POST /api/books/{id}/archive` | Archive book |
| `POST /api/books/{id}/discontinue` | Discontinue book |

**Example: Create Book**

```
REQUEST:
POST /api/books
Content-Type: application/json

{
  "title": "1984",
  "author": "George Orwell",
  "isbn": "978-0-451-52494-2",
  "description": "A dystopian novel about totalitarian surveillance",
  "price": 15.99,
  "imageUrl": "https://example.com/covers/1984.jpg"
}

FLOW:
1. ASP.NET deserializes to CreateBookRequest
2. Validators run (check all rules)
3. If validation fails → 400 Bad Request
4. Controller calls: bookService.CreateBookAsync(...)
5. Service checks business rules (ISBN unique)
6. If business error → Conflict (409)
7. Creates Book entity
8. Saves to database
9. Returns: 201 Created with BookResponse

RESPONSE:
HTTP/1.1 201 Created
Location: /api/books/123e4567-e89b-12d3-a456-426614174000
Content-Type: application/json

{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "1984",
  "author": "George Orwell",
  "isbn": "978-0-451-52494-2",
  "description": "A dystopian novel...",
  "price": 15.99,
  "status": "Draft",
  "imageUrl": "https://example.com/covers/1984.jpg",
  "publishedDate": null,
  "createdAt": "2024-03-11T10:30:00Z",
  "updatedAt": "2024-03-11T10:30:00Z"
}
```

**Error handling:**

```
400 Bad Request:
{
  "status": 400,
  "message": "Description must be at least 10 characters",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2024-03-11T10:30:00Z"
}

409 Conflict:
{
  "status": 409,
  "message": "ISBN '978-0-451-52494-2' already exists",
  "errorCode": "ISBN_DUPLICATE",
  "timestamp": "2024-03-11T10:30:00Z"
}

404 Not Found:
{
  "status": 404,
  "message": "Book not found",
  "errorCode": "NOT_FOUND",
  "timestamp": "2024-03-11T10:30:00Z"
}
```

---

## 🔄 Request Flow

### **Create Book Example:**

```
1. Frontend/Client
   POST /api/books
   { "title": "1984", "author": "Orwell", ... }
   ↓

2. HTTP Pipeline
   - Parse JSON to CreateBookRequest
   - Run validators
   ↓ If validation fails
   - Return 400 Bad Request
   ↓

3. BooksController.CreateBook()
   - Check errors
   - Call bookService.CreateBookAsync()
   ↓

4. BookService.CreateBookAsync()
   - Check ISBN is unique
   - Create Book entity (validates)
   - Call repository.CreateAsync()
   ↓

5. BookRepository.CreateAsync()
   - dbContext.Books.Add(book)
   - SaveChangesAsync()
   ↓

6. Entity Framework Core
   - Generate SQL
   - Execute against PostgreSQL
   ↓

7. PostgreSQL Database
   - Insert into Books table
   - Return success
   ↓

8. Response back up the chain
   - Repository ✓
   - Service ✓
   - Controller maps to DTO
   - Return 201 Created + BookResponse
   ↓

9. Frontend/Client receives
   HTTP 201 Created
   { "id": "123...", "title": "1984", ... }
```

---

## 📊 Complete API Routes

```
GET    /api/books
GET    /api/books/{id}
GET    /api/books/published
GET    /api/books/status/{status}
GET    /api/books/statistics
POST   /api/books
PUT    /api/books/{id}
DELETE /api/books/{id}
POST   /api/books/{id}/publish
POST   /api/books/{id}/archive
POST   /api/books/{id}/discontinue
```

---

## 📖 Swagger/OpenAPI Documentation

Access at: `http://localhost:5000/swagger`

Shows:
- All endpoints
- Request/response examples
- Error codes
- Try it out! (Test endpoints directly)

```bash
# Build should include XML comments
# So Swagger shows all /// <summary> comments
```

---

## ✅ Program.cs Updates

Added registrations:

```csharp
// FluentValidation
builder.Services.AddValidatorsFromAssemblyContaining<CreateBookRequestValidator>();

// Global exception middleware
app.UseGlobalExceptionMiddleware();
```

---

## 🧪 How to Test (Manual)

### **Using curl:**

```bash
# Create a book
curl -X POST http://localhost:5000/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "1984",
    "author": "George Orwell",
    "isbn": "978-0-451-52494-2",
    "description": "A dystopian novel about totalitarian surveillance",
    "price": 15.99
  }'

# Get all books
curl http://localhost:5000/api/books

# Get one book (replace ID)
curl http://localhost:5000/api/books/123e4567-e89b-12d3-a456-426614174000

# Update book
curl -X PUT http://localhost:5000/api/books/{id} \
  -H "Content-Type: application/json" \
  -d '{ "title": "New Title", ... }'

# Publish book
curl -X POST http://localhost:5000/api/books/{id}/publish

# Delete book
curl -X DELETE http://localhost:5000/api/books/{id}
```

### **Using Swagger UI:**

1. Go to http://localhost:5000/swagger
2. Click "Try it out" on any endpoint
3. Fill in parameters
4. Click "Execute"
5. See response

---

## 🎯 HTTP Status Codes Used

| Code | Meaning | When |
|------|---------|------|
| 200 | OK | Successful GET/PUT |
| 201 | Created | Successful POST |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation failed |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Business rule violation (e.g., duplicate ISBN) |
| 500 | Internal Server Error | Unexpected error |

---

## 🏆 Best Practices Implemented

| Practice | Where | Why |
|----------|-------|-----|
| **DTOs** | BookDtos.cs | Separate API from domain |
| **Validation** | Validators | Catch bad input early |
| **Exception Middleware** | GlobalExceptionMiddleware | Consistent error handling |
| **Logging** | On each endpoint | Debug & monitor |
| **Status Codes** | BooksController | RESTful compliance |
| **Async/Await** | All methods | Scalable |
| **Dependency Injection** | Program.cs | Testability |
| **Documentation** | Swagger + comments | Self-documented API |

---

## 📚 Architecture Layers Summary

```
API Layer (Phase 1D) ✅
├─ Controllers (HTTP entry point)
├─ DTOs (request/response models)
├─ Validators (input validation)
└─ Middleware (error handling)
     ↓
Domain Layer (Phase 1B) ✅
├─ Entities (Book)
├─ Services (BookService)
├─ Interfaces (IBookRepository)
└─ Value Objects (Result, BookStatus)
     ↓
Infrastructure Layer (Phase 1C) ✅
├─ DbContext (EF Core)
├─ Repository (BookRepository)
└─ Extensions (DI setup)
     ↓
Database Layer ✅
└─ PostgreSQL (Books table)
```

---

## 🚀 Stunning Feature: Swagger UI

When you run the app, navigate to `/swagger`

You can:
- ✅ See all endpoints documented
- ✅ Try endpoints interactively
- ✅ See request/response examples
- ✅ Download API specification (OpenAPI JSON)

Try on a book creation:
1. Click POST /api/books
2. Click "Try it out"
3. Fill example data
4. Click "Execute"
5. See request & response!

---

## 🎓 Learning Checkpoint

After Phase 1D, you have:

✅ Full REST API  
✅ Input validation  
✅ Error handling  
✅ Data mapping  
✅ API documentation  
✅ Self-testing capability  

You can now make HTTP requests and get structured responses!

---

## 🚀 What's Next: Phase 1E

**Phase 1E - Testing:**
1. Unit tests for BookService
2. Repository tests with Testcontainers
3. Integration tests for API endpoints
4. Test coverage > 80%

---

## 💡 Key Concepts Mastered

| Concept | What | Where | Why |
|---------|------|-------|-----|
| **Controller** | HTTP handler | BooksController | Receive requests |
| **DTO** | Data transfer object | DTOs.cs | Separate API from domain |
| **Validator** | Input checker | Validators.cs | Fail fast on bad input |
| **Middleware** | Request processor | GlobalExceptionMiddleware | Global error handling |
| **Exception Handling** | Error conversion | Middleware | Consistent responses |
| **Routing** | URL mapping | Controller attributes | Know which code runs |
| **HTTP Status** | Response codes | BooksController | REST compliance |

---

## ✨ Summary

> **API Layer = User-Facing Interface**
> 
> - Controllers receive HTTP requests
> - Validators check input is valid
> - DTOs transfer data (not entities!)
> - Service does business logic
> - Middleware handles errors
> - Swagger documents everything
> - Client gets structured responses
> - Errors are predictable and helpful

---

Ready for Phase 1E (Testing)? 🧪

Or commit first:

```bash
git add services/book-service/
git commit -m "feat: implement API layer (Phase 1D)

- Add BooksController with full REST API
- Add DTOs for request/response serialization
- Add FluentValidation for input validation
- Add GlobalExceptionMiddleware for error handling
- Add extensive Swagger/OpenAPI documentation
- Implement all CRUD endpoints
- Add state change endpoints (publish, archive, discontinue)
- Comprehensive logging and error codes"
```
