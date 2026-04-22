package si.um.feri.orders.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

/**
 * Reactive repository facade encapsulating persistence logic for orders.
 */
@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<OrderEntity, UUID> {

    /**
     * Persist an order entity and flush within a transaction boundary.
     */
    @WithTransaction
    public Uni<OrderEntity> persistAndFlush(OrderEntity entity) {
        return this.persist(entity).replaceWith(entity);
    }
}
