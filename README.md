# GRPC-IPC-Server

This GRPC IPC Server acts as a bridge between minions running gRPC IPC strategy and OpenNMS running Kafka strategy. 


## compile:
```
mvn package
```

## prerequisites
Minions running GRPC strategy.
OpenNMS running Kafka strategy with single topic.


## Start the server

```
java -jar grpc-ipc-server.jar
```

## Start the server with kafka hostname

```
java -jar target/grpc-ipc-server.jar localhost:9092
```