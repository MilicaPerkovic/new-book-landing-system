package si.um.feri.orders.application.service;

import si.um.feri.orders.application.dto.CreateOrderRequest;
import si.um.feri.orders.application.dto.OrderResponse;
import si.um.feri.orders.application.exception.InvalidOrderStateTransitionException;
import si.um.feri.orders.application.exception.OrderNotFoundException;
import si.um.feri.orders.application.exception.OrderValidationException;
import si.um.feri.orders.application.mapper.OrderMapper;
import si.um.feri.orders.domain.OrderEntity;
import si.um.feri.orders.domain.OrderRepository;
import si.um.feri.orders.domain.OrderStatus;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service orchestrating order use cases.
 * 
 * Responsibilities:
 * - Validate user input
 * - Enforce business rules
 * - Coordinate domain operations
 * - Translate between DTOs and domain entities
 * 
 * All methods are transaction-safe and support reactive execution.
 */
@ApplicationScoped
public class OrderApplicationService {

    @Inject
    OrderRepository orderRepository;

    @Inject
    OrderMapper orderMapper;

    /**
     * Create a new preorder for a book.
     * 
     * Business Rules:
     * - All fields are required
     * - Quantity must be positive
     * - Price must be positive
     * 
     * @param request Contains bookId, userId, quantity, price
     * @return Created order response
     * @throws OrderValidationException if input fails validation
     */
    public Uni<OrderResponse> createOrder(CreateOrderRequest request) {
        // Validate input
        validateCreateOrderRequest(request);

        // Create domain entity (factory enforces invariants)
        OrderEntity order = OrderEntity.create(
            request.getBookId(),
            request.getUserId(),
            request.getQuantity(),
            request.getPrice()
        );

        // Persist and return as DTO
        return orderRepository.persistAndFlush(order)
            .map(orderMapper::toDtoResponse);
    }

    /**
     * Retrieve an order by its ID.
     * 
     * @param orderId Unique order identifier
     * @return Order response if found
     * @throws OrderNotFoundException if order doesn't exist
     */
    public Uni<OrderResponse> getOrderById(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must be provided");

        return orderRepository.findById(orderId)
            .onItem().ifNull().failWith(() -> new OrderNotFoundException(orderId))
            .map(orderMapper::toDtoResponse);
    }

    /**
     * Transition order from PENDING to CONFIRMED.
     * 
     * Business Rule: Only PENDING orders can be confirmed.
     * 
     * @param orderId Order to confirm
     * @return Updated order response with status=CONFIRMED
     * @throws OrderNotFoundException if order doesn't exist
     * @throws InvalidOrderStateTransitionException if order is not PENDING
     */
    public Uni<OrderResponse> confirmOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must be provided");

        return orderRepository.findById(orderId)
            .onItem().ifNull().failWith(() -> new OrderNotFoundException(orderId))
            .map(order -> {
                // Check current state before transition
                if (order.getStatus() != OrderStatus.PENDING) {
                    throw new InvalidOrderStateTransitionException(
                        orderId, order.getStatus(), OrderStatus.CONFIRMED
                    );
                }
                // Perform transition
                order.updateStatus(OrderStatus.CONFIRMED);
                return order;
            })
            .chain(order -> orderRepository.persistAndFlush(order))
            .map(orderMapper::toDtoResponse);
    }

    /**
     * Transition order to CANCELLED.
     * 
     * Business Rule: Only non-terminal orders can be cancelled.
     * Terminal orders (CANCELLED, FULFILLED) cannot be cancelled.
     * 
     * @param orderId Order to cancel
     * @return Updated order response with status=CANCELLED
     * @throws OrderNotFoundException if order doesn't exist
     * @throws InvalidOrderStateTransitionException if order is already terminal
     */
    public Uni<OrderResponse> cancelOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must be provided");

        return orderRepository.findById(orderId)
            .onItem().ifNull().failWith(() -> new OrderNotFoundException(orderId))
            .map(order -> {
                // Check if order is already terminal
                if (order.getStatus().isTerminal()) {
                    throw new InvalidOrderStateTransitionException(
                        orderId, order.getStatus(), OrderStatus.CANCELLED
                    );
                }
                // Perform transition
                order.updateStatus(OrderStatus.CANCELLED);
                return order;
            })
            .chain(order -> orderRepository.persistAndFlush(order))
            .map(orderMapper::toDtoResponse);
    }

    /**
     * Transition order to FULFILLED.
     * 
     * Business Rule: Only CONFIRMED orders can be fulfilled.
     * 
     * @param orderId Order to fulfill
     * @return Updated order response with status=FULFILLED
     * @throws OrderNotFoundException if order doesn't exist
     * @throws InvalidOrderStateTransitionException if order is not CONFIRMED
     */
    public Uni<OrderResponse> fulfillOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId must be provided");

        return orderRepository.findById(orderId)
            .onItem().ifNull().failWith(() -> new OrderNotFoundException(orderId))
            .map(order -> {
                // Check current state before transition
                if (order.getStatus() != OrderStatus.CONFIRMED) {
                    throw new InvalidOrderStateTransitionException(
                        orderId, order.getStatus(), OrderStatus.FULFILLED
                    );
                }
                // Perform transition
                order.updateStatus(OrderStatus.FULFILLED);
                return order;
            })
            .chain(order -> orderRepository.persistAndFlush(order))
            .map(orderMapper::toDtoResponse);
    }

    /**
     * Private validation method for create order request.
     * 
     * Validates:
     * - bookId is not null
     * - userId is not null
     * - quantity is positive
     * - price is positive
     * 
     * @param request Request to validate
     * @throws OrderValidationException if any validation fails
     */
    private void validateCreateOrderRequest(CreateOrderRequest request) {
        if (request == null) {
            throw new OrderValidationException("Create order request cannot be null");
        }

        if (request.getBookId() == null) {
            throw new OrderValidationException("bookId is required");
        }

        if (request.getUserId() == null) {
            throw new OrderValidationException("userId is required");
        }

        if (request.getQuantity() <= 0) {
            throw new OrderValidationException(
                String.format("quantity must be positive, got %d", request.getQuantity())
            );
        }

        if (request.getPrice() == null) {
            throw new OrderValidationException("price is required");
        }

        if (request.getPrice().signum() <= 0) {
            throw new OrderValidationException(
                String.format("price must be positive, got %s", request.getPrice())
            );
        }
    }
}
