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

import java.util.Map;
import java.util.Properties;

public interface GrpcServerConstants {

    String TOPIC_NAME_AT_LOCATION = "%s.%s.%s";
    String TOPIC_NAME_WITHOUT_LOCATION = "%s.%s";
    String SINK_TOPIC_NAME_WITHOUT_LOCATION = "%s.%s.%s";
    String SINK_MODULE_PREFIX = "Sink";
    String OPENNMS_INSTANCE_ID_SYS_PROP = "org.opennms.instance.id";
    String DEFAULT_INSTANCE_ID = "OpenNMS";
    String KAFKA_PRODUCER_SYS_PROP_PREFIX = "org.opennms.core.ipc.grpc.kafa.producer.";
    String KAFKA_CONSUMER_SYS_PROP_PREFIX = "org.opennms.core.ipc.grpc.kafa.consumer.";
    String GRPC_SERVER_PID = "org.opennms.core.ipc.grpc.server";
    String GRPC_MAX_INBOUND_SIZE = "max.message.size";
    int DEFAULT_MESSAGE_SIZE = 10485760; //10MB
    String DEFAULT_MESSAGE_SIZE_STRING = "10485760"; //10MB
    int DEFAULT_GRPC_PORT = 8990;
    String DEFAULT_GRPC_PORT_STRING = "8990";
    String GRPC_SERVER_PORT = "port";
    String TLS_ENABLED = "tls.enabled";
    String SERVER_CERTIFICATE_FILE_PATH = "server.cert.filepath";
    String CLIENT_CERTIFICATE_FILE_PATH = "client.cert.filepath";
    String PRIVATE_KEY_FILE_PATH = "server.private.key.filepath";
    String RPC_REQUEST_TOPIC_NAME = "rpc-request";
    String RPC_RESPONSE_TOPIC_NAME = "rpc-response";
    //By default, kafka allows 1MB buffer sizes, here rpcContent is limited to 900KB to allow space for other
    // parameters in proto file.
    int MAX_BUFFER_SIZE_CONFIGURED = 921600;

    static Properties getProperties(String sysPropPrefix) {
        final Properties config = new Properties();
        // Find all of the system properties that start with provided prefix (kafkaSysPropPrefix)
        // and add them to the config.
        // See https://kafka.apache.org/0100/documentation.html#newconsumerconfigs for the list of supported properties
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            final Object keyAsObject = entry.getKey();
            if (keyAsObject == null || !(keyAsObject instanceof String)) {
                continue;
            }
            final String key = (String) keyAsObject;
            if (key.length() > sysPropPrefix.length()
                    && key.startsWith(sysPropPrefix)) {
                final String kafkaConfigKey = key.substring(sysPropPrefix.length());
                config.put(kafkaConfigKey, entry.getValue());
            }
        }
        return config;
    }

    // Calculate remaining buffer size for each chunk.
    static int getBufferSize(int messageSize, int maxBufferSize, int chunk) {
        int bufferSize = messageSize;
        if (messageSize > maxBufferSize) {
            int remaining = messageSize - chunk * maxBufferSize;
            bufferSize = (remaining > maxBufferSize) ? maxBufferSize : remaining;
        }
        return bufferSize;
    }

    static String getOpenNMSInstanceId() {
        return System.getProperty(OPENNMS_INSTANCE_ID_SYS_PROP, DEFAULT_INSTANCE_ID);
    }

    static String getRequestTopicAtLocation(String location) {

        return String.format(TOPIC_NAME_AT_LOCATION, getOpenNMSInstanceId(), location, RPC_REQUEST_TOPIC_NAME);
    }

    static String getResponseTopic() {

        return String.format(TOPIC_NAME_WITHOUT_LOCATION, getOpenNMSInstanceId(), RPC_RESPONSE_TOPIC_NAME);
    }


    static String getSinkTopic(String module) {
        return String.format(SINK_TOPIC_NAME_WITHOUT_LOCATION, getOpenNMSInstanceId(), SINK_MODULE_PREFIX, module);
    }
}
