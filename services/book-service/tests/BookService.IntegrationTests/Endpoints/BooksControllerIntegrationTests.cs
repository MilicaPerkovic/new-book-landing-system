using System;
using System.Collections.Generic;
using System.Net;
using System.Net.Http.Json;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc.Testing;
using Xunit;
using BookService.API.DTOs;
using BookService.API;

namespace BookService.IntegrationTests.Endpoints;

/// <summary>
/// Integration tests for BooksController endpoints.
/// Uses WebApplicationFactory to host the API during tests.
/// Tests complete request/response lifecycle.
/// </summary>
public class BooksControllerIntegrationTests : IAsyncLifetime
{
    private WebApplicationFactory<Program>? _factory;
    private HttpClient? _client;

    public async Task InitializeAsync()
    {
        // Create factory with test database
        _factory = new WebApplicationFactory<Program>()
            .WithWebHostBuilder(builder =>
            {
                // Can configure test-specific settings here if needed
            });

        _client = _factory.CreateClient();
        await Task.CompletedTask;
    }

    public async Task DisposeAsync()
    {
        _client?.Dispose();
        await (_factory?.DisposeAsync() ?? Task.CompletedTask);
    }

    #region GET All Books Tests

    [Fact]
    public async Task GetAllBooks_ReturnsOkWithBooks()
    {
        // Act: Get all books
        var response = await _client!.GetAsync("/api/books");

        // Assert: Success
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var content = await response.Content.ReadFromJsonAsync<List<BookResponse>>();
        Assert.NotNull(content);
    }

    #endregion

    #region Create Book Tests

    [Fact]
    public async Task CreateBook_WithValidData_Returns201Created()
    {
        // Arrange: Valid book data
        var request = new CreateBookRequest
        {
            Title = "Test Book",
            Author = "Test Author",
            Isbn = "978-0-451-52494-2",
            Description = "This is a test description for a book",
            Price = 19.99m,
            ImageUrl = "https://example.com/book.jpg"
        };

        // Act: Create book
        var response = await _client!.PostAsJsonAsync("/api/books", request);

        // Assert: Created
        Assert.Equal(HttpStatusCode.Created, response.StatusCode);
        var content = await response.Content.ReadFromJsonAsync<BookResponse>();
        Assert.NotNull(content);
        Assert.Equal("Test Book", content.Title);
        Assert.Equal("Draft", content.Status.ToString());
    }

    [Fact]
    public async Task CreateBook_WithInvalidISBN_Returns400BadRequest()
    {
        // Arrange: Invalid ISBN (not 13 digits)
        var request = new CreateBookRequest
        {
            Title = "Test Book",
            Author = "Test Author",
            Isbn = "123", // Invalid: too short
            Description = "This is a test description",
            Price = 19.99m
        };

        // Act: Try to create
        var response = await _client!.PostAsJsonAsync("/api/books", request);

        // Assert: Bad request
        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task CreateBook_WithMissingTitle_Returns400BadRequest()
    {
        // Arrange: Missing title
        var request = new CreateBookRequest
        {
            Title = "", // Empty
            Author = "Test Author",
            Isbn = "978-0-451-52494-2",
            Description = "Test description",
            Price = 19.99m
        };

        // Act: Try to create
        var response = await _client!.PostAsJsonAsync("/api/books", request);

        // Assert: Bad request
        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task CreateBook_WithNegativePrice_Returns400BadRequest()
    {
        // Arrange: Negative price
        var request = new CreateBookRequest
        {
            Title = "Test Book",
            Author = "Test Author",
            Isbn = "978-0-451-52494-2",
            Description = "Test description",
            Price = -10m // Negative
        };

        // Act: Try to create
        var response = await _client!.PostAsJsonAsync("/api/books", request);

        // Assert: Bad request
        Assert.Equal(HttpStatusCode.BadRequest, response.StatusCode);
    }

    [Fact]
    public async Task CreateBook_WithDuplicateISBN_Returns409Conflict()
    {
        // Arrange: Create first book
        var request1 = new CreateBookRequest
        {
            Title = "Book 1",
            Author = "Author",
            Isbn = "978-0-451-52494-3",
            Description = "First book",
            Price = 10m
        };

        await _client!.PostAsJsonAsync("/api/books", request1);

        // Create second book with same ISBN
        var request2 = new CreateBookRequest
        {
            Title = "Book 2",
            Author = "Author",
            Isbn = "978-0-451-52494-3", // Duplicate ISBN
            Description = "Second book",
            Price = 15m
        };

        // Act: Try to create duplicate
        var response = await _client.PostAsJsonAsync("/api/books", request2);

        // Assert: Conflict
        Assert.Equal(HttpStatusCode.Conflict, response.StatusCode);
    }

    #endregion

    #region Get Book by ID Tests

    [Fact]
    public async Task GetBookById_WithValidId_ReturnsOk()
    {
        // Arrange: Create a book first
        var createRequest = new CreateBookRequest
        {
            Title = "Findable Book",
            Author = "Author",
            Isbn = "978-0-451-52494-4",
            Description = "A book to find",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException("Failed to create test book");

        // Act: Get by ID
        var response = await _client.GetAsync($"/api/books/{created.Id}");

        // Assert: Found
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var book = await response.Content.ReadFromJsonAsync<BookResponse>();
        Assert.NotNull(book);
        Assert.Equal("Findable Book", book.Title);
    }

    [Fact]
    public async Task GetBookById_WithInvalidId_Returns404NotFound()
    {
        // Act: Get non-existent book
        var response = await _client!.GetAsync($"/api/books/{Guid.NewGuid()}");

        // Assert: Not found
        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    #endregion

    #region Update Book Tests

    [Fact]
    public async Task UpdateBook_WithValidData_ReturnsOk()
    {
        // Arrange: Create book
        var createRequest = new CreateBookRequest
        {
            Title = "Original Title",
            Author = "Author",
            Isbn = "978-0-451-52494-5",
            Description = "Original description",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        // Update
        var updateRequest = new UpdateBookRequest
        {
            Title = "Updated Title",
            Author = "Updated Author",
            Description = "Updated description",
            Price = 15m
        };

        // Act: Update book
        var response = await _client.PutAsJsonAsync($"/api/books/{created.Id}", updateRequest);

        // Assert: Updated
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var updated = await response.Content.ReadFromJsonAsync<BookResponse>();
        Assert.NotNull(updated);
        Assert.Equal("Updated Title", updated.Title);
        Assert.Equal(15m, updated.Price);
    }

    [Fact]
    public async Task UpdateBook_WithInvalidId_Returns404NotFound()
    {
        // Arrange: Invalid ID
        var updateRequest = new UpdateBookRequest
        {
            Title = "Title",
            Author = "Author",
            Description = "Description",
            Price = 10m
        };

        // Act: Try to update non-existent book
        var response = await _client!.PutAsJsonAsync($"/api/books/{Guid.NewGuid()}", updateRequest);

        // Assert: Not found
        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    #endregion

    #region Delete Book Tests

    [Fact]
    public async Task DeleteBook_WithValidId_Returns204NoContent()
    {
        // Arrange: Create book
        var createRequest = new CreateBookRequest
        {
            Title = "To Delete",
            Author = "Author",
            Isbn = "978-0-451-52494-6",
            Description = "Will be deleted",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        // Act: Delete
        var response = await _client.DeleteAsync($"/api/books/{created.Id}");

        // Assert: Deleted
        Assert.Equal(HttpStatusCode.NoContent, response.StatusCode);

        // Verify it's gone
        var getResponse = await _client.GetAsync($"/api/books/{created.Id}");
        Assert.Equal(HttpStatusCode.NotFound, getResponse.StatusCode);
    }

    [Fact]
    public async Task DeleteBook_WithInvalidId_Returns404NotFound()
    {
        // Act: Try to delete non-existent book
        var response = await _client!.DeleteAsync($"/api/books/{Guid.NewGuid()}");

        // Assert: Not found
        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    #endregion

    #region State Transition Tests

    [Fact]
    public async Task PublishBook_WithValidId_ReturnsOkWithPublishedStatus()
    {
        // Arrange: Create book
        var createRequest = new CreateBookRequest
        {
            Title = "Book to Publish",
            Author = "Author",
            Isbn = "978-0-451-52494-7",
            Description = "About to be published",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        // Act: Publish
        var response = await _client.PostAsync($"/api/books/{created.Id}/publish", null);

        // Assert: Published
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var updated = await response.Content.ReadFromJsonAsync<BookResponse>();
        Assert.NotNull(updated);
        Assert.Equal("Published", updated.Status.ToString());
        Assert.NotNull(updated.PublishedDate);
    }

    [Fact]
    public async Task ArchiveBook_WithPublishedBook_ReturnsOkWithArchivedStatus()
    {
        // Arrange: Create and publish book
        var createRequest = new CreateBookRequest
        {
            Title = "Book to Archive",
            Author = "Author",
            Isbn = "978-0-451-52494-8",
            Description = "About to be archived",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        // Publish first
        await _client.PostAsync($"/api/books/{created.Id}/publish", null);

        // Act: Archive
        var response = await _client.PostAsync($"/api/books/{created.Id}/archive", null);

        // Assert: Archived
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var updated = await response.Content.ReadFromJsonAsync<BookResponse>();
        Assert.NotNull(updated);
        Assert.Equal("Archived", updated.Status.ToString());
    }

    [Fact]
    public async Task DiscontinueBook_WithPublishedBook_ReturnsOkWithDiscontinuedStatus()
    {
        // Arrange: Create and publish book
        var createRequest = new CreateBookRequest
        {
            Title = "Book to Discontinue",
            Author = "Author",
            Isbn = "978-0-451-52494-9",
            Description = "About to be discontinued",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        // Publish first
        await _client.PostAsync($"/api/books/{created.Id}/publish", null);

        // Act: Discontinue
        var response = await _client.PostAsync($"/api/books/{created.Id}/discontinue", null);

        // Assert: Discontinued
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var updated = await response.Content.ReadFromJsonAsync<BookResponse>();
        Assert.NotNull(updated);
        Assert.Equal("Discontinued", updated.Status);
    }

    #endregion

    #region Filter and Query Tests

    [Fact]
    public async Task GetPublishedBooks_ReturnsOnlyPublishedBooks()
    {
        // Arrange: Create published book
        var request = new CreateBookRequest
        {
            Title = "Published Book",
            Author = "Author",
            Isbn = "978-0-451-52494-1",
            Description = "Already published",
            Price = 10m
        };

        var createResponse = await _client!.PostAsJsonAsync("/api/books", request);
        var created = await createResponse.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        // Publish it
        await _client.PostAsync($"/api/books/{created.Id}/publish", null);

        // Act: Get published books
        var response = await _client.GetAsync("/api/books/published");

        // Assert: Contains published book
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var books = await response.Content.ReadFromJsonAsync<List<BookResponse>>();
        Assert.NotEmpty(books);
        Assert.True(books.TrueForAll(b => b.Status == "Published"));
    }

    [Fact]
    public async Task GetBooksByStatus_FiltersByStatus()
    {
        // Act: Get draft books
        var response = await _client!.GetAsync("/api/books/status/Draft");

        // Assert: Success
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var books = await response.Content.ReadFromJsonAsync<List<BookResponse>>();
        Assert.NotNull(books);
    }

    [Fact]
    public async Task GetStatistics_ReturnsBookCounts()
    {
        // Act: Get statistics
        var response = await _client!.GetAsync("/api/books/statistics");

        // Assert: Success
        Assert.Equal(HttpStatusCode.OK, response.StatusCode);
        var stats = await response.Content.ReadFromJsonAsync<BookStatisticsResponse>();
        Assert.NotNull(stats);
        Assert.True(stats.DraftCount >= 0);
        Assert.True(stats.PublishedCount >= 0);
    }

    #endregion

    #region Error Handling Tests

    [Fact]
    public async Task InvalidEndpoint_Returns404NotFound()
    {
        // Act: Hit non-existent endpoint
        var response = await _client!.GetAsync("/api/nonexistent");

        // Assert: Not found
        Assert.Equal(HttpStatusCode.NotFound, response.StatusCode);
    }

    [Fact]
    public async Task MalformedJson_Returns400BadRequest()
    {
        // Act: Send malformed JSON
        var content = new StringContent("{invalid json}", System.Text.Encoding.UTF8, "application/json");
        var response = await _client!.PostAsync("/api/books", content);

        // Assert: Bad request
        Assert.True(
            response.StatusCode == HttpStatusCode.BadRequest || 
            response.StatusCode == HttpStatusCode.UnprocessableEntity
        );
    }

    #endregion

    #region End-to-End Workflow Tests

    [Fact]
    public async Task CompleteBookLifecycle_Draft_Published_Archived()
    {
        // 1. Create book (Draft)
        var createRequest = new CreateBookRequest
        {
            Title = "Complete Lifecycle Book",
            Author = "Author",
            Isbn = "978-0-451-52494-0",
            Description = "Testing complete lifecycle",
            Price = 25m
        };

        var created = await _client!.PostAsJsonAsync("/api/books", createRequest);
        var book = await created.Content.ReadFromJsonAsync<BookResponse>()
            ?? throw new InvalidOperationException();

        Assert.Equal("Draft", book.Status);

        // 2. Publish
        var published = await _client.PostAsync($"/api/books/{book.Id}/publish", null);
        var publishedBook = await published.Content.ReadFromJsonAsync<BookResponse>();
        Assert.Equal("Published", publishedBook!.Status);

        // 3. Archive
        var archived = await _client.PostAsync($"/api/books/{book.Id}/archive", null);
        var archivedBook = await archived.Content.ReadFromJsonAsync<BookResponse>();
        Assert.Equal("Archived", archivedBook!.Status);

        // 4. Verify final state
        var final = await _client.GetAsync($"/api/books/{book.Id}");
        var finalBook = await final.Content.ReadFromJsonAsync<BookResponse>();
        Assert.Equal("Archived", finalBook!.Status);
    }

    #endregion
}
