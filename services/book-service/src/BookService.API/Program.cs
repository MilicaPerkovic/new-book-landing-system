using Serilog;
using Serilog.Formatting.Compact;

var builder = WebApplication.CreateBuilder(args);

// ============================================================
// 1. LOGGING SETUP - Configure Serilog for structured logging
// ============================================================
// Serilog allows us to write logs in a structured format (JSON)
// This makes it easy to search and analyze logs later
Log.Logger = new LoggerConfiguration()
    .MinimumLevel.Information()
    .Enrich.FromLogContext()
    .Enrich.WithMachineName()
    .Enrich.WithThreadId()
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

    // ============================================================
    // 3. BUSINESS LOGIC LAYER
    // ============================================================
    // TODO: In Phase 2, we will register BookService here
    // builder.Services.AddScoped<IBookService, BookService>();
    // builder.Services.AddScoped<IBookRepository, BookRepository>();

    // ============================================================
    // 4. DATABASE SETUP
    // ============================================================
    // TODO: In Phase 2, we will add Entity Framework Core here
    // builder.Services.AddDbContext<BookServiceDbContext>(options =>
    //     options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection"))
    // );

    // Build the application
    var app = builder.Build();

    // ============================================================
    // 5. HTTP REQUEST PIPELINE - Configure middleware
    // ============================================================
    // Middleware is code that runs on EVERY incoming HTTP request
    // It processes the request and passes it down the pipeline

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
