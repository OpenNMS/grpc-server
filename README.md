# GRPC-IPC-Server

This GRPC IPC Server acts as a bridge between minions running gRPC IPC strategy and OpenNMS running Kafka strategy. 


## Compile:

Make sure to have OpenJDK 11 and Maven 3 installed on your system.
```
mvn package
```

## Compile with Docker:

```
docker build -t opennms/grpc-server .
```

## Prerequisites

* Minions running GRPC strategy.
* OpenNMS running Kafka strategy with single topic.

## Start the server

```
java -jar grpc-ipc-server.jar
```

## Start the server with kafka hostname

```
java -jar target/grpc-ipc-server.jar localhost:9092
```

A more comprehensive example:

```
java -jar target/grpc-ipc-server.jar \
  -Dorg.opennms.instance.id=Apex \
  -Dtls.enabled=true \
  -Dserver.private.key.filepath=/grpc/key.pem \
  -Dserver.cert.filepath=/grpc/cert.pem \
  -Dclient.cert.filepath=/grpc/client.pem \
  -Dport=8990 \
  -Dmax.message.size=10485760 \
  -Dorg.opennms.core.ipc.grpc.kafka.producer.acks=1 \
  kafka1.example.com:9092
```

## Start the server using Docker

```
docker run \
  -e INSTANCE_ID=Apex \
  -e TLS_ENABLED=true \
  -e SERVER_PRIVATE_KEY=/grpc/key.pem \
  -e SERVER_CERT=/grpc/cert.pem \
  -e CLIENT_CERT=/grpc/client.pem \
  -e PORT=8990 \
  -e MAX_MESSAGE_SIZE=10485760 \
  -e PRODUCER_ACKS=1 \
  -e BOOTSTRAP_SERVERS=kafka1.example.com:9092 \
  -v $(pwd)/grpc:/grpc
  opennms/grpc-server
```

