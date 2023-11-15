package com.application.chat;

import com.application.config.KafkaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaService {
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public KafkaService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    /**
     * Listen for messages from Kafka, and route them to different websocket topics.
     */
    @KafkaListener(topics = KafkaConfig.MESSAGE_TOPIC)
    public void messageListener(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException {
        log.info("✅ message after Kafka: " + message);
        log.info("✅ topic: " + topic);
        log.info("✅ key: " + key);

        OutgoingMessage outgoingMessage = new ObjectMapper().readValue(message, OutgoingMessage.class);
        String sender = outgoingMessage.sender;
        String receiver = outgoingMessage.receiver;

        // send message to another user
        if(!sender.equals(receiver))
            messagingTemplate.convertAndSendToUser(receiver, "/queue/private." + sender, outgoingMessage);

        // echo message to yourself
        messagingTemplate.convertAndSendToUser(sender, "/queue/private.echo." + receiver, outgoingMessage);

        // not working
//		messagingTemplate.convertAndSendToUser(receiver + "/" + principal.getName(), "/queue/private", greetingMessage);
//		messagingTemplate.convertAndSendToUser(receiver, principal.getName() + "/queue/private", greetingMessage);
    }

    /**
     * Listen for new messages from Kafka, and route them to different websocket topics.
     */
    @KafkaListener(topics = KafkaConfig.NEW_MESSAGE_TOPIC)
    public void newMessageEventListener(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException {
        log.info("✅ new message event from Kafka: " + message);
        log.info("✅ topic: " + topic);
        log.info("✅ key: " + key);

        NewMessageEvent newMessageEvent = new ObjectMapper().readValue(message, NewMessageEvent.class);
        String sender = newMessageEvent.sender;
        String receiver = newMessageEvent.receiver;

        // send message to another user
        if(!sender.equals(receiver))
            messagingTemplate.convertAndSendToUser(receiver, "/queue/global.newmessage", newMessageEvent);

        // echo message to yourself
        messagingTemplate.convertAndSendToUser(sender, "/queue/global.newmessage", newMessageEvent);
    }

    /**
     * Listen for read event from Kafka, and route them to different websocket topics.
     */
    @KafkaListener(topics = KafkaConfig.READ_TOPIC)
    public void readEventListener(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, @Header(KafkaHeaders.RECEIVED_KEY) String key) throws JsonProcessingException {
        log.info("✅ read event from Kafka: " + message);
        log.info("✅ topic: " + topic);
        log.info("✅ key: " + key);

        ReadEvent event = new ObjectMapper().readValue(message, ReadEvent.class);
        String sender = event.sender;
        String receiver = event.receiver;

        // send message to another user
        if(!sender.equals(receiver))
            messagingTemplate.convertAndSendToUser(receiver, "/queue/global.read", event);

        // echo message to yourself
        messagingTemplate.convertAndSendToUser(sender, "/queue/global.read", event);
    }
}
