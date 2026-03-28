package si.um.feri.userservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.userservice.application.command.RegisterUserCommand;
import si.um.feri.userservice.application.command.UpdateUserProfileCommand;
import si.um.feri.userservice.application.result.UserResult;
import si.um.feri.userservice.domain.exception.DuplicateEmailException;
import si.um.feri.userservice.domain.exception.UserNotFoundException;
import si.um.feri.userservice.domain.model.User;
import si.um.feri.userservice.domain.repository.UserRepository;

import java.util.UUID;

@Service
public class UserApplicationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final UserMapper userMapper;

    public UserApplicationService(UserRepository userRepository,
                                  PasswordHasher passwordHasher,
                                  UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResult register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(command.email());
        }

        User user = new User();
        user.setEmail(command.email().trim().toLowerCase());
        user.setPasswordHash(passwordHasher.hash(command.password()));
        user.setFullName(command.fullName().trim());
        user.setRole(command.role());

        User saved = userRepository.save(user);
        return userMapper.toResult(saved);
    }

    @Transactional(readOnly = true)
    public UserResult getById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResult(user);
    }

    @Transactional
    public UserResult updateProfile(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.setFullName(command.fullName().trim());
        User saved = userRepository.save(user);

        return userMapper.toResult(saved);
    }
}
