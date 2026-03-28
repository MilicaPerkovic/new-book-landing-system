package si.um.feri.userservice.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.um.feri.userservice.application.command.AuthenticateUserCommand;
import si.um.feri.userservice.application.result.AuthResult;
import si.um.feri.userservice.domain.exception.InvalidCredentialsException;
import si.um.feri.userservice.domain.model.User;
import si.um.feri.userservice.domain.model.UserRole;
import si.um.feri.userservice.domain.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    private static final String EMAIL = "author@example.com";
    private static final String HASHED_SECRET = "hashed";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void shouldAuthenticateUser() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setEmail(EMAIL);
        user.setPasswordHash(HASHED_SECRET);
        user.setRole(UserRole.AUTHOR);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("StrongPassword123", HASHED_SECRET)).thenReturn(true);
        when(tokenService.generateAccessToken(userId, UserRole.AUTHOR)).thenReturn("token-value");

        AuthResult result = service.authenticate(new AuthenticateUserCommand(EMAIL, "StrongPassword123"));

        assertThat(result.accessToken()).isEqualTo("token-value");
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.role()).isEqualTo(UserRole.AUTHOR);
    }

    @Test
    void shouldFailAuthenticationWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash(HASHED_SECRET);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", HASHED_SECRET)).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(new AuthenticateUserCommand(EMAIL, "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldFailAuthenticationWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(new AuthenticateUserCommand("missing@example.com", "pwd")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
