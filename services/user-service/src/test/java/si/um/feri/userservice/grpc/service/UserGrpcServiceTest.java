package si.um.feri.userservice.grpc.service;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import si.um.feri.userservice.application.command.RegisterUserCommand;
import si.um.feri.userservice.application.result.AuthResult;
import si.um.feri.userservice.application.result.TokenValidationResult;
import si.um.feri.userservice.application.result.UserResult;
import si.um.feri.userservice.application.service.AuthenticationService;
import si.um.feri.userservice.application.service.TokenService;
import si.um.feri.userservice.application.service.UserApplicationService;
import si.um.feri.userservice.domain.exception.DuplicateEmailException;
import si.um.feri.userservice.domain.model.UserRole;
import si.um.feri.userservice.grpc.AuthenticateUserRequest;
import si.um.feri.userservice.grpc.GetUserByIdRequest;
import si.um.feri.userservice.grpc.GetUserByIdResponse;
import si.um.feri.userservice.grpc.RegisterUserRequest;
import si.um.feri.userservice.grpc.RegisterUserResponse;
import si.um.feri.userservice.grpc.ValidateTokenRequest;
import si.um.feri.userservice.grpc.ValidateTokenResponse;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGrpcServiceTest {

    private static final String EMAIL = "author@example.com";
    private static final String FULL_NAME = "Author Name";
    private static final String RAW_SECRET = "StrongPassword123";

    @Mock
    private UserApplicationService userApplicationService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserGrpcService grpcService;

    @Mock
    private StreamObserver<RegisterUserResponse> registerObserver;

    @Mock
    private StreamObserver<si.um.feri.userservice.grpc.AuthenticateUserResponse> authObserver;

    @Mock
    private StreamObserver<GetUserByIdResponse> getByIdObserver;

    @Mock
    private StreamObserver<ValidateTokenResponse> validateObserver;

    @Test
    void registerUserShouldReturnUserId() {
        UUID userId = UUID.randomUUID();
        UserResult result = new UserResult(
                userId,
            EMAIL,
            FULL_NAME,
                UserRole.AUTHOR,
                Instant.now(),
                Instant.now()
        );

        when(userApplicationService.register(any(RegisterUserCommand.class))).thenReturn(result);

        RegisterUserRequest request = RegisterUserRequest.newBuilder()
            .setEmail(EMAIL)
            .setPassword(RAW_SECRET)
            .setFullName(FULL_NAME)
                .setRole("AUTHOR")
                .build();

        grpcService.registerUser(request, registerObserver);

        ArgumentCaptor<RegisterUserResponse> responseCaptor = ArgumentCaptor.forClass(RegisterUserResponse.class);
        verify(registerObserver).onNext(responseCaptor.capture());
        verify(registerObserver).onCompleted();
        verify(registerObserver, never()).onError(any(Throwable.class));

        assertThat(responseCaptor.getValue().getUserId()).isEqualTo(userId.toString());
    }

    @Test
    void registerUserShouldReturnAlreadyExistsWhenEmailDuplicated() {
        when(userApplicationService.register(any(RegisterUserCommand.class)))
            .thenThrow(new DuplicateEmailException(EMAIL));

        RegisterUserRequest request = RegisterUserRequest.newBuilder()
            .setEmail(EMAIL)
            .setPassword(RAW_SECRET)
            .setFullName(FULL_NAME)
                .setRole("AUTHOR")
                .build();

        grpcService.registerUser(request, registerObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(registerObserver).onError(errorCaptor.capture());
        verify(registerObserver, never()).onCompleted();

        assertThat(errorCaptor.getValue()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException error = (StatusRuntimeException) errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.ALREADY_EXISTS);
    }

    @Test
    void authenticateUserShouldReturnToken() {
        UUID userId = UUID.randomUUID();
        when(authenticationService.authenticate(any())).thenReturn(new AuthResult("token123", userId, UserRole.AUTHOR));

        AuthenticateUserRequest request = AuthenticateUserRequest.newBuilder()
            .setEmail(EMAIL)
            .setPassword(RAW_SECRET)
                .build();

        grpcService.authenticateUser(request, authObserver);

        ArgumentCaptor<si.um.feri.userservice.grpc.AuthenticateUserResponse> responseCaptor = ArgumentCaptor.forClass(si.um.feri.userservice.grpc.AuthenticateUserResponse.class);
        verify(authObserver).onNext(responseCaptor.capture());
        verify(authObserver).onCompleted();
        verify(authObserver, never()).onError(any(Throwable.class));

        assertThat(responseCaptor.getValue().getAccessToken()).isEqualTo("token123");
    }

    @Test
    void getUserByIdShouldReturnNotFoundForUnknownUser() {
        UUID missingId = UUID.randomUUID();
        when(userApplicationService.getById(missingId)).thenThrow(new si.um.feri.userservice.domain.exception.UserNotFoundException(missingId));

        GetUserByIdRequest request = GetUserByIdRequest.newBuilder()
                .setUserId(missingId.toString())
                .build();

        grpcService.getUserById(request, getByIdObserver);

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(getByIdObserver).onError(errorCaptor.capture());
        verify(getByIdObserver, never()).onCompleted();

        assertThat(errorCaptor.getValue()).isInstanceOf(StatusRuntimeException.class);
        StatusRuntimeException error = (StatusRuntimeException) errorCaptor.getValue();
        assertThat(error.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
    }

    @Test
    void validateTokenShouldReturnValidResponse() {
        UUID userId = UUID.randomUUID();
        when(tokenService.validateAccessToken("valid-token"))
                .thenReturn(Optional.of(new TokenValidationResult(userId, UserRole.ADMIN)));

        ValidateTokenRequest request = ValidateTokenRequest.newBuilder()
                .setToken("valid-token")
                .build();

        grpcService.validateToken(request, validateObserver);

        ArgumentCaptor<ValidateTokenResponse> responseCaptor = ArgumentCaptor.forClass(ValidateTokenResponse.class);
        verify(validateObserver).onNext(responseCaptor.capture());
        verify(validateObserver).onCompleted();
        verify(validateObserver, never()).onError(any(Throwable.class));

        assertThat(responseCaptor.getValue().getValid()).isTrue();
        assertThat(responseCaptor.getValue().getUserId()).isEqualTo(userId.toString());
        assertThat(responseCaptor.getValue().getRole()).isEqualTo("ADMIN");
    }
}
