package si.um.feri.orders.application.exception;

import java.util.UUID;

/**
 * Thrown when attempting to retrieve an order that does not exist.
 */
public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(UUID orderId) {
        super("ORDER_NOT_FOUND", String.format("Order with id %s not found", orderId));
    }
}
