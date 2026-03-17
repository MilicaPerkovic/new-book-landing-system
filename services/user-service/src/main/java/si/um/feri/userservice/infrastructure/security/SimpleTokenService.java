package si.um.feri.userservice.infrastructure.security;

import org.springframework.stereotype.Component;
import si.um.feri.userservice.application.result.TokenValidationResult;
import si.um.feri.userservice.application.service.TokenService;
import si.um.feri.userservice.domain.model.UserRole;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleTokenService implements TokenService {

    @Override
    public String generateAccessToken(UUID userId, UserRole role) {
        String payload = userId + ":" + role + ":" + System.currentTimeMillis();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<TokenValidationResult> validateAccessToken(String token) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = payload.split(":");
            if (parts.length < 2) {
                return Optional.empty();
            }

            UUID userId = UUID.fromString(parts[0]);
            UserRole role = UserRole.valueOf(parts[1]);
            return Optional.of(new TokenValidationResult(userId, role));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
