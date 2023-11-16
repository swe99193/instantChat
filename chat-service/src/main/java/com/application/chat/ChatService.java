package com.application.chat;


import ConversationServiceLib.ConversationServiceGrpc.ConversationServiceBlockingStub;
import ConversationServiceLib.UpdateLastReadRequest;
import ConversationServiceLib.UpdateLastReadResponse;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatService {
    // FIXME: gRPC client cannot be Autowired

    @GrpcClient("grpc-server-conversation")
    private ConversationServiceBlockingStub conversationService;


    @Autowired
    public ChatService() {
    }


    /**
     * Update the read timestamp (of a user) of the conversation.
     */
    public void updateConversationRead(String sender, String receiver, Long timestamp){
        // gRPC
        UpdateLastReadRequest request = UpdateLastReadRequest.newBuilder().setSender(sender).setReceiver(receiver).setTimestamp(timestamp).build();
        UpdateLastReadResponse response = conversationService.updateLastRead(request);
    }
}
