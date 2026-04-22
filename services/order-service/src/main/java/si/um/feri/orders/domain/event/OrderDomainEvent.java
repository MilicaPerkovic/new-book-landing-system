package si.um.feri.orders.domain.event;

import java.util.UUID;

/**
 * Base class for all order domain events.
 *
 * Domain events capture important facts that occur within the order aggregate.
 * Each event represents a state change that other services might be interested in.
 *
 * Events are published to a message broker for asynchronous consumption.
 * This enables loose coupling between order-service and its subscribers
 * (notifications, analytics, audit, inventory management, etc).
 *
 * Properties:
 * - aggregateId: The Order ID (root aggregate)
 * - eventType: Human-readable event name (OrderCreated, OrderConfirmed, etc)
 * - eventId: Unique identifier for this specific event
 * - timestamp: When the event occurred (milliseconds since epoch)
 * - topicName: ActiveMQ topic to publish to (can vary by event type)
 */
public abstract class OrderDomainEvent {

    private final UUID aggregateId;
    private final String eventType;
    private final String eventId;
    private final long timestamp;

    protected OrderDomainEvent(UUID aggregateId, String eventType) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Get the order ID (aggregate root identifier).
     */
    public UUID getAggregateId() {
        return aggregateId;
    }

    /**
     * Get the event type name.
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * Get the unique event identifier.
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Get when this event occurred.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Determine which topic this event should be published to.
     * Subclasses override to specify topic names.
     *
     * @return ActiveMQ topic name (e.g., "order.events", "order.confirmed")
     */
    public abstract String getTopicName();

    @Override
    public String toString() {
        return String.format("%s{eventId=%s, aggregateId=%s, timestamp=%d}",
                eventType, eventId, aggregateId, timestamp);
    }
}
