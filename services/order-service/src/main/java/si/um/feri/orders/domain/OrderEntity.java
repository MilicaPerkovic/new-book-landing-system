package si.um.feri.orders.domain;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

/**
 * Reactive JPA aggregate that captures the lifecycle of a preorder.
 */
@Entity
@Table(name = "orders", schema = "orders")
public class OrderEntity extends PanacheEntityBase {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "book_id", nullable = false, updatable = false)
    private UUID bookId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private int version;

    public static OrderEntity create(UUID bookId, UUID userId, int quantity, BigDecimal priceSnapshot) {
        Objects.requireNonNull(bookId, "bookId must be provided");
        Objects.requireNonNull(userId, "userId must be provided");
        Objects.requireNonNull(priceSnapshot, "priceSnapshot must be provided");

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }

        OrderEntity entity = new OrderEntity();
        entity.id = UUID.randomUUID();
        entity.bookId = bookId;
        entity.userId = userId;
        entity.quantity = quantity;
        entity.priceSnapshot = priceSnapshot.setScale(2, RoundingMode.HALF_UP);
        entity.status = OrderStatus.PENDING;
        // Initialize timestamps eagerly for both persisted and in-memory scenarios
        OffsetDateTime now = OffsetDateTime.now(UTC);
        entity.createdAt = now;
        entity.updatedAt = now;
        return entity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getVersion() {
        return version;
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = Objects.requireNonNull(newStatus, "status must be provided");
    }

    public void overrideId(UUID id) {
        this.id = Objects.requireNonNull(id, "id must be provided");
    }

    @PrePersist
    void onPersist() {
        OffsetDateTime now = OffsetDateTime.now(UTC);
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(UTC);
    }
}
