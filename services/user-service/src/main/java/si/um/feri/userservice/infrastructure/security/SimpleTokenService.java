package si.um.feri.userservice.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import si.um.feri.userservice.application.result.TokenValidationResult;
import si.um.feri.userservice.application.service.TokenService;
import si.um.feri.userservice.domain.model.UserRole;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class SimpleTokenService implements TokenService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public SimpleTokenService(@Value("${security.jwt.secret}") String secret,
                              @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(normalizeSecret(secret).getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateAccessToken(UUID userId, UserRole role) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public Optional<TokenValidationResult> validateAccessToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            String roleValue = claims.get("role", String.class);
            if (subject == null || roleValue == null) {
                return Optional.empty();
            }

            UUID userId = UUID.fromString(subject);
            UserRole role = UserRole.valueOf(roleValue);
            return Optional.of(new TokenValidationResult(userId, role));
        } catch (JwtException | IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    private static String normalizeSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be empty");
        }

        String normalized = secret.trim();
        if (normalized.length() < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 characters long");
        }

        return normalized;
    }
}
