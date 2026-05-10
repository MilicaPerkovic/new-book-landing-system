package si.um.feri.orders.grpc.service;

import si.um.feri.orders.application.dto.CreateOrderRequest;
import si.um.feri.orders.application.dto.OrderResponse;
import si.um.feri.orders.application.exception.InvalidOrderStateTransitionException;
import si.um.feri.orders.application.exception.OrderNotFoundException;
import si.um.feri.orders.application.exception.OrderValidationException;
import si.um.feri.orders.application.service.OrderApplicationService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * gRPC Service Implementation for Order Operations.
 * 
 * Handles inter-service communication for order management.
 * Translates between gRPC Protocol Buffer messages and application domain objects.
 * 
 * Responsibilities:
 * - Accept gRPC requests from other services
 * - Validate and convert gRPC messages to application DTOs
 * - Call OrderApplicationService business logic
 * - Map application responses to gRPC Protocol Buffer messages
 * - Handle errors and translate to gRPC Status codes
 * - Log all interactions with request correlation
 * 
 * Port: 9091 (HTTP/2)
 * Protocol: gRPC with Protocol Buffers
 */
@GrpcService
public class OrderGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(OrderGrpcService.class);

    @Inject
    OrderApplicationService applicationService;

    /**
     * Create a new order (gRPC endpoint).
     * 
     * Called by other services via gRPC stub.
     * 
     * @param request gRPC request with book_id, user_id, quantity, price
     * @param responseObserver Callback to send response/error
     */
    @Override
    public void createOrder(
            si.um.feri.orders.grpc.service.CreateOrderRequest request,
            StreamObserver<si.um.feri.orders.grpc.service.OrderResponse> responseObserver) {
        try {
            log.info("gRPC: CreateOrder requested for book {} by user {}",
                request.getBookId(), request.getUserId());

            // Convert gRPC request to application DTO
            var appRequest = new CreateOrderRequest(
                UUID.fromString(request.getBookId()),
                UUID.fromString(request.getUserId()),
                request.getQuantity(),
                new BigDecimal(request.getPrice())
            );

            // Call application service (reactive - must block for gRPC)
            var appResponse = applicationService
                .createOrder(appRequest, null)
                .await()
                .indefinitely();

            // Map to gRPC response
            var grpcResponse = mapToGrpcResponse(appResponse);

            // Send response
            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("gRPC: CreateOrder completed with id {}", grpcResponse.getId());

        } catch (OrderValidationException e) {
            log.warn("gRPC: CreateOrder validation failed: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription(e.getMessage())
                .asException());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC: CreateOrder parsing failed: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid UUID format or numeric value")
                .asException());

        } catch (Exception e) {
            log.error("gRPC: CreateOrder failed with exception", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asException());
        }
    }

    /**
     * Retrieve an order by ID (gRPC endpoint).
     * 
     * @param request gRPC request with order_id
     * @param responseObserver Callback to send response/error
     */
    @Override
    public void getOrder(
            si.um.feri.orders.grpc.service.GetOrderRequest request,
            StreamObserver<si.um.feri.orders.grpc.service.OrderResponse> responseObserver) {
        try {
            UUID orderId = UUID.fromString(request.getOrderId());
            log.info("gRPC: GetOrder requested for id {}", orderId);

            // Call application service
            var appResponse = applicationService
                .getOrderById(orderId)
                .await()
                .indefinitely();

            // Map to gRPC response
            var grpcResponse = mapToGrpcResponse(appResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("gRPC: GetOrder completed");

        } catch (OrderNotFoundException e) {
            log.warn("gRPC: GetOrder not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asException());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC: GetOrder invalid UUID: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid UUID format")
                .asException());

        } catch (Exception e) {
            log.error("gRPC: GetOrder failed with exception", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asException());
        }
    }

    /**
     * Confirm an order (PENDING → CONFIRMED) (gRPC endpoint).
     * 
     * @param request gRPC request with order_id
     * @param responseObserver Callback to send response/error
     */
    @Override
    public void confirmOrder(
            si.um.feri.orders.grpc.service.ConfirmOrderRequest request,
            StreamObserver<si.um.feri.orders.grpc.service.OrderResponse> responseObserver) {
        try {
            UUID orderId = UUID.fromString(request.getOrderId());
            log.info("gRPC: ConfirmOrder requested for id {}", orderId);

            // Call application service
            var appResponse = applicationService
                .confirmOrder(orderId)
                .await()
                .indefinitely();

            // Map to gRPC response
            var grpcResponse = mapToGrpcResponse(appResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("gRPC: ConfirmOrder completed");

        } catch (OrderNotFoundException e) {
            log.warn("gRPC: ConfirmOrder not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asException());

        } catch (InvalidOrderStateTransitionException e) {
            log.warn("gRPC: ConfirmOrder invalid state: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asException());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC: ConfirmOrder invalid UUID: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid UUID format")
                .asException());

        } catch (Exception e) {
            log.error("gRPC: ConfirmOrder failed with exception", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asException());
        }
    }

    /**
     * Cancel an order (gRPC endpoint).
     * 
     * @param request gRPC request with order_id
     * @param responseObserver Callback to send response/error
     */
    @Override
    public void cancelOrder(
            si.um.feri.orders.grpc.service.CancelOrderRequest request,
            StreamObserver<si.um.feri.orders.grpc.service.OrderResponse> responseObserver) {
        try {
            UUID orderId = UUID.fromString(request.getOrderId());
            log.info("gRPC: CancelOrder requested for id {}", orderId);

            // Call application service
            var appResponse = applicationService
                .cancelOrder(orderId)
                .await()
                .indefinitely();

            // Map to gRPC response
            var grpcResponse = mapToGrpcResponse(appResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("gRPC: CancelOrder completed");

        } catch (OrderNotFoundException e) {
            log.warn("gRPC: CancelOrder not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asException());

        } catch (InvalidOrderStateTransitionException e) {
            log.warn("gRPC: CancelOrder invalid state: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asException());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC: CancelOrder invalid UUID: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid UUID format")
                .asException());

        } catch (Exception e) {
            log.error("gRPC: CancelOrder failed with exception", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asException());
        }
    }

    /**
     * Fulfill an order (CONFIRMED → FULFILLED) (gRPC endpoint).
     * 
     * @param request gRPC request with order_id
     * @param responseObserver Callback to send response/error
     */
    @Override
    public void fulfillOrder(
            si.um.feri.orders.grpc.service.FulfillOrderRequest request,
            StreamObserver<si.um.feri.orders.grpc.service.OrderResponse> responseObserver) {
        try {
            UUID orderId = UUID.fromString(request.getOrderId());
            log.info("gRPC: FulfillOrder requested for id {}", orderId);

            // Call application service
            var appResponse = applicationService
                .fulfillOrder(orderId)
                .await()
                .indefinitely();

            // Map to gRPC response
            var grpcResponse = mapToGrpcResponse(appResponse);

            responseObserver.onNext(grpcResponse);
            responseObserver.onCompleted();

            log.info("gRPC: FulfillOrder completed");

        } catch (OrderNotFoundException e) {
            log.warn("gRPC: FulfillOrder not found: {}", e.getMessage());
            responseObserver.onError(Status.NOT_FOUND
                .withDescription(e.getMessage())
                .asException());

        } catch (InvalidOrderStateTransitionException e) {
            log.warn("gRPC: FulfillOrder invalid state: {}", e.getMessage());
            responseObserver.onError(Status.FAILED_PRECONDITION
                .withDescription(e.getMessage())
                .asException());

        } catch (IllegalArgumentException e) {
            log.warn("gRPC: FulfillOrder invalid UUID: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid UUID format")
                .asException());

        } catch (Exception e) {
            log.error("gRPC: FulfillOrder failed with exception", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asException());
        }
    }

    /**
     * List all orders for a user (gRPC endpoint).
     * 
     * @param request gRPC request with user_id
     * @param responseObserver Callback to send response/error
     */
    @Override
    public void listOrdersByUser(
            si.um.feri.orders.grpc.service.ListOrdersRequest request,
            StreamObserver<si.um.feri.orders.grpc.service.OrderList> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            log.info("gRPC: ListOrdersByUser requested for user {}", userId);

            // TODO: Implement query by user (future enhancement)
            // For now, return empty list
            var response = si.um.feri.orders.grpc.service.OrderList.newBuilder()
                .setTotalCount(0)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("gRPC: ListOrdersByUser completed");

        } catch (IllegalArgumentException e) {
            log.warn("gRPC: ListOrdersByUser invalid UUID: {}", e.getMessage());
            responseObserver.onError(Status.INVALID_ARGUMENT
                .withDescription("Invalid UUID format")
                .asException());

        } catch (Exception e) {
            log.error("gRPC: ListOrdersByUser failed with exception", e);
            responseObserver.onError(Status.INTERNAL
                .withDescription("Internal server error")
                .asException());
        }
    }

    /**
     * Map application OrderResponse DTO to gRPC OrderResponse message.
     * 
     * Converts all fields from application DTO format to gRPC Protocol Buffer format:
     * - UUIDs: UUID objects → strings
     * - Timestamps: OffsetDateTime → ISO-8601 strings
     * - Status: String → gRPC OrderStatus enum
     * - Price: BigDecimal → string
     * 
     * @param appResponse Application service response DTO
     * @return gRPC OrderResponse message
     */
    private si.um.feri.orders.grpc.service.OrderResponse mapToGrpcResponse(
            si.um.feri.orders.application.dto.OrderResponse appResponse) {
        return si.um.feri.orders.grpc.service.OrderResponse.newBuilder()
            .setId(appResponse.getId().toString())
            .setBookId(appResponse.getBookId().toString())
            .setUserId(appResponse.getUserId().toString())
            .setQuantity(appResponse.getQuantity())
            .setPrice(appResponse.getPriceSnapshot().toString())
            .setStatus(mapStatusStringToGrpc(appResponse.getStatus()))
            .setCreatedAt(appResponse.getCreatedAt().toString())
            .setUpdatedAt(appResponse.getUpdatedAt().toString())
            .build();
    }

    /**
     * Map application OrderStatus string to gRPC OrderStatus enum.
     * 
     * The application DTO stores status as a string, while gRPC uses an enum.
     * 
     * @param statusString Application status string (PENDING, CONFIRMED, CANCELLED, FULFILLED)
     * @return gRPC OrderStatus enum value
     */
    private si.um.feri.orders.grpc.service.OrderStatus mapStatusStringToGrpc(
            String statusString) {
        return switch (statusString) {
            case "PENDING" -> si.um.feri.orders.grpc.service.OrderStatus.PENDING;
            case "CONFIRMED" -> si.um.feri.orders.grpc.service.OrderStatus.CONFIRMED;
            case "CANCELLED" -> si.um.feri.orders.grpc.service.OrderStatus.CANCELLED;
            case "FULFILLED" -> si.um.feri.orders.grpc.service.OrderStatus.FULFILLED;
            default -> si.um.feri.orders.grpc.service.OrderStatus.PENDING;
        };
    }
}
