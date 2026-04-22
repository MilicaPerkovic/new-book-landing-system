package si.um.feri.orders.application.exception;

/**
 * Thrown when user input fails validation checks.
 * Example: negative quantity, missing required fields.
 */
public class OrderValidationException extends OrderDomainException {

    public OrderValidationException(String message) {
        super("ORDER_VALIDATION_ERROR", message);
    }

    public OrderValidationException(String message, Throwable cause) {
        super("ORDER_VALIDATION_ERROR", message, cause);
    }
}
