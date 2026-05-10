package si.um.feri.orders.api;

import si.um.feri.orders.application.dto.CreateOrderRequest;
import si.um.feri.orders.application.dto.OrderResponse;
import si.um.feri.orders.application.exception.InvalidOrderStateTransitionException;
import si.um.feri.orders.application.exception.OrderNotFoundException;
import si.um.feri.orders.application.exception.OrderValidationException;
import si.um.feri.orders.application.service.OrderApplicationService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

/**
 * REST Resource exposing order service endpoints.
 * 
 * All endpoints are reactive (return Uni<Response>) and support:
 * - JSON request/response
 * - Exception mapping to HTTP status codes
 * - Request ID correlation for tracing
 * 
 * Endpoints:
 * - POST /api/orders - Create new order
 * - GET /api/orders/{id} - Retrieve order
 * - POST /api/orders/{id}/confirm - Confirm order
 * - POST /api/orders/{id}/cancel - Cancel order
 * - POST /api/orders/{id}/fulfill - Fulfill order
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class OrderResource {

    @Inject
    OrderApplicationService orderApplicationService;

    /**
     * Create a new order.
     * 
     * Request validation:
     * - bookId is not null
     * - userId is not null
     * - quantity is positive
     * - price is positive
     * 
     * @param request Order creation request
     * @param idempotencyKey Optional header Idempotency-Key
     * @return 202 Accepted with OrderResponse
     * @throws OrderValidationException (400) if input invalid
     */
    @POST
    public Uni<Response> createOrder(CreateOrderRequest request, @HeaderParam("Idempotency-Key") String idempotencyKey) {
        return orderApplicationService.createOrder(request, idempotencyKey)
            .map(dto -> Response.status(202).entity(dto).build());
    }

    /**
     * Retrieve an existing order by ID.
     * 
     * @param orderId Unique order identifier
     * @return 200 OK with OrderResponse
     * @throws OrderNotFoundException (404) if order doesn't exist
     */
    @GET
    @Path("{orderId}")
    public Uni<Response> getOrder(@PathParam("orderId") UUID orderId) {
        return orderApplicationService.getOrderById(orderId)
            .map(dto -> Response.ok(dto).build());
    }

    /**
     * Get all orders for a specific user.
     * 
     * @param userId User identifier
     * @return 200 OK with list of OrderResponse
     */
    @GET
    @Path("user/{userId}")
    public Uni<Response> getOrdersByUser(@PathParam("userId") UUID userId) {
        return orderApplicationService.getOrdersByUserId(userId)
            .map(orders -> Response.ok(orders).build());
    }

    /**
     * Get all orders in the system (admin endpoint).
     * 
     * @return 200 OK with list of all OrderResponse
     */
    @GET
    public Uni<Response> getAllOrders() {
        return orderApplicationService.getAllOrders()
            .map(orders -> Response.ok(orders).build());
    }

    /**
     * Confirm an order (PENDING → CONFIRMED).
     * 
     * Business Rule: Only PENDING orders can be confirmed.
     * 
     * @param orderId Order to confirm
     * @return 200 OK with updated OrderResponse
     * @throws OrderNotFoundException (404) if order doesn't exist
     * @throws InvalidOrderStateTransitionException (409) if not PENDING
     */
    @POST
    @Path("{orderId}/confirm")
    public Uni<Response> confirmOrder(@PathParam("orderId") UUID orderId) {
        return orderApplicationService.confirmOrder(orderId)
            .map(dto -> Response.ok(dto).build());
    }

    /**
     * Cancel an order (non-terminal → CANCELLED).
     * 
     * Business Rule: Terminal orders (CANCELLED, FULFILLED) cannot be cancelled.
     * 
     * @param orderId Order to cancel
     * @return 200 OK with updated OrderResponse
     * @throws OrderNotFoundException (404) if order doesn't exist
     * @throws InvalidOrderStateTransitionException (409) if already terminal
     */
    @POST
    @Path("{orderId}/cancel")
    public Uni<Response> cancelOrder(@PathParam("orderId") UUID orderId) {
        return orderApplicationService.cancelOrder(orderId)
            .map(dto -> Response.ok(dto).build());
    }

    /**
     * Fulfill an order (CONFIRMED → FULFILLED).
     * 
     * Business Rule: Only CONFIRMED orders can be fulfilled.
     * 
     * @param orderId Order to fulfill
     * @return 200 OK with updated OrderResponse
     * @throws OrderNotFoundException (404) if order doesn't exist
     * @throws InvalidOrderStateTransitionException (409) if not CONFIRMED
     */
    @POST
    @Path("{orderId}/fulfill")
    public Uni<Response> fulfillOrder(@PathParam("orderId") UUID orderId) {
        return orderApplicationService.fulfillOrder(orderId)
            .map(dto -> Response.ok(dto).build());
    }
}
