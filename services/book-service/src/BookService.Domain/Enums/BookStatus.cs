namespace BookService.Domain.Enums;

/// <summary>
/// Enum representing the status of a book in the system.
/// 
/// WHY?: This helps us track whether a book is:
/// - Draft (being created, not yet published)
/// - Published (visible to users)
/// - Archived (old book, hidden from lists)
/// - Discontinued (no longer available)
/// 
/// USAGE: When querying books, we can filter by status:
///   var publishedBooks = await bookRepository.GetByStatusAsync(BookStatus.Published);
/// </summary>
public enum BookStatus
{
    /// <summary>Book is not yet published - only visible to author</summary>
    Draft = 0,

    /// <summary>Book is published and visible to everyone</summary>
    Published = 1,

    /// <summary>Book is hidden from search but still exists</summary>
    Archived = 2,

    /// <summary>Book is discontinued and unavailable</summary>
    Discontinued = 3
}
