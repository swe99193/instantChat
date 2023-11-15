package com.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String NEW_MESSAGE_TOPIC = "newmessage";
    public static final String READ_TOPIC = "read";

    @Bean
    public NewTopic newMessageTopic() {
        int partitions = 1;
        short replicationFactor = 1;

        return new NewTopic(NEW_MESSAGE_TOPIC, partitions, replicationFactor);
    }

    @Bean
    public NewTopic readTopic() {
        int partitions = 1;
        short replicationFactor = 1;

        return new NewTopic(READ_TOPIC, partitions, replicationFactor);
    }
}
