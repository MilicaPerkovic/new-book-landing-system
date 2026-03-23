using BookService.Domain.Entities;
using BookService.Domain.Enums;
using BookService.Domain.Interfaces;
using Moq;
using Xunit;

namespace BookService.UnitTests.Services;

public class BookServiceTests
{
    private readonly Mock<IBookRepository> _repositoryMock;
    private readonly BookService.Domain.Services.BookService _bookService;

    public BookServiceTests()
    {
        _repositoryMock = new Mock<IBookRepository>();
        _bookService = new BookService.Domain.Services.BookService(_repositoryMock.Object);
    }

    [Fact]
    public async Task CreateBookAsync_WithValidInput_ReturnsCreatedBook()
    {
        _repositoryMock
            .Setup(r => r.IsbnExistsAsync("9780451524935", It.IsAny<CancellationToken>()))
            .ReturnsAsync(false);

        _repositoryMock
            .Setup(r => r.CreateAsync(It.IsAny<Book>(), It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        var result = await _bookService.CreateBookAsync(
            "1984",
            "George Orwell",
            "9780451524935",
            "A dystopian novel",
            15.99m,
            "https://example.com/1984.jpg");

        Assert.True(result.IsSuccess);
        Assert.NotNull(result.Data);
        Assert.Equal("1984", result.Data!.Title);
        Assert.Equal(BookStatus.Draft, result.Data.Status);
        Assert.Null(result.ErrorMessage);
    }

    [Fact]
    public async Task CreateBookAsync_WithDuplicateIsbn_ReturnsFailure()
    {
        _repositoryMock
            .Setup(r => r.IsbnExistsAsync("9780451524935", It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        var result = await _bookService.CreateBookAsync(
            "1984",
            "George Orwell",
            "9780451524935",
            "A dystopian novel",
            15.99m);

        Assert.False(result.IsSuccess);
        Assert.Null(result.Data);
        Assert.Equal("ISBN_DUPLICATE", result.ErrorCode);
    }

    [Fact]
    public async Task CreateBookAsync_WithInvalidTitle_ReturnsValidationError()
    {
        _repositoryMock
            .Setup(r => r.IsbnExistsAsync("9780451524935", It.IsAny<CancellationToken>()))
            .ReturnsAsync(false);

        var result = await _bookService.CreateBookAsync(
            string.Empty,
            "George Orwell",
            "9780451524935",
            "A dystopian novel",
            15.99m);

        Assert.False(result.IsSuccess);
        Assert.Equal("VALIDATION_ERROR", result.ErrorCode);
    }

    [Fact]
    public async Task GetBookByIdAsync_WithExistingBook_ReturnsSuccess()
    {
        var bookId = Guid.NewGuid();
        var book = Book.CreateNew("1984", "George Orwell", "9780451524935", "A dystopian novel", 15.99m);

        _repositoryMock
            .Setup(r => r.GetByIdAsync(bookId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(book);

        var result = await _bookService.GetBookByIdAsync(bookId);

        Assert.True(result.IsSuccess);
        Assert.Equal("1984", result.Data!.Title);
    }

    [Fact]
    public async Task GetBookByIdAsync_WithMissingBook_ReturnsNotFound()
    {
        var bookId = Guid.NewGuid();

        _repositoryMock
            .Setup(r => r.GetByIdAsync(bookId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((Book?)null);

        var result = await _bookService.GetBookByIdAsync(bookId);

        Assert.False(result.IsSuccess);
        Assert.Equal("NOT_FOUND", result.ErrorCode);
    }

    [Fact]
    public async Task UpdateBookAsync_WithExistingBook_UpdatesAndReturnsSuccess()
    {
        var bookId = Guid.NewGuid();
        var book = Book.CreateNew("1984", "George Orwell", "9780451524935", "A dystopian novel", 15.99m);

        _repositoryMock
            .Setup(r => r.GetByIdAsync(bookId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(book);

        _repositoryMock
            .Setup(r => r.UpdateAsync(It.IsAny<Book>(), It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        var result = await _bookService.UpdateBookAsync(
            bookId,
            "1984 - Updated",
            "George Orwell",
            "Updated description",
            20.99m,
            "https://example.com/updated.jpg");

        Assert.True(result.IsSuccess);
        Assert.Equal("1984 - Updated", result.Data!.Title);
        Assert.Equal(20.99m, result.Data.Price);
    }

    [Fact]
    public async Task DeleteBookAsync_WithExistingBook_ReturnsSuccess()
    {
        var bookId = Guid.NewGuid();

        _repositoryMock
            .Setup(r => r.ExistsAsync(bookId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);

        _repositoryMock
            .Setup(r => r.DeleteAsync(bookId, It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        var result = await _bookService.DeleteBookAsync(bookId);

        Assert.True(result.IsSuccess);
    }

    [Fact]
    public async Task PublishBookAsync_FromDraft_ChangesStatusToPublished()
    {
        var bookId = Guid.NewGuid();
        var book = Book.CreateNew("1984", "George Orwell", "9780451524935", "A dystopian novel", 15.99m);

        _repositoryMock
            .Setup(r => r.GetByIdAsync(bookId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(book);

        _repositoryMock
            .Setup(r => r.UpdateAsync(It.IsAny<Book>(), It.IsAny<CancellationToken>()))
            .Returns(Task.CompletedTask);

        var result = await _bookService.PublishBookAsync(bookId);

        Assert.True(result.IsSuccess);
        Assert.Equal(BookStatus.Published, result.Data!.Status);
        Assert.NotNull(result.Data.PublishedDate);
    }

    [Fact]
    public async Task GetPublishedBooksAsync_ReturnsPublishedBooks()
    {
        var publishedBook = Book.CreateNew("1984", "George Orwell", "9780451524935", "A dystopian novel", 15.99m);
        publishedBook.Publish();

        _repositoryMock
            .Setup(r => r.GetByStatusAsync(BookStatus.Published, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<Book> { publishedBook });

        var result = await _bookService.GetPublishedBooksAsync();

        Assert.True(result.IsSuccess);
        Assert.Single(result.Data!);
    }

    [Fact]
    public async Task GetStatisticsAsync_ReturnsCorrectTotals()
    {
        _repositoryMock
            .Setup(r => r.GetCountByStatusAsync(BookStatus.Published, It.IsAny<CancellationToken>()))
            .ReturnsAsync(3);
        _repositoryMock
            .Setup(r => r.GetCountByStatusAsync(BookStatus.Draft, It.IsAny<CancellationToken>()))
            .ReturnsAsync(2);
        _repositoryMock
            .Setup(r => r.GetCountByStatusAsync(BookStatus.Archived, It.IsAny<CancellationToken>()))
            .ReturnsAsync(1);
        _repositoryMock
            .Setup(r => r.GetCountByStatusAsync(BookStatus.Discontinued, It.IsAny<CancellationToken>()))
            .ReturnsAsync(4);

        var result = await _bookService.GetStatisticsAsync();

        Assert.True(result.IsSuccess);
        Assert.Equal(10, result.Data!.TotalCount);
    }
}
