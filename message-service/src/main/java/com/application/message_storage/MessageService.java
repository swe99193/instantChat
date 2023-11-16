package com.application.message_storage;

// aws sdk v1
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
//import com.amazonaws.services.dynamodbv2.model.AttributeValue;

// aws sdk v2

import ConversationServiceLib.*;
import MessageServiceGrpcLib.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
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

@Slf4j
@GrpcService // also include @Service
public class MessageService extends MessageServiceGrpc.MessageServiceImplBase {
    @GrpcClient("grpc-server-conversation")
    private ConversationServiceGrpc.ConversationServiceBlockingStub conversationService;

    private final DynamoDbClient client;
    private final S3Presigner s3Presigner;

    @Value("${amazon.s3.bucketname}")
    private String bucketName;

    private static final String tableName = "message";

    @Autowired
    public MessageService(DynamoDbClient client, S3Presigner s3Presigner) {
        this.client = client;
        this.s3Presigner = s3Presigner;
    }

    private String getConversationId(String sender, String receiver){
        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        return response.getConversationId();
    }

    /**
     * Save a message.
     */
    public void saveMessage(String sender, String receiver, String content, String contentType, Long fileSize, Long timestamp){
        String conversationId = getConversationId(sender, receiver);

        Message message = new Message(conversationId, timestamp, sender, receiver, contentType, content, fileSize);

        // save to dynamodb
        saveMessage(message);
    }

    @Override
    public void saveMessage(SaveMessageRequest request, StreamObserver<SaveMessageResponse> responseObserver){
        String conversationId = request.getConversationId();
        Long timestamp = request.getTimestamp();
        String sender = request.getSender();
        String receiver = request.getReceiver();
        String contentType = request.getContentType();
        String content = request.getContent();
        Long fileSize = request.getFileSize();

        log.info("🟢 gRPC message service saveMessage: {} -> {} ({})", sender, receiver, conversationId);

        Message message = new Message(conversationId, timestamp, sender, receiver, contentType, content, fileSize);

        // save to dynamodb
        saveMessage(message);

        SaveMessageResponse response = SaveMessageResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Save a message to DynamoDB
     */
    private void saveMessage(Message message) {
        HashMap<String,AttributeValue> itemValues = new HashMap<>();
        itemValues.put("conversation_id", AttributeValue.builder().s(message.conversationId).build());
        itemValues.put("timestamp", AttributeValue.builder().n(message.timestamp.toString()).build());
        itemValues.put("sender", AttributeValue.builder().s(message.sender).build());
        itemValues.put("receiver", AttributeValue.builder().s(message.receiver).build());
        itemValues.put("content_type", AttributeValue.builder().s(message.contentType).build());
        itemValues.put("content", AttributeValue.builder().s(message.content).build());

        if(message.fileSize != null)
            itemValues.put("file_size", AttributeValue.builder().n(message.fileSize.toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        PutItemResponse response = client.putItem(request);
    }

    /**
     * List messages (pagination).
     */
    public List<Message> listMessage(String sender, String receiver, Long timestamp, Integer pageSize){
        String conversationId = getConversationId(sender, receiver);

        return listMessage(conversationId, timestamp, pageSize);
    }

    /**
     * Query DynamoDB based on the partition key "conversation_id" and sort key "timestamp".
     *
     * Pagination by timestamp.
     */
    private List<Message> listMessage(String conversationId, Long timestamp, Integer pageSize){
        // Set up an alias for the partition key name in case it's a reserved word.
        HashMap<String,String> attrNameAlias = new HashMap<String,String>();
        attrNameAlias.put("#T", "timestamp");


        HashMap<String, AttributeValue> attrValues = new HashMap<>();
        attrValues.put(":conversation_id", AttributeValue.builder().s(conversationId).build());
        attrValues.put(":timestamp", AttributeValue.builder().n(timestamp.toString()).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression("conversation_id = :conversation_id AND #T < :timestamp")
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
                    item.get("conversation_id").s(),
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

    /**
     * Get temporary file download url.
     */
    public String getPresignedUrl(String objectName, String receiver, String sender) {
        String conversationId = getConversationId(sender, receiver);

        // check if file belongs to current conversation by filename
        if(!conversationId.equals(objectName.split("/")[1]))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return getPresignedUrl(objectName);
    }

    /**
     * Get S3 temporary download url (Get Object).
     */
    // ref: https://docs.aws.amazon.com/AmazonS3/latest/userguide/example_s3_Scenario_PresignedUrl_section.html
    private String getPresignedUrl(String objectName) {

        // get presigned url
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofSeconds(10))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
        String url = presignedGetObjectRequest.url().toString();
        log.info("✅ presign url (GetObject): " + url);
        return url;
    }


    /**
     * Update the latest message and timestamp of the conversation.
     */
    public void updateConversationLatestMessage(String sender, String receiver, String latestMessage, Long latestTimestamp){
        // gRPC
        UpdateLatestMessageRequest request = UpdateLatestMessageRequest.newBuilder().setSender(sender).setReceiver(receiver).setLatestMessage(latestMessage).setLatestTimestamp(latestTimestamp).build();
        UpdateLatestMessageResponse response = conversationService.updateLatestMessage(request);
    }
}
