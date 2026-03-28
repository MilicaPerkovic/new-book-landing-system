package si.um.feri.orders.api.exception;

import si.um.feri.orders.api.dto.ErrorResponse;
import si.um.feri.orders.application.exception.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Global exception mapper for order service domain exceptions.
 * 
 * Converts OrderDomainException subtypes to appropriate HTTP status codes:
 * - 400 Bad Request: OrderValidationException
 * - 404 Not Found: OrderNotFoundException
 * - 409 Conflict: InvalidOrderStateTransitionException
 * - 422 Unprocessable Entity: OrderDomainException (generic)
 * - 500 Internal Server Error: Any other exception
 * 
 * All responses include request context (path, requestId) for debugging.
 */
@Provider
public class OrderExceptionMapper implements ExceptionMapper<OrderDomainException> {

    @Override
    public Response toResponse(OrderDomainException exception) {
        String requestId = Optional.ofNullable(MDC.get("requestId")).orElse("unknown");
        String path = Optional.ofNullable(MDC.get("path")).orElse("/api/orders");

        // Check each exception type and map to HTTP status
        if (exception instanceof OrderValidationException) {
            return Response.status(400)
                .entity(ErrorResponse.of(
                    "VALIDATION_ERROR",
                    exception.getMessage(),
                    path,
                    requestId
                ))
                .build();
        }

        if (exception instanceof OrderNotFoundException) {
            return Response.status(404)
                .entity(ErrorResponse.of(
                    "ORDER_NOT_FOUND",
                    exception.getMessage(),
                    path,
                    requestId
                ))
                .build();
        }

        if (exception instanceof InvalidOrderStateTransitionException) {
            return Response.status(409)
                .entity(ErrorResponse.of(
                    "INVALID_STATE_TRANSITION",
                    exception.getMessage(),
                    path,
                    requestId
                ))
                .build();
        }

        // Generic OrderDomainException - 422
        return Response.status(422)
            .entity(ErrorResponse.of(
                "DOMAIN_ERROR",
                exception.getMessage(),
                path,
                requestId
            ))
            .build();
    }
}
