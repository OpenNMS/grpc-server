FROM maven:3.6-jdk-11 AS builder
WORKDIR /app
ENV GRPC_HEALTH_PROBE_VERSION=v0.3.2
RUN wget -qOgrpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    git clone https://github.com/OpenNMS/grpc-server.git && \
    cd grpc-server && \
    mvn package

FROM openjdk:11.0-jre-slim
COPY --from=builder /app/grpc-server/target/grpc-ipc-server.jar /
COPY --from=builder /app/grpc_health_probe /bin
COPY docker-entrypoint.sh /
RUN useradd grpc && chmod +x /bin/grpc_health_probe
USER grpc
ENTRYPOINT [ "/docker-entrypoint.sh" ]
