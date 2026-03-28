package si.um.feri.orders.infrastructure.event;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.MutinyEmitter;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import si.um.feri.orders.application.exception.EventPublicationException;
import si.um.feri.orders.domain.event.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderEventPublisher.
 * 
 * Verifies event emission to ActiveMQ message broker:
 * - Event payload structure
 * - Topic naming
 * - Error handling
 * - Non-blocking behavior
 */
@QuarkusTest
class OrderEventPublisherTest {

    private OrderEventPublisher publisher;

    @Mock
    private MutinyEmitter<OrderDomainEvent> eventEmitter;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        publisher = new OrderEventPublisher();
        publisher.eventEmitter = eventEmitter;
    }

    // ==================== ORDER CREATED EVENT TESTS ====================

    @Test
    void publishOrderCreated_withValidEvent_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        OrderCreated event = new OrderCreated(orderId, bookId, userId, 2, new BigDecimal("19.99"));

        // Act
        publisher.publish(event);

        // Assert
        verify(eventEmitter).send(event);
    }

    @Test
    void publishOrderCreated_eventHasCorrectTopicName() {
        // Arrange
        OrderCreated event = new OrderCreated(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            new BigDecimal("10.00")
        );

        // Act & Assert
        assertEquals("order.events", event.getTopicName());
    }

    @Test
    void publishOrderCreated_eventTypeCorrect() {
        // Arrange
        OrderCreated event = new OrderCreated(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            new BigDecimal("10.00")
        );

        // Act & Assert
        assertEquals("OrderCreated", event.getEventType());
    }

    // ==================== ORDER CONFIRMED EVENT TESTS ====================

    @Test
    void publishOrderConfirmed_withValidEvent_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();
        
        OrderConfirmed event = new OrderConfirmed(orderId, bookId, timestamp);

        // Act
        publisher.publish(event);

        // Assert
        verify(eventEmitter).send(any(OrderConfirmed.class));
    }

    @Test
    void publishOrderConfirmed_eventHasCorrectTopicName() {
        // Arrange
        OrderConfirmed event = new OrderConfirmed(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals("order.confirmed", event.getTopicName());
    }

    @Test
    void publishOrderConfirmed_eventTypeCorrect() {
        // Arrange
        OrderConfirmed event = new OrderConfirmed(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals("OrderConfirmed", event.getEventType());
    }

    @Test
    void publishOrderConfirmed_preservesAggregateId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        OrderConfirmed event = new OrderConfirmed(orderId, bookId, OffsetDateTime.now());

        // Act & Assert
        assertEquals(orderId, event.getAggregateId());
    }

    // ==================== ORDER CANCELLED EVENT TESTS ====================

    @Test
    void publishOrderCancelled_withValidEvent_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String reason = "Customer request";
        OffsetDateTime timestamp = OffsetDateTime.now();
        
        OrderCancelled event = new OrderCancelled(orderId, reason, timestamp);

        // Act
        publisher.publish(event);

        // Assert
        verify(eventEmitter).send(any(OrderCancelled.class));
    }

    @Test
    void publishOrderCancelled_eventHasCorrectTopicName() {
        // Arrange
        OrderCancelled event = new OrderCancelled(
            UUID.randomUUID(),
            "Cancelled by user",
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals("order.cancelled", event.getTopicName());
    }

    @Test
    void publishOrderCancelled_includesCancellationReason() {
        // Arrange
        String expectedReason = "Out of stock";
        OrderCancelled event = new OrderCancelled(
            UUID.randomUUID(),
            expectedReason,
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals(expectedReason, event.getReason());
    }

    @Test
    void publishOrderCancelled_eventTypeCorrect() {
        // Arrange
        OrderCancelled event = new OrderCancelled(
            UUID.randomUUID(),
            "Reason",
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals("OrderCancelled", event.getEventType());
    }

    // ==================== ORDER FULFILLED EVENT TESTS ====================

    @Test
    void publishOrderFulfilled_withValidEvent_succeeds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.now();
        
        OrderFulfilled event = new OrderFulfilled(orderId, bookId, timestamp);

        // Act
        publisher.publish(event);

        // Assert
        verify(eventEmitter).send(any(OrderFulfilled.class));
    }

    @Test
    void publishOrderFulfilled_eventHasCorrectTopicName() {
        // Arrange
        OrderFulfilled event = new OrderFulfilled(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals("order.fulfilled", event.getTopicName());
    }

    @Test
    void publishOrderFulfilled_eventTypeCorrect() {
        // Arrange
        OrderFulfilled event = new OrderFulfilled(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now()
        );

        // Act & Assert
        assertEquals("OrderFulfilled", event.getEventType());
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    void publish_whenEmitterThrows_wrapsInEventPublicationException() {
        // Arrange
        OrderCreated event = new OrderCreated(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            new BigDecimal("10.00")
        );
        
        doThrow(new RuntimeException("Connection failed"))
            .when(eventEmitter).send(any(OrderDomainEvent.class));

        // Act & Assert
        assertThrows(EventPublicationException.class, () -> publisher.publish(event));
    }

    @Test
    void publish_exceptionIncludesEventDetails() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        OrderCreated event = new OrderCreated(
            orderId,
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            new BigDecimal("10.00")
        );
        
        doThrow(new RuntimeException("Publishing failed"))
            .when(eventEmitter).send(any(OrderDomainEvent.class));

        // Act & Assert
        EventPublicationException exception = assertThrows(
            EventPublicationException.class,
            () -> publisher.publish(event)
        );
        
        assertTrue(exception.getMessage().contains("OrderCreated"));
    }

    @Test
    void publish_nonBlockingEmitter_succeeded() {
        // Arrange
        OrderConfirmed event = new OrderConfirmed(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now()
        );

        // Act
        publisher.publish(event);

        // Assert - Verify emitter.send was called (non-blocking)
        verify(eventEmitter).send(any(OrderConfirmed.class));
    }

    // ==================== EVENT IDENTITY TESTS ====================

    @Test
    void publishEvent_hasEventId() {
        // All domain events should have a unique event ID
        OrderCreated event1 = new OrderCreated(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            new BigDecimal("10.00")
        );
        
        OrderCreated event2 = new OrderCreated(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            1,
            new BigDecimal("10.00")
        );

        // Assert - Event IDs should be unique
        assertNotEquals(event1.getEventId(), event2.getEventId());
    }

    @Test
    void publishEvent_hasTimestamp() {
        // Arrange
        OrderFulfilled event = new OrderFulfilled(
            UUID.randomUUID(),
            UUID.randomUUID(),
            OffsetDateTime.now()
        );

        // Assert
        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
    }

    // ==================== EVENT PAYLOAD STRUCTURE TESTS ====================

    @Test
    void orderCreatedEvent_containsAllRequiredData() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        int quantity = 2;
        BigDecimal price = new BigDecimal("19.99");
        
        OrderCreated event = new OrderCreated(orderId, bookId, userId, quantity, price);

        // Assert
        assertEquals(orderId, event.getAggregateId());
        assertEquals(bookId, event.getBookId());
        assertEquals(userId, event.getUserId());
        assertEquals(quantity, event.getQuantity());
        assertEquals(price, event.getPrice());
    }

    @Test
    void orderConfirmedEvent_containsOrderAndBookIds() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        
        OrderConfirmed event = new OrderConfirmed(orderId, bookId, OffsetDateTime.now());

        // Assert
        assertEquals(orderId, event.getAggregateId());
        assertEquals(bookId, event.getBookId());
    }

    @Test
    void orderCancelledEvent_containsReason() {
        // Arrange
        String reason = "Customer requested cancellation";
        OrderCancelled event = new OrderCancelled(UUID.randomUUID(), reason, OffsetDateTime.now());

        // Assert
        assertEquals(reason, event.getReason());
    }
}
