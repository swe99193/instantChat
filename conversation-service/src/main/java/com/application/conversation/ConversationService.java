package com.application.conversation;

import ConversationServiceLib.ConversationServiceGrpc;
import ConversationServiceLib.saveConversationRequest;
import ConversationServiceLib.saveConversationResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.text.SimpleDateFormat;
import java.util.*;

//@Service
@GrpcService
public class ConversationService extends ConversationServiceGrpc.ConversationServiceImplBase {

    private static final String tableName = "conversation";

    private final DynamoDbClient client;

    @Autowired
    public ConversationService(DynamoDbClient client) {
        this.client = client;
    }

    public void saveConversation(Conversation conversation) {
        HashMap<String, AttributeValue> itemValues = new HashMap<>();
        itemValues.put("username", AttributeValue.builder().s(conversation.getUsername()).build());
        itemValues.put("chatUser", AttributeValue.builder().s(conversation.getChatUser()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        PutItemResponse response = client.putItem(request);
    }

    /**
     * gRPC method, wrap internal function {@link ConversationService#saveConversation(Conversation) saveConversation}
     */
    @Override
    public void saveConversation(saveConversationRequest req, StreamObserver<saveConversationResponse> responseObserver) {
        System.out.printf("🟢 gRPC conversation-service: saveConversation   ---  %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

        saveConversation(new Conversation(req.getUsername(), req.getChatUser()));

        saveConversationResponse res = saveConversationResponse.newBuilder().build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    /**
     * query the database based on the partition key "username"
     * */
    public List<Conversation> listConversation(String username){
        System.out.printf("🟢 conversation-service: listConversation   ---  %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

        // Set up an alias for the partition key name in case it's a reserved word.
        HashMap<String,String> attrNameAlias = new HashMap<>();
        attrNameAlias.put("#U", "username");


        HashMap<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":username", AttributeValue.builder().s(username).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("#U = :username")
                .expressionAttributeNames(attrNameAlias)
                .expressionAttributeValues(attrValues)
                .build();

        QueryResponse response = client.query(request);


        // convert response items into list of Message
        List<Conversation> conversations = new ArrayList<>();
        for(Map<String, AttributeValue> item: response.items()){

            Conversation message = new Conversation(
                    item.get("username").s(),
                    item.get("chatUser").s()
            );
            conversations.add(message);
        }

        return conversations;
    }
}
