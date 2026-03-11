using BookService.Domain.Entities;
using BookService.Domain.Interfaces;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace BookService.Infrastructure.Repositories;

/// <summary>
/// BookRepository - Implements IBookRepository using Entity Framework Core and PostgreSQL
/// 
/// WHAT IS THIS?
/// This is where the ACTUAL database work happens.
/// 
/// RELATIONSHIP:
/// Domain Layer: IBookRepository (interface - what we need)
///                    ↓
/// Infrastructure: BookRepository (implementation - how we do it)
///                    ↓
/// Database: PostgreSQL (where data is stored)
/// 
/// BENEFITS OF THIS SEPARATION:
/// 1. Domain doesn't know about PostgreSQL/EF Core
/// 2. We can swap to MongoDB/MySQL later by creating new repository
/// 3. Tests can use fake repository instead of real database
/// 4. Clear separation: interface (contract) vs implementation (details)
/// 
/// HOW IT WORKS:
/// When BookService calls:
///   await repository.GetByIdAsync(id)
/// 
/// This repository converts it to:
///   SELECT * FROM Books WHERE id = @id
/// 
/// Entity Framework does the SQL generation automatically.
/// </summary>
public class BookRepository : IBookRepository
{
    /// <summary>Entity Framework database context</summary>
    private readonly BookServiceDbContext _dbContext;

    /// <summary>Logger for debugging and troubleshooting</summary>
    private readonly ILogger<BookRepository> _logger;

    /// <summary>
    /// Constructor - receives dependencies via DI
    /// 
    /// HOW DEPENDENCY INJECTION WORKS:
    /// In Program.cs we register:
    ///   services.AddScoped<IBookRepository, BookRepository>();
    /// 
    /// When a class needs IBookRepository, .NET:
    /// 1. Sees it's been registered
    /// 2. Creates a BookRepository instance
    /// 3. Provides it to the requesting class
    /// 4. All returned instances share same database context (per request)
    /// </summary>
    public BookRepository(BookServiceDbContext dbContext, ILogger<BookRepository> logger)
    {
        _dbContext = dbContext ?? throw new ArgumentNullException(nameof(dbContext));
        _logger = logger ?? throw new ArgumentNullException(nameof(logger));
    }

    // ============================================================
    // READ OPERATIONS
    // ============================================================

    /// <summary>
    /// Get a single book by ID.
    /// 
    /// EXAMPLE:
    ///   var book = await repository.GetByIdAsync(new Guid("123e4567..."));
    /// 
    /// DATABASE OPERATION:
    ///   SELECT * FROM Books WHERE id = @id
    /// 
    /// RETURNS: Book if found, null if not found
    /// 
    /// PERFORMANCE: O(1) - indexed by primary key
    /// </summary>
    public async Task<Book?> GetByIdAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Fetching book with ID: {BookId}", id);

        var book = await _dbContext.Books
            .FirstOrDefaultAsync(b => b.Id == id, cancellationToken);

        if (book == null)
        {
            _logger.LogWarning("Book not found with ID: {BookId}", id);
        }

        return book;
    }

    /// <summary>
    /// Get all books in the database.
    /// 
    /// EXAMPLE:
    ///   var allBooks = await repository.GetAllAsync();
    /// 
    /// DATABASE OPERATION:
    ///   SELECT * FROM Books ORDER BY created_at DESC
    /// 
    /// RETURNS: Collection of all books (could be empty)
    /// 
    /// WARNING: For large datasets, consider pagination!
    /// TODO: Add GetPageAsync(pageNumber, pageSize)
    /// </summary>
    public async Task<IEnumerable<Book>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Fetching all books");

        var books = await _dbContext.Books
            .OrderByDescending(b => b.CreatedAt)
            .ToListAsync(cancellationToken);

        _logger.LogInformation("Retrieved {BookCount} books", books.Count);
        return books;
    }

    /// <summary>
    /// Get books filtered by status.
    /// 
    /// EXAMPLE:
    ///   var published = await repository.GetByStatusAsync(BookStatus.Published);
    /// 
    /// DATABASE OPERATION:
    ///   SELECT * FROM Books WHERE status = @status ORDER BY created_at DESC
    /// 
    /// RETURNS: Books with that status
    /// 
    /// USES INDEX: idx_books_status for fast filtering
    /// </summary>
    public async Task<IEnumerable<Book>> GetByStatusAsync(
        BookStatus status,
        CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Fetching books with status: {Status}", status);

        var books = await _dbContext.Books
            .Where(b => b.Status == status)
            .OrderByDescending(b => b.CreatedAt)
            .ToListAsync(cancellationToken);

        _logger.LogInformation("Retrieved {BookCount} books with status {Status}", books.Count, status);
        return books;
    }

    /// <summary>
    /// Get a book by ISBN.
    /// 
    /// EXAMPLE:
    ///   var book = await repository.GetByIsbnAsync("978-3-16-148410-0");
    /// 
    /// DATABASE OPERATION:
    ///   SELECT * FROM Books WHERE isbn = @isbn
    /// 
    /// RETURNS: Book if found, null if not found
    /// 
    /// USES INDEX: idx_books_isbn_unique (unique constraint)
    /// One book per ISBN guaranteed by database
    /// </summary>
    public async Task<Book?> GetByIsbnAsync(string isbn, CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrWhiteSpace(isbn))
        {
            _logger.LogWarning("GetByIsbnAsync called with empty ISBN");
            return null;
        }

        _logger.LogInformation("Fetching book with ISBN: {ISBN}", isbn);

        var book = await _dbContext.Books
            .FirstOrDefaultAsync(b => b.ISBN == isbn, cancellationToken);

        return book;
    }

    /// <summary>
    /// Check if a book exists by ID.
    /// 
    /// EXAMPLE:
    ///   bool exists = await repository.ExistsAsync(bookId);
    /// 
    /// DATABASE OPERATION:
    ///   SELECT 1 FROM Books WHERE id = @id LIMIT 1
    /// 
    /// RETURNS: true if exists, false otherwise
    /// 
    /// PERFORMANCE NOTE:
    /// .Any() is faster than .FirstOrDefault() because it only
    /// checks existence, doesn't load the entire book data
    /// </summary>
    public async Task<bool> ExistsAsync(Guid id, CancellationToken cancellationToken = default)
    {
        return await _dbContext.Books
            .AnyAsync(b => b.Id == id, cancellationToken);
    }

    /// <summary>
    /// Check if ISBN already exists in database.
    /// 
    /// EXAMPLE:
    ///   bool isbnExists = await repository.IsbnExistsAsync("978-3-16-148410-0");
    /// 
    /// DATABASE OPERATION:
    ///   SELECT 1 FROM Books WHERE isbn = @isbn LIMIT 1
    /// 
    /// WHY?
    /// We need to know before creating if ISBN is unique.
    /// ISBN has unique constraint in database.
    /// </summary>
    public async Task<bool> IsbnExistsAsync(string isbn, CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrWhiteSpace(isbn))
            return false;

        return await _dbContext.Books
            .AnyAsync(b => b.ISBN == isbn, cancellationToken);
    }

    /// <summary>
    /// Get count of books with specific status.
    /// 
    /// EXAMPLE:
    ///   int publishedCount = await repository.GetCountByStatusAsync(BookStatus.Published);
    /// 
    /// DATABASE OPERATION:
    ///   SELECT COUNT(*) FROM Books WHERE status = @status
    /// 
    /// RETURNS: Number of books with that status
    /// 
    /// USE CASE: Dashboard statistics
    /// </summary>
    public async Task<int> GetCountByStatusAsync(BookStatus status, CancellationToken cancellationToken = default)
    {
        return await _dbContext.Books
            .CountAsync(b => b.Status == status, cancellationToken);
    }

    // ============================================================
    // WRITE OPERATIONS
    // ============================================================

    /// <summary>
    /// Create (insert) a new book into the database.
    /// 
    /// EXAMPLE:
    ///   var book = Book.CreateNew("1984", "Orwell", "...", "...", 15.99m);
    ///   await repository.CreateAsync(book);
    /// 
    /// DATABASE OPERATION:
    ///   INSERT INTO Books (id, title, author, isbn, ...)
    ///   VALUES (@id, @title, @author, @isbn, ...)
    /// 
    /// CONSTRAINTS CHECKED:
    ///   - ISBN must be unique (throws if duplicate)
    ///   - Foreign keys (if any)
    ///   - Column constraints (NOT NULL, etc.)
    /// 
    /// THROWS: DbUpdateException if constraint violated
    /// 
    /// AFTER CALLING:
    /// The book is now in the database with an ID
    /// </summary>
    public async Task CreateAsync(Book book, CancellationToken cancellationToken = default)
    {
        if (book == null)
            throw new ArgumentNullException(nameof(book));

        _logger.LogInformation("Creating new book: {Title} by {Author}", book.Title, book.Author);

        _dbContext.Books.Add(book);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
            _logger.LogInformation("Book created with ID: {BookId}", book.Id);
        }
        catch (DbUpdateException ex)
        {
            _logger.LogError(ex, "Failed to create book - database constraint violation");
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unexpected error creating book");
            throw;
        }
    }

    /// <summary>
    /// Update an existing book in the database.
    /// 
    /// EXAMPLE:
    ///   var book = await repository.GetByIdAsync(bookId);
    ///   book.Update("New Title", "New Author", "Description", 29.99m);
    ///   await repository.UpdateAsync(book);
    /// 
    /// DATABASE OPERATION:
    ///   UPDATE Books SET title = @title, author = @author, ... WHERE id = @id
    /// 
    /// HOW ENTITY FRAMEWORK KNOWS WHAT CHANGED?
    /// Change tracking: EF tracks that the book object was modified
    /// SaveChangesAsync() generates UPDATE statement with only changed columns
    /// 
    /// AFTER CALLING:
    /// The book is updated in the database
    /// UpdatedAt timestamp is automatically set
    /// </summary>
    public async Task UpdateAsync(Book book, CancellationToken cancellationToken = default)
    {
        if (book == null)
            throw new ArgumentNullException(nameof(book));

        _logger.LogInformation("Updating book: {BookId}", book.Id);

        // Mark as modified (tells EF to update)
        _dbContext.Books.Update(book);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
            _logger.LogInformation("Book updated: {BookId}", book.Id);
        }
        catch (DbUpdateException ex)
        {
            _logger.LogError(ex, "Failed to update book - database error");
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unexpected error updating book");
            throw;
        }
    }

    /// <summary>
    /// Delete a book from the database.
    /// 
    /// EXAMPLE:
    ///   await repository.DeleteAsync(bookId);
    /// 
    /// DATABASE OPERATION:
    ///   DELETE FROM Books WHERE id = @id
    /// 
    /// CONSIDERATIONS:
    /// - This is a HARD DELETE (removed permanently)
    /// - If foreign keys exist (orders, etc.), this might fail
    /// - Consider SOFT DELETE instead (mark as deleted)
    /// 
    /// THROWS: DbUpdateException if foreign key constraint violated
    /// 
    /// AFTER CALLING:
    /// The book is completely removed from the database
    /// </summary>
    public async Task DeleteAsync(Guid id, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Deleting book: {BookId}", id);

        // Find the book first
        var book = await _dbContext.Books.FindAsync(new object[] { id }, cancellationToken: cancellationToken);

        if (book == null)
        {
            _logger.LogWarning("Book not found for deletion: {BookId}", id);
            throw new InvalidOperationException($"Book with ID {id} not found");
        }

        // Remove from database
        _dbContext.Books.Remove(book);

        try
        {
            await _dbContext.SaveChangesAsync(cancellationToken);
            _logger.LogInformation("Book deleted: {BookId}", id);
        }
        catch (DbUpdateException ex)
        {
            _logger.LogError(ex, "Failed to delete book - constraint violation (may have related records)");
            throw;
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unexpected error deleting book");
            throw;
        }
    }
}
