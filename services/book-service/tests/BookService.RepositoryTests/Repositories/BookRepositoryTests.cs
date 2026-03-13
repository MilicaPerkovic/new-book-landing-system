using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Logging.Abstractions;
using Testcontainers.PostgreSql;
using Xunit;
using BookService.Domain.Entities;
using BookService.Domain.Enums;
using BookService.Infrastructure.Data;
using BookService.Infrastructure.Repositories;

namespace BookService.RepositoryTests.Repositories;

/// <summary>
/// Repository integration tests using Testcontainers for PostgreSQL.
/// Requires Docker to be running.
/// Tests real database operations with EF Core.
/// </summary>
public class BookRepositoryTests : IAsyncLifetime
{
    private PostgreSqlContainer? _postgres;
    private BookServiceDbContext? _dbContext;
    private BookRepository? _repository;

    /// <summary>
    /// Initialize Docker container and database before tests run.
    /// </summary>
    public async Task InitializeAsync()
    {
        // Set up PostgreSQL container
        _postgres = new PostgreSqlBuilder()
            .WithImage("postgres:latest")
            .WithDatabase("bookservice_test")
            .WithUsername("postgres")
            .WithPassword("postgres")
            .Build();

        await _postgres.StartAsync();

        // Get connection string from container
        var connectionString = _postgres.GetConnectionString();

        // Create DbContext with test database
        var options = new DbContextOptionsBuilder<BookServiceDbContext>()
            .UseNpgsql(connectionString)
            .Options;

        _dbContext = new BookServiceDbContext(options);

        // Create tables from EF Core model (migrations)
        await _dbContext.Database.EnsureCreatedAsync();

        // Initialize repository
        var logger = NullLogger<BookRepository>.Instance;
        _repository = new BookRepository(_dbContext, logger);
    }

    /// <summary>
    /// Cleanup: Stop container and dispose resources.
    /// </summary>
    public async Task DisposeAsync()
    {
        if (_dbContext != null)
        {
            await _dbContext.DisposeAsync();
        }

        if (_postgres != null)
        {
            await _postgres.StopAsync();
        }
    }

    #region Create Tests

    [Fact]
    public async Task CreateAsync_WithValidBook_SavesToDatabase()
    {
        // Arrange: Create a book entity
        var book = Book.CreateNew(
            "1984",
            "George Orwell",
            "978-0-451-52494-2",
            "A dystopian novel about totalitarian surveillance",
            15.99m
        );

        // Act: Create book in repository
        await _repository!.CreateAsync(book);

        // Assert: Book saved with ID
        var savedBook = await _dbContext!.Books.FirstOrDefaultAsync(b => b.Isbn == "978-0-451-52494-2");
        Assert.NotNull(savedBook);
        Assert.NotEqual(Guid.Empty, savedBook.Id);
        Assert.Equal("1984", savedBook.Title);
        Assert.Equal("George Orwell", savedBook.Author);
    }

    [Fact]
    public async Task CreateAsync_WithDuplicateISBN_ThrowsException()
    {
        // Arrange: Create first book
        var book1 = Book.CreateNew(
            "1984",
            "George Orwell",
            "978-0-451-52494-2",
            "Dystopian",
            15.99m
        );
        await _repository!.CreateAsync(book1);

        // Create another book with same ISBN
        var book2 = Book.CreateNew(
            "Another Book",
            "Another Author",
            "978-0-451-52494-2",
            "Different book same ISBN",
            12.99m
        );

        // Act & Assert: Should throw unique constraint violation
        await Assert.ThrowsAsync<DbUpdateException>(async () =>
        {
            await _repository.CreateAsync(book2);
        });
    }

    #endregion

    #region Read Tests

    [Fact]
    public async Task GetByIdAsync_WithValidId_ReturnsBook()
    {
        // Arrange: Create and save a book
        var book = Book.CreateNew(
            "1984",
            "George Orwell",
            "978-0-451-52494-2",
            "Dystopian",
            15.99m
        );
        await _repository!.CreateAsync(book);
        var savedBook = await _dbContext!.Books.FirstAsync();


        // Act: Retrieve by ID
        var retrieved = await _repository.GetByIdAsync(savedBook.Id);

        // Assert: Found book with correct data
        Assert.NotNull(retrieved);
        Assert.Equal("1984", retrieved.Title);
        Assert.Equal(BookStatus.Draft, retrieved.Status);
    }

    [Fact]
    public async Task GetByIdAsync_WithInvalidId_ReturnsNull()
    {
        // Act: Try to fetch non-existent book
        var retrieved = await _repository!.GetByIdAsync(Guid.NewGuid());

        // Assert: No book found
        Assert.Null(retrieved);
    }

    [Fact]
    public async Task GetAllAsync_ReturnAllBooks()
    {
        // Arrange: Create multiple books
        var book1 = Book.CreateNew("1984", "Orwell", "978-0-451-52494-2", "Dystopian", 15.99m);
        var book2 = Book.CreateNew("Brave New World", "Huxley", "978-0-06-085052-4", "Dystopian", 12.99m);

        await _repository!.CreateAsync(book1);
        await _repository.CreateAsync(book2);

        // Act: Get all books
        var books = await _repository.GetAllAsync();

        // Assert: Returns all books
        Assert.NotEmpty(books);
        Assert.Equal(2, books.Count());
    }

    [Fact]
    public async Task GetByStatusAsync_FiltersCorrectly()
    {
        // Arrange: Create books with different statuses
        var draftBook = Book.CreateNew("Draft Book", "Author", "978-0-451-52494-2", "Draft", 10m);
        var publishedBook = Book.CreateNew("Published Book", "Author", "978-0-451-52494-3", "Published", 15m);
        publishedBook.Publish();

        await _repository!.CreateAsync(draftBook);
        await _repository.CreateAsync(publishedBook);

        // Act: Get only published books
        var published = await _repository.GetByStatusAsync(BookStatus.Published);

        // Assert: Only published books returned
        Assert.NotEmpty(published);
        Assert.Single(published);
        Assert.All(published, b => Assert.Equal(BookStatus.Published, b.Status));
    }

    [Fact]
    public async Task GetByIsbnAsync_FindsBookByISBN()
    {
        // Arrange: Create book with specific ISBN
        var isbn = "978-0-451-52494-2";
        var book = Book.CreateNew("1984", "Orwell", isbn, "Dystopian", 15.99m);
        await _repository!.CreateAsync(book);

        // Act: Find by ISBN
        var found = await _repository.GetByIsbnAsync(isbn);

        // Assert: Found correct book
        Assert.NotNull(found);
        Assert.Equal("1984", found.Title);
    }

    [Fact]
    public async Task ExistsAsync_WithExistingId_ReturnsTrue()
    {
        // Arrange: Create book
        var book = Book.CreateNew("1984", "Orwell", "978-0-451-52494-2", "Dystopian", 15.99m);
        await _repository!.CreateAsync(book);
        var savedBook = await _dbContext!.Books.FirstAsync();

        // Act: Check exists
        var exists = await _repository.ExistsAsync(savedBook.Id);

        // Assert: Book exists
        Assert.True(exists);
    }

    [Fact]
    public async Task ExistsAsync_WithNonExistentId_ReturnsFalse()
    {
        // Act: Check non-existent book
        var exists = await _repository!.ExistsAsync(Guid.NewGuid());

        // Assert: Book doesn't exist
        Assert.False(exists);
    }

    [Fact]
    public async Task IsbnExistsAsync_WithExistingISBN_ReturnsTrue()
    {
        // Arrange: Create book with ISBN
        var isbn = "978-0-451-52494-2";
        var book = Book.CreateNew("1984", "Orwell", isbn, "Dystopian", 15.99m);
        await _repository!.CreateAsync(book);

        // Act: Check ISBN exists
        var exists = await _repository.IsbnExistsAsync(isbn);

        // Assert: ISBN exists
        Assert.True(exists);
    }

    [Fact]
    public async Task IsbnExistsAsync_WithNonExistentISBN_ReturnsFalse()
    {
        // Act: Check non-existent ISBN
        var exists = await _repository!.IsbnExistsAsync("978-0-000-00000-0");

        // Assert: ISBN doesn't exist
        Assert.False(exists);
    }

    #endregion

    #region Update Tests

    [Fact]
    public async Task UpdateAsync_ModifiesBook()
    {
        // Arrange: Create and save book
        var book = Book.CreateNew("Original Title", "Author", "978-0-451-52494-2", "Original", 10m);
        await _repository!.CreateAsync(book);

        // Get the saved book
        var savedBook = await _repository.GetByIsbnAsync("978-0-451-52494-2");
        Assert.NotNull(savedBook);

        // Update title 
        savedBook.Update("New Title", "Author", "Updated description", 15m);

        // Act: Update in repository
        await _repository.UpdateAsync(savedBook);

        // Assert: Changes persisted
        var updated = await _repository.GetByIdAsync(savedBook.Id);
        Assert.NotNull(updated);
        Assert.Equal("New Title", updated.Title);
        Assert.Equal(15m, updated.Price);
    }

    [Fact]
    public async Task UpdateAsync_WithNonExistentBook_ThrowsException()
    {
        // Arrange: Create a book that doesn't exist in DB
        var book = Book.CreateNew("Phantom", "Ghost", "978-0-000-00000-0", "Desc", 5m);

        // Act & Assert: Should throw
        await Assert.ThrowsAsync<DbUpdateConcurrencyException>(async () =>
        {
            await _repository!.UpdateAsync(book);
        });
    }

    #endregion

    #region Delete Tests

    [Fact]
    public async Task DeleteAsync_RemovesBook()
    {
        // Arrange: Create book
        var book = Book.CreateNew("To Delete", "Author", "978-0-451-52494-2", "Will be deleted", 10m);
        await _repository!.CreateAsync(book);
        var savedBook = await _dbContext!.Books.FirstAsync();


        // Verify it exists
        var exists = await _repository.ExistsAsync(savedBook.Id);
        Assert.True(exists);

        // Act: Delete
        await _repository.DeleteAsync(savedBook.Id);

        // Assert: No longer exists
        var stillExists = await _repository.ExistsAsync(savedBook.Id);
        Assert.False(stillExists);
    }

    [Fact]
    public async Task DeleteAsync_WithNonExistentId_DoesNotThrow()
    {
        // Act & Assert: Should not throw for non-existent ID
        var ex = await Record.ExceptionAsync(() => _repository!.DeleteAsync(Guid.NewGuid()));
        Assert.Null(ex);
    }

    #endregion

    #region Statistics Tests

    [Fact]
    public async Task GetCountByStatusAsync_CountsCorrectly()
    {
        // Arrange: Create books with different statuses
        var draft = Book.CreateNew("Draft", "Author", "978-0-451-52494-2", "Desc", 10m);
        var published = Book.CreateNew("Published", "Author", "978-0-451-52494-3", "Desc", 10m);
        published.Publish();

        await _repository!.CreateAsync(draft);
        await _repository.CreateAsync(published);

        // Act: Count by status
        var draftCount = await _repository.GetCountByStatusAsync(BookStatus.Draft);
        var publishedCount = await _repository.GetCountByStatusAsync(BookStatus.Published);

        // Assert: Correct counts
        Assert.Equal(1, draftCount);
        Assert.Equal(1, publishedCount);
    }

    #endregion

    #region Transaction Tests

    [Fact]
    public async Task MultipleOperations_MaintainDataIntegrity()
    {
        // Arrange: Create multiple books
        var books = Enumerable.Range(1, 5)
            .Select(i => Book.CreateNew($"Book {i}", $"Author {i}", $"978-0-451-5249{i}-2", $"Desc {i}", 10m + i))
            .ToList();

        // Act: Create all books
        foreach (var book in books)
        {
            await _repository!.CreateAsync(book);
        }

        // Assert: All created and retrievable
        var allBooks = await _repository.GetAllAsync();
        Assert.Equal(5, allBooks.Count());
    }

    #endregion
}
