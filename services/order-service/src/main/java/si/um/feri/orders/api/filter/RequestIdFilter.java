package si.um.feri.orders.api.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * HTTP filter for request correlation and tracing.
 * 
 * Responsibilities:
 * 1. Extract or generate X-Request-Id header
 * 2. Store request ID in MDC (Mapped Diagnostic Context)
 * 3. Add request ID to response headers
 * 4. Store request path in MDC for error context
 * 
 * Usage:
 * - Client can pass X-Request-Id header for custom tracking
 * - If not provided, service generates UUID
 * - All logs for request include requestId automatically
 * - Error responses include requestId for correlation
 */
@Provider
public class RequestIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_PROPERTY = "requestId";
    private static final String PATH_PROPERTY = "path";

    /**
     * Extract or generate request ID from inbound request.
     * Store request metadata in MDC for logging.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Get request ID from header or generate new one
        String requestId = requestContext.getHeaderString(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        // Store in request context for later retrieval
        requestContext.setProperty(REQUEST_PROPERTY, requestId);

        // Store request path
        String path = requestContext.getUriInfo().getPath();
        requestContext.setProperty(PATH_PROPERTY, path);

        // Store in MDC for logging (all subsequent logs include requestId and path)
        MDC.put("requestId", requestId);
        MDC.put("path", path);
    }

    /**
     * Add request ID to response headers for client reference.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) {
        String requestId = (String) requestContext.getProperty(REQUEST_PROPERTY);
        if (requestId != null) {
            responseContext.getHeaders().putSingle(REQUEST_ID_HEADER, requestId);
        }
    }
}
