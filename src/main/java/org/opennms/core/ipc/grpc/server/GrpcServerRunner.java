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

// Test
public class GrpcServerRunner {


    public static void main(String[] args) {
        System.setProperty(String.format("%s%s", GrpcServerConstants.KAFKA_CONSUMER_SYS_PROP_PREFIX,
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), "localhost:9092");
        System.setProperty(String.format("%s%s", GrpcServerConstants.KAFKA_PRODUCER_SYS_PROP_PREFIX,
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG), "localhost:9092");
        GrpcIpcServer grpcIpcServer = new GrpcIpcServer();
        try {
            grpcIpcServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
