using BookService.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace BookService.Infrastructure.Data;

/// <summary>
/// BookServiceDbContext - Entity Framework Core Database Context
/// 
/// WHAT IS THIS?
/// This is the "bridge" between your C# objects (Book.cs entity) and the PostgreSQL database.
/// Entity Framework Core converts C# code to SQL automatically.
/// 
/// HOW IT WORKS:
/// 1. DbSet<Book> defines a table called "Books" in the database
/// 2. When you call SaveChangesAsync(), EF Core generates SQL and runs it
/// 3. When you query, EF Core converts LINQ to SQL
/// 
/// EXAMPLE:
/// 
/// INSERT:
///   var book = Book.CreateNew("1984", "Orwell", "...", "...", 15.99m);
///   dbContext.Books.Add(book);
///   await dbContext.SaveChangesAsync();
///   ↓ EF Core generates:
///   INSERT INTO Books (Id, Title, Author, ...) VALUES (...)
/// 
/// QUERY:
///   var books = await dbContext.Books
///       .Where(b => b.Status == BookStatus.Published)
///       .ToListAsync();
///   ↓ EF Core generates:
///   SELECT * FROM Books WHERE Status = 1
/// 
/// WHY IN INFRASTRUCTURE LAYER?
/// - This is technical/database-specific code
/// - Domain layer (Book.cs) doesn't know about Entity Framework
/// - Infrastructure handles "how to persist" not "what to persist"
/// </summary>
public class BookServiceDbContext : DbContext
{
    /// <summary>
    /// DbSet represents the "Books" table in the database.
    /// 
    /// USAGE:
    ///   var books = await dbContext.Books.ToListAsync();
    ///   var book = await dbContext.Books.FirstOrDefaultAsync(b => b.Id == id);
    ///   dbContext.Books.Add(newBook);
    /// </summary>
    public DbSet<Book> Books { get; set; } = null!;

    /// <summary>
    /// Constructor that receives DbContextOptions.
    /// 
    /// WHY?
    /// Entity Framework needs to know:
    /// - Which database provider (PostgreSQL, MySQL, SQL Server, etc.)
    /// - Connection string
    /// - Other options (logging, migrations, etc.)
    /// 
    /// This is passed from Program.cs when dependency injection creates the context.
    /// </summary>
    public BookServiceDbContext(DbContextOptions<BookServiceDbContext> options)
        : base(options)
    {
    }

    /// <summary>
    /// OnModelCreating - Configure Entity Framework mappings.
    /// 
    /// WHAT IT DOES:
    /// Defines how C# entities map to database tables, columns, constraints, etc.
    /// 
    /// CONFIGURATION:
    /// - Table names and schemas
    /// - Column types and lengths
    /// - Primary keys and foreign keys
    /// - Indexes
    /// - Default values
    /// - Constraints
    /// 
    /// TWO WAYS TO CONFIGURE:
    /// 1. Data annotations on properties (e.g., [MaxLength(500)])
    /// 2. Fluent API here in OnModelCreating (more powerful)
    /// 
    /// We use Fluent API because it's more flexible and easier to test.
    /// </summary>
    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // ============================================================
        // Configure the Book entity
        // ============================================================

        modelBuilder.Entity<Book>(entity =>
        {
            // Table name
            entity.ToTable("Books", "public");

            // Primary Key
            entity.HasKey(b => b.Id);

            // Properties configuration
            entity.Property(b => b.Id)
                .HasColumnName("id")
                .HasColumnType("uuid")
                .ValueGeneratedNever();  // We set this in C# with Guid.NewGuid()

            entity.Property(b => b.Title)
                .HasColumnName("title")
                .HasColumnType("character varying(500)")
                .IsRequired()
                .HasMaxLength(500);

            entity.Property(b => b.Author)
                .HasColumnName("author")
                .HasColumnType("character varying(300)")
                .IsRequired()
                .HasMaxLength(300);

            entity.Property(b => b.ISBN)
                .HasColumnName("isbn")
                .HasColumnType("character varying(20)")
                .IsRequired()
                .HasMaxLength(20);

            entity.Property(b => b.Description)
                .HasColumnName("description")
                .HasColumnType("text")
                .IsRequired();

            entity.Property(b => b.Price)
                .HasColumnName("price")
                .HasColumnType("numeric(10,2)")
                .IsRequired();

            entity.Property(b => b.Status)
                .HasColumnName("status")
                .HasColumnType("integer")
                .IsRequired()
                .HasDefaultValue(0);  // Default: Draft

            entity.Property(b => b.ImageUrl)
                .HasColumnName("image_url")
                .HasColumnType("character varying(1000)")
                .IsRequired(false);

            entity.Property(b => b.PublishedDate)
                .HasColumnName("published_date")
                .HasColumnType("timestamp without time zone")
                .IsRequired(false);

            entity.Property(b => b.CreatedAt)
                .HasColumnName("created_at")
                .HasColumnType("timestamp without time zone")
                .IsRequired()
                .HasDefaultValueSql("CURRENT_TIMESTAMP");

            entity.Property(b => b.UpdatedAt)
                .HasColumnName("updated_at")
                .HasColumnType("timestamp without time zone")
                .IsRequired()
                .HasDefaultValueSql("CURRENT_TIMESTAMP");

            // ============================================================
            // Indexes for performance
            // ============================================================

            // ISBN should be unique (no two books with same ISBN)
            entity.HasIndex(b => b.ISBN)
                .IsUnique()
                .HasDatabaseName("idx_books_isbn_unique");

            // Search by status (for filtering published books, etc.)
            entity.HasIndex(b => b.Status)
                .HasDatabaseName("idx_books_status");

            // Search by created date (for sorting recent books)
            entity.HasIndex(b => b.CreatedAt)
                .HasDatabaseName("idx_books_created_at");

            // ============================================================
            // Value Converters (convert C# types to database types)
            // ============================================================

            // BookStatus enum stores as integer in database
            entity.Property(b => b.Status)
                .HasConversion<int>();
        });
    }

    /// <summary>
    /// Override SaveChangesAsync to add automatic timestamp updates.
    /// 
    /// WHAT IT DOES:
    /// Every time we save, automatically update the UpdatedAt timestamp.
    /// This gives us an audit trail of when data was modified.
    /// 
    /// WHY NOT IN DATABASE?
    /// We could use database triggers, but doing it in code is:
    /// - Portable (works with any database)
    /// - Testable
    /// - Explicit
    /// </summary>
    public override async Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        // Get all modified entities
        var entries = ChangeTracker.Entries()
            .Where(e => e.State == EntityState.Modified);

        // Update the UpdatedAt timestamp for each modified entity
        foreach (var entry in entries)
        {
            if (entry.Entity is Book book)
            {
                entry.Property(nameof(Book.UpdatedAt)).CurrentValue = DateTime.UtcNow;
            }
        }

        return await base.SaveChangesAsync(cancellationToken);
    }
}
