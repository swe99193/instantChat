package com.application.conversation_list;

import ConversationServiceLib.ConversationServiceGrpc;
import ConversationServiceLib.saveConversationRequest;
import ConversationServiceLib.saveConversationResponse;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@Service
@GrpcService
public class ConversationService extends ConversationServiceGrpc.ConversationServiceImplBase {
    private final AmazonDynamoDB client;

    @Autowired
    public ConversationService(AmazonDynamoDB client) {
        this.client = client;
    }

    public void saveConversation(Conversation conversation) {
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        mapper.save(conversation);
    }

    /**
     * gRPC method, wrap original service function
     */
    @Override
    public void saveConversation(saveConversationRequest req, StreamObserver<saveConversationResponse> responseObserver) {
        saveConversation(new Conversation(req.getUsername(), req.getChatUser()));

        saveConversationResponse res = saveConversationResponse.newBuilder().build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    /**
     * query the database based on the partition key "username"
     * */
    public List<Conversation> listConversation(String username){
        DynamoDBMapper mapper = new DynamoDBMapper(client);
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(username));
        DynamoDBQueryExpression<Conversation> queryExpression = new DynamoDBQueryExpression<Conversation>().withKeyConditionExpression("username = :val1")
                .withExpressionAttributeValues(eav);

        return mapper.query(Conversation.class, queryExpression);
    }
}
