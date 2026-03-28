package si.um.feri.userservice.application.result;

import si.um.feri.userservice.domain.model.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResult(
        UUID id,
        String email,
        String fullName,
        UserRole role,
        Instant createdAt,
        Instant updatedAt
) {
}
