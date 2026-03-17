package si.um.feri.userservice.application.command;

public record AuthenticateUserCommand(
        String email,
        String password
) {
}
