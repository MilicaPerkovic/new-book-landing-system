using BookService.API.DTOs;
using BookService.Domain.Entities;
using BookService.Domain.Enums;
using BookService.Domain.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using DomainBookService = BookService.Domain.Services.BookService;

namespace BookService.API.Controllers;

/// <summary>
/// BooksController - REST API endpoints for book operations
/// 
/// WHAT IS A CONTROLLER?
/// Controller = HTTP endpoint handler
/// It receives HTTP requests and returns HTTP responses
/// 
/// ROUTING:
/// [ApiController]        → ASP.NET Core will treat this as API controller
/// [Route("api/[controller]")] → Base route is /api/books
/// 
/// METHOD ROUTING:
/// [HttpGet]              → GET /api/books
/// [HttpGet("{id}")]      → GET /api/books/{id}
/// [HttpPost]             → POST /api/books
/// [HttpPut("{id}")]      → PUT /api/books/{id}
/// [HttpDelete("{id}")]   → DELETE /api/books/{id}
/// 
/// RESPONSIBILITY:
/// 1. Receive HTTP requests
/// 2. Parse and validate input
/// 3. Call domain service
/// 4. Map domain models to DTOs
/// 5. Return HTTP responses
/// 
/// DOES NOT:
/// - Business logic (that's BookService)
/// - Database access (that's Repository)
/// - Validation rules (that's FluentValidation)
/// 
/// API DOCUMENTATION (Swagger):
/// - [Summary] - Description of endpoint
/// - Example values in DTOs show up in Swagger
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class BooksController : ControllerBase
{
    private readonly DomainBookService _bookService;
    private readonly ILogger<BooksController> _logger;

    /// <summary>
    /// Constructor - receives dependencies via DI
    /// 
    /// BookService: Business logic for books
    /// ILogger: For logging debug/error information
    /// </summary>
    public BooksController(DomainBookService bookService, ILogger<BooksController> logger)
    {
        _bookService = bookService ?? throw new ArgumentNullException(nameof(bookService));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    // ============================================================
    // GET ENDPOINTS
    // ============================================================

    /// <summary>
    /// Get all books
    /// 
    /// ENDPOINT: GET /api/books
    /// 
    /// EXAMPLE REQUEST:
    /// GET /api/books HTTP/1.1
    /// 
    /// EXAMPLE RESPONSE:
    /// HTTP/1.1 200 OK
    /// [
    ///   {
    ///     "id": "123e4567-e89b-12d3-a456-426614174000",
    ///     "title": "1984",
    ///     "author": "George Orwell",
    ///     ...
    ///   }
    /// ]
    /// 
    /// RETURNS: 200 OK with array of books
    /// </summary>
    [HttpGet]
    public async Task<ActionResult<IEnumerable<BookResponse>>> GetAllBooks(CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting all books");

        var result = await _bookService.GetAllBooksAsync(cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(500, new ErrorResponse
            {
                Status = 500,
                Message = result.ErrorMessage ?? "Failed to retrieve books"
            });
        }

        // Map domain entities to response DTOs
        var response = result.Data
            .Select(b => MapBookToResponse(b))
            .ToList();

        return Ok(response);
    }

    /// <summary>
    /// Get a single book by ID
    /// 
    /// ENDPOINT: GET /api/books/{id}
    /// 
    /// EXAMPLE REQUEST:
    /// GET /api/books/123e4567-e89b-12d3-a456-426614174000 HTTP/1.1
    /// 
    /// EXAMPLE RESPONSE:
    /// HTTP/1.1 200 OK
    /// {
    ///   "id": "123e4567-e89b-12d3-a456-426614174000",
    ///   "title": "1984",
    ///   ...
    /// }
    /// 
    /// RETURNS:
    /// - 200 OK with book data if found
    /// - 404 Not Found if not found
    /// </summary>
    [HttpGet("{id}")]
    public async Task<ActionResult<BookResponse>> GetBookById(
        [FromRoute] Guid id,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting book with ID: {BookId}", id);

        var result = await _bookService.GetBookByIdAsync(id, cancellationToken);

        if (!result.IsSuccess)
        {
            return NotFound(new ErrorResponse
            {
                Status = 404,
                Message = result.ErrorMessage ?? "Book not found",
                ErrorCode = result.ErrorCode
            });
        }

        return Ok(MapBookToResponse(result.Data));
    }

    /// <summary>
    /// Get published books only
    /// 
    /// ENDPOINT: GET /api/books/published
    /// 
    /// RETURNS: Array of published books
    /// </summary>
    [HttpGet("published")]
    public async Task<ActionResult<IEnumerable<BookResponse>>> GetPublishedBooks(
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting published books");

        var result = await _bookService.GetPublishedBooksAsync(cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(500, new ErrorResponse
            {
                Status = 500,
                Message = result.ErrorMessage ?? "Failed to retrieve published books"
            });
        }

        var response = result.Data
            .Select(b => MapBookToResponse(b))
            .ToList();

        return Ok(response);
    }

    /// <summary>
    /// Get books by status
    /// 
    /// ENDPOINT: GET /api/books/status/{status}
    /// 
    /// STATUS VALUES: draft, published, archived, discontinued
    /// 
    /// EXAMPLE:
    /// GET /api/books/status/published
    /// </summary>
    [HttpGet("status/{status}")]
    public async Task<ActionResult<IEnumerable<BookResponse>>> GetBooksByStatus(
        [FromRoute] string status,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting books with status: {Status}", status);

        // Parse status from string to enum
        if (!Enum.TryParse<BookStatus>(status, ignoreCase: true, out var bookStatus))
        {
            return BadRequest(new ErrorResponse
            {
                Status = 400,
                Message = $"Invalid status: {status}. Valid values: Draft, Published, Archived, Discontinued",
                ErrorCode = "INVALID_STATUS"
            });
        }

        var result = await _bookService.GetBooksByStatusAsync(bookStatus, cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(500, new ErrorResponse
            {
                Status = 500,
                Message = result.ErrorMessage ?? "Failed to retrieve books"
            });
        }

        var response = result.Data
            .Select(b => MapBookToResponse(b))
            .ToList();

        return Ok(response);
    }

    /// <summary>
    /// Get book statistics
    /// 
    /// ENDPOINT: GET /api/books/statistics
    /// 
    /// RETURNS: Count of books by status
    /// 
    /// EXAMPLE RESPONSE:
    /// {
    ///   "publishedCount": 42,
    ///   "draftCount": 8,
    ///   "archivedCount": 5,
    ///   "discontinuedCount": 2,
    ///   "totalCount": 57
    /// }
    /// </summary>
    [HttpGet("statistics")]
    public async Task<ActionResult<BookStatisticsResponse>> GetStatistics(
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Getting book statistics");

        var result = await _bookService.GetStatisticsAsync(cancellationToken);

        if (!result.IsSuccess)
        {
            return StatusCode(500, new ErrorResponse
            {
                Status = 500,
                Message = result.ErrorMessage ?? "Failed to retrieve statistics"
            });
        }

        var response = new BookStatisticsResponse
        {
            PublishedCount = result.Data.PublishedCount,
            DraftCount = result.Data.DraftCount,
            ArchivedCount = result.Data.ArchivedCount,
            DiscontinuedCount = result.Data.DiscontinuedCount,
            TotalCount = result.Data.TotalCount
        };

        return Ok(response);
    }

    // ============================================================
    // POST (CREATE) ENDPOINTS
    // ============================================================

    /// <summary>
    /// Create a new book
    /// 
    /// ENDPOINT: POST /api/books
    /// 
    /// REQUEST BODY:
    /// {
    ///   "title": "1984",
    ///   "author": "George Orwell",
    ///   "isbn": "978-0-451-52494-2",
    ///   "description": "A dystopian novel...",
    ///   "price": 15.99
    /// }
    /// 
    /// RETURNS:
    /// - 201 Created with created book data
    /// - 400 Bad Request if validation fails
    /// - 409 Conflict if ISBN already exists
    /// </summary>
    [HttpPost]
    public async Task<ActionResult<BookResponse>> CreateBook(
        [FromBody] CreateBookRequest request,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Creating new book: {Title} by {Author}", request.Title, request.Author);

        var result = await _bookService.CreateBookAsync(
            request.Title,
            request.Author,
            request.ISBN,
            request.Description,
            request.Price,
            request.ImageUrl,
            cancellationToken);

        if (!result.IsSuccess)
        {
            // Check if it's a duplicate ISBN error
            if (result.ErrorCode == "ISBN_DUPLICATE")
            {
                return Conflict(new ErrorResponse
                {
                    Status = 409,
                    Message = result.ErrorMessage ?? "ISBN already exists",
                    ErrorCode = "ISBN_DUPLICATE"
                });
            }

            // Validation error
            return BadRequest(new ErrorResponse
            {
                Status = 400,
                Message = result.ErrorMessage ?? "Failed to create book",
                ErrorCode = result.ErrorCode ?? "VALIDATION_ERROR"
            });
        }

        var created = result.Data;
        return CreatedAtAction(nameof(GetBookById), new { id = created.Id }, MapBookToResponse(created));
    }

    // ============================================================
    // PUT (UPDATE) ENDPOINTS
    // ============================================================

    /// <summary>
    /// Update an existing book
    /// 
    /// ENDPOINT: PUT /api/books/{id}
    /// 
    /// REQUEST BODY:
    /// {
    ///   "title": "1984 - New Edition",
    ///   "author": "George Orwell",
    ///   "description": "Updated description...",
    ///   "price": 18.99
    /// }
    /// 
    /// RETURNS:
    /// - 200 OK with updated book
    /// - 404 Not Found if book doesn't exist
    /// - 400 Bad Request if validation fails
    /// </summary>
    [HttpPut("{id}")]
    public async Task<ActionResult<BookResponse>> UpdateBook(
        [FromRoute] Guid id,
        [FromBody] UpdateBookRequest request,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Updating book: {BookId}", id);

        var result = await _bookService.UpdateBookAsync(
            id,
            request.Title,
            request.Author,
            request.Description,
            request.Price,
            request.ImageUrl,
            cancellationToken);

        if (!result.IsSuccess)
        {
            if (result.ErrorCode == "NOT_FOUND")
            {
                return NotFound(new ErrorResponse
                {
                    Status = 404,
                    Message = result.ErrorMessage ?? "Book not found",
                    ErrorCode = "NOT_FOUND"
                });
            }

            return BadRequest(new ErrorResponse
            {
                Status = 400,
                Message = result.ErrorMessage ?? "Failed to update book",
                ErrorCode = result.ErrorCode ?? "VALIDATION_ERROR"
            });
        }

        return Ok(MapBookToResponse(result.Data));
    }

    // ============================================================
    // DELETE ENDPOINT
    // ============================================================

    /// <summary>
    /// Delete a book
    /// 
    /// ENDPOINT: DELETE /api/books/{id}
    /// 
    /// RETURNS:
    /// - 204 No Content if successful
    /// - 404 Not Found if book doesn't exist
    /// </summary>
    [HttpDelete("{id}")]
    public async Task<IActionResult> DeleteBook(
        [FromRoute] Guid id,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Deleting book: {BookId}", id);

        var result = await _bookService.DeleteBookAsync(id, cancellationToken);

        if (!result.IsSuccess)
        {
            return NotFound(new ErrorResponse
            {
                Status = 404,
                Message = result.ErrorMessage ?? "Book not found",
                ErrorCode = "NOT_FOUND"
            });
        }

        return NoContent();
    }

    // ============================================================
    // STATE CHANGE ENDPOINTS
    // ============================================================

    /// <summary>
    /// Publish a book (make it visible to users)
    /// 
    /// ENDPOINT: POST /api/books/{id}/publish
    /// </summary>
    [HttpPost("{id}/publish")]
    public async Task<ActionResult<BookResponse>> PublishBook(
        [FromRoute] Guid id,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Publishing book: {BookId}", id);

        var result = await _bookService.PublishBookAsync(id, cancellationToken);

        if (!result.IsSuccess)
        {
            if (result.ErrorCode == "NOT_FOUND")
            {
                return NotFound(new ErrorResponse
                {
                    Status = 404,
                    Message = result.ErrorMessage
                });
            }

            return Conflict(new ErrorResponse
            {
                Status = 409,
                Message = result.ErrorMessage,
                ErrorCode = result.ErrorCode
            });
        }

        return Ok(MapBookToResponse(result.Data));
    }

    /// <summary>
    /// Archive a book (hide from search but keep data)
    /// 
    /// ENDPOINT: POST /api/books/{id}/archive
    /// </summary>
    [HttpPost("{id}/archive")]
    public async Task<ActionResult<BookResponse>> ArchiveBook(
        [FromRoute] Guid id,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Archiving book: {BookId}", id);

        var result = await _bookService.ArchiveBookAsync(id, cancellationToken);

        if (!result.IsSuccess)
        {
            return NotFound(new ErrorResponse
            {
                Status = 404,
                Message = result.ErrorMessage
            });
        }

        return Ok(MapBookToResponse(result.Data));
    }

    /// <summary>
    /// Discontinue a book (mark as unavailable)
    /// 
    /// ENDPOINT: POST /api/books/{id}/discontinue
    /// </summary>
    [HttpPost("{id}/discontinue")]
    public async Task<ActionResult<BookResponse>> DiscontinueBook(
        [FromRoute] Guid id,
        CancellationToken cancellationToken)
    {
        _logger.LogInformation("Discontinuing book: {BookId}", id);

        var result = await _bookService.DiscontinueBookAsync(id, cancellationToken);

        if (!result.IsSuccess)
        {
            return NotFound(new ErrorResponse
            {
                Status = 404,
                Message = result.ErrorMessage
            });
        }

        return Ok(MapBookToResponse(result.Data));
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    /// <summary>
    /// Map Book entity to BookResponse DTO
    /// 
    /// WHY SEPARATE METHOD?
    /// - Reusable mapping logic
    /// - Single place to change DTO structure
    /// - Easy to test
    /// </summary>
    private static BookResponse MapBookToResponse(Book book)
    {
        return new BookResponse
        {
            Id = book.Id,
            Title = book.Title,
            Author = book.Author,
            ISBN = book.ISBN,
            Description = book.Description,
            Price = book.Price,
            Status = book.Status.ToString(),
            ImageUrl = book.ImageUrl,
            PublishedDate = book.PublishedDate,
            CreatedAt = book.CreatedAt,
            UpdatedAt = book.UpdatedAt
        };
    }
}
