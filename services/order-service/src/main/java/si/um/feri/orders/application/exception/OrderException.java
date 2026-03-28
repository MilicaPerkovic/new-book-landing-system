package si.um.feri.orders.application.exception;

/**
 * Base exception for order-service domain errors.
 * Used to signal business rule violations and invalid state transitions.
 */
public abstract class OrderException extends RuntimeException {

    public OrderException(String message) {
        super(message);
    }

    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
