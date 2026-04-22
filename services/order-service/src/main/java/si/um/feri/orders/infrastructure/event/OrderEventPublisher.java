package si.um.feri.orders.infrastructure.event;

import io.smallrye.reactive.messaging.MutinyEmitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.um.feri.orders.application.exception.EventPublicationException;
import si.um.feri.orders.domain.event.OrderDomainEvent;

/**
 * Publishes order domain events to ActiveMQ message broker.
 *
 * Responsibilities:
 * - Accept domain events from OrderApplicationService
 * - Publish to appropriate topic based on event type
 * - Handle publication errors with logging and exceptions
 * - Enable asynchronous communication with event subscribers
 *
 * Integration Points:
 * - Injected into OrderApplicationService
 * - Publishes to ActiveMQ topics (order.events, order.confirmed, etc)
 * - Subscribes listen on these topics asynchronously
 *
 * Configuration:
 * - Channel name: "order-events" (see application.properties)
 * - Destination type: topic (JMS topic, not queue)
 * - Topic names provided by each event's getTopicName() method
 */
@ApplicationScoped
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    @Inject
    @Channel("order-events")
    MutinyEmitter<OrderDomainEvent> eventEmitter;

    /**
     * Publish a domain event to the message broker.
     *
     * The event is routed to its appropriate topic (order.events, order.confirmed, etc)
     * for asynchronous consumption by interested subscribers.
     *
     * @param event The domain event to publish
     * @throws EventPublicationException if publishing fails
     */
    public void publish(OrderDomainEvent event) {
        try {
            log.info("Publishing event: {} for order {} to topic: {}",
                    event.getEventType(),
                    event.getAggregateId(),
                    event.getTopicName());

            // Send event to the reactive messaging channel
            // SmallRye handles routing to the correct JMS topic
            eventEmitter.send(event);

            log.info("Event published successfully. EventId: {}, EventType: {}",
                    event.getEventId(),
                    event.getEventType());

        } catch (Exception e) {
            log.error("Failed to publish event: {} for order {}. EventId: {}",
                    event.getEventType(),
                    event.getAggregateId(),
                    event.getEventId(),
                    e);
            throw new EventPublicationException(
                    "Failed to publish " + event.getEventType() + " event for order " + event.getAggregateId(),
                    e);
        }
    }
}
