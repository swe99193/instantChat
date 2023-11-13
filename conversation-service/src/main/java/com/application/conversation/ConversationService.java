package com.application.conversation;

import ConversationServiceLib.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@GrpcService // also include @Service
public class ConversationService extends ConversationServiceGrpc.ConversationServiceImplBase {

    private final ConversationRepository conversationRepository;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    /**
     * Get conversation list.
     */
    public List<Conversation> listConversation(String username) {
        return conversationRepository.findByUser1OrUser2OrderByLatestTimestampDesc(username, username);
    }


    /**
     * Get conversation id given two users.
     * <p>
     * If conversation id not found, create a new one, and create a new conversation record.
    */
    private String findConversationId(String user1, String user2){
        // swap to ensure user1 < user2
        if (user1.compareTo(user2) > 0){
            String tmp = user1;
            user1 = user2;
            user2 = tmp;
        }

        String conversationId = conversationRepository.findConversationIdByUsers(user1, user2);

        if (conversationId == null){
            // create a new conversation
            Conversation conversation = new Conversation(user1, user2);
            conversation = conversationRepository.save(conversation);
            conversationId = conversation.id.toString();
        }

        return conversationId;
    }


    /**
     * gRPC method, wrap internal function {@link ConversationService#findConversationId(String, String) findConversationId}
     */
    @Override
    public void findConversationId(FindConversationIdRequest request, StreamObserver<FindConversationIdResponse> responseObserver){
        String user1 = request.getUser1();
        String user2 = request.getUser2();

        String conversationId = findConversationId(user1, user2);

        log.info("🟢 gRPC conversation service findConversationId: {}", conversationId);

        FindConversationIdResponse response = FindConversationIdResponse.newBuilder().setConversationId(conversationId).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * Update latest message and timestamp of the conversation.
     */
    @Transactional
    private void updateLatestMessage(String user1, String user2, String latestMessage, Long latestTimestamp){
        // swap to ensure user1 < user2
        if (user1.compareTo(user2) > 0){
            String tmp = user1;
            user1 = user2;
            user2 = tmp;
        }

        conversationRepository.updateLatestMessage(user1, user2, latestMessage, latestTimestamp);
    }

    /**
     * gRPC method, wrap internal function {@link ConversationService#updateLatestMessage(String, String, String, Long) updateLatestMessage}
     */
    @Transactional
    @Override
    public void updateLatestMessage(UpdateLatestMessageRequest request, StreamObserver<UpdateLatestMessageResponse> responseObserver){
        String user1 = request.getUser1();
        String user2 = request.getUser2();
        String latestMessage = request.getLatestMessage();
        Long latestTimestamp = request.getLatestTimestamp();

        updateLatestMessage(user1, user2, latestMessage, latestTimestamp);

        log.info("🟢 gRPC conversation service updateLatestMessage: {}, {}", user1, user2);

        UpdateLatestMessageResponse response = UpdateLatestMessageResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
