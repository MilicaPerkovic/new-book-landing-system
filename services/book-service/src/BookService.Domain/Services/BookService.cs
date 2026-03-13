using BookService.Domain.Entities;
using BookService.Domain.Interfaces;
using BookService.Domain.Common;
using BookService.Domain.Enums;

namespace BookService.Domain.Services;

/// <summary>
/// BookService - Contains business logic for book operations.
/// 
/// WHY A SEPARATE SERVICE?
/// The Repository handles DATABASE operations.
/// The Service handles BUSINESS LOGIC.
/// 
/// EXAMPLE:
/// Repository: "Save this book to the database"
/// Service: "Check if ISBN is unique before calling repository to save"
/// 
/// LAYERING:
/// API Controller (HTTP)
///    ↓
/// BookService (Business Logic) ← You are here
///    ↓
/// IBookRepository (Database)
/// 
/// BENEFITS:
/// - Logic is reusable (web API, job queue, message broker all use same service)
/// - Easy to test (mock the repository)
/// - Clear separation of concerns
/// - Business logic is not tied to HTTP
/// 
/// RESPONSIBILITIES:
/// - Validate business rules
/// - Coordinate between repository and other services
/// - Handle transactions if needed
/// - Return business-friendly results
/// 
/// DOES NOT:
/// - Handle HTTP stuff (status codes, headers) - that's API layer
/// - Access database directly - that's repository
/// - Know about Entity Framework, PostgreSQL, etc.
/// </summary>
public class BookService
{
    private readonly IBookRepository _bookRepository;

    /// <summary>
    /// Constructor - receives repository via Dependency Injection.
    /// 
    /// WHY INJECT?
    /// Instead of: var repository = new BookRepository();
    /// We receive: IBookRepository repository (parameter)
    /// 
    /// BENEFIT: Can inject different implementations
    /// - Test: Inject fake repository with test data
    /// - Production: Inject real PostgreSQL repository
    /// 
    /// This makes testing SO much easier!
    /// </summary>
    public BookService(IBookRepository bookRepository)
    {
        _bookRepository = bookRepository ?? throw new ArgumentNullException(nameof(bookRepository));
    }

    // ============================================================
    // GET OPERATIONS
    // ============================================================

    /// <summary>
    /// Get a book by ID.
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.GetBookByIdAsync(bookId);
    ///   if (!result.IsSuccess)
    ///   {
    ///       Console.WriteLine($"Error: {result.ErrorMessage}");
    ///       return;
    ///   }
    ///   Console.WriteLine($"Found: {result.Data.Title}");
    /// 
    /// RETURNS: Result with book if found, error if not found
    /// </summary>
    public async Task<Result<Book>> GetBookByIdAsync(
        Guid id,
        CancellationToken cancellationToken = default)
    {
        if (id == Guid.Empty)
            return Result<Book>.Failure("Invalid book ID");

        var book = await _bookRepository.GetByIdAsync(id, cancellationToken);

        if (book == null)
            return Result<Book>.Failure("Book not found", "NOT_FOUND");

        return Result<Book>.Success(book);
    }

    /// <summary>
    /// Get all books.
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.GetAllBooksAsync();
    ///   if (result.IsSuccess)
    ///   {
    ///       foreach (var book in result.Data)
    ///       {
    ///           Console.WriteLine($"- {book.Title}");
    ///       }
    ///   }
    /// 
    /// RETURNS: Result with list of books (could be empty)
    /// 
    /// TODO: Consider adding pagination
    /// GetBooksAsync(int page, int pageSize)
    /// </summary>
    public async Task<Result<IEnumerable<Book>>> GetAllBooksAsync(
        CancellationToken cancellationToken = default)
    {
        var books = await _bookRepository.GetAllAsync(cancellationToken);
        return Result<IEnumerable<Book>>.Success(books);
    }

    /// <summary>
    /// Get books by status (Published, Draft, Archived, etc.).
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.GetPublishedBooksAsync();
    ///   var publishedBooks = result.Data;
    /// 
    /// RETURNS: Result with list of published books
    /// </summary>
    public async Task<Result<IEnumerable<Book>>> GetPublishedBooksAsync(
        CancellationToken cancellationToken = default)
    {
        var books = await _bookRepository.GetByStatusAsync(BookStatus.Published, cancellationToken);
        return Result<IEnumerable<Book>>.Success(books);
    }

    /// <summary>Get books by specific status</summary>
    public async Task<Result<IEnumerable<Book>>> GetBooksByStatusAsync(
        BookStatus status,
        CancellationToken cancellationToken = default)
    {
        var books = await _bookRepository.GetByStatusAsync(status, cancellationToken);
        return Result<IEnumerable<Book>>.Success(books);
    }

    // ============================================================
    // CREATE OPERATION
    // ============================================================

    /// <summary>
    /// Create a new book.
    /// 
    /// BUSINESS RULES ENFORCED:
    /// 1. All required fields must be provided
    /// 2. ISBN must be unique (not already in database)
    /// 3. ISBN format must be valid
    /// 4. Price must be positive
    /// etc.
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.CreateBookAsync(
    ///       title: "1984",
    ///       author: "George Orwell",
    ///       isbn: "978-0-451-52494-2",
    ///       description: "A dystopian novel...",
    ///       price: 15.99m
    ///   );
    ///
    ///   if (!result.IsSuccess)
    ///   {
    ///       // Handle error
    ///       Console.WriteLine($"Failed to create book: {result.ErrorMessage}");
    ///       return;
    ///   }
    ///
    ///   // Success
    ///   var createdBook = result.Data;
    ///   Console.WriteLine($"Book created with ID: {createdBook.Id}");
    /// 
    /// RETURNS: Result with created book if successful, error otherwise
    /// </summary>
    public async Task<Result<Book>> CreateBookAsync(
        string title,
        string author,
        string isbn,
        string description,
        decimal price,
        string? imageUrl = null,
        CancellationToken cancellationToken = default)
    {
        try
        {
            // Check if ISBN already exists
            var isbnExists = await _bookRepository.IsbnExistsAsync(isbn, cancellationToken);
            if (isbnExists)
                return Result<Book>.Failure($"ISBN '{isbn}' already exists", "ISBN_DUPLICATE");

            // Create entity (this validates all inputs)
            var book = Book.CreateNew(title, author, isbn, description, price, imageUrl);

            // Save to database
            await _bookRepository.CreateAsync(book, cancellationToken);

            // Return successful result with created book
            return Result<Book>.Success(book);
        }
        catch (ArgumentException ex)
        {
            // Validation error from Book.CreateNew()
            return Result<Book>.Failure(ex.Message, "VALIDATION_ERROR");
        }
        catch (Exception ex)
        {
            // Unexpected error
            return Result<Book>.Failure($"Failed to create book: {ex.Message}", "CREATE_ERROR");
        }
    }

    // ============================================================
    // UPDATE OPERATION
    // ============================================================

    /// <summary>
    /// Update an existing book.
    /// 
    /// BUSINESS RULES:
    /// 1. Book must exist
    /// 2. New data must be valid
    /// 3. Cannot change ISBN property (it's locked after creation)
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.UpdateBookAsync(
    ///       id: bookId,
    ///       title: "1984 - Anniversary Edition",
    ///       author: "George Orwell",
    ///       description: "Updated description...",
    ///       price: 16.99m
    ///   );
    ///
    ///   if (result.IsSuccess)
    ///   {
    ///       Console.WriteLine("Book updated");
    ///   }
    /// 
    /// RETURNS: Result with updated book if successful, error otherwise
    /// </summary>
    public async Task<Result<Book>> UpdateBookAsync(
        Guid id,
        string title,
        string author,
        string description,
        decimal price,
        string? imageUrl = null,
        CancellationToken cancellationToken = default)
    {
        try
        {
            // Get the existing book
            var book = await _bookRepository.GetByIdAsync(id, cancellationToken);
            if (book == null)
                return Result<Book>.Failure("Book not found", "NOT_FOUND");

            // Update the book (this validates inputs)
            book.Update(title, author, description, price, imageUrl);

            // Save changes
            await _bookRepository.UpdateAsync(book, cancellationToken);

            return Result<Book>.Success(book);
        }
        catch (ArgumentException ex)
        {
            return Result<Book>.Failure(ex.Message, "VALIDATION_ERROR");
        }
        catch (Exception ex)
        {
            return Result<Book>.Failure($"Failed to update book: {ex.Message}", "UPDATE_ERROR");
        }
    }

    // ============================================================
    // DELETE OPERATION
    // ============================================================

    /// <summary>
    /// Delete a book.
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.DeleteBookAsync(bookId);
    ///   
    ///   if (result.IsSuccess)
    ///   {
    ///       Console.WriteLine("Book deleted successfully");
    ///   }
    ///   else
    ///   {
    ///       Console.WriteLine($"Error: {result.ErrorMessage}");
    ///   }
    /// 
    /// RETURNS: Result with success/failure
    /// 
    /// TODO: Consider soft delete instead of hard delete
    /// (Mark as deleted instead of removing completely)
    /// This preserves referential integrity with orders, etc.
    /// </summary>
    public async Task<Result> DeleteBookAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            // Check book exists
            var exists = await _bookRepository.ExistsAsync(id, cancellationToken);
            if (!exists)
                return Result.Failure("Book not found", "NOT_FOUND");

            // Delete
            await _bookRepository.DeleteAsync(id, cancellationToken);

            return Result.Success();
        }
        catch (Exception ex)
        {
            return Result.Failure($"Failed to delete book: {ex.Message}", "DELETE_ERROR");
        }
    }

    // ============================================================
    // PUBLISH/ARCHIVE/DISCONTINUE OPERATIONS
    // ============================================================

    /// <summary>
    /// Publish a book (make it visible to users).
    /// 
    /// EXAMPLE:
    ///   var result = await bookService.PublishBookAsync(bookId);
    /// </summary>
    public async Task<Result<Book>> PublishBookAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            var book = await _bookRepository.GetByIdAsync(id, cancellationToken);
            if (book == null)
                return Result<Book>.Failure("Book not found", "NOT_FOUND");

            book.Publish();
            await _bookRepository.UpdateAsync(book, cancellationToken);

            return Result<Book>.Success(book);
        }
        catch (InvalidOperationException ex)
        {
            return Result<Book>.Failure(ex.Message, "INVALID_OPERATION");
        }
        catch (Exception ex)
        {
            return Result<Book>.Failure($"Failed to publish book: {ex.Message}", "PUBLISH_ERROR");
        }
    }

    /// <summary>Archive a book (hide from search but keep in database).</summary>
    public async Task<Result<Book>> ArchiveBookAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            var book = await _bookRepository.GetByIdAsync(id, cancellationToken);
            if (book == null)
                return Result<Book>.Failure("Book not found", "NOT_FOUND");

            book.Archive();
            await _bookRepository.UpdateAsync(book, cancellationToken);

            return Result<Book>.Success(book);
        }
        catch (InvalidOperationException ex)
        {
            return Result<Book>.Failure(ex.Message, "INVALID_OPERATION");
        }
        catch (Exception ex)
        {
            return Result<Book>.Failure($"Failed to archive book: {ex.Message}", "ARCHIVE_ERROR");
        }
    }

    /// <summary>Discontinue a book (mark as unavailable).</summary>
    public async Task<Result<Book>> DiscontinueBookAsync(Guid id, CancellationToken cancellationToken = default)
    {
        try
        {
            var book = await _bookRepository.GetByIdAsync(id, cancellationToken);
            if (book == null)
                return Result<Book>.Failure("Book not found", "NOT_FOUND");

            book.Discontinue();
            await _bookRepository.UpdateAsync(book, cancellationToken);

            return Result<Book>.Success(book);
        }
        catch (InvalidOperationException ex)
        {
            return Result<Book>.Failure(ex.Message, "INVALID_OPERATION");
        }
        catch (Exception ex)
        {
            return Result<Book>.Failure($"Failed to discontinue book: {ex.Message}", "DISCONTINUE_ERROR");
        }
    }

    // ============================================================
    // STATISTICS OPERATIONS
    // ============================================================

    /// <summary>
    /// Get statistics about books in the system.
    /// 
    /// EXAMPLE:
    ///   var stats = await bookService.GetStatisticsAsync();
    ///   Console.WriteLine($"Published books: {stats.PublishedCount}");
    ///   Console.WriteLine($"Draft books: {stats.DraftCount}");
    /// </summary>
    public async Task<Result<BookStatistics>> GetStatisticsAsync(CancellationToken cancellationToken = default)
    {
        try
        {
            var publishedCount = await _bookRepository.GetCountByStatusAsync(BookStatus.Published, cancellationToken);
            var draftCount = await _bookRepository.GetCountByStatusAsync(BookStatus.Draft, cancellationToken);
            var archivedCount = await _bookRepository.GetCountByStatusAsync(BookStatus.Archived, cancellationToken);
            var discontinuedCount = await _bookRepository.GetCountByStatusAsync(BookStatus.Discontinued, cancellationToken);

            var stats = new BookStatistics
            {
                PublishedCount = publishedCount,
                DraftCount = draftCount,
                ArchivedCount = archivedCount,
                DiscontinuedCount = discontinuedCount,
                TotalCount = publishedCount + draftCount + archivedCount + discontinuedCount
            };

            return Result<BookStatistics>.Success(stats);
        }
        catch (Exception ex)
        {
            return Result<BookStatistics>.Failure($"Failed to get statistics: {ex.Message}");
        }
    }
}

/// <summary>
/// Book statistics DTO.
/// 
/// WHY DTO?
/// Data Transfer Object - Used to transfer data between layers.
/// Simpler than the full Book entity.
/// </summary>
public class BookStatistics
{
    public int PublishedCount { get; set; }
    public int DraftCount { get; set; }
    public int ArchivedCount { get; set; }
    public int DiscontinuedCount { get; set; }
    public int TotalCount { get; set; }
}
