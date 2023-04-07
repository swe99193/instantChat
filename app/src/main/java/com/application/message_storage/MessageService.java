package com.application.message_storage;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Dynamodb sdk ref:
// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb-items.html
// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.QueryScanExample.html
@Service
public class MessageService {

    @Autowired
    private AmazonDynamoDB client;

    public void saveMessage(Message message) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(message);
    }

    /**
    * query the database based on the partition key "channel id"
    * */
    public void listMessage(String channel_id){
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(channel_id));
        DynamoDBQueryExpression<Message> queryExpression = new DynamoDBQueryExpression<Message>().withKeyConditionExpression("channel_id = :val1")
                .withExpressionAttributeValues(eav);

        // Note: mapper.query with pagination
        List<Message> messageList = mapper.query(Message.class, queryExpression);
        for (Message message: messageList) {
            System.out.printf("channel_id: %s, timestamp: %d, sender: %s, receiver: %s, contentType: %s, content: %s\n"
                    , message.getChannel_id(), message.getTimestamp(), message.getSender(), message.getReceiver(), message.getContentType(), message.getContent());
        }
    }
}
