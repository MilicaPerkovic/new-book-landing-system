package si.um.feri.userservice.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.um.feri.userservice.application.command.RegisterUserCommand;
import si.um.feri.userservice.application.command.UpdateUserProfileCommand;
import si.um.feri.userservice.application.result.UserResult;
import si.um.feri.userservice.domain.exception.DuplicateEmailException;
import si.um.feri.userservice.domain.exception.UserNotFoundException;
import si.um.feri.userservice.domain.model.User;
import si.um.feri.userservice.domain.model.UserRole;
import si.um.feri.userservice.domain.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

        private static final String EMAIL = "author@example.com";
        private static final String RAW_PASSWORD = "StrongPassword123";
        private static final String FULL_NAME = "Test Author";
        private static final String HASHED_SECRET = "hashed";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserApplicationService service;

    @Test
    void shouldRegisterUser() {
        RegisterUserCommand command = new RegisterUserCommand(
                EMAIL,
                RAW_PASSWORD,
                FULL_NAME,
                UserRole.AUTHOR
        );

        User persisted = new User();
        UUID userId = UUID.randomUUID();
        persisted.setId(userId);
        persisted.setEmail(EMAIL);
        persisted.setPasswordHash(HASHED_SECRET);
        persisted.setFullName(FULL_NAME);
        persisted.setRole(UserRole.AUTHOR);
        persisted.setCreatedAt(Instant.now());
        persisted.setUpdatedAt(Instant.now());

        UserResult mapped = new UserResult(
                userId,
                persisted.getEmail(),
                persisted.getFullName(),
                persisted.getRole(),
                persisted.getCreatedAt(),
                persisted.getUpdatedAt()
        );

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordHasher.hash(RAW_PASSWORD)).thenReturn(HASHED_SECRET);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(persisted);
        when(userMapper.toResult(persisted)).thenReturn(mapped);

        UserResult result = service.register(command);

        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.role()).isEqualTo(UserRole.AUTHOR);
    }

    @Test
    void shouldFailRegisterWhenEmailExists() {
        RegisterUserCommand command = new RegisterUserCommand(
                EMAIL,
                RAW_PASSWORD,
                FULL_NAME,
                UserRole.AUTHOR
        );

        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.register(command))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void shouldUpdateProfile() {
        UUID userId = UUID.randomUUID();

        User existing = new User();
        existing.setId(userId);
        existing.setEmail(EMAIL);
        existing.setPasswordHash(HASHED_SECRET);
        existing.setFullName("Old Name");
        existing.setRole(UserRole.AUTHOR);

        User saved = new User();
        saved.setId(userId);
        saved.setEmail(EMAIL);
        saved.setPasswordHash(HASHED_SECRET);
        saved.setFullName("New Name");
        saved.setRole(UserRole.AUTHOR);
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());

        UserResult mapped = new UserResult(
                userId,
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(saved);
        when(userMapper.toResult(saved)).thenReturn(mapped);

        UserResult result = service.updateProfile(new UpdateUserProfileCommand(userId, " New Name "));

        assertThat(result.fullName()).isEqualTo("New Name");
    }

    @Test
    void shouldFailWhenUserNotFoundById() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}
