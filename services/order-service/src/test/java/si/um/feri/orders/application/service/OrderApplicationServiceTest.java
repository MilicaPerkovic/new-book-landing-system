package si.um.feri.orders.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import si.um.feri.orders.domain.event.OrderCreated;
import si.um.feri.orders.domain.event.OrderConfirmed;
import si.um.feri.orders.domain.event.OrderCancelled;
import si.um.feri.orders.domain.event.OrderFulfilled;
import si.um.feri.orders.infrastructure.event.OrderEventPublisher;

/**
 * Unit tests for OrderApplicationService.
 * 
 * Tests the business logic layer:
 * - Input validation
 * - State machine transitions
 * - Error handling
 * - Use case orchestration
 */
class OrderApplicationServiceTest {

    private OrderApplicationService service;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderEventPublisher eventPublisher;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        service = new OrderApplicationService();
        service.orderRepository = orderRepository;
        service.orderMapper = orderMapper;
        service.eventPublisher = eventPublisher;
    }

    // ==================== CREATE ORDER TESTS ====================

    @Test
    void createOrderWithValidData_succeeds() {
        // Arrange
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(bookId, userId, 2, new BigDecimal("19.99"));

        OrderEntity createdEntity = OrderEntity.create(bookId, userId, 2, new BigDecimal("19.99"));
        OrderResponse expectedResponse = new OrderResponse(
            createdEntity.getId(), bookId, userId, 2, new BigDecimal("19.99"),
            "PENDING", createdEntity.getCreatedAt(), createdEntity.getUpdatedAt(), 0
        );

        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(createdEntity));
        when(orderMapper.toDtoResponse(createdEntity))
            .thenReturn(expectedResponse);

        // Act
        OrderResponse result = service.createOrder(request).await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(bookId, result.getBookId());
        assertEquals(userId, result.getUserId());
        assertEquals(2, result.getQuantity());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void createOrderWithNullRequest_throwsValidationException() {
        // Act & Assert
        assertThrows(OrderValidationException.class, () ->
            service.createOrder(null).await().indefinitely()
        );
    }

    @Test
    void createOrderWithNullBookId_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(null, UUID.randomUUID(), 1, BigDecimal.TEN);

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    @Test
    void createOrderWithNullUserId_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), null, 1, BigDecimal.TEN);

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    @Test
    void createOrderWithZeroQuantity_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), UUID.randomUUID(), 0, BigDecimal.TEN);

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    @Test
    void createOrderWithNegativeQuantity_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), UUID.randomUUID(), -5, BigDecimal.TEN);

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    @Test
    void createOrderWithNullPrice_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(UUID.randomUUID(), UUID.randomUUID(), 1, null);

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    @Test
    void createOrderWithZeroPrice_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(), UUID.randomUUID(), 1, BigDecimal.ZERO
        );

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    @Test
    void createOrderWithNegativePrice_throwsValidationException() {
        CreateOrderRequest request = new CreateOrderRequest(
            UUID.randomUUID(), UUID.randomUUID(), 1, new BigDecimal("-10.00")
        );

        assertThrows(OrderValidationException.class, () ->
            service.createOrder(request).await().indefinitely()
        );
    }

    // ==================== GET ORDER TESTS ====================

    @Test
    void getOrderByIdWhenExists_returnsOrderResponse() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 1, BigDecimal.TEN);
        order.overrideId(orderId);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, order.getBookId(), order.getUserId(), 1, BigDecimal.TEN,
            "PENDING", order.getCreatedAt(), order.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderMapper.toDtoResponse(order))
            .thenReturn(expectedResponse);

        // Act
        OrderResponse result = service.getOrderById(orderId).await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getOrderByIdWhenNotExists_throwsNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().nullItem());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () ->
            service.getOrderById(orderId).await().indefinitely()
        );
    }

    @Test
    void getOrderWithNullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
            service.getOrderById(null).await().indefinitely()
        );
    }

    // ==================== CONFIRM ORDER TESTS ====================

    @Test
    void confirmPendingOrder_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Create fresh entity for repository mock
        OrderEntity order = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        order.overrideId(orderId);
        
        // Create separate entity for the confirmed state (simulating DB persistence)
        OrderEntity confirmedOrder = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        confirmedOrder.overrideId(orderId);
        confirmedOrder.updateStatus(OrderStatus.CONFIRMED);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, bookId, userId, 1, BigDecimal.TEN,
            "CONFIRMED", confirmedOrder.getCreatedAt(), confirmedOrder.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(confirmedOrder));
        when(orderMapper.toDtoResponse(confirmedOrder))
            .thenReturn(expectedResponse);

        // Act
        OrderResponse result = service.confirmOrder(orderId).await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void confirmNonPendingOrder_throwsInvalidStateTransitionException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 1, BigDecimal.TEN);
        order.overrideId(orderId);
        order.updateStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));

        // Act & Assert
        assertThrows(InvalidOrderStateTransitionException.class, () ->
            service.confirmOrder(orderId).await().indefinitely()
        );
    }

    // ==================== CANCEL ORDER TESTS ====================

    @Test
    void cancelPendingOrder_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Create fresh entity for repository mock
        OrderEntity order = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        order.overrideId(orderId);
        
        // Create separate entity for the cancelled state (simulating DB persistence)
        OrderEntity cancelledOrder = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        cancelledOrder.overrideId(orderId);
        cancelledOrder.updateStatus(OrderStatus.CANCELLED);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, bookId, userId, 1, BigDecimal.TEN,
            "CANCELLED", cancelledOrder.getCreatedAt(), cancelledOrder.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(cancelledOrder));
        when(orderMapper.toDtoResponse(cancelledOrder))
            .thenReturn(expectedResponse);

        // Act
        OrderResponse result = service.cancelOrder(orderId).await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    void cancelTerminalOrder_throwsInvalidStateTransitionException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 1, BigDecimal.TEN);
        order.overrideId(orderId);
        order.updateStatus(OrderStatus.FULFILLED);  // Terminal state

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));

        // Act & Assert
        assertThrows(InvalidOrderStateTransitionException.class, () ->
            service.cancelOrder(orderId).await().indefinitely()
        );
    }

    // ==================== FULFILL ORDER TESTS ====================

    @Test
    void fulfillConfirmedOrder_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        // Create entity in CONFIRMED state
        OrderEntity order = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        order.overrideId(orderId);
        order.updateStatus(OrderStatus.CONFIRMED);
        
        // Create separate entity for the fulfilled state (simulating DB persistence)
        OrderEntity fulfilledOrder = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        fulfilledOrder.overrideId(orderId);
        fulfilledOrder.updateStatus(OrderStatus.CONFIRMED);
        fulfilledOrder.updateStatus(OrderStatus.FULFILLED);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, bookId, userId, 1, BigDecimal.TEN,
            "FULFILLED", fulfilledOrder.getCreatedAt(), fulfilledOrder.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(fulfilledOrder));
        when(orderMapper.toDtoResponse(fulfilledOrder))
            .thenReturn(expectedResponse);

        // Act
        OrderResponse result = service.fulfillOrder(orderId).await().indefinitely();

        // Assert
        assertNotNull(result);
        assertEquals("FULFILLED", result.getStatus());
    }

    @Test
    void fulfillNonConfirmedOrder_throwsInvalidStateTransitionException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderEntity order = OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 1, BigDecimal.TEN);
        order.overrideId(orderId);
        // Order is still PENDING, not CONFIRMED

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));

        // Act & Assert
        assertThrows(InvalidOrderStateTransitionException.class, () ->
            service.fulfillOrder(orderId).await().indefinitely()
        );
    }

    // ==================== EVENT PUBLISHING TESTS ====================

    @Test
    void createOrder_publishesOrderCreatedEvent() {
        // Arrange
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CreateOrderRequest request = new CreateOrderRequest(bookId, userId, 2, new BigDecimal("19.99"));

        OrderEntity createdEntity = OrderEntity.create(bookId, userId, 2, new BigDecimal("19.99"));
        OrderResponse expectedResponse = new OrderResponse(
            createdEntity.getId(), bookId, userId, 2, new BigDecimal("19.99"),
            "PENDING", createdEntity.getCreatedAt(), createdEntity.getUpdatedAt(), 0
        );

        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(createdEntity));
        when(orderMapper.toDtoResponse(createdEntity))
            .thenReturn(expectedResponse);

        // Act
        service.createOrder(request).await().indefinitely();

        // Assert
        verify(eventPublisher).publish(any(OrderCreated.class));
    }

    @Test
    void confirmOrder_publishesOrderConfirmedEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        OrderEntity order = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        order.overrideId(orderId);
        
        OrderEntity confirmedOrder = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        confirmedOrder.overrideId(orderId);
        confirmedOrder.updateStatus(OrderStatus.CONFIRMED);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, bookId, userId, 1, BigDecimal.TEN,
            "CONFIRMED", confirmedOrder.getCreatedAt(), confirmedOrder.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(confirmedOrder));
        when(orderMapper.toDtoResponse(confirmedOrder))
            .thenReturn(expectedResponse);

        // Act
        service.confirmOrder(orderId).await().indefinitely();

        // Assert
        verify(eventPublisher).publish(any(OrderConfirmed.class));
    }

    @Test
    void cancelOrder_publishesOrderCancelledEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        OrderEntity order = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        order.overrideId(orderId);
        
        OrderEntity cancelledOrder = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        cancelledOrder.overrideId(orderId);
        cancelledOrder.updateStatus(OrderStatus.CANCELLED);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, bookId, userId, 1, BigDecimal.TEN,
            "CANCELLED", cancelledOrder.getCreatedAt(), cancelledOrder.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(cancelledOrder));
        when(orderMapper.toDtoResponse(cancelledOrder))
            .thenReturn(expectedResponse);

        // Act
        service.cancelOrder(orderId).await().indefinitely();

        // Assert
        verify(eventPublisher).publish(any(OrderCancelled.class));
    }

    @Test
    void fulfillOrder_publishesOrderFulfilledEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        OrderEntity order = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        order.overrideId(orderId);
        order.updateStatus(OrderStatus.CONFIRMED);
        
        OrderEntity fulfilledOrder = OrderEntity.create(bookId, userId, 1, BigDecimal.TEN);
        fulfilledOrder.overrideId(orderId);
        fulfilledOrder.updateStatus(OrderStatus.CONFIRMED);
        fulfilledOrder.updateStatus(OrderStatus.FULFILLED);

        OrderResponse expectedResponse = new OrderResponse(
            orderId, bookId, userId, 1, BigDecimal.TEN,
            "FULFILLED", fulfilledOrder.getCreatedAt(), fulfilledOrder.getUpdatedAt(), 0
        );

        when(orderRepository.findById(orderId))
            .thenReturn(Uni.createFrom().item(order));
        when(orderRepository.persistAndFlush(any(OrderEntity.class)))
            .thenReturn(Uni.createFrom().item(fulfilledOrder));
        when(orderMapper.toDtoResponse(fulfilledOrder))
            .thenReturn(expectedResponse);

        // Act
        service.fulfillOrder(orderId).await().indefinitely();

        // Assert
        verify(eventPublisher).publish(any(OrderFulfilled.class));
    }
}
