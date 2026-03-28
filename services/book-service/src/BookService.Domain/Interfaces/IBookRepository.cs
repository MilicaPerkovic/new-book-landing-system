using BookService.Domain.Entities;
using BookService.Domain.Common;
using BookService.Domain.Enums;

namespace BookService.Domain.Interfaces;

/// <summary>
/// IBookRepository Interface - Defines what operations the database needs to support.
/// 
/// WHY AN INTERFACE?
/// This is the "contract" - we're saying "whatever handles database operations
/// MUST implement these methods". This allows us to:
/// 
/// 1. TESTING: Mock the repository in unit tests (fake database)
///    Example: Create a FakeBookRepository that returns test data
/// 
/// 2. FLEXIBILITY: Change database implementation without changing business logic
///    Example: Swap PostgreSQL for MongoDB, but code stays the same
/// 
/// 3. DEPENDENCY INJECTION: .NET can inject different implementations
///    In tests: Use FakeBookRepository
///    In production: Use PgSqlBookRepository
/// 
/// WHICH METHODS DO WE NEED?
/// - GetByIdAsync: Get one book by ID
/// - GetAllAsync: Get all books
/// - GetByStatusAsync: Get books by status (draft, published, etc.)
/// - CreateAsync: Insert new book into database
/// - UpdateAsync: Modify existing book
/// - DeleteAsync: Remove book from database
/// - ExistsAsync: Check if book exists
/// 
/// ASYNC/AWAIT?
/// All methods are async because database operations are slow.
/// "Async" means we don't block while waiting for the database.
/// The application can do other things while waiting.
/// </summary>
public interface IBookRepository
{
    /// <summary>
    /// Get a single book by its ID.
    /// 
    /// EXAMPLE:
    ///   var book = await repository.GetByIdAsync(new Guid("123e4567-e89b-12d3-a456-426614174000"));
    /// 
    /// RETURNS: The book if found, null if not found
    /// </summary>
    Task<Book?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get all books in the database.
    /// 
    /// EXAMPLE:
    ///   var allBooks = await repository.GetAllAsync();
    /// 
    /// RETURNS: List of all books (could be empty list)
    /// 
    /// NOTE: Be careful with large datasets!
    /// Consider adding pagination (GetPage, GetRange, etc.)
    /// </summary>
    Task<IEnumerable<Book>> GetAllAsync(CancellationToken cancellationToken = default);

    /// <summary>
    /// Get books by status (e.g., all published books).
    /// 
    /// EXAMPLE:
    ///   var publishedBooks = await repository.GetByStatusAsync(BookStatus.Published);
    /// 
    /// RETURNS: List of books with the specified status
    /// </summary>
    Task<IEnumerable<Book>> GetByStatusAsync(
        BookStatus status,
        CancellationToken cancellationToken = default);

    /// <summary>
    /// Get a book by ISBN.
    /// 
    /// EXAMPLE:
    ///   var book = await repository.GetByIsbnAsync("978-3-16-148410-0");
    /// 
    /// RETURNS: The book if found, null if not found
    /// 
    /// WHY? ISBN is a unique identifier for books, so we can search by it
    /// </summary>
    Task<Book?> GetByIsbnAsync(string isbn, CancellationToken cancellationToken = default);

    /// <summary>
    /// Save a new book to the database.
    /// 
    /// EXAMPLE:
    ///   var book = Book.CreateNew("Harry Potter", "J.K. Rowling", "...", "...", 19.99m);
    ///   await repository.CreateAsync(book);
    /// 
    /// THROWS: Exception if ISBN already exists (unique constraint)
    /// 
    /// AFTER CALLING:
    /// The book now has an ID and is in the database.
    /// </summary>
    Task CreateAsync(Book book, CancellationToken cancellationToken = default);

    /// <summary>
    /// Update an existing book in the database.
    /// 
    /// EXAMPLE:
    ///   var book = await repository.GetByIdAsync(bookId);
    ///   book.Update("New Title", "New Author", "Description", 29.99m);
    ///   await repository.UpdateAsync(book);
    /// 
    /// THROWS: Exception if book doesn't exist
    /// </summary>
    Task UpdateAsync(Book book, CancellationToken cancellationToken = default);

    /// <summary>
    /// Delete a book from the database.
    /// 
    /// EXAMPLE:
    ///   await repository.DeleteAsync(bookId);
    /// 
    /// THROWS: Exception if book doesn't exist
    /// 
    /// NOTE: Hard delete or soft delete?
    /// Hard delete: Remove from database completely
    /// Soft delete: Mark as deleted but keep in database
    /// Consider which approach makes sense for your business
    /// </summary>
    Task DeleteAsync(Guid id, CancellationToken cancellationToken = default);

    /// <summary>
    /// Check if a book exists by ID.
    /// 
    /// EXAMPLE:
    ///   bool exists = await repository.ExistsAsync(bookId);
    ///   if (!exists)
    ///   {
    ///       throw new NotFoundException("Book not found");
    ///   }
    /// 
    /// RETURNS: true if book exists, false otherwise
    /// 
    /// WHY? Performance - just checking existence is faster than loading entire book
    /// </summary>
    Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default);

    /// <summary>
    /// Check if ISBN already exists (before creating).
    /// 
    /// EXAMPLE:
    ///   bool isbnExists = await repository.IsbnExistsAsync("978-3-16-148410-0");
    ///   if (isbnExists)
    ///   {
    ///       throw new InvalidOperationException("ISBN already exists");
    ///   }
    /// 
    /// RETURNS: true if ISBN exists, false otherwise
    /// 
    /// WHY? ISBN must be unique. Check before creating to provide better error message.
    /// </summary>
    Task<bool> IsbnExistsAsync(string isbn, CancellationToken cancellationToken = default);

    /// <summary>
    /// Get count of books with a specific status.
    /// 
    /// EXAMPLE:
    ///   int publishedCount = await repository.GetCountByStatusAsync(BookStatus.Published);
    /// 
    /// RETURNS: Number of books with that status
    /// 
    /// WHY? Useful for dashboard statistics
    /// </summary>
    Task<int> GetCountByStatusAsync(BookStatus status, CancellationToken cancellationToken = default);
}
