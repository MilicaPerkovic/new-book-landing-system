package si.um.feri.orders.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Event published when an order is fulfilled (shipped/delivered).
 *
 * Fulfillment occurs when confirmed orders transition to FULFILLED state.
 *
 * Subscribers interested in order fulfillment:
 * - Notification Service: send shipment tracking information
 * - Analytics Service: track fulfillment time metrics
 * - Accounting Service: mark order as revenue-recognized
 * - Loyalty Service: award loyalty points
 * - Audit Service: log fulfillment timestamp
 */
public class OrderFulfilled extends OrderDomainEvent {

    private final UUID bookId;
    private final OffsetDateTime fulfilledAt;

    public OrderFulfilled(UUID orderId, UUID bookId, OffsetDateTime fulfilledAt) {
        super(orderId, "OrderFulfilled");
        this.bookId = bookId;
        this.fulfilledAt = fulfilledAt;
    }

    public UUID getBookId() {
        return bookId;
    }

    public OffsetDateTime getFulfilledAt() {
        return fulfilledAt;
    }

    @Override
    public String getTopicName() {
        return "order.fulfilled";  // Specific topic for fulfillments
    }
}
