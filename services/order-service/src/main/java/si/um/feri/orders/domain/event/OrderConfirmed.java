package si.um.feri.orders.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Event published when an order transitions from PENDING to CONFIRMED state.
 *
 * Subscribers interested in order confirmation:
 * - Notification Service: send order confirmed notification
 * - Payment Service: process payment for confirmed order
 * - Fulfillment Service: start preparing shipment
 * - Analytics Service: track confirmation metrics
 */
public class OrderConfirmed extends OrderDomainEvent {

    private final UUID bookId;
    private final OffsetDateTime confirmedAt;

    public OrderConfirmed(UUID orderId, UUID bookId, OffsetDateTime confirmedAt) {
        super(orderId, "OrderConfirmed");
        this.bookId = bookId;
        this.confirmedAt = confirmedAt;
    }

    public UUID getBookId() {
        return bookId;
    }

    public OffsetDateTime getConfirmedAt() {
        return confirmedAt;
    }

    @Override
    public String getTopicName() {
        return "order.confirmed";  // Specific topic for confirmations
    }
}
