package si.um.feri.orders.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a new preorder.
 * Validated by application service before persistence.
 */
public class CreateOrderRequest {

    private UUID bookId;
    private UUID userId;
    private int quantity;
    private BigDecimal price;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(UUID bookId, UUID userId, int quantity, BigDecimal price) {
        this.bookId = bookId;
        this.userId = userId;
        this.quantity = quantity;
        this.price = price;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
