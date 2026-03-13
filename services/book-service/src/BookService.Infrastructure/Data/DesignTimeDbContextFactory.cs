using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Microsoft.Extensions.Configuration;
using System.IO;

namespace BookService.Infrastructure.Data;

/// <summary>
/// DesignTimeDbContextFactory - Creates DbContext instances at design-time.
/// 
/// WHAT IS THIS?
/// Entity Framework Core needs a DbContext instance to:
/// - Generate migrations (database schema changes)
/// - Update databases
/// - Scaffold code
/// 
/// WHY CAN'T WE USE DEPENDENCY INJECTION?
/// At design-time (when running EF Core commands):
/// 1. No ASP.NET Core host is running
/// 2. No dependency injection container exists
/// 3. EF Core CLI can't access Program.cs services
/// 
/// SOLUTION: Factory that creates DbContext without DI
/// 
/// WHEN IS THIS USED?
/// When you run:
///   dotnet ef migrations add CreateInitial
///   dotnet ef database update
///   dotnet ef migrations list
/// 
/// EF Core CLI looks for IDesignTimeDbContextFactory<T> implementation
/// and calls CreateDbContext() to get a context
/// 
/// CONFIGURATION:
/// Reads from appsettings.json to get connection string
/// Works in development environment
/// </summary>
public class DesignTimeDbContextFactory : IDesignTimeDbContextFactory<BookServiceDbContext>
{
    /// <summary>
    /// Create a DbContext instance for design-time tools.
    /// 
    /// CALLED BY: EF Core CLI tool (dotnet ef)
    /// 
    /// PARAMETERS: args - command line arguments (usually empty)
    /// 
    /// HOW IT WORKS:
    /// 1. Load configuration from appsettings.json
    /// 2. Get connection string
    /// 3. Build DbContextOptions with PostgreSQL provider
    /// 4. Create and return BookServiceDbContext
    /// 
    /// AFTER THIS:
    /// EF Core can now generate migrations, update database, etc.
    /// </summary>
    public BookServiceDbContext CreateDbContext(string[] args)
    {
        // Use local configuration file
        var configuration = new ConfigurationBuilder()
            .SetBasePath(Directory.GetCurrentDirectory())
            .AddJsonFile("appsettings.json")
            .AddJsonFile("appsettings.Development.json", optional: true)
            .Build();

        // Get connection string for PostgreSQL
        var connectionString = configuration.GetConnectionString("DefaultConnection");

        if (string.IsNullOrEmpty(connectionString))
        {
            throw new InvalidOperationException(
                "Connection string 'DefaultConnection' not found. " +
                "Check appsettings.json or appsettings.Development.json");
        }

        // Build options for PostgreSQL
        var optionsBuilder = new DbContextOptionsBuilder<BookServiceDbContext>();
        optionsBuilder.UseNpgsql(connectionString);

        return new BookServiceDbContext(optionsBuilder.Options);
    }
}
