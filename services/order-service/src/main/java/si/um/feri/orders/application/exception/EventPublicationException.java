package si.um.feri.orders.application.exception;

/**
 * Exception thrown when an event fails to publish to the message broker.
 *
 * This is a critical error as it indicates a failure in the asynchronous
 * event pipeline. Event publication failures should trigger alerting and
 * potentially retry mechanisms.
 */
public class EventPublicationException extends RuntimeException {

    public EventPublicationException(String message) {
        super(message);
    }

    public EventPublicationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventPublicationException(Throwable cause) {
        super(cause);
    }
}
