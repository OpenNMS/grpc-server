package org.opennms.core.ipc.grpc.common;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 * service definitions of IPC between Minion and OpenNMS
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.24.0)",
    comments = "Source: ipc.proto")
public final class OpenNMSIpcGrpc {

  private OpenNMSIpcGrpc() {}

  public static final String SERVICE_NAME = "OpenNMSIpc";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.RpcResponseProto,
      org.opennms.core.ipc.grpc.common.RpcRequestProto> getRpcStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RpcStreaming",
      requestType = org.opennms.core.ipc.grpc.common.RpcResponseProto.class,
      responseType = org.opennms.core.ipc.grpc.common.RpcRequestProto.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.RpcResponseProto,
      org.opennms.core.ipc.grpc.common.RpcRequestProto> getRpcStreamingMethod() {
    io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.RpcResponseProto, org.opennms.core.ipc.grpc.common.RpcRequestProto> getRpcStreamingMethod;
    if ((getRpcStreamingMethod = OpenNMSIpcGrpc.getRpcStreamingMethod) == null) {
      synchronized (OpenNMSIpcGrpc.class) {
        if ((getRpcStreamingMethod = OpenNMSIpcGrpc.getRpcStreamingMethod) == null) {
          OpenNMSIpcGrpc.getRpcStreamingMethod = getRpcStreamingMethod =
              io.grpc.MethodDescriptor.<org.opennms.core.ipc.grpc.common.RpcResponseProto, org.opennms.core.ipc.grpc.common.RpcRequestProto>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RpcStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.grpc.common.RpcResponseProto.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.grpc.common.RpcRequestProto.getDefaultInstance()))
              .setSchemaDescriptor(new OpenNMSIpcMethodDescriptorSupplier("RpcStreaming"))
              .build();
        }
      }
    }
    return getRpcStreamingMethod;
  }

  private static volatile io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.SinkMessage,
      org.opennms.core.ipc.grpc.common.Empty> getSinkStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SinkStreaming",
      requestType = org.opennms.core.ipc.grpc.common.SinkMessage.class,
      responseType = org.opennms.core.ipc.grpc.common.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
  public static io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.SinkMessage,
      org.opennms.core.ipc.grpc.common.Empty> getSinkStreamingMethod() {
    io.grpc.MethodDescriptor<org.opennms.core.ipc.grpc.common.SinkMessage, org.opennms.core.ipc.grpc.common.Empty> getSinkStreamingMethod;
    if ((getSinkStreamingMethod = OpenNMSIpcGrpc.getSinkStreamingMethod) == null) {
      synchronized (OpenNMSIpcGrpc.class) {
        if ((getSinkStreamingMethod = OpenNMSIpcGrpc.getSinkStreamingMethod) == null) {
          OpenNMSIpcGrpc.getSinkStreamingMethod = getSinkStreamingMethod =
              io.grpc.MethodDescriptor.<org.opennms.core.ipc.grpc.common.SinkMessage, org.opennms.core.ipc.grpc.common.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SinkStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.grpc.common.SinkMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  org.opennms.core.ipc.grpc.common.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new OpenNMSIpcMethodDescriptorSupplier("SinkStreaming"))
              .build();
        }
      }
    }
    return getSinkStreamingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OpenNMSIpcStub newStub(io.grpc.Channel channel) {
    return new OpenNMSIpcStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OpenNMSIpcBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new OpenNMSIpcBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OpenNMSIpcFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new OpenNMSIpcFutureStub(channel);
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static abstract class OpenNMSIpcImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Streams RPC messages between OpenNMS and Minion.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcResponseProto> rpcStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcRequestProto> responseObserver) {
      return asyncUnimplementedStreamingCall(getRpcStreamingMethod(), responseObserver);
    }

    /**
     * <pre>
     * Streams Sink messages from Minion to OpenNMS
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.SinkMessage> sinkStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.Empty> responseObserver) {
      return asyncUnimplementedStreamingCall(getSinkStreamingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRpcStreamingMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.opennms.core.ipc.grpc.common.RpcResponseProto,
                org.opennms.core.ipc.grpc.common.RpcRequestProto>(
                  this, METHODID_RPC_STREAMING)))
          .addMethod(
            getSinkStreamingMethod(),
            asyncClientStreamingCall(
              new MethodHandlers<
                org.opennms.core.ipc.grpc.common.SinkMessage,
                org.opennms.core.ipc.grpc.common.Empty>(
                  this, METHODID_SINK_STREAMING)))
          .build();
    }
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OpenNMSIpcStub extends io.grpc.stub.AbstractStub<OpenNMSIpcStub> {
    private OpenNMSIpcStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenNMSIpcStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenNMSIpcStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenNMSIpcStub(channel, callOptions);
    }

    /**
     * <pre>
     * Streams RPC messages between OpenNMS and Minion.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcResponseProto> rpcStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcRequestProto> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getRpcStreamingMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Streams Sink messages from Minion to OpenNMS
     * </pre>
     */
    public io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.SinkMessage> sinkStreaming(
        io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.Empty> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(getSinkStreamingMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OpenNMSIpcBlockingStub extends io.grpc.stub.AbstractStub<OpenNMSIpcBlockingStub> {
    private OpenNMSIpcBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenNMSIpcBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenNMSIpcBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenNMSIpcBlockingStub(channel, callOptions);
    }
  }

  /**
   * <pre>
   * service definitions of IPC between Minion and OpenNMS
   * </pre>
   */
  public static final class OpenNMSIpcFutureStub extends io.grpc.stub.AbstractStub<OpenNMSIpcFutureStub> {
    private OpenNMSIpcFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenNMSIpcFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenNMSIpcFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenNMSIpcFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_RPC_STREAMING = 0;
  private static final int METHODID_SINK_STREAMING = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OpenNMSIpcImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OpenNMSIpcImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_RPC_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.rpcStreaming(
              (io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.RpcRequestProto>) responseObserver);
        case METHODID_SINK_STREAMING:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.sinkStreaming(
              (io.grpc.stub.StreamObserver<org.opennms.core.ipc.grpc.common.Empty>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OpenNMSIpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OpenNMSIpcBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.opennms.core.ipc.grpc.common.MinionIpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OpenNMSIpc");
    }
  }

  private static final class OpenNMSIpcFileDescriptorSupplier
      extends OpenNMSIpcBaseDescriptorSupplier {
    OpenNMSIpcFileDescriptorSupplier() {}
  }

  private static final class OpenNMSIpcMethodDescriptorSupplier
      extends OpenNMSIpcBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OpenNMSIpcMethodDescriptorSupplier(String methodName) {
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
      synchronized (OpenNMSIpcGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OpenNMSIpcFileDescriptorSupplier())
              .addMethod(getRpcStreamingMethod())
              .addMethod(getSinkStreamingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
