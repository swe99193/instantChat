package com.application.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String MESSAGE_TOPIC = "message";
    public static final String NEW_MESSAGE_TOPIC = "newmessage";

    @Bean
    public NewTopic messageTopic() {
        int partitions = 1;
        short replicationFactor = 1;

        return new NewTopic(MESSAGE_TOPIC, partitions, replicationFactor);
    }
}
