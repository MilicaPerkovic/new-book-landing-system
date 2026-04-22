package si.um.feri.orders.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event published when a new order is created.
 *
 * Subscribers interested in order creation:
 * - Notification Service: send order confirmation email
 * - Analytics Service: track order inflow metrics
 * - Audit Service: log order creation
 * - Inventory Service: reduce book stock
 */
public class OrderCreated extends OrderDomainEvent {

    private final UUID bookId;
    private final UUID userId;
    private final int quantity;
    private final BigDecimal price;

    public OrderCreated(UUID orderId, UUID bookId, UUID userId, int quantity, BigDecimal price) {
        super(orderId, "OrderCreated");
        this.bookId = bookId;
        this.userId = userId;
        this.quantity = quantity;
        this.price = price;
    }

    public UUID getBookId() {
        return bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String getTopicName() {
        return "order.events";  // Publish to general events topic
    }
}
