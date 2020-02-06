/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.ipc.grpc.server;

import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.CLIENT_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_GRPC_PORT_STRING;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_MESSAGE_SIZE_STRING;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_MAX_INBOUND_SIZE;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_SERVER_PORT;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.KAFKA_CONSUMER_SYS_PROP_PREFIX;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.KAFKA_PRODUCER_SYS_PROP_PREFIX;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.MAX_BUFFER_SIZE_CONFIGURED;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.PRIVATE_KEY_FILE_PATH;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.SERVER_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.TLS_ENABLED;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.getOpenNMSInstanceId;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.getProperties;

import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.opennms.core.ipc.grpc.common.Empty;
import org.opennms.core.ipc.grpc.common.OpenNMSIpcGrpc;
import org.opennms.core.ipc.grpc.common.RpcMessageProto;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;
import org.opennms.core.ipc.grpc.common.RpcResponseProto;
import org.opennms.core.ipc.grpc.common.SinkMessage;
import org.opennms.core.ipc.grpc.common.SinkMessageProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.math.IntMath;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.stub.StreamObserver;

public class GrpcIpcServer {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcIpcServer.class);
    private Server server;
    private KafkaProducer<String, byte[]> producer;
    private List<String> minions = new ArrayList<>();
    // cache to hold rpcId and ByteString when there are multiple chunks for the message.
    private Map<String, ByteString> messageCache = new ConcurrentHashMap<>();
    private Map<String, Integer> currentChunkCache = new ConcurrentHashMap<>();
    // Delay queue which caches rpcId and removes when rpcId reaches expiration time.
    private DelayQueue<RpcId> rpcIdQueue = new DelayQueue<>();
    private ExecutorService delayQueueExecutor = Executors.newSingleThreadExecutor();
    private final ThreadFactory consumerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-kafka-consumer-%d")
            .build();
    private ExecutorService kafkaConsumerExecutor = Executors.newCachedThreadPool(consumerThreadFactory);
    private Map<String, KafkaConsumerRunner> consumerRunnerByLocation = new ConcurrentHashMap<>();
    // Maintains map of minionId and rpc handler for that minion. Used for directed RPC requests.
    private Map<String, StreamObserver<RpcRequestProto>> rpcHandlerByMinionId = new HashMap<>();
    // Maintains multi element map of location and rpc handlers for that location.
    // Used to get one of the rpc handlers for a specific location.
    private Multimap<String, StreamObserver<RpcRequestProto>> rpcHandlerByLocation = LinkedListMultimap.create();
    // Maintains the state of iteration for the list of minions for a given location.
    private Map<Collection<StreamObserver<RpcRequestProto>>, Iterator<StreamObserver<RpcRequestProto>>> rpcHandlerIteratorMap = new HashMap<>();

    public void start() throws IOException {
        boolean tlsEnabled = Boolean.getBoolean(TLS_ENABLED);
        int port = Integer.parseInt(System.getProperty(GRPC_SERVER_PORT, DEFAULT_GRPC_PORT_STRING));
        int maxInboundMessageSize = Integer.parseInt(System.getProperty(GRPC_MAX_INBOUND_SIZE, DEFAULT_MESSAGE_SIZE_STRING));

        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(port))
                .addService(new IpcServer())
                .maxInboundMessageSize(maxInboundMessageSize);
        if (tlsEnabled) {
            SslContextBuilder sslContextBuilder = getSslContextBuilder();
            if (sslContextBuilder != null) {
                serverBuilder.sslContext(sslContextBuilder.build());
                LOG.info("TLS enabled for gRPC");
            }
        }
        server = serverBuilder.build();
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getCanonicalName());
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getCanonicalName());
        producerConfig.putAll(getProperties(KAFKA_PRODUCER_SYS_PROP_PREFIX));
        producer = new KafkaProducer<>(producerConfig);
        delayQueueExecutor.execute(() -> {
            while (true) {
                try {
                    RpcId rpcId = rpcIdQueue.take();
                    messageCache.remove(rpcId.getRpcId());
                    currentChunkCache.remove(rpcId.getRpcId());
                } catch (InterruptedException e) {
                    LOG.error("Delay Queue has been interrupted ", e);
                    break;
                }
            }
        });
        server.start();
    }


    private SslContextBuilder getSslContextBuilder() {
        String certChainFilePath = System.getProperty(SERVER_CERTIFICATE_FILE_PATH);
        String privateKeyFilePath = System.getProperty(PRIVATE_KEY_FILE_PATH);
        String clientCertChainFilePath = System.getProperty(CLIENT_CERTIFICATE_FILE_PATH);
        if (Strings.isNullOrEmpty(certChainFilePath) || Strings.isNullOrEmpty(privateKeyFilePath)) {
            return null;
        }

        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath),
                new File(privateKeyFilePath));
        if (!Strings.isNullOrEmpty(clientCertChainFilePath)) {
            sslClientContextBuilder.trustManager(new File(clientCertChainFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder,
                SslProvider.OPENSSL);
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
        consumerRunnerByLocation.forEach((location, consumerRunner) -> {
            consumerRunner.stop();
        });
        delayQueueExecutor.shutdown();
        kafkaConsumerExecutor.shutdown();
        messageCache.clear();
        currentChunkCache.clear();
        rpcHandlerByMinionId.clear();
        rpcHandlerByLocation.clear();
    }


    class IpcServer extends OpenNMSIpcGrpc.OpenNMSIpcImplBase {

        @Override
        public StreamObserver<RpcResponseProto> rpcStreaming(
                StreamObserver<RpcRequestProto> responseObserver) {

            return new StreamObserver<RpcResponseProto>() {

                public void onNext(RpcResponseProto rpcResponseProto) {
                    if (isHeaders(rpcResponseProto)) {
                        addRpcHandler(rpcResponseProto.getLocation(), rpcResponseProto.getSystemId(), responseObserver);
                        startConsumingForLocation(rpcResponseProto.getLocation());
                    } else {
                        try {
                            transformAndSendResponse(rpcResponseProto);
                        } catch (Throwable e) {
                            LOG.error("Encountered exception while sending rpc response {} to kafka", rpcResponseProto, e);
                        }

                    }
                }

                public void onError(Throwable t) {

                }

                public void onCompleted() {

                }
            };
        }

        @Override
        public io.grpc.stub.StreamObserver<SinkMessage> sinkStreaming(
                io.grpc.stub.StreamObserver<Empty> responseObserver) {

            return new StreamObserver<SinkMessage>() {

                public void onNext(SinkMessage sinkMessage) {
                    try {
                        transformAndSendSinkMessage(sinkMessage);
                    } catch (Throwable e) {
                        LOG.error("Encountered exception while sending sink message {} to kafka", sinkMessage, e);
                    }
                }

                public void onError(Throwable t) {

                }

                public void onCompleted() {

                }
            };
        }

    }

    private void transformAndSendSinkMessage(SinkMessage sinkMessage) {
        SinkMessageProto.Builder builder = SinkMessageProto.newBuilder()
                .setMessageId(sinkMessage.getMessageId());
        final ByteString content = sinkMessage.getContent();
        String response = content.toStringUtf8();
        byte[] messageInBytes = response.getBytes();
        int totalChunks = IntMath.divide(messageInBytes.length, MAX_BUFFER_SIZE_CONFIGURED, RoundingMode.UP);
        for (int chunk = 0; chunk < totalChunks; chunk++) {
            // Calculate remaining bufferSize for each chunk.
            int bufferSize = GrpcServerConstants.getBufferSize(messageInBytes.length, MAX_BUFFER_SIZE_CONFIGURED, chunk);

            ByteString byteString = ByteString.copyFrom(messageInBytes, chunk * MAX_BUFFER_SIZE_CONFIGURED, bufferSize);
            SinkMessageProto sinkMessageProto = builder.setCurrentChunkNumber(chunk)
                    .setContent(byteString)
                    .setCurrentChunkNumber(chunk)
                    .setTotalChunks(totalChunks)
                    .build();
            sendSinkMessageToKafka(sinkMessageProto, GrpcServerConstants.getSinkTopic(sinkMessage.getModuleId()));
        }

    }

    private void sendSinkMessageToKafka(SinkMessageProto sinkMessage, String topic) {
        String messageId = sinkMessage.getMessageId();
        final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
                topic, messageId, sinkMessage.toByteArray());
        producer.send(producerRecord, (recordMetadata, e) -> {
            if (e != null) {
                LOG.error(" Sink message {} with id {} couldn't be sent to Kafka", sinkMessage, messageId, e);
            }
        });
    }

    private void startConsumingForLocation(String location) {
        if (consumerRunnerByLocation.get(location) != null) {
            return;
        }
        Properties consumerConfig = new Properties();
        consumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, getOpenNMSInstanceId());
        consumerConfig.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getCanonicalName());
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getCanonicalName());
        consumerConfig.putAll(getProperties(KAFKA_CONSUMER_SYS_PROP_PREFIX));
        KafkaConsumer<String, byte[]> kafkaConsumer = new KafkaConsumer<>(consumerConfig);
        KafkaConsumerRunner consumerRunner = new KafkaConsumerRunner(kafkaConsumer, location);
        consumerRunnerByLocation.put(location, consumerRunner);
        kafkaConsumerExecutor.execute(consumerRunner);
    }

    private void transformAndSendResponse(RpcResponseProto rpcResponseProto) {
        RpcMessageProto.Builder builder = RpcMessageProto.newBuilder()
                .setRpcId(rpcResponseProto.getRpcId())
                .setRpcContent(rpcResponseProto.getRpcContent())
                .setModuleId(rpcResponseProto.getModuleId())
                .setSystemId(rpcResponseProto.getSystemId());
        final ByteString content = rpcResponseProto.getRpcContent();
        String response = content.toStringUtf8();
        byte[] messageInBytes = response.getBytes();
        int totalChunks = IntMath.divide(messageInBytes.length, MAX_BUFFER_SIZE_CONFIGURED, RoundingMode.UP);
        for (int chunk = 0; chunk < totalChunks; chunk++) {
            // Calculate remaining bufferSize for each chunk.
            int bufferSize = GrpcServerConstants.getBufferSize(messageInBytes.length, MAX_BUFFER_SIZE_CONFIGURED, chunk);

            ByteString byteString = ByteString.copyFrom(messageInBytes, chunk * MAX_BUFFER_SIZE_CONFIGURED, bufferSize);
            RpcMessageProto rpcMessage = builder.setCurrentChunkNumber(chunk)
                    .setRpcContent(byteString)
                    .setCurrentChunkNumber(chunk)
                    .setTotalChunks(totalChunks)
                    .build();
            sendRpcResponseToKafka(rpcMessage, GrpcServerConstants.getResponseTopic());
        }

    }

    private void sendRpcResponseToKafka(RpcMessageProto rpcMessage, String topic) {
        String rpcId = rpcMessage.getRpcId();
        final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
                topic, rpcMessage.getRpcId(), rpcMessage.toByteArray());
        producer.send(producerRecord, (recordMetadata, e) -> {
            if (e != null) {
                LOG.error(" RPC response {} with id {} couldn't be sent to Kafka", rpcMessage, rpcId, e);
            }
        });
    }

    private synchronized StreamObserver<RpcRequestProto> getRpcHandler(String location, String systemId) {

        if (!Strings.isNullOrEmpty(systemId)) {
            return rpcHandlerByMinionId.get(systemId);
        }
        Collection<StreamObserver<RpcRequestProto>> streamObservers = rpcHandlerByLocation.get(location);
        if (streamObservers.isEmpty()) {
            return null;
        }
        Iterator<StreamObserver<RpcRequestProto>> iterator = rpcHandlerIteratorMap.get(streamObservers);
        if (iterator == null) {
            iterator = Iterables.cycle(streamObservers).iterator();
            rpcHandlerIteratorMap.put(streamObservers, iterator);
        }
        return iterator.next();
    }

    private synchronized void addRpcHandler(String location, String systemId, StreamObserver<RpcRequestProto> rpcHandler) {
        if (Strings.isNullOrEmpty(location) || Strings.isNullOrEmpty(systemId)) {
            LOG.error("Invalid metadata received with location = {} , systemId = {}", location, systemId);
            return;
        }
        if (!rpcHandlerByLocation.containsValue(rpcHandler)) {
            StreamObserver<RpcRequestProto> obsoleteObserver = rpcHandlerByMinionId.get(systemId);
            if (obsoleteObserver != null) {
                rpcHandlerByLocation.values().remove(obsoleteObserver);
            }
            rpcHandlerByLocation.put(location, rpcHandler);
            rpcHandlerByMinionId.put(systemId, rpcHandler);
            LOG.info("Added RPC handler for minion {} at location {}", systemId, location);
        }
    }


    private boolean isHeaders(RpcResponseProto rpcMessage) {
        return !Strings.isNullOrEmpty(rpcMessage.getSystemId()) &&
                rpcMessage.getRpcId().equals(rpcMessage.getSystemId());
    }


    private boolean sendRequest(String location, RpcRequestProto requestProto) {
        StreamObserver<RpcRequestProto> rpcHandler = getRpcHandler(location, requestProto.getSystemId());
        if (rpcHandler == null) {
            LOG.warn("No RPC handlers found for location {}", requestProto.getLocation());
            return false;
        }
        try {
            sendRpcRequest(rpcHandler, requestProto);
            return true;
        } catch (Throwable e) {
            LOG.error("Encountered exception while sending request {}", requestProto, e);
        }
        return false;
    }

    /**
     * Writing message through stream observer is not thread safe.
     */
    private synchronized void sendRpcRequest(StreamObserver<RpcRequestProto> rpcHandler, RpcRequestProto rpcMessage) {
        rpcHandler.onNext(rpcMessage);
    }

    private class KafkaConsumerRunner implements Runnable {

        private final KafkaConsumer<String, byte[]> consumer;
        private final String location;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private KafkaConsumerRunner(KafkaConsumer<String, byte[]> consumer, String location) {
            this.consumer = consumer;
            this.location = location;
        }

        @Override
        public void run() {

            // This assumes OpenNMS is running with single-topic per location.
            String requestTopic = GrpcServerConstants.getRequestTopicAtLocation(location);
            consumer.subscribe(Arrays.asList(requestTopic));
            LOG.info("subscribed to topic {}", requestTopic);
            while (!closed.get()) {
                try {

                    ConsumerRecords<String, byte[]> records = consumer.poll(java.time.Duration.ofMillis(Long.MAX_VALUE));
                    for (ConsumerRecord<String, byte[]> record : records) {

                        RpcMessageProto rpcMessage = RpcMessageProto.parseFrom(record.value());
                        ByteString rpcContent = rpcMessage.getRpcContent();
                        String rpcId = rpcMessage.getRpcId();
                        rpcIdQueue.offer(new RpcId(rpcId, rpcMessage.getExpirationTime()));
                        // For larger messages which get split into multiple chunks, cache them until all of them arrive.
                        if (rpcMessage.getTotalChunks() > 1) {
                            // Handle multiple chunks
                            boolean allChunksReceived = handleChunks(rpcMessage);
                            if (!allChunksReceived) {
                                continue;
                            }
                            rpcContent = messageCache.get(rpcId);
                            //Remove rpcId from cache.
                            messageCache.remove(rpcId);
                            currentChunkCache.remove(rpcId);
                        }
                        RpcRequestProto rpcRequestProto = transformToGrpcProto(rpcMessage, rpcContent);
                        sendRequest(location, rpcRequestProto);
                    }

                } catch (WakeupException e) {
                    LOG.info("consumer got wakeup exception, closed = {} ", closed.get(), e);
                } catch (Throwable e) {
                    LOG.error("Unexpected error in kafka consumer.", e);
                }
            }
        }

        private RpcRequestProto transformToGrpcProto(RpcMessageProto rpcMessageProto, ByteString rpcContent) {
            RpcRequestProto.Builder builder = RpcRequestProto.newBuilder()
                    .setRpcId(rpcMessageProto.getRpcId())
                    .setModuleId(rpcMessageProto.getModuleId())
                    .setExpirationTime(rpcMessageProto.getExpirationTime())
                    .setRpcContent(rpcContent)
                    .setLocation(location);

            if (!Strings.isNullOrEmpty(rpcMessageProto.getSystemId())) {
                builder.setSystemId(rpcMessageProto.getSystemId());
            }
            rpcMessageProto.getTracingInfoMap().forEach(builder::putTracingInfo);
            return builder.build();
        }

        private boolean handleChunks(RpcMessageProto rpcMessage) {
            // Avoid duplicate chunks. discard if chunk is repeated.
            String rpcId = rpcMessage.getRpcId();
            currentChunkCache.putIfAbsent(rpcId, 0);
            Integer chunkNumber = currentChunkCache.get(rpcId);
            if (chunkNumber != rpcMessage.getCurrentChunkNumber()) {
                LOG.debug("Expected chunk = {} but got chunk = {}, ignoring.", chunkNumber, rpcMessage.getCurrentChunkNumber());
                return false;
            }
            ByteString byteString = messageCache.get(rpcId);
            if (byteString != null) {
                messageCache.put(rpcId, byteString.concat(rpcMessage.getRpcContent()));
            } else {
                messageCache.put(rpcId, rpcMessage.getRpcContent());
            }
            currentChunkCache.put(rpcId, ++chunkNumber);

            return rpcMessage.getTotalChunks() == chunkNumber;
        }

        public void stop() {
            closed.set(true);
            consumer.wakeup();
        }

    }

    /**
     * RpcId is used to remove rpcId from DelayQueue after it reaches expirationTime.
     */
    private class RpcId implements Delayed {

        private final long expirationTime;

        private final String rpcId;

        public RpcId(String rpcId, long expirationTime) {
            this.rpcId = rpcId;
            this.expirationTime = expirationTime;
        }

        @Override
        public int compareTo(Delayed other) {
            long myDelay = getDelay(TimeUnit.MILLISECONDS);
            long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(myDelay, otherDelay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long now = System.currentTimeMillis();
            return unit.convert(expirationTime - now, TimeUnit.MILLISECONDS);
        }

        public String getRpcId() {
            return rpcId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RpcId that = (RpcId) o;
            return expirationTime == that.expirationTime &&
                    Objects.equals(rpcId, that.rpcId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(expirationTime, rpcId);
        }
    }
}
