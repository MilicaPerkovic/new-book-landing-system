package si.um.feri.userservice.application.service;

import org.springframework.stereotype.Component;
import si.um.feri.userservice.application.result.UserResult;
import si.um.feri.userservice.domain.model.User;

@Component
public class UserMapper {

    public UserResult toResult(User user) {
        return new UserResult(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
