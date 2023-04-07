package com.application.message_storage;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    @Autowired
    private AmazonDynamoDB client;

    public void saveMessage(Message message){
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(message);
    }
}
