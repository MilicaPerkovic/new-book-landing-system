package si.um.feri.userservice.application.result;

import si.um.feri.userservice.domain.model.UserRole;

import java.util.UUID;

public record AuthResult(
        String accessToken,
        UUID userId,
        UserRole role
) {
}
