package si.um.feri.orders.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "idempotency_keys", schema = "orders")
public class IdempotencyKeyEntity extends PanacheEntityBase {

    @Id
    @Column(name = "key", nullable = false, updatable = false)
    public String key;

    @Column(name = "response_body")
    public String responseBody;

    @Column(name = "status_code")
    public Integer statusCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @PrePersist
    protected void onPrePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
    
    // Default constructor for JPA
    public IdempotencyKeyEntity() {}

    public IdempotencyKeyEntity(String key, String responseBody, Integer statusCode) {
        this.key = key;
        this.responseBody = responseBody;
        this.statusCode = statusCode;
    }
}
