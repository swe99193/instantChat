package com.application.message_storage;

// aws sdk v1
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
//import com.amazonaws.services.dynamodbv2.model.AttributeValue;

// aws sdk v2

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// Dynamodb sdk v2:
// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-items.html
// https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/example_code/dynamodb

// sdk v1
// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb-items.html
// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.QueryScanExample.html

@Service
public class MessageService {

    private final DynamoDbClient client;
    private static final String tableName = "message";

    @Autowired
    public MessageService(DynamoDbClient client) {
        this.client = client;
    }

    /**
     * save a message to DynamoDB
     */
    public void saveMessage(Message message) {
        HashMap<String,AttributeValue> itemValues = new HashMap<>();
        itemValues.put("channel_id", AttributeValue.builder().s(message.getChannelId()).build());
        itemValues.put("timestamp", AttributeValue.builder().n(message.getTimestamp().toString()).build());
        itemValues.put("sender", AttributeValue.builder().s(message.getSender()).build());
        itemValues.put("receiver", AttributeValue.builder().s(message.getReceiver()).build());
        itemValues.put("content_type", AttributeValue.builder().s(message.getContentType()).build());
        itemValues.put("content", AttributeValue.builder().s(message.getContent()).build());

        if(message.getFileSize() != null)
            itemValues.put("file_size", AttributeValue.builder().n(message.getFileSize().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        PutItemResponse response = client.putItem(request);
    }

    /**
     * Query the database based on the partition key "channel id" and sort key "timestamp".
     *
     * Pagination by timestamp.
     */
    public List<Message> listMessage(String channelId, Long timestamp, Integer pageSize){
        // Set up an alias for the partition key name in case it's a reserved word.
        HashMap<String,String> attrNameAlias = new HashMap<String,String>();
        attrNameAlias.put("#T", "timestamp");


        HashMap<String, AttributeValue> attrValues = new HashMap<>();

        attrValues.put(":channel_id", AttributeValue.builder().s(channelId).build());
        attrValues.put(":timestamp", AttributeValue.builder().n(timestamp.toString()).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
//                .keyConditionExpression("channel_i    d = :channel_id")
                .keyConditionExpression("channel_id = :channel_id AND #T < :timestamp")
                .expressionAttributeNames(attrNameAlias)
                .expressionAttributeValues(attrValues)
                .scanIndexForward(false)
                .limit(pageSize)
                .build();

        QueryResponse response = client.query(request);
//        QueryIterable response = client.queryPaginator(request);  // pagination

//        Map<String, AttributeValue> lastEvaluatedKey = response.lastEvaluatedKey();

        // convert response items into list of Message
        List<Message> messageList = new ArrayList<>();
        for(Map<String, AttributeValue> item: response.items()){

            Message message = new Message(
                    item.get("channel_id").s(),
                    Long.valueOf(item.get("timestamp").n()),
                    item.get("sender").s(),
                    item.get("receiver").s(),
                    item.get("content_type").s(),
                    item.get("content").s(),
                    item.containsKey("file_size") ? Long.valueOf(item.get("file_size").n()): null
            );
            messageList.add(message);
        }

        return messageList;
    }
}
