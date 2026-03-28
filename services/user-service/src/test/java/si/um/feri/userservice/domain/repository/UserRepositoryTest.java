package si.um.feri.userservice.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import si.um.feri.userservice.domain.model.User;
import si.um.feri.userservice.domain.model.UserRole;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByEmail() {
        User user = new User();
        user.setEmail("author@example.com");
        user.setPasswordHash("hashed-password");
        user.setFullName("Test Author");
        user.setRole(UserRole.AUTHOR);

        User saved = userRepository.save(user);
        Optional<User> loaded = userRepository.findByEmail(saved.getEmail());

        assertThat(loaded).isPresent();
        assertThat(loaded.get().getFullName()).isEqualTo("Test Author");
        assertThat(loaded.get().getRole()).isEqualTo(UserRole.AUTHOR);
        assertThat(loaded.get().getCreatedAt()).isNotNull();
        assertThat(loaded.get().getUpdatedAt()).isNotNull();
    }
}
