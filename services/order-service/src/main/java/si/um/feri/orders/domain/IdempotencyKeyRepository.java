package si.um.feri.orders.domain;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdempotencyKeyRepository implements PanacheRepositoryBase<IdempotencyKeyEntity, String> {
}
