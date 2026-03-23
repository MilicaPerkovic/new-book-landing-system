using BookService.Domain.Enums;

namespace BookService.Domain.Entities;

/// <summary>
/// Book Entity - Represents a book in the system.
/// 
/// WHY ENTITY?
/// An entity is a core business object. It represents a "Book" and all its properties.
/// It lives in the Domain layer because it's pure business logic - no database or web stuff.
/// 
/// PROPERTIES:
/// - Id: Unique identifier (primary key in database)
/// - Title: Name of the book
/// - Author: Who wrote the book
/// - ISBN: ISBN-13 (International Standard Book Number - unique identifier)
/// - Description: Summary/marketing text
/// - Price: How much the book costs
/// - Status: Draft, Published, Archived, Discontinued
/// - ImageUrl: Cover image location
/// - PublishedDate: When it was published
/// - CreatedAt: When record was created
/// - UpdatedAt: When record was last modified
/// 
/// DESIGN NOTES:
/// - All properties have private setters
///   WHY? To prevent accidental modification outside of intended methods
/// - Validation happens in constructor or through methods
///   WHY? To ensure data is always valid
/// - Timestamps (CreatedAt, UpdatedAt) are set automatically
///   WHY? For audit trail - knowing when data changed
/// </summary>
public class Book
{
    // ============================================================
    // Properties
    // ============================================================

    /// <summary>Unique identifier for the book</summary>
    public Guid Id { get; private set; }

    /// <summary>Title/Name of the book</summary>
    public string Title { get; private set; } = string.Empty;

    /// <summary>Author's full name</summary>
    public string Author { get; private set; } = string.Empty;

    /// <summary>ISBN-13 (International Standard Book Number)</summary>
    /// <remarks>Format: XXX-X-XXXXX-X (13 digits with hyphens)</remarks>
    public string ISBN { get; private set; } = string.Empty;

    /// <summary>Book description/summary for marketing</summary>
    public string Description { get; private set; } = string.Empty;

    /// <summary>Price in EUR</summary>
    public decimal Price { get; private set; }

    /// <summary>Current status of the book (Draft, Published, etc.)</summary>
    public BookStatus Status { get; private set; } = BookStatus.Draft;

    /// <summary>URL to the book cover image</summary>
    public string? ImageUrl { get; private set; }

    /// <summary>When the book was published</summary>
    public DateTime? PublishedDate { get; private set; }

    /// <summary>When this record was created</summary>
    public DateTime CreatedAt { get; private set; }

    /// <summary>When this record was last updated</summary>
    public DateTime UpdatedAt { get; private set; }

    // ============================================================
    // Constructor
    // ============================================================

    /// <summary>
    /// Private constructor for Entity Framework.
    /// 
    /// WHY PRIVATE?
    /// We don't want external code creating books directly.
    /// They should use the CreateNew factory method instead.
    /// This ensures validation always happens.
    /// </summary>
    private Book() { }

    // ============================================================
    // Factory Methods
    // ============================================================

    /// <summary>
    /// Create a new book with validation.
    /// 
    /// WHY FACTORY METHOD?
    /// Instead of: var book = new Book();
    /// We use: var book = Book.CreateNew(title, author, isbn, ...);
    /// 
    /// BENEFIT: Validation happens automatically, object is always in valid state.
    /// 
    /// EXAMPLE:
    ///   var book = Book.CreateNew(
    ///       title: "Harry Potter",
    ///       author: "J.K. Rowling",
    ///       isbn: "978-1-4028-9462-6",
    ///       description: "A young wizard...",
    ///       price: 19.99m
    ///   );
    /// </summary>
    public static Book CreateNew(
        string title,
        string author,
        string isbn,
        string description,
        decimal price,
        string? imageUrl = null)
    {
        // Validate inputs before creating
        ValidateInput(title, author, isbn, description, price);

        return new Book
        {
            Id = Guid.NewGuid(),
            Title = title.Trim(),
            Author = author.Trim(),
            ISBN = isbn.Trim(),
            Description = description.Trim(),
            Price = price,
            ImageUrl = imageUrl?.Trim(),
            Status = BookStatus.Draft,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow
        };
    }

    // ============================================================
    // Business Methods
    // ============================================================

    /// <summary>
    /// Update book details.
    /// 
    /// WHY NOT JUST SET PROPERTIES?
    /// Properties have private setters. Methods allow controlled changes with validation.
    /// This ensures the object stays in a valid state.
    /// </summary>
    public void Update(
        string title,
        string author,
        string description,
        decimal price,
        string? imageUrl = null)
    {
        ValidateInput(title, author, ISBN, description, price);

        Title = title.Trim();
        Author = author.Trim();
        Description = description.Trim();
        Price = price;
        ImageUrl = imageUrl?.Trim();
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>Publish the book (make it visible to users)</summary>
    public void Publish()
    {
        if (Status == BookStatus.Published)
            throw new InvalidOperationException("Book is already published");

        Status = BookStatus.Published;
        PublishedDate = DateTime.UtcNow;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>Archive the book (hide from search but keep in database)</summary>
    public void Archive()
    {
        if (Status == BookStatus.Archived)
            throw new InvalidOperationException("Book is already archived");

        Status = BookStatus.Archived;
        UpdatedAt = DateTime.UtcNow;
    }

    /// <summary>Discontinue the book (mark as unavailable)</summary>
    public void Discontinue()
    {
        if (Status == BookStatus.Discontinued)
            throw new InvalidOperationException("Book is already discontinued");

        Status = BookStatus.Discontinued;
        UpdatedAt = DateTime.UtcNow;
    }

    // ============================================================
    // Validation
    // ============================================================

    /// <summary>
    /// Validate book input data.
    /// 
    /// WHY SEPARATE VALIDATION METHOD?
    /// - Reusable in multiple places (CreateNew, Update)
    /// - Clear, single responsibility
    /// - Easy to test
    /// - Easy to maintain
    /// 
    /// WHAT WE VALIDATE:
    /// - Title is not empty and length is reasonable
    /// - Author is not empty
    /// - ISBN format is valid (ISBN-13)
    /// - Description is not empty
    /// - Price is positive
    /// 
    /// WHY VALIDATE?
    /// Garbage in = Garbage out
    /// If we allow invalid data into the database, everything breaks.
    /// </summary>
    private static void ValidateInput(
        string title,
        string author,
        string isbn,
        string description,
        decimal price)
    {
        // Check if strings are empty
        if (string.IsNullOrWhiteSpace(title))
            throw new ArgumentException("Title cannot be empty", nameof(title));

        if (string.IsNullOrWhiteSpace(author))
            throw new ArgumentException("Author cannot be empty", nameof(author));

        if (string.IsNullOrWhiteSpace(isbn))
            throw new ArgumentException("ISBN cannot be empty", nameof(isbn));

        if (string.IsNullOrWhiteSpace(description))
            throw new ArgumentException("Description cannot be empty", nameof(description));

        // Check length constraints
        if (title.Length > 500)
            throw new ArgumentException("Title cannot exceed 500 characters", nameof(title));

        if (author.Length > 300)
            throw new ArgumentException("Author cannot exceed 300 characters", nameof(author));

        if (description.Length > 5000)
            throw new ArgumentException("Description cannot exceed 5000 characters", nameof(description));

        // Validate ISBN format
        // ISBN-13 should be: XXX-X-XXXXX-X (with hyphens) or 13 digits without
        var isbnDigits = isbn.Replace("-", "");
        if (isbnDigits.Length != 13 || !isbnDigits.All(char.IsDigit))
            throw new ArgumentException(
                "ISBN must be a valid 13-digit ISBN (format: XXX-X-XXXXX-X or 13 digits)",
                nameof(isbn));

        // Check price is positive
        if (price <= 0)
            throw new ArgumentException("Price must be greater than 0", nameof(price));

        if (price > 10000)
            throw new ArgumentException("Price cannot exceed 10000", nameof(price));
    }
}
