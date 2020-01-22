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

import java.io.IOException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcServerRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcIpcServer.class);

    public static void main(String[] args) {
        String bootstrapServers = args.length > 0 ? args[0] : "localhost:9092";
        System.setProperty(String.format("%s%s", GrpcServerConstants.KAFKA_CONSUMER_SYS_PROP_PREFIX,
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), bootstrapServers);
        System.setProperty(String.format("%s%s", GrpcServerConstants.KAFKA_PRODUCER_SYS_PROP_PREFIX,
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), bootstrapServers);
        GrpcIpcServer grpcIpcServer = new GrpcIpcServer();
        // Add shutdown hook.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("shutting down gRPC server");
                grpcIpcServer.stop();
                System.err.println("gRPC server shut down");
            }
        });
        try {
            grpcIpcServer.start();
            LOG.info("gRPC server started");
        } catch (IOException e) {
            LOG.error("Encountered exception while starting gRPC server", e);
        }
        try {
            grpcIpcServer.blockUntilShutdown();
        } catch (InterruptedException e) {
            LOG.error("gRPC server got interrupted", e);
        }

    }
}
