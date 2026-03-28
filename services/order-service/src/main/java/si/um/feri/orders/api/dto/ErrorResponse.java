package si.um.feri.orders.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Standard error response format for all API errors.
 * 
 * Fields:
 * - errorCode: Machine-readable error identifier
 * - message: Human-readable error description
 * - timestamp: When error occurred (ISO-8601)
 * - path: HTTP request path where error occurred
 * - requestId: Unique request identifier for tracing
 */
public record ErrorResponse(
    String errorCode,
    String message,
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime timestamp,
    String path,
    String requestId
) {

    /**
     * Factory method for creating error responses.
     */
    public static ErrorResponse of(String errorCode, String message, String path, String requestId) {
        return new ErrorResponse(
            errorCode,
            message,
            LocalDateTime.now(),
            path,
            requestId
        );
    }
}
