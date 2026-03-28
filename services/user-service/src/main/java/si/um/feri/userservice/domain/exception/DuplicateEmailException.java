package si.um.feri.userservice.domain.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("User with email already exists: " + email);
    }
}
