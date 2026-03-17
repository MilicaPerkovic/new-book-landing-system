package si.um.feri.userservice.grpc.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import si.um.feri.userservice.application.command.AuthenticateUserCommand;
import si.um.feri.userservice.application.command.RegisterUserCommand;
import si.um.feri.userservice.application.command.UpdateUserProfileCommand;
import si.um.feri.userservice.application.result.AuthResult;
import si.um.feri.userservice.application.result.TokenValidationResult;
import si.um.feri.userservice.application.result.UserResult;
import si.um.feri.userservice.application.service.AuthenticationService;
import si.um.feri.userservice.application.service.TokenService;
import si.um.feri.userservice.application.service.UserApplicationService;
import si.um.feri.userservice.domain.exception.DuplicateEmailException;
import si.um.feri.userservice.domain.exception.InvalidCredentialsException;
import si.um.feri.userservice.domain.exception.UserNotFoundException;
import si.um.feri.userservice.domain.model.UserRole;
import si.um.feri.userservice.grpc.AuthenticateUserRequest;
import si.um.feri.userservice.grpc.AuthenticateUserResponse;
import si.um.feri.userservice.grpc.GetUserByIdRequest;
import si.um.feri.userservice.grpc.GetUserByIdResponse;
import si.um.feri.userservice.grpc.RegisterUserRequest;
import si.um.feri.userservice.grpc.RegisterUserResponse;
import si.um.feri.userservice.grpc.UpdateUserProfileRequest;
import si.um.feri.userservice.grpc.UpdateUserProfileResponse;
import si.um.feri.userservice.grpc.UserServiceGrpc;
import si.um.feri.userservice.grpc.ValidateTokenRequest;
import si.um.feri.userservice.grpc.ValidateTokenResponse;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(UserGrpcService.class);

    private final UserApplicationService userApplicationService;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    public UserGrpcService(UserApplicationService userApplicationService,
                           AuthenticationService authenticationService,
                           TokenService tokenService) {
        this.userApplicationService = userApplicationService;
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
    }

    @Override
    public void registerUser(RegisterUserRequest request, StreamObserver<RegisterUserResponse> responseObserver) {
        try {
            LOG.info("Handling RegisterUser request: email={}, role={}", request.getEmail(), request.getRole());
            validateRegisterRequest(request);

            RegisterUserCommand command = new RegisterUserCommand(
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName(),
                    parseRole(request.getRole())
            );

            UserResult user = userApplicationService.register(command);
            RegisterUserResponse response = RegisterUserResponse.newBuilder()
                    .setUserId(user.id().toString())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            LOG.info("RegisterUser succeeded: userId={}", user.id());
        } catch (Exception ex) {
            onError(ex, responseObserver);
        }
    }

    @Override
    public void authenticateUser(AuthenticateUserRequest request,
                                 StreamObserver<AuthenticateUserResponse> responseObserver) {
        try {
            LOG.info("Handling AuthenticateUser request: email={}", request.getEmail());
            if (request.getEmail().isBlank() || request.getPassword().isBlank()) {
                throw Status.INVALID_ARGUMENT.withDescription("email and password are required").asRuntimeException();
            }

            AuthResult authResult = authenticationService.authenticate(
                    new AuthenticateUserCommand(request.getEmail(), request.getPassword())
            );

            AuthenticateUserResponse response = AuthenticateUserResponse.newBuilder()
                    .setAccessToken(authResult.accessToken())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            LOG.info("AuthenticateUser succeeded: email={}", request.getEmail());
        } catch (Exception ex) {
            onError(ex, responseObserver);
        }
    }

    @Override
    public void getUserById(GetUserByIdRequest request, StreamObserver<GetUserByIdResponse> responseObserver) {
        try {
            UUID userId = parseUuid(request.getUserId(), "user_id");
            LOG.info("Handling GetUserById request: userId={}", userId);
            UserResult user = userApplicationService.getById(userId);

            GetUserByIdResponse response = GetUserByIdResponse.newBuilder()
                    .setUserId(user.id().toString())
                    .setEmail(user.email())
                    .setFullName(user.fullName())
                    .setRole(user.role().name())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            LOG.info("GetUserById succeeded: userId={}", userId);
        } catch (Exception ex) {
            onError(ex, responseObserver);
        }
    }

    @Override
    public void updateUserProfile(UpdateUserProfileRequest request,
                                  StreamObserver<UpdateUserProfileResponse> responseObserver) {
        try {
            UUID userId = parseUuid(request.getUserId(), "user_id");
            LOG.info("Handling UpdateUserProfile request: userId={}", userId);
            if (request.getFullName().isBlank()) {
                throw Status.INVALID_ARGUMENT.withDescription("full_name is required").asRuntimeException();
            }

            UserResult user = userApplicationService.updateProfile(
                    new UpdateUserProfileCommand(userId, request.getFullName())
            );

            UpdateUserProfileResponse response = UpdateUserProfileResponse.newBuilder()
                    .setUserId(user.id().toString())
                    .setFullName(user.fullName())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            LOG.info("UpdateUserProfile succeeded: userId={}", userId);
        } catch (Exception ex) {
            onError(ex, responseObserver);
        }
    }

    @Override
    public void validateToken(ValidateTokenRequest request, StreamObserver<ValidateTokenResponse> responseObserver) {
        try {
            LOG.info("Handling ValidateToken request");
            if (request.getToken().isBlank()) {
                throw Status.INVALID_ARGUMENT.withDescription("token is required").asRuntimeException();
            }

            Optional<TokenValidationResult> validationResult = tokenService.validateAccessToken(request.getToken());
            ValidateTokenResponse.Builder responseBuilder = ValidateTokenResponse.newBuilder()
                    .setValid(validationResult.isPresent());

            validationResult.ifPresent(result -> responseBuilder
                    .setUserId(result.userId().toString())
                    .setRole(result.role().name()));

            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            LOG.info("ValidateToken finished: valid={}", validationResult.isPresent());
        } catch (Exception ex) {
            onError(ex, responseObserver);
        }
    }

    private void validateRegisterRequest(RegisterUserRequest request) {
        if (request.getEmail().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("email is required").asRuntimeException();
        }
        if (request.getPassword().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("password is required").asRuntimeException();
        }
        if (request.getFullName().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("full_name is required").asRuntimeException();
        }
        if (request.getRole().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("role is required").asRuntimeException();
        }
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (Exception ex) {
            throw Status.INVALID_ARGUMENT
                    .withDescription(fieldName + " must be a valid UUID")
                    .asRuntimeException();
        }
    }

    private UserRole parseRole(String roleValue) {
        try {
            return UserRole.valueOf(roleValue.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("role must be one of AUTHOR, PUBLISHER, ADMIN, READER")
                    .asRuntimeException();
        }
    }

    private void onError(Exception ex, StreamObserver<?> responseObserver) {
        LOG.warn("gRPC request failed: {}", ex.getMessage());
        if (ex instanceof io.grpc.StatusRuntimeException statusRuntimeException) {
            responseObserver.onError(statusRuntimeException);
            return;
        }

        if (ex instanceof DuplicateEmailException) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(ex.getMessage()).asRuntimeException());
            return;
        }
        if (ex instanceof InvalidCredentialsException) {
            responseObserver.onError(Status.UNAUTHENTICATED.withDescription(ex.getMessage()).asRuntimeException());
            return;
        }
        if (ex instanceof UserNotFoundException) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
            return;
        }

        LOG.error("Unhandled gRPC exception", ex);
        responseObserver.onError(Status.INTERNAL.withDescription("internal server error").asRuntimeException());
    }
}
