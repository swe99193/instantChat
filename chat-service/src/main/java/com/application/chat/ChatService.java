package com.application.chat;


import ConversationServiceLib.ConversationServiceGrpc.ConversationServiceBlockingStub;
import ConversationServiceLib.FindConversationIdRequest;
import ConversationServiceLib.FindConversationIdResponse;
import ConversationServiceLib.UpdateLatestMessageRequest;
import ConversationServiceLib.UpdateLatestMessageResponse;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
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
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ChatService {
    // FIXME: gRPC client cannot be Autowired

    @GrpcClient("grpc-server-conversation")
    private ConversationServiceBlockingStub conversationService;

    private final MessageService messageService;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;


    @Value("${amazon.s3.bucketname}")
    private String bucketName;


    @Autowired
    public ChatService(MessageService messageService, S3Client s3Client, S3Presigner s3Presigner) {
        this.messageService = messageService;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    /**
     * save to DynamoDB
     */
    public void saveMessage(String sender, String receiver, String content, String contentType, Long fileSize, Long timestamp){
        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        String conversationId = response.getConversationId();

        Message message = new Message(conversationId, timestamp, sender, receiver, contentType, content, fileSize);

        // save to dynamodb
        messageService.saveMessage(message);
    }

    public List<Message> listMessage(String sender, String receiver, Long timestamp, Integer pageSize){
        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        String conversationId = response.getConversationId();

        List<Message> messageList = messageService.listMessage(conversationId, timestamp, pageSize);

        return messageList;
    }

    /**
     * upload to S3
     * <p>
     * filename format: file-message/(conversation id)/(random UUID)_(original filename)
     */
    public String saveFile(String sender, String receiver, MultipartFile file) throws IOException {
        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        String conversationId = response.getConversationId();


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
     * download from s3
     */
    public byte[] getFile(String objectName, String receiver, String sender) throws Exception {

        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        String conversationId = response.getConversationId();


        // check if file belongs to current conversation by filename
        if(!conversationId.equals(objectName.split("/")[1]))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);


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
     * Get S3 temporary download url (Get Object).
     */
    // ref: https://docs.aws.amazon.com/AmazonS3/latest/userguide/example_s3_Scenario_PresignedUrl_section.html
    public String getPresignedUrl(String objectName, String receiver, String sender) {
        //

        // gRPC, get conversation id
        FindConversationIdRequest request = FindConversationIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        FindConversationIdResponse response = conversationService.findConversationId(request);
        String conversationId = response.getConversationId();


        // check if file belongs to current conversation by filename
        if(!conversationId.equals(objectName.split("/")[1]))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

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
