package com.application.chat;

import ChannelMappingServiceLib.ChannelMappingServiceGrpc.ChannelMappingServiceBlockingStub;
import ChannelMappingServiceLib.findChannelIdRequest;
import ChannelMappingServiceLib.findChannelIdResponse;
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

    @GrpcClient("grpc-server-channel-mapping")
    private ChannelMappingServiceBlockingStub channelMappingService;

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
//        String channelId = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channelId
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();

        Message message = new Message(channelId, timestamp, sender, receiver, contentType, content, fileSize);

        // save to dynamodb
        messageService.saveMessage(message);
    }

    public List<Message> listMessage(String sender, String receiver, Long timestamp, Integer pageSize){

//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();

        List<Message> messageList = messageService.listMessage(channelId, timestamp, pageSize);

        return messageList;
    }

    /**
     * upload to S3
     * <p>
     * filename format: file-message/(channel id)/(random UUID)_(original filename)
     */
    public String saveFile(String sender, String receiver, MultipartFile file) throws IOException {
//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();


        //  filename format: file-message/<channel id>/<random UUID>_<original filename>
        String objectName = String.format("file-message/%s/%s_%s", channelId, UUID.randomUUID().toString(), file.getOriginalFilename());


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

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();


        // check if file belongs to current channel by filename
        if(!channelId.equals(objectName.split("/")[1]))
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

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();


        // check if file belongs to current channel by filename
        if(!channelId.equals(objectName.split("/")[1]))
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

}
