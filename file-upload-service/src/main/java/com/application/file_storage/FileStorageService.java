package com.application.file_storage;

// aws sdk v1
//import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
//import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
//import com.amazonaws.services.dynamodbv2.model.AttributeValue;

// aws sdk v2

import ConversationServiceLib.*;
import MessageServiceGrpcLib.MessageServiceGrpc;
import MessageServiceGrpcLib.SaveMessageRequest;
import MessageServiceGrpcLib.SaveMessageResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


// Dynamodb sdk v2:
// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-items.html
// https://github.com/awsdocs/aws-doc-sdk-examples/tree/main/javav2/example_code/dynamodb

// sdk v1
// https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-dynamodb-items.html
// https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBMapper.QueryScanExample.html

@Slf4j
@Service
public class FileStorageService{
    @GrpcClient("grpc-server-conversation")
    private ConversationServiceGrpc.ConversationServiceBlockingStub conversationService;

    @GrpcClient("grpc-server-message")
    private MessageServiceGrpc.MessageServiceBlockingStub messageService;

    private final S3Client s3Client;
    @Value("${amazon.s3.bucketname}")
    private String bucketName;

    @Autowired
    public FileStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private String getConversationId(String sender, String receiver){
        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        return response.getConversationId();
    }

    /**
     * Upload a file.
     */
    public String saveFile(String sender, String receiver, MultipartFile file) throws IOException {
        String conversationId = getConversationId(sender, receiver);

        return saveFile(conversationId, file);
    }

    /**
     * upload to S3
     * <p>
     * filename format: file-message/(conversation id)/(random UUID)_(original filename)
     */
    private String saveFile(String conversationId, MultipartFile file) throws IOException {

        //  filename format: file-message/<conversation id>/<random UUID>_<original filename>
        String objectName = String.format("file-message/%s/%s_%s", conversationId, UUID.randomUUID().toString(), file.getOriginalFilename());


        // store to S3
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        InputStream inputStream = file.getInputStream();
        s3Client.putObject(objectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        inputStream.close();

        return objectName;
    }

    /**
     * Download a file.
     */
    public byte[] getFile(String objectName, String receiver, String sender) throws Exception {
        String conversationId = getConversationId(sender, receiver);

        // check if file belongs to current conversation by filename
        if (!conversationId.equals(objectName.split("/")[1]))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return getFile(objectName);
    }

    /**
     * Download file from s3
     */
    private byte[] getFile(String objectName) throws Exception {
        //  download from S3
        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);

        return objectBytes.asByteArray();
    }


    /**
     * Update the latest message and timestamp of the conversation.
     */
    public void updateConversationLatestMessage(String sender, String receiver, String latestMessage, Long latestTimestamp){
        // gRPC
        UpdateLatestMessageRequest request = UpdateLatestMessageRequest.newBuilder().setSender(sender).setReceiver(receiver).setLatestMessage(latestMessage).setLatestTimestamp(latestTimestamp).build();
        UpdateLatestMessageResponse response = conversationService.updateLatestMessage(request);
    }

    /**
     * Save a message.
     */
    public void saveMessage(String sender, String receiver, String objectName, String contentType, Long fileSize, Long timestamp) {
        String conversationId = getConversationId(sender, receiver);

        // gRPC, save message
        SaveMessageRequest request = SaveMessageRequest.newBuilder().setConversationId(conversationId).setTimestamp(timestamp).setSender(sender).setReceiver(receiver).setContentType(contentType).setContent(objectName).setFileSize(fileSize).build();
        SaveMessageResponse response = messageService.saveMessage(request);
    }
}
