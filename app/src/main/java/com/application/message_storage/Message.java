package com.application.message_storage;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

@DynamoDBTable(tableName = "message")
public class Message {
    private String channel_id;
    private Long timestamp;
    private String sender;
    private String receiver;
    private String contentType;
    private String content;

    public Message(String channel_id, Long timestamp, String sender, String receiver, String contentType, String content) {
        this.channel_id = channel_id;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.contentType = contentType;
        this.content = content;
    }

    @DynamoDBHashKey(attributeName="channel_id")
    public String getChannel_id() {
        return channel_id;
    }
    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    @DynamoDBRangeKey(attributeName="timestamp")
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @DynamoDBAttribute(attributeName="sender")
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }

    @DynamoDBAttribute(attributeName="receiver")
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @DynamoDBAttribute(attributeName="contentType")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @DynamoDBAttribute(attributeName="content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}