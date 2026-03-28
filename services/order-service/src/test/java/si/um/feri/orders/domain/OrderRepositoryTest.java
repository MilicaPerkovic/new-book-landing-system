package si.um.feri.orders.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for order aggregate and factory methods.
 * Repository persistence tests are deferred to Part 3 (integration tests with live DB).
 */
class OrderRepositoryTest {

    @Test
    void createOrderWithValidData() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("19.99");

        OrderEntity order = OrderEntity.create(bookId, userId, 2, price);

        assertNotNull(order.getId());
        assertEquals(bookId, order.getBookId());
        assertEquals(userId, order.getUserId());
        assertEquals(2, order.getQuantity());
        assertEquals(price, order.getPriceSnapshot());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void rejectZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 0, BigDecimal.ONE)
        );
    }

    @Test
    void rejectNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
            OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), -5, BigDecimal.ONE)
        );
    }

    @Test
    void rejectNullBookId() {
        assertThrows(NullPointerException.class, () ->
            OrderEntity.create(null, UUID.randomUUID(), 1, BigDecimal.ONE)
        );
    }

    @Test
    void rejectNullUserId() {
        assertThrows(NullPointerException.class, () ->
            OrderEntity.create(UUID.randomUUID(), null, 1, BigDecimal.ONE)
        );
    }

    @Test
    void rejectNullPrice() {
        assertThrows(NullPointerException.class, () ->
            OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 1, null)
        );
    }

    @Test
    void canUpdateStatus() {
        OrderEntity order = OrderEntity.create(UUID.randomUUID(), UUID.randomUUID(), 1, BigDecimal.TEN);
        assertEquals(OrderStatus.PENDING, order.getStatus());

        order.updateStatus(OrderStatus.CONFIRMED);
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        order.updateStatus(OrderStatus.FULFILLED);
        assertEquals(OrderStatus.FULFILLED, order.getStatus());
    }

    @Test
    void terminalStatusCheck() {
        assertEquals(false, OrderStatus.PENDING.isTerminal());
        assertEquals(false, OrderStatus.CONFIRMED.isTerminal());
        assertEquals(true, OrderStatus.CANCELLED.isTerminal());
        assertEquals(true, OrderStatus.FULFILLED.isTerminal());
    }
}
