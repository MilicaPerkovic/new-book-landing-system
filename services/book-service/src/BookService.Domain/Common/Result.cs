namespace BookService.Domain.Common;

/// <summary>
/// Result class for encapsulating operation outcomes.
/// 
/// WHY USE THIS?
/// Instead of throwing exceptions or returning null, we return a Result object.
/// This makes error handling predictable and explicit.
/// 
/// EXAMPLE:
///   var result = await bookService.CreateBookAsync(bookData);
///   if (!result.IsSuccess)
///   {
///       Console.WriteLine($"Error: {result.ErrorMessage}");
///       return;
///   }
///   Console.WriteLine($"Book created: {result.Data.Id}");
/// 
/// BENEFITS:
/// - Easy to chain operations
/// - No null reference exceptions
/// - Error handling is explicit
/// - Can include additional metadata (error codes, etc.)
/// </summary>
public class Result<TData>
{
    /// <summary>Whether the operation succeeded</summary>
    public bool IsSuccess { get; private set; }

    /// <summary>The actual data/result from the operation (if successful)</summary>
    public TData? Data { get; private set; }

    /// <summary>Error message (if operation failed)</summary>
    public string? ErrorMessage { get; private set; }

    /// <summary>Error code for categorizing the error</summary>
    public string? ErrorCode { get; private set; }

    private Result(bool isSuccess, TData? data, string? errorMessage, string? errorCode)
    {
        IsSuccess = isSuccess;
        Data = data;
        ErrorMessage = errorMessage;
        ErrorCode = errorCode;
    }

    /// <summary>Create a successful result with data</summary>
    public static Result<TData> Success(TData data) =>
        new(true, data, null, null);

    /// <summary>Create a failed result with error message</summary>
    public static Result<TData> Failure(string errorMessage, string? errorCode = null) =>
        new(false, default, errorMessage, errorCode);

    /// <summary>Create a failed result with exception</summary>
    public static Result<TData> Failure(Exception exception) =>
        new(false, default, exception.Message, "EXCEPTION");
}

/// <summary>
/// Result class without data (for operations that don't return anything).
/// 
/// Example: Delete operation - we don't return the deleted item, just success/failure
/// </summary>
public class Result
{
    public bool IsSuccess { get; private set; }
    public string? ErrorMessage { get; private set; }
    public string? ErrorCode { get; private set; }

    private Result(bool isSuccess, string? errorMessage, string? errorCode)
    {
        IsSuccess = isSuccess;
        ErrorMessage = errorMessage;
        ErrorCode = errorCode;
    }

    public static Result Success() =>
        new(true, null, null);

    public static Result Failure(string errorMessage, string? errorCode = null) =>
        new(false, errorMessage, errorCode);
}
