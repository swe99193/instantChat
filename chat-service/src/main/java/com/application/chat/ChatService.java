package com.application.chat;

import ChannelMappingServiceLib.ChannelMappingServiceGrpc.ChannelMappingServiceBlockingStub;
import ChannelMappingServiceLib.findChannelIdRequest;
import ChannelMappingServiceLib.findChannelIdResponse;
import com.application.message_storage.Message;
import com.application.message_storage.MessageService;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {
    // FIXME: gRPC client cannot be Autowired

    @GrpcClient("grpc-server-channel-mapping")
    private ChannelMappingServiceBlockingStub channelMappingService;

    private final MessageService messageService;


    @Value("${amazon.s3.bucketname}")
    private String bucketName;


    @Autowired
    public ChatService(MessageService messageService) {
        this.messageService = messageService;
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

    public List<Message> listMessage(String sender, String receiver){

//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();

        List<Message> messageList = messageService.listMessage(channelId);

        return messageList;
    }

    /**
     * upload to S3
     */
    public String saveFile(String sender, String receiver, MultipartFile file) throws IOException {
//        String channel_id = channelMappingService.findChannelId(sender, receiver);

        // gRPC, get channel_id
        findChannelIdRequest _req = findChannelIdRequest.newBuilder().setUser1(sender).setUser2(receiver).build();
        findChannelIdResponse _res = channelMappingService.findChannelId(_req);
        String channelId = _res.getChannelId();

        // filename format: <channel id>_<random UUID>_<original filename>
        String objectName = String.format("%s_%s_%s", channelId, UUID.randomUUID().toString(), file.getOriginalFilename());

        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        InputStream inputStream = file.getInputStream();
        s3.putObject(objectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
        inputStream.close();

        s3.close();

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

        // check if file belongs to current channel by prefix of filename, i.e., channelId
        if(!channelId.equals(objectName.split("_")[0]))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        //  download from S3
        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        GetObjectRequest objectRequest = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);

        s3.close();

        return objectBytes.asByteArray();
    }


}
