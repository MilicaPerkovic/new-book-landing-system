package si.um.feri.orders.application.exception;

import si.um.feri.orders.domain.OrderStatus;
import java.util.UUID;

/**
 * Thrown when attempting to transition an order to an invalid state.
 * Example: trying to confirm an order that is already cancelled.
 */
public class InvalidOrderStateTransitionException extends OrderDomainException {

    public InvalidOrderStateTransitionException(UUID orderId, OrderStatus currentStatus, OrderStatus requestedStatus) {
        super(
            "INVALID_STATE_TRANSITION",
            String.format(
                "Cannot transition order %s from %s to %s",
                orderId, currentStatus, requestedStatus
            )
        );
    }
}
