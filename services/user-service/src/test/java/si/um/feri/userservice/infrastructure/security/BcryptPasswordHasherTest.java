package si.um.feri.userservice.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordHasherTest {

    private final BcryptPasswordHasher hasher = new BcryptPasswordHasher();

    @Test
    void shouldHashAndMatchPassword() {
        String rawPassword = "StrongPassword123";

        String hash = hasher.hash(rawPassword);

        assertThat(hash).isNotBlank();
        assertThat(hash).isNotEqualTo(rawPassword);
        assertThat(hasher.matches(rawPassword, hash)).isTrue();
    }
}
