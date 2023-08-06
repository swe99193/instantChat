package com.application.channel_mapping;

import ChannelMappingServiceLib.ChannelMappingServiceGrpc;
import ChannelMappingServiceLib.findChannelIdRequest;
import ChannelMappingServiceLib.findChannelIdResponse;
import ConversationServiceLib.ConversationServiceGrpc.ConversationServiceBlockingStub;
import ConversationServiceLib.saveConversationRequest;
import ConversationServiceLib.saveConversationResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

@GrpcService // also include @Service
public class ChannelMappingService extends ChannelMappingServiceGrpc.ChannelMappingServiceImplBase {

    private final ChannelMappingRepository channelMappingRepository;

    @GrpcClient("grpc-server-conversation")
    private ConversationServiceBlockingStub conversationListService;

    @Autowired
    public ChannelMappingService(ChannelMappingRepository channelMappingRepository) {
        this.channelMappingRepository = channelMappingRepository;
    }

    /**
     * Get channel id given two users.
     * <p>
     * If channel id not found, create a new one, and create a new conversation record.
    */
    public String findChannelId(String user1, String user2){
        // swap to ensure user1 < user2
        if (user1.compareTo(user2) > 0){
            String tmp = user1;
            user1 = user2;
            user2 = tmp;
        }

        String channel_id = channelMappingRepository.findChannelIdByUsers(user1, user2);

        if (channel_id == null){
            // create a new channel mapping
            ChannelMapping channelMapping = new ChannelMapping(user1, user2);
            channelMappingRepository.save(channelMapping);
            channel_id = channelMappingRepository.findChannelIdByUsers(user1, user2);

            // create an entry for each user
            saveConversationGrpc(user1, user2);
            saveConversationGrpc(user2, user1);
        }

        return channel_id;
    }

    /**
     * call conversation gRPC, save conversation
     *
     * @param user1
     * @param user2
     */
    public void saveConversationGrpc(String user1, String user2){
        saveConversationRequest req = saveConversationRequest.newBuilder().setUsername(user1).setChatUser(user2).build();
        saveConversationResponse res = conversationListService.saveConversation(req);
    }

    /**
     * gRPC method, wrap internal function {@link ChannelMappingService#findChannelId(String, String) findChannelId}
     */
    public void findChannelId(findChannelIdRequest req, StreamObserver<findChannelIdResponse> responseObserver){
        System.out.printf("🟢 gRPC channel-mapping service: findChannelId   ---  %s\n", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()));

        String user1 = req.getUser1();
        String user2 = req.getUser2();

        String channel_id = findChannelId(user1, user2);

        findChannelIdResponse res = findChannelIdResponse.newBuilder().setChannelId(channel_id).build();
        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }
}
