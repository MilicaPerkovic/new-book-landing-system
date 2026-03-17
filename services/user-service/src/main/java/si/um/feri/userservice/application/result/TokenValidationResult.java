package si.um.feri.userservice.application.result;

import si.um.feri.userservice.domain.model.UserRole;

import java.util.UUID;

public record TokenValidationResult(
        UUID userId,
        UserRole role
) {
}
