package si.um.feri.userservice.infrastructure.security;

import org.junit.jupiter.api.Test;
import si.um.feri.userservice.application.result.TokenValidationResult;
import si.um.feri.userservice.domain.model.UserRole;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleTokenServiceTest {

    private static final String SIGNING_KEY_TEXT = "12345678901234567890123456789012";

    @Test
    void shouldGenerateAndValidateJwtToken() {
        SimpleTokenService tokenService = new SimpleTokenService(SIGNING_KEY_TEXT, 3600);
        UUID userId = UUID.randomUUID();

        String token = tokenService.generateAccessToken(userId, UserRole.ADMIN);
        Optional<TokenValidationResult> result = tokenService.validateAccessToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(userId);
        assertThat(result.get().role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void shouldRejectTamperedToken() {
        SimpleTokenService tokenService = new SimpleTokenService(SIGNING_KEY_TEXT, 3600);
        UUID userId = UUID.randomUUID();

        String token = tokenService.generateAccessToken(userId, UserRole.AUTHOR);
        String tamperedToken = token + "x";

        Optional<TokenValidationResult> result = tokenService.validateAccessToken(tamperedToken);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldRejectExpiredToken() {
        SimpleTokenService tokenService = new SimpleTokenService(SIGNING_KEY_TEXT, -1);
        UUID userId = UUID.randomUUID();

        String token = tokenService.generateAccessToken(userId, UserRole.READER);
        Optional<TokenValidationResult> result = tokenService.validateAccessToken(token);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFailOnShortSecret() {
        assertThatThrownBy(() -> new SimpleTokenService("short-secret", 3600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 32 characters");
    }
}
