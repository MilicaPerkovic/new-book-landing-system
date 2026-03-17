package si.um.feri.userservice.infrastructure.logging;

import io.grpc.ForwardingServerCall;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

@GrpcGlobalServerInterceptor
public class GrpcLoggingInterceptor implements ServerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcLoggingInterceptor.class);
    private static final Metadata.Key<String> REQUEST_ID_HEADER =
            Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <R, S> ServerCall.Listener<R> interceptCall(ServerCall<R, S> call,
                                                       Metadata headers,
                                                       ServerCallHandler<R, S> next) {
        String requestId = headers.get(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String method = call.getMethodDescriptor().getFullMethodName();
        final long startedAt = System.currentTimeMillis();

        withRequestId(requestId, () -> LOG.info("gRPC request started: method={}", method));

        ServerCall<R, S> forwardingCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                long durationMs = System.currentTimeMillis() - startedAt;
                withRequestId(requestId, () -> LOG.info(
                        "gRPC request finished: method={}, status={}, durationMs={}",
                        method,
                        status.getCode(),
                        durationMs
                ));
                super.close(status, trailers);
            }
        };

        ServerCall.Listener<R> listener = next.startCall(forwardingCall, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onCancel() {
                withRequestId(requestId, () -> LOG.warn("gRPC request cancelled: method={}", method));
                super.onCancel();
            }

            @Override
            public void onComplete() {
                withRequestId(requestId, () -> LOG.debug("gRPC request complete callback: method={}", method));
                super.onComplete();
            }

            @Override
            public void onHalfClose() {
                withRequestId(requestId, () -> LOG.debug("gRPC request half-close: method={}", method));
                super.onHalfClose();
            }

            @Override
            public void onReady() {
                withRequestId(requestId, () -> LOG.debug("gRPC call ready: method={}", method));
                super.onReady();
            }
        };
    }

    private void withRequestId(String requestId, Runnable runnable) {
        MDC.put("requestId", requestId);
        try {
            runnable.run();
        } finally {
            MDC.remove("requestId");
        }
    }
}
