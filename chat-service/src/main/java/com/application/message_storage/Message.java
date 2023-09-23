package com.application.message_storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {
    private String channelId;
    private Long timestamp;
    private String sender;
    private String receiver;
    private String contentType;
    private String content;
    private Long fileSize;
}