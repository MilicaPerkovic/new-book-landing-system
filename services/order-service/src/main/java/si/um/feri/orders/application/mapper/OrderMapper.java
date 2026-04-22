package si.um.feri.orders.application.mapper;

import si.um.feri.orders.application.dto.OrderResponse;
import si.um.feri.orders.domain.OrderEntity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Maps between OrderEntity (domain) and OrderResponse (DTO).
 * Handles conversion logic centrally.
 */
@ApplicationScoped
public class OrderMapper {

    /**
     * Convert domain entity to response DTO.
     */
    public OrderResponse toDtoResponse(OrderEntity entity) {
        if (entity == null) {
            return null;
        }

        return new OrderResponse(
            entity.getId(),
            entity.getBookId(),
            entity.getUserId(),
            entity.getQuantity(),
            entity.getPriceSnapshot(),
            entity.getStatus().name(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }
}
