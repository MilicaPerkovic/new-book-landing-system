using BookService.API.DTOs;
using FluentValidation;

namespace BookService.API.Validators;

/// <summary>
/// CreateBookRequestValidator - Validates CreateBookRequest
/// 
/// WHY FLUENT VALIDATION?
/// Instead of: if(string.IsNullOrEmpty(request.Title)) throw ...
/// We use declarative validators:
/// 
/// RuleFor(x => x.Title)
///     .NotEmpty()
///     .MaximumLength(500)
/// 
/// BENEFITS:
/// 1. DECLARATIVE - Rules are clear and readable
/// 2. REUSABLE - Same validator used everywhere
/// 3. TESTABLE - Easy to test validation rules
/// 4. AUTOMATIC - ASP.NET Core applies automatically
/// 5. LOCALIZED - Can localize error messages
/// 
/// FLOW:
/// 1. Client sends POST /api/books with data
/// 2. ASP.NET Core deserializes to CreateBookRequest
/// 3. ModelState validator runs
/// 4. FluentValidation runs (extra validation)
/// 5. If valid, proceeds to controller
/// 6. If invalid, returns 400 Bad Request with errors
/// 
/// EXAMPLE ERROR RESPONSE:
/// HTTP/1.1 400 Bad Request
/// {
///   "errors": {
///     "title": ["'Title' must not be empty"],
///     "isbn": ["'ISBN' must be a valid 13-digit ISBN"]
///   }
/// }
/// </summary>
public class CreateBookRequestValidator : AbstractValidator<CreateBookRequest>
{
    public CreateBookRequestValidator()
    {
        // Title validation
        RuleFor(x => x.Title)
            .NotEmpty()
            .WithMessage("Title is required")
            .MaximumLength(500)
            .WithMessage("Title cannot exceed 500 characters")
            .MinimumLength(1)
            .WithMessage("Title must be at least 1 character");

        // Author validation
        RuleFor(x => x.Author)
            .NotEmpty()
            .WithMessage("Author is required")
            .MaximumLength(300)
            .WithMessage("Author cannot exceed 300 characters")
            .MinimumLength(1)
            .WithMessage("Author must be at least 1 character");

        // ISBN validation
        RuleFor(x => x.ISBN)
            .NotEmpty()
            .WithMessage("ISBN is required")
            .Must(BeValidISBN)
            .WithMessage("ISBN must be a valid 13-digit ISBN (format: XXX-X-XXXXX-X or 13 digits)");

        // Description validation
        RuleFor(x => x.Description)
            .NotEmpty()
            .WithMessage("Description is required")
            .MaximumLength(5000)
            .WithMessage("Description cannot exceed 5000 characters")
            .MinimumLength(10)
            .WithMessage("Description must be at least 10 characters");

        // Price validation
        RuleFor(x => x.Price)
            .GreaterThan(0)
            .WithMessage("Price must be greater than 0")
            .LessThanOrEqualTo(10000)
            .WithMessage("Price cannot exceed 10000");

        // ImageUrl validation (optional)
        RuleFor(x => x.ImageUrl)
            .Must(BeValidUrl)
            .When(x => !string.IsNullOrEmpty(x.ImageUrl))
            .WithMessage("ImageUrl must be a valid URL")
            .MaximumLength(1000)
            .WithMessage("ImageUrl cannot exceed 1000 characters");
    }

    /// <summary>
    /// Custom validation: Check if ISBN is valid 13-digit format
    /// 
    /// ISBN-13 format: XXX-X-XXXXX-X (with hyphens) or 13 digits without
    /// </summary>
    private static bool BeValidISBN(string isbn)
    {
        if (string.IsNullOrWhiteSpace(isbn))
            return false;

        var isbnDigits = isbn.Replace("-", "");
        return isbnDigits.Length == 13 && isbnDigits.All(char.IsDigit);
    }

    /// <summary>
    /// Custom validation: Check if URL is valid
    /// 
    /// WHY? Prevent storing invalid URLs
    /// </summary>
    private static bool BeValidUrl(string? url)
    {
        if (string.IsNullOrWhiteSpace(url))
            return true; // Optional field, so empty is OK

        return Uri.TryCreate(url, UriKind.Absolute, out var uriResult)
            && (uriResult.Scheme == Uri.UriSchemeHttp || uriResult.Scheme == Uri.UriSchemeHttps);
    }
}

/// <summary>
/// UpdateBookRequestValidator - Validates UpdateBookRequest
/// 
/// Note: Similar to Create but might have different rules
/// Example: Create requires all fields, Update might only update some
/// </summary>
public class UpdateBookRequestValidator : AbstractValidator<UpdateBookRequest>
{
    public UpdateBookRequestValidator()
    {
        // Same rules as CreateBookRequestValidator
        // (ISBN is excluded intentionally - can't update ISBN)

        RuleFor(x => x.Title)
            .NotEmpty()
            .WithMessage("Title is required")
            .MaximumLength(500)
            .WithMessage("Title cannot exceed 500 characters");

        RuleFor(x => x.Author)
            .NotEmpty()
            .WithMessage("Author is required")
            .MaximumLength(300)
            .WithMessage("Author cannot exceed 300 characters");

        RuleFor(x => x.Description)
            .NotEmpty()
            .WithMessage("Description is required")
            .MaximumLength(5000)
            .WithMessage("Description cannot exceed 5000 characters")
            .MinimumLength(10)
            .WithMessage("Description must be at least 10 characters");

        RuleFor(x => x.Price)
            .GreaterThan(0)
            .WithMessage("Price must be greater than 0")
            .LessThanOrEqualTo(10000)
            .WithMessage("Price cannot exceed 10000");

        RuleFor(x => x.ImageUrl)
            .Must(BeValidUrl)
            .When(x => !string.IsNullOrEmpty(x.ImageUrl))
            .WithMessage("ImageUrl must be a valid URL")
            .MaximumLength(1000)
            .WithMessage("ImageUrl cannot exceed 1000 characters");
    }

    private static bool BeValidUrl(string? url)
    {
        if (string.IsNullOrWhiteSpace(url))
            return true;

        return Uri.TryCreate(url, UriKind.Absolute, out var uriResult)
            && (uriResult.Scheme == Uri.UriSchemeHttp || uriResult.Scheme == Uri.UriSchemeHttps);
    }
}
