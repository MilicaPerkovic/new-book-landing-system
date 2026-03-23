package si.um.feri.userservice.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: user_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UserServiceGrpc {

  private UserServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "user.v1.UserService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.RegisterUserRequest,
      si.um.feri.userservice.grpc.RegisterUserResponse> getRegisterUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterUser",
      requestType = si.um.feri.userservice.grpc.RegisterUserRequest.class,
      responseType = si.um.feri.userservice.grpc.RegisterUserResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.RegisterUserRequest,
      si.um.feri.userservice.grpc.RegisterUserResponse> getRegisterUserMethod() {
    io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.RegisterUserRequest, si.um.feri.userservice.grpc.RegisterUserResponse> getRegisterUserMethod;
    if ((getRegisterUserMethod = UserServiceGrpc.getRegisterUserMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getRegisterUserMethod = UserServiceGrpc.getRegisterUserMethod) == null) {
          UserServiceGrpc.getRegisterUserMethod = getRegisterUserMethod =
              io.grpc.MethodDescriptor.<si.um.feri.userservice.grpc.RegisterUserRequest, si.um.feri.userservice.grpc.RegisterUserResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.RegisterUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.RegisterUserResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("RegisterUser"))
              .build();
        }
      }
    }
    return getRegisterUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.AuthenticateUserRequest,
      si.um.feri.userservice.grpc.AuthenticateUserResponse> getAuthenticateUserMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AuthenticateUser",
      requestType = si.um.feri.userservice.grpc.AuthenticateUserRequest.class,
      responseType = si.um.feri.userservice.grpc.AuthenticateUserResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.AuthenticateUserRequest,
      si.um.feri.userservice.grpc.AuthenticateUserResponse> getAuthenticateUserMethod() {
    io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.AuthenticateUserRequest, si.um.feri.userservice.grpc.AuthenticateUserResponse> getAuthenticateUserMethod;
    if ((getAuthenticateUserMethod = UserServiceGrpc.getAuthenticateUserMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getAuthenticateUserMethod = UserServiceGrpc.getAuthenticateUserMethod) == null) {
          UserServiceGrpc.getAuthenticateUserMethod = getAuthenticateUserMethod =
              io.grpc.MethodDescriptor.<si.um.feri.userservice.grpc.AuthenticateUserRequest, si.um.feri.userservice.grpc.AuthenticateUserResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AuthenticateUser"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.AuthenticateUserRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.AuthenticateUserResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("AuthenticateUser"))
              .build();
        }
      }
    }
    return getAuthenticateUserMethod;
  }

  private static volatile io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.GetUserByIdRequest,
      si.um.feri.userservice.grpc.GetUserByIdResponse> getGetUserByIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserById",
      requestType = si.um.feri.userservice.grpc.GetUserByIdRequest.class,
      responseType = si.um.feri.userservice.grpc.GetUserByIdResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.GetUserByIdRequest,
      si.um.feri.userservice.grpc.GetUserByIdResponse> getGetUserByIdMethod() {
    io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.GetUserByIdRequest, si.um.feri.userservice.grpc.GetUserByIdResponse> getGetUserByIdMethod;
    if ((getGetUserByIdMethod = UserServiceGrpc.getGetUserByIdMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getGetUserByIdMethod = UserServiceGrpc.getGetUserByIdMethod) == null) {
          UserServiceGrpc.getGetUserByIdMethod = getGetUserByIdMethod =
              io.grpc.MethodDescriptor.<si.um.feri.userservice.grpc.GetUserByIdRequest, si.um.feri.userservice.grpc.GetUserByIdResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserById"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.GetUserByIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.GetUserByIdResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("GetUserById"))
              .build();
        }
      }
    }
    return getGetUserByIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.UpdateUserProfileRequest,
      si.um.feri.userservice.grpc.UpdateUserProfileResponse> getUpdateUserProfileMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateUserProfile",
      requestType = si.um.feri.userservice.grpc.UpdateUserProfileRequest.class,
      responseType = si.um.feri.userservice.grpc.UpdateUserProfileResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.UpdateUserProfileRequest,
      si.um.feri.userservice.grpc.UpdateUserProfileResponse> getUpdateUserProfileMethod() {
    io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.UpdateUserProfileRequest, si.um.feri.userservice.grpc.UpdateUserProfileResponse> getUpdateUserProfileMethod;
    if ((getUpdateUserProfileMethod = UserServiceGrpc.getUpdateUserProfileMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getUpdateUserProfileMethod = UserServiceGrpc.getUpdateUserProfileMethod) == null) {
          UserServiceGrpc.getUpdateUserProfileMethod = getUpdateUserProfileMethod =
              io.grpc.MethodDescriptor.<si.um.feri.userservice.grpc.UpdateUserProfileRequest, si.um.feri.userservice.grpc.UpdateUserProfileResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateUserProfile"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.UpdateUserProfileRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.UpdateUserProfileResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("UpdateUserProfile"))
              .build();
        }
      }
    }
    return getUpdateUserProfileMethod;
  }

  private static volatile io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.ValidateTokenRequest,
      si.um.feri.userservice.grpc.ValidateTokenResponse> getValidateTokenMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateToken",
      requestType = si.um.feri.userservice.grpc.ValidateTokenRequest.class,
      responseType = si.um.feri.userservice.grpc.ValidateTokenResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.ValidateTokenRequest,
      si.um.feri.userservice.grpc.ValidateTokenResponse> getValidateTokenMethod() {
    io.grpc.MethodDescriptor<si.um.feri.userservice.grpc.ValidateTokenRequest, si.um.feri.userservice.grpc.ValidateTokenResponse> getValidateTokenMethod;
    if ((getValidateTokenMethod = UserServiceGrpc.getValidateTokenMethod) == null) {
      synchronized (UserServiceGrpc.class) {
        if ((getValidateTokenMethod = UserServiceGrpc.getValidateTokenMethod) == null) {
          UserServiceGrpc.getValidateTokenMethod = getValidateTokenMethod =
              io.grpc.MethodDescriptor.<si.um.feri.userservice.grpc.ValidateTokenRequest, si.um.feri.userservice.grpc.ValidateTokenResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateToken"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.ValidateTokenRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  si.um.feri.userservice.grpc.ValidateTokenResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UserServiceMethodDescriptorSupplier("ValidateToken"))
              .build();
        }
      }
    }
    return getValidateTokenMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UserServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserServiceStub>() {
        @java.lang.Override
        public UserServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserServiceStub(channel, callOptions);
        }
      };
    return UserServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UserServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserServiceBlockingStub>() {
        @java.lang.Override
        public UserServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserServiceBlockingStub(channel, callOptions);
        }
      };
    return UserServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UserServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UserServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UserServiceFutureStub>() {
        @java.lang.Override
        public UserServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UserServiceFutureStub(channel, callOptions);
        }
      };
    return UserServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void registerUser(si.um.feri.userservice.grpc.RegisterUserRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.RegisterUserResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterUserMethod(), responseObserver);
    }

    /**
     */
    default void authenticateUser(si.um.feri.userservice.grpc.AuthenticateUserRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.AuthenticateUserResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAuthenticateUserMethod(), responseObserver);
    }

    /**
     */
    default void getUserById(si.um.feri.userservice.grpc.GetUserByIdRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.GetUserByIdResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserByIdMethod(), responseObserver);
    }

    /**
     */
    default void updateUserProfile(si.um.feri.userservice.grpc.UpdateUserProfileRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.UpdateUserProfileResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateUserProfileMethod(), responseObserver);
    }

    /**
     */
    default void validateToken(si.um.feri.userservice.grpc.ValidateTokenRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.ValidateTokenResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateTokenMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UserService.
   */
  public static abstract class UserServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UserServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UserService.
   */
  public static final class UserServiceStub
      extends io.grpc.stub.AbstractAsyncStub<UserServiceStub> {
    private UserServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserServiceStub(channel, callOptions);
    }

    /**
     */
    public void registerUser(si.um.feri.userservice.grpc.RegisterUserRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.RegisterUserResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void authenticateUser(si.um.feri.userservice.grpc.AuthenticateUserRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.AuthenticateUserResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAuthenticateUserMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserById(si.um.feri.userservice.grpc.GetUserByIdRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.GetUserByIdResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserByIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void updateUserProfile(si.um.feri.userservice.grpc.UpdateUserProfileRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.UpdateUserProfileResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateUserProfileMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void validateToken(si.um.feri.userservice.grpc.ValidateTokenRequest request,
        io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.ValidateTokenResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateTokenMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UserService.
   */
  public static final class UserServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UserServiceBlockingStub> {
    private UserServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public si.um.feri.userservice.grpc.RegisterUserResponse registerUser(si.um.feri.userservice.grpc.RegisterUserRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public si.um.feri.userservice.grpc.AuthenticateUserResponse authenticateUser(si.um.feri.userservice.grpc.AuthenticateUserRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAuthenticateUserMethod(), getCallOptions(), request);
    }

    /**
     */
    public si.um.feri.userservice.grpc.GetUserByIdResponse getUserById(si.um.feri.userservice.grpc.GetUserByIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserByIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public si.um.feri.userservice.grpc.UpdateUserProfileResponse updateUserProfile(si.um.feri.userservice.grpc.UpdateUserProfileRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateUserProfileMethod(), getCallOptions(), request);
    }

    /**
     */
    public si.um.feri.userservice.grpc.ValidateTokenResponse validateToken(si.um.feri.userservice.grpc.ValidateTokenRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateTokenMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UserService.
   */
  public static final class UserServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<UserServiceFutureStub> {
    private UserServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UserServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UserServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<si.um.feri.userservice.grpc.RegisterUserResponse> registerUser(
        si.um.feri.userservice.grpc.RegisterUserRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterUserMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<si.um.feri.userservice.grpc.AuthenticateUserResponse> authenticateUser(
        si.um.feri.userservice.grpc.AuthenticateUserRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAuthenticateUserMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<si.um.feri.userservice.grpc.GetUserByIdResponse> getUserById(
        si.um.feri.userservice.grpc.GetUserByIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserByIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<si.um.feri.userservice.grpc.UpdateUserProfileResponse> updateUserProfile(
        si.um.feri.userservice.grpc.UpdateUserProfileRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateUserProfileMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<si.um.feri.userservice.grpc.ValidateTokenResponse> validateToken(
        si.um.feri.userservice.grpc.ValidateTokenRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateTokenMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REGISTER_USER = 0;
  private static final int METHODID_AUTHENTICATE_USER = 1;
  private static final int METHODID_GET_USER_BY_ID = 2;
  private static final int METHODID_UPDATE_USER_PROFILE = 3;
  private static final int METHODID_VALIDATE_TOKEN = 4;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REGISTER_USER:
          serviceImpl.registerUser((si.um.feri.userservice.grpc.RegisterUserRequest) request,
              (io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.RegisterUserResponse>) responseObserver);
          break;
        case METHODID_AUTHENTICATE_USER:
          serviceImpl.authenticateUser((si.um.feri.userservice.grpc.AuthenticateUserRequest) request,
              (io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.AuthenticateUserResponse>) responseObserver);
          break;
        case METHODID_GET_USER_BY_ID:
          serviceImpl.getUserById((si.um.feri.userservice.grpc.GetUserByIdRequest) request,
              (io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.GetUserByIdResponse>) responseObserver);
          break;
        case METHODID_UPDATE_USER_PROFILE:
          serviceImpl.updateUserProfile((si.um.feri.userservice.grpc.UpdateUserProfileRequest) request,
              (io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.UpdateUserProfileResponse>) responseObserver);
          break;
        case METHODID_VALIDATE_TOKEN:
          serviceImpl.validateToken((si.um.feri.userservice.grpc.ValidateTokenRequest) request,
              (io.grpc.stub.StreamObserver<si.um.feri.userservice.grpc.ValidateTokenResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getRegisterUserMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              si.um.feri.userservice.grpc.RegisterUserRequest,
              si.um.feri.userservice.grpc.RegisterUserResponse>(
                service, METHODID_REGISTER_USER)))
        .addMethod(
          getAuthenticateUserMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              si.um.feri.userservice.grpc.AuthenticateUserRequest,
              si.um.feri.userservice.grpc.AuthenticateUserResponse>(
                service, METHODID_AUTHENTICATE_USER)))
        .addMethod(
          getGetUserByIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              si.um.feri.userservice.grpc.GetUserByIdRequest,
              si.um.feri.userservice.grpc.GetUserByIdResponse>(
                service, METHODID_GET_USER_BY_ID)))
        .addMethod(
          getUpdateUserProfileMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              si.um.feri.userservice.grpc.UpdateUserProfileRequest,
              si.um.feri.userservice.grpc.UpdateUserProfileResponse>(
                service, METHODID_UPDATE_USER_PROFILE)))
        .addMethod(
          getValidateTokenMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              si.um.feri.userservice.grpc.ValidateTokenRequest,
              si.um.feri.userservice.grpc.ValidateTokenResponse>(
                service, METHODID_VALIDATE_TOKEN)))
        .build();
  }

  private static abstract class UserServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UserServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return si.um.feri.userservice.grpc.UserServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UserService");
    }
  }

  private static final class UserServiceFileDescriptorSupplier
      extends UserServiceBaseDescriptorSupplier {
    UserServiceFileDescriptorSupplier() {}
  }

  private static final class UserServiceMethodDescriptorSupplier
      extends UserServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UserServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UserServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UserServiceFileDescriptorSupplier())
              .addMethod(getRegisterUserMethod())
              .addMethod(getAuthenticateUserMethod())
              .addMethod(getGetUserByIdMethod())
              .addMethod(getUpdateUserProfileMethod())
              .addMethod(getValidateTokenMethod())
              .build();
        }
      }
    }
    return result;
  }
}
