using Serilog;
using Serilog.Formatting.Compact;
using BookService.Infrastructure.Extensions;
using BookService.Domain.Services;
using BookService.Domain.Interfaces;
using BookService.API.DTOs;
using BookService.API.Validators;
using BookService.API.Middleware;
using FluentValidation;

var builder = WebApplication.CreateBuilder(args);

// ============================================================
// 1. LOGGING SETUP - Configure Serilog for structured logging
// ============================================================
// Serilog allows us to write logs in a structured format (JSON)
// This makes it easy to search and analyze logs later
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .Enrich.FromLogContext()
    .WriteTo.Console(new CompactJsonFormatter())
    .WriteTo.File(
        path: "logs/bookservice-.txt",
        rollingInterval: RollingInterval.Day,
        outputTemplate: "{Timestamp:yyyy-MM-dd HH:mm:ss.fff zzz} [{Level:u3}] {Message:lj}{NewLine}{Exception}"
    )
    .CreateLogger();

builder.Host.UseSerilog();

try
{
    Log.Information("Starting Book Service...");

    // ============================================================
    // 2. DEPENDENCY INJECTION - Register services
    // ============================================================
    // This is where we tell .NET what to inject when a class needs 
    // a dependency. For example: if a controller needs IBookService,
    // .NET will automatically create/provide an instance.

    // Add services to the container
    builder.Services.AddControllers();

    // Swagger/OpenAPI - Interactive API documentation
    builder.Services.AddEndpointsApiExplorer();
    builder.Services.AddSwaggerGen(options =>
    {
        options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
        {
            Title = "Book Service API",
            Version = "v1.0",
            Description = "API for managing books. Part of Book Landing System."
        });
    });

    // CORS - Allow frontend to call this backend API
    builder.Services.AddCors(options =>
    {
        options.AddPolicy("AllowAll",
            policy =>
            {
                policy.AllowAnyOrigin()
                    .AllowAnyMethod()
                    .AllowAnyHeader();
            });
    });

    // FluentValidation - Input data validation
    builder.Services.AddValidatorsFromAssemblyContaining<CreateBookRequestValidator>();

    // ============================================================
    // 3. BUSINESS LOGIC LAYER
    // ============================================================
    // Register BookService (business logic)
builder.Services.AddScoped<BookService.Domain.Services.BookService>();
    // ============================================================
    // 4. INFRASTRUCTURE LAYER (Phase 1C)
    // ============================================================
    // Register all infrastructure services:
    // - DbContext (Entity Framework Core)
    // - IBookRepository → BookRepository (PostgreSQL implementation)
    // - Database migration support
    builder.Services.AddInfrastructure(builder.Configuration);

    // Build the application
    var app = builder.Build();

    // ============================================================
    // 5. HTTP REQUEST PIPELINE - Configure middleware
    // ============================================================
    // Middleware is code that runs on EVERY incoming HTTP request
    // It processes the request and passes it down the pipeline

    // ============================================================
    // 5A. GLOBAL EXCEPTION MIDDLEWARE
    // ============================================================
    // Must be near the top to catch exceptions from other middleware
    app.UseGlobalExceptionMiddleware();

    if (app.Environment.IsDevelopment())
    {
        // Swagger UI - Interactive API documentation
        // Access at: http://localhost:5000/swagger
        app.UseSwagger();
        app.UseSwaggerUI(options =>
        {
            options.SwaggerEndpoint("/swagger/v1/swagger.json", "Book Service API v1");
            options.RoutePrefix = "swagger";
        });
    }

    // Use HTTPS redirection in production
    app.UseHttpsRedirection();

    // CORS - Enable cross-origin requests
    app.UseCors("AllowAll");

    // Enable authentication/authorization (we'll use this later)
    // app.UseAuthentication();
    // app.UseAuthorization();

    // Map controllers (finds all controller classes and routes)
    app.MapControllers();

        // Health check endpoint - used by Docker to verify the service is alive
        app.MapGet("/health", () => Results.Ok(new { status = "healthy", service = "book-service" }));

    // ============================================================
    // 6. DATABASE INITIALIZATION
    // ============================================================
    // Apply migrations and initialize database
    // This runs ONCE at startup:
    // 1. Creates database if not exists
    // 2. Applies pending migrations
    // 3. Runs any seeding code
    try
    {
        Log.Information("Initializing database...");
        await app.Services.InitializeDatabaseAsync();
        Log.Information("Database initialized successfully");
    }
    catch (Exception ex)
    {
        Log.Fatal(ex, "Failed to initialize database");
        throw;
    }

    Log.Information("Book Service started successfully");
    await app.RunAsync();
}
catch (Exception ex)
{
    Log.Fatal(ex, "Book Service terminated unexpectedly");
}
finally
{
    Log.CloseAndFlush();
}

// Make Program accessible for integration testing
public partial class Program { }
