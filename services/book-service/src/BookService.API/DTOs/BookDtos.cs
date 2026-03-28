using System;
using BookService.Domain.Enums;

namespace BookService.API.DTOs;

/// <summary>
/// CreateBookRequest - DTO for creating a new book
/// 
/// DTO = Data Transfer Object
/// WHY USE DTOs?
/// 
/// 1. SEPARATION: API input != domain entity
///    Reason: API needs flexible input, domain entity has strict rules
/// 
/// 2. VALIDATION: Declare requirements upfront
///    Example: [Required], [MaxLength(500)]
///    Applied automatically by ASP.NET Core
/// 
/// 3. SECURITY: Don't expose internal fields
///    Example: Don't have "DatabaseId" in request
/// 
/// 4. VERSIONING: Can change DTO without affecting entity
///    Example: Add/remove fields for different API versions
/// 
/// FLOW:
/// 1. Client sends: { "title": "1984", "author": "Orwell", ... }
/// 2. ASP.NET Core deserializes to CreateBookRequest
/// 3. Validators check it's valid
/// 4. Map to domain: Book.CreateNew(...)
/// 5. Save to database
/// 
/// EXAMPLE:
/// POST /api/books
/// Content-Type: application/json
/// 
/// {
///   "title": "1984",
///   "author": "George Orwell",
///   "isbn": "978-0-451-52494-2",
///   "description": "A dystopian novel about totalitarian surveillance",
///   "price": 15.99,
///   "imageUrl": "https://example.com/covers/1984.jpg"
/// }
/// </summary>
public class CreateBookRequest
{
    /// <summary>Book title</summary>
    /// <example>1984</example>
    public string Title { get; set; } = string.Empty;

    /// <summary>Author name</summary>
    /// <example>George Orwell</example>
    public string Author { get; set; } = string.Empty;

    /// <summary>ISBN-13 (format: XXX-X-XXXXX-X)</summary>
    /// <example>978-0-451-52494-2</example>
    public string ISBN { get; set; } = string.Empty;

    /// <summary>Book description/summary</summary>
    /// <example>A dystopian novel about totalitarian surveillance</example>
    public string Description { get; set; } = string.Empty;

    /// <summary>Price in EUR</summary>
    /// <example>15.99</example>
    public decimal Price { get; set; }

    /// <summary>Cover image URL (optional)</summary>
    /// <example>https://example.com/covers/1984.jpg</example>
    public string? ImageUrl { get; set; }
}

/// <summary>
/// UpdateBookRequest - DTO for updating an existing book
/// 
/// NOTE: We DON'T include ISBN in update requests
/// WHY? ISBN is a unique identifier - it shouldn't change
/// If we allowed changing ISBN:
/// - Introduces complexity
/// - Could cause constraint violations
/// - Real-world: books have immutable identifiers
/// 
/// EXAMPLE:
/// PUT /api/books/{id}
/// Content-Type: application/json
/// 
/// {
///   "title": "1984 - Classic Edition",
///   "author": "George Orwell",
///   "description": "Updated description...",
///   "price": 18.99,
///   "imageUrl": "https://example.com/covers/1984-v2.jpg"
/// }
/// </summary>
public class UpdateBookRequest
{
    /// <summary>New title</summary>
    public string Title { get; set; } = string.Empty;

    /// <summary>New author name</summary>
    public string Author { get; set; } = string.Empty;

    /// <summary>New description</summary>
    public string Description { get; set; } = string.Empty;

    /// <summary>New price</summary>
    public decimal Price { get; set; }

    /// <summary>New cover image URL (optional)</summary>
    public string? ImageUrl { get; set; }
}

/// <summary>
/// BookResponse - DTO for responding with book data
/// 
/// WHY SEPARATE FROM ENTITY?
/// 1. Entity has internals (like CreatedAt, UpdatedAt)
/// 2. Response only includes public data
/// 3. Can add computed fields (e.g., status as text instead of number)
/// 
/// EXAMPLE RESPONSE:
/// HTTP/1.1 200 OK
/// Content-Type: application/json
/// 
/// {
///   "id": "123e4567-e89b-12d3-a456-426614174000",
///   "title": "1984",
///   "author": "George Orwell",
///   "isbn": "978-0-451-52494-2",
///   "description": "A dystopian novel...",
///   "price": 15.99,
///   "status": "Published",
///   "imageUrl": "https://example.com/covers/1984.jpg",
///   "publishedDate": "2024-03-10T12:30:00Z",
///   "createdAt": "2024-03-10T10:00:00Z",
///   "updatedAt": "2024-03-10T12:30:00Z"
/// }
/// </summary>
public class BookResponse
{
    /// <summary>Unique book identifier</summary>
    public Guid Id { get; set; }

    /// <summary>Book title</summary>
    public string Title { get; set; } = string.Empty;

    /// <summary>Author name</summary>
    public string Author { get; set; } = string.Empty;

    /// <summary>ISBN-13</summary>
    public string ISBN { get; set; } = string.Empty;

    /// <summary>Book description</summary>
    public string Description { get; set; } = string.Empty;

    /// <summary>Price in EUR</summary>
    public decimal Price { get; set; }

    /// <summary>Book status: "Draft", "Published", "Archived", "Discontinued"</summary>
    public string Status { get; set; } = string.Empty;

    /// <summary>Cover image URL</summary>
    public string? ImageUrl { get; set; }

    /// <summary>When the book was published (null if not published yet)</summary>
    public DateTime? PublishedDate { get; set; }

    /// <summary>When this record was created</summary>
    public DateTime CreatedAt { get; set; }

    /// <summary>When this record was last updated</summary>
    public DateTime UpdatedAt { get; set; }
}

/// <summary>
/// BookStatisticsResponse - DTO for statistics</summary>
public class BookStatisticsResponse
{
    /// <summary>Number of published books</summary>
    public int PublishedCount { get; set; }

    /// <summary>Number of draft books</summary>
    public int DraftCount { get; set; }

    /// <summary>Number of archived books</summary>
    public int ArchivedCount { get; set; }

    /// <summary>Number of discontinued books</summary>
    public int DiscontinuedCount { get; set; }

    /// <summary>Total number of books</summary>
    public int TotalCount { get; set; }
}

/// <summary>
/// ErrorResponse - DTO for error responses
/// 
/// EXAMPLE:
/// HTTP/1.1 400 Bad Request
/// Content-Type: application/json
/// 
/// {
///   "status": 400,
///   "message": "ISBN already exists",
///   "errorCode": "ISBN_DUPLICATE",
///   "timestamp": "2024-03-10T12:30:00Z"
/// }
/// </summary>
public class ErrorResponse
{
    /// <summary>HTTP status code</summary>
    public int Status { get; set; }

    /// <summary>Error message</summary>
    public string Message { get; set; } = string.Empty;

    /// <summary>Error code for client-side handling</summary>
    public string? ErrorCode { get; set; }

    /// <summary>Timestamp of the error</summary>
    public DateTime Timestamp { get; set; } = DateTime.UtcNow;
}
