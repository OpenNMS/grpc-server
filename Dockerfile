FROM alpine AS downloader

ARG GRPC_HEALTH_PROBE_VERSION=v0.3.2

RUN apk add --no-cache wget
RUN wget -qOgrpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    chmod 755 grpc_health_probe

FROM openjdk:11.0-jre-slim

RUN useradd grpc

COPY --from=downloader /grpc_health_probe /bin
COPY target/grpc-ipc-server.jar /
COPY docker-entrypoint.sh /

USER grpc

ENTRYPOINT [ "/docker-entrypoint.sh" ]
