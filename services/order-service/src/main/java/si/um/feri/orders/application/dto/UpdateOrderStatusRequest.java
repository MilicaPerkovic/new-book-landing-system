package si.um.feri.orders.application.dto;

/**
 * Request DTO for updating order status.
 * Used for confirm, cancel, fulfill operations.
 */
public class UpdateOrderStatusRequest {

    private String targetStatus;

    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(String targetStatus) {
        this.targetStatus = targetStatus;
    }

    public String getTargetStatus() {
        return targetStatus;
    }

    public void setTargetStatus(String targetStatus) {
        this.targetStatus = targetStatus;
    }
}
