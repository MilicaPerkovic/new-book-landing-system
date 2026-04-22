package si.um.feri.orders.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO representing a complete order state.
 * Returned by all order service operations.
 */
public class OrderResponse {

    private UUID id;
    private UUID bookId;
    private UUID userId;
    private int quantity;
    private BigDecimal priceSnapshot;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int version;

    public OrderResponse() {
    }

    public OrderResponse(UUID id, UUID bookId, UUID userId, int quantity,
                         BigDecimal priceSnapshot, String status, OffsetDateTime createdAt,
                         OffsetDateTime updatedAt, int version) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.quantity = quantity;
        this.priceSnapshot = priceSnapshot;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBookId() {
        return bookId;
    }

    public void setBookId(UUID bookId) {
        this.bookId = bookId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceSnapshot() {
        return priceSnapshot;
    }

    public void setPriceSnapshot(BigDecimal priceSnapshot) {
        this.priceSnapshot = priceSnapshot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
