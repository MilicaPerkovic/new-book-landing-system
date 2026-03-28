package si.um.feri.userservice.application.command;

import si.um.feri.userservice.domain.model.UserRole;

public record RegisterUserCommand(
        String email,
        String password,
        String fullName,
        UserRole role
) {
}
