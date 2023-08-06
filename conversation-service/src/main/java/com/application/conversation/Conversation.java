package com.application.conversation;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName = "conversation")
public class Conversation {
    private String username;
    private String chatUser;

    public Conversation() {
    }

    public Conversation(String username, String chatUser) {
        this.username = username;
        this.chatUser = chatUser;
    }

    @DynamoDBHashKey(attributeName="username")
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @DynamoDBRangeKey(attributeName="chatUser")
    public String getChatUser() {
        return chatUser;
    }
    public void setChatUser(String chatUser) {
        this.chatUser = chatUser;
    }
}