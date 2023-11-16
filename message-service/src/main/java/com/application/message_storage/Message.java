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
    public String conversationId;
    public Long timestamp;
    public String sender;
    public String receiver;
    public String contentType;
    public String content;
    public Long fileSize;
}