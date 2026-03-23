package si.um.feri.userservice.application.command;

import java.util.UUID;

public record UpdateUserProfileCommand(
        UUID userId,
        String fullName
) {
}
