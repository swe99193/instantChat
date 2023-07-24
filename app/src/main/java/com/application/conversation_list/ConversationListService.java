package com.application.conversation_list;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationListService {
    private final AmazonDynamoDB client;

    @Autowired
    public ConversationListService(AmazonDynamoDB client) {
        this.client = client;
    }

    public void saveConversation(ConversationUser conversationUser) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(conversationUser);
    }

    /**
     * query the database based on the partition key "username"
     * */
    public List<ConversationUser> listConversationUser(String username){
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(username));
        DynamoDBQueryExpression<ConversationUser> queryExpression = new DynamoDBQueryExpression<ConversationUser>().withKeyConditionExpression("username = :val1")
                .withExpressionAttributeValues(eav);

        return mapper.query(ConversationUser.class, queryExpression);
    }
}
