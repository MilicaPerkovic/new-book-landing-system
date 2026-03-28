package si.um.feri.orders.grpc.interceptor;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.quarkus.grpc.GlobalInterceptor;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * gRPC Logging Interceptor.
 * 
 * Intercepts all gRPC calls to provide:
 * - Request correlation via unique request IDs
 * - Performance tracking (method duration)
 * - Structured logging with call context
 * - Distributed tracing support (X-Request-Id header)
 * 
 * Applied globally to all gRPC services.
 * All logs for a request include the same requestId for correlation.
 */
@ApplicationScoped
@GlobalInterceptor
public class GrpcLoggingInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GrpcLoggingInterceptor.class);

    private static final Metadata.Key<String> REQUEST_ID_KEY =
        Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);

    private static final Metadata.Key<String> CALLER_ID_KEY =
        Metadata.Key.of("x-caller-id", Metadata.ASCII_STRING_MARSHALLER);

    /**
     * Intercept gRPC call.
     * 
     * Flow:
     * 1. Extract correlation metadata from request headers
     * 2. Store in MDC (Mapped Diagnostic Context) for logging
     * 3. Wrap response to capture completion/error
     * 4. Clean up MDC after response
     * 
     * @param call Server call metadata
     * @param headers Request headers (metadata)
     * @param next Handler for actual service call
     * @return Listener for response
     */
    @Override
    public <ReqT, RespT> Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        
        // Extract or generate request ID (make final for inner class use)
        final String requestId = headers.get(REQUEST_ID_KEY) != null 
            ? headers.get(REQUEST_ID_KEY) 
            : UUID.randomUUID().toString();
        
        final String callerId = headers.get(CALLER_ID_KEY);
        
        // Store in MDC for all logs in this request
        MDC.put("X-Request-Id", requestId);
        if (callerId != null) {
            MDC.put("X-Caller-Id", callerId);
        }
        
        final long startTime = System.currentTimeMillis();
        final String methodName = call.getMethodDescriptor().getFullMethodName();
        
        log.info("gRPC call started: {} [{}]", methodName, requestId);
        
        // Wrap the call to track completion
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
            @Override
            public void close(io.grpc.Status status, Metadata trailers) {
                long duration = System.currentTimeMillis() - startTime;
                
                if (status.isOk()) {
                    log.info("gRPC call completed: {} [{}] ({} ms)", methodName, requestId, duration);
                } else {
                    log.error("gRPC call failed: {} [{}] status={} ({} ms)", 
                        methodName, requestId, status.getCode(), duration);
                }
                
                super.close(status, trailers);
                
                // Clean up MDC after response
                MDC.remove("X-Request-Id");
                if (callerId != null) {
                    MDC.remove("X-Caller-Id");
                }
            }
        };
        
        // Wrap the response listener
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
            next.startCall(wrappedCall, headers)) {
            
            @Override
            public void onHalfClose() {
                log.debug("gRPC onHalfClose: {} [{}]", methodName, requestId);
                super.onHalfClose();
            }
            
            @Override
            public void onCancel() {
                log.warn("gRPC call cancelled: {} [{}]", methodName, requestId);
                super.onCancel();
            }
        };
    }
}
