package si.um.feri.orders.application.exception;

/**
 * Base exception for all order-related domain errors.
 * Subclasses represent specific business rule violations.
 */
public class OrderDomainException extends RuntimeException {

    private final String errorCode;

    public OrderDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public OrderDomainException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
