package com.application.message_storage;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "message")
public class Message {
    private String channelId;
    private Long timestamp;
    private String sender;
    private String receiver;
    private String contentType;
    private String content;
    private Long fileSize;

    public Message() {
    }

    public Message(String channelId, Long timestamp, String sender, String receiver, String contentType, String content, Long fileSize) {
        this.channelId = channelId;
        this.timestamp = timestamp;
        this.sender = sender;
        this.receiver = receiver;
        this.contentType = contentType;
        this.content = content;
        this.fileSize = fileSize;
    }

    @DynamoDBHashKey(attributeName="channel_id")
    public String getChannelId() {
        return channelId;
    }
    public void setChannelId(String channelId) {
        this.channelId = channelId;
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

    @DynamoDBAttribute(attributeName="content_type")
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

    @DynamoDBAttribute(attributeName="file_size")
    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}