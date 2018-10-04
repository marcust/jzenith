package org.jzenith.kafka.consumer;

import org.jzenith.core.configuration.ConfigDefault;

import java.util.List;

public interface KafkaConsumerConfiguration {

    @ConfigDefault("localhost:9092")
    List<String> getBootstrapServers();

    @ConfigDefault("jzenith")
    String getGroupName();

}
