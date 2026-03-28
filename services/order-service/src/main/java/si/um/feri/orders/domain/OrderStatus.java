package si.um.feri.orders.domain;

/**
 * Enumerates the supported lifecycle states of an order aggregate.
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    FULFILLED;

    public boolean isTerminal() {
        return this == CANCELLED || this == FULFILLED;
    }
}
