using BookService.API.DTOs;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Builder;
using System;
using System.Net;
using System.Threading.Tasks;
using System.Text.Json;
using Microsoft.Extensions.Logging;

namespace BookService.API.Middleware;

/// <summary>
/// GlobalExceptionMiddleware - Catches ALL exceptions and returns proper error responses
/// 
/// WHAT IS MIDDLEWARE?
/// Middleware is code that runs on EVERY HTTP request/response.
/// It's like a chain:
/// 
/// Request:  HTTP → Auth → CORS → Exception Handler → Controller → Domain → Repo
/// Response: HTTP ← Auth ← CORS ← Exception Handler ← Controller ← Domain ← Repo
/// 
/// WHAT THIS MIDDLEWARE DOES:
/// 1. Wraps the entire request in try-catch
/// 2. If exception occurs anywhere, catches it
/// 3. Logs the error
/// 4. Converts to ErrorResponse (JSON)
/// 5. Returns appropriate HTTP status code
/// 
/// WITHOUT THIS MIDDLEWARE:
/// Unhandled exception → 500 Internal Server Error (generic message)
/// User sees: "Internal server error" (not helpful)
/// 
/// WITH THIS MIDDLEWARE:
/// Custom exceptions → Meaningful errors
/// User sees: "ISBN already exists" (helpful)
/// 
/// EXAMPLE FLOW:
/// 1. POST /api/books with duplicate ISBN
/// 2. BookService detects duplicate
/// 3. Returns Result.Failure("ISBN already exists")
/// 4. Controller checks result.IsSuccess
/// 5. If not success, throws custom exception
/// 6. Middleware catches it
/// 7. Returns 400 Bad Request with error message
/// 8. Client knows exactly what went wrong
/// </summary>
public class GlobalExceptionMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<GlobalExceptionMiddleware> _logger;

    public GlobalExceptionMiddleware(RequestDelegate next, ILogger<GlobalExceptionMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    /// <summary>
    /// Middleware invoke method - runs on every request
    /// 
    /// HttpContext contains:
    /// - Request: HTTP method, path, headers, body
    /// - Response: status code, headers, body
    /// </summary>
    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            // Continue to next middleware/endpoint
            await _next(context);
        }
        catch (Exception ex)
        {
            // Exception occurred - handle it
            _logger.LogError(ex, "Unhandled exception occurred");
            await HandleExceptionAsync(context, ex);
        }
    }

    /// <summary>
    /// Handle the exception and return appropriate response
    /// 
    /// CONVERTS EXCEPTIONS TO HTTP RESPONSES:
    /// - ArgumentException → 400 Bad Request
    /// - KeyNotFoundException → 404 Not Found
    /// - InvalidOperationException → 409 Conflict
    /// - Other → 500 Internal Server Error
    /// </summary>
    private static Task HandleExceptionAsync(HttpContext context, Exception exception)
    {
        // Set response type to JSON
        context.Response.ContentType = "application/json";

        // Determine HTTP status code based on exception type
        var response = exception switch
        {
            // 400 Bad Request - Invalid input
            ArgumentException => new ErrorResponse
            {
                Status = StatusCodes.Status400BadRequest,
                Message = exception.Message,
                ErrorCode = "VALIDATION_ERROR"
            },

            // 404 Not Found
            KeyNotFoundException => new ErrorResponse
            {
                Status = StatusCodes.Status404NotFound,
                Message = exception.Message,
                ErrorCode = "NOT_FOUND"
            },

            // 409 Conflict - Invalid operation (e.g., try to publish already published book)
            InvalidOperationException => new ErrorResponse
            {
                Status = StatusCodes.Status409Conflict,
                Message = exception.Message,
                ErrorCode = "INVALID_OPERATION"
            },

            // 500 Internal Server Error - Unexpected error
            _ => new ErrorResponse
            {
                Status = StatusCodes.Status500InternalServerError,
                Message = "An unexpected error occurred",
                ErrorCode = "INTERNAL_ERROR"
            }
        };

        // Set HTTP status code
        context.Response.StatusCode = response.Status;

        // Return error as JSON
        return context.Response.WriteAsJsonAsync(response);
    }
}

/// <summary>
/// Extension method to register GlobalExceptionMiddleware
/// 
/// USAGE in Program.cs:
/// app.UseGlobalExceptionMiddleware();
/// 
/// Note: Must be one of the FIRST middlewares
/// So it catches exceptions from all other middlewares
/// </summary>
public static class GlobalExceptionMiddlewareExtensions
{
    public static IApplicationBuilder UseGlobalExceptionMiddleware(this IApplicationBuilder app)
    {
        return app.UseMiddleware<GlobalExceptionMiddleware>();
    }
}
