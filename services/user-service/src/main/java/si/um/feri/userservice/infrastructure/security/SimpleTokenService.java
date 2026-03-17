package si.um.feri.userservice.infrastructure.security;

import org.springframework.stereotype.Component;
import si.um.feri.userservice.application.service.TokenService;
import si.um.feri.userservice.domain.model.UserRole;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Component
public class SimpleTokenService implements TokenService {

    @Override
    public String generateAccessToken(UUID userId, UserRole role) {
        String payload = userId + ":" + role + ":" + System.currentTimeMillis();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
