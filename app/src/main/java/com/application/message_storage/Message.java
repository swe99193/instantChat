package com.application.message_storage;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Message {
    private String channel_id;
    private Long timestamp;
    private String sender;
    private String receiver;
    private String contentType;
    private String content;

    public Message() {
    }

    public Message(String channel_id, Long timestamp, String sender, String receiver, String contentType, String content) {
        this.channel_id = channel_id;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.contentType = contentType;
        this.content = content;
    }
}