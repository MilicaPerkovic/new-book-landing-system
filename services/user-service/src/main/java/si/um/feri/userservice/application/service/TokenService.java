package si.um.feri.userservice.application.service;

import si.um.feri.userservice.domain.model.UserRole;

import java.util.UUID;

public interface TokenService {
    String generateAccessToken(UUID userId, UserRole role);
}
