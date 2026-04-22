package si.um.feri.orders.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Event published when an order is cancelled.
 *
 * Cancellation can occur from any state (PENDING, CONFIRMED, etc).
 *
 * Subscribers interested in order cancellation:
 * - Notification Service: notify user of cancellation reason
 * - Refund Service: process refunds if payment was taken
 * - Inventory Service: restore book stock
 * - Analytics Service: track cancellation metrics
 * - Audit Service: log cancellation reason
 */
public class OrderCancelled extends OrderDomainEvent {

    private final String reason;
    private final OffsetDateTime cancelledAt;

    public OrderCancelled(UUID orderId, String reason, OffsetDateTime cancelledAt) {
        super(orderId, "OrderCancelled");
        this.reason = reason;
        this.cancelledAt = cancelledAt;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCancelledAt() {
        return cancelledAt;
    }

    @Override
    public String getTopicName() {
        return "order.cancelled";  // Specific topic for cancellations
    }
}
