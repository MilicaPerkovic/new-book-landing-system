package si.um.feri.userservice.application.service;

import si.um.feri.userservice.application.result.TokenValidationResult;
import si.um.feri.userservice.domain.model.UserRole;

import java.util.Optional;
import java.util.UUID;

public interface TokenService {
    String generateAccessToken(UUID userId, UserRole role);

    Optional<TokenValidationResult> validateAccessToken(String token);
}
