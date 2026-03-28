package si.um.feri.userservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.userservice.application.command.AuthenticateUserCommand;
import si.um.feri.userservice.application.result.AuthResult;
import si.um.feri.userservice.domain.exception.InvalidCredentialsException;
import si.um.feri.userservice.domain.model.User;
import si.um.feri.userservice.domain.repository.UserRepository;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordHasher passwordHasher,
                                 TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public AuthResult authenticate(AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenService.generateAccessToken(user.getId(), user.getRole());
        return new AuthResult(accessToken, user.getId(), user.getRole());
    }
}
