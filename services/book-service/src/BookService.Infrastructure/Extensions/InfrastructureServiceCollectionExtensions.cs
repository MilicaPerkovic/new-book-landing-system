using System;
using BookService.Domain.Interfaces;
using BookService.Infrastructure.Data;
using BookService.Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace BookService.Infrastructure.Extensions;

/// <summary>
/// InfrastructureServiceCollectionExtensions
/// 
/// WHAT IS THIS?
/// This is an extension method that registers all infrastructure services
/// with the dependency injection container in Program.cs
/// 
/// WHY SEPARATE FILE?
/// Clean separation of concerns:
/// - Program.cs stays simple and readable
/// - All infrastructure setup is in one place
/// - Easy to reuse across multiple API projects
/// 
/// EXTENSION METHOD?
/// Allows us to call: services.AddInfrastructure(configuration)
/// Instead of manually registering 5-10 services
/// 
/// HOW TO USE (in Program.cs):
/// 
///   var builder = WebApplication.CreateBuilder(args);
///   builder.Services.AddInfrastructure(builder.Configuration);
///   
/// That single line registers:
/// - BookServiceDbContext (database context)
/// - IBookRepository → BookRepository (repository implementation)
/// - Database options
/// - Migration tool support
/// </summary>
public static class InfrastructureServiceCollectionExtensions
{
    /// <summary>
    /// Add infrastructure services to the dependency injection container.
    /// 
    /// PARAMETERS:
    /// - services: The DI container
    /// - configuration: Configuration from appsettings.json
    /// 
    /// REGISTERED SERVICES:
    /// 1. DbContext - for database access
    /// 2. Repository - for data operations
    /// 3. Database migrations - for schema management
    /// 
    /// SCOPED vs TRANSIENT vs SINGLETON:
    /// - DbContext: Scoped (one per HTTP request)
    ///   Why? Tracks entities per request, disposed after response
    /// - IBookRepository: Scoped (depends on DbContext)
    ///   Why? Should live same lifetime as DbContext
    /// </summary>
    public static IServiceCollection AddInfrastructure(
        this IServiceCollection services,
        IConfiguration configuration)
    {
        // Get connection string
        var connectionString = configuration.GetConnectionString("DefaultConnection");

        if (string.IsNullOrEmpty(connectionString))
        {
            throw new InvalidOperationException(
                "Connection string 'DefaultConnection' not found in appsettings.json");
        }

        // ============================================================
        // 1. Register DbContext for PostgreSQL
        // ============================================================
        // This tells .NET to use PostgreSQL with the specified connection string
        services.AddDbContext<BookServiceDbContext>(options =>
        {
            // UseNpgsql = use Npgsql provider (PostgreSQL driver)
            options.UseNpgsql(connectionString, npgsqlOptions =>
            {
                // Enable retry on transient failures (temporary database issues)
                npgsqlOptions.EnableRetryOnFailure(
                    maxRetryCount: 3,
                    maxRetryDelay: TimeSpan.FromMilliseconds(1000),
                    errorCodesToAdd: null);
            });

            // Log SQL queries (helpful for debugging)
            options.LogTo(Console.WriteLine);
        });

        // ============================================================
        // 2. Register Repository
        // ============================================================
        // When code needs IBookRepository, inject BookRepository
        // Scoped = new instance per HTTP request
        services.AddScoped<IBookRepository, BookRepository>();

        return services;
    }

    /// <summary>
    /// Initialize database (create if not exists).
    /// 
    /// WHEN TO USE:
    /// Call this in Program.cs after building the app:
    /// 
    ///   var app = builder.Build();
    ///   await app.Services.InitializeDatabaseAsync();
    /// 
    /// WHAT IT DOES:
    /// 1. Applies any pending migrations
    /// 2. Creates database if needed
    /// 3. Runs any seeding code
    /// 
    /// EXAMPLE:
    ///   // Automatically create/update database schema
    ///   await app.Services.InitializeDatabaseAsync();
    /// 
    /// SAFE TO CALL MULTIPLE TIMES:
    /// Yes! Migrations only run if not already applied
    /// </summary>
    public static async Task InitializeDatabaseAsync(this IServiceProvider services)
    {
        using var scope = services.CreateScope();
        var dbContext = scope.ServiceProvider.GetRequiredService<BookServiceDbContext>();

        try
        {
            // EnsureCreatedAsync creates all tables from the current EF Core model
            // if they do not already exist.  This is the right approach when there
            // are no migration files in the project yet.  Once you add migrations
            // (dotnet ef migrations add ...) you can switch back to MigrateAsync().
            await dbContext.Database.EnsureCreatedAsync();

            Console.WriteLine("Database initialized successfully");
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error initializing database: {ex.Message}");
            throw;
        }
    }
}
