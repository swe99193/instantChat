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

    private final AmazonDynamoDB client;

    @Autowired
    public MessageService(AmazonDynamoDB client) {
        this.client = client;
    }

    public void saveMessage(Message message) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(message);
    }

    /**
    * query the database based on the partition key "channel id"
    * */
    public List<Message> listMessage(String channel_id){
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(channel_id));
        DynamoDBQueryExpression<Message> queryExpression = new DynamoDBQueryExpression<Message>().withKeyConditionExpression("channel_id = :val1")
                .withExpressionAttributeValues(eav);

        // Note: mapper.query has support for pagination
        return mapper.query(Message.class, queryExpression);
    }
}
