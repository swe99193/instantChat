package com.application.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    public static final String MESSAGE_TOPIC = "message";
    public static final String NEW_MESSAGE_TOPIC = "newmessage";
    public static final String READ_TOPIC = "read";

}
