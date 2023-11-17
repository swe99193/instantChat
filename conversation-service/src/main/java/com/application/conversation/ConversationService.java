package com.application.conversation;

import ConversationServiceLib.*;
import MessageServiceGrpcLib.*;
import com.application.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@GrpcService // also include @Service
public class ConversationService extends ConversationServiceGrpc.ConversationServiceImplBase {
    @GrpcClient("grpc-server-message")
    private MessageServiceGrpc.MessageServiceBlockingStub messageService;

    private final ConversationRepository conversationRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public ConversationService(ConversationRepository conversationRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.conversationRepository = conversationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Get conversation list.
     */
    public List<Map<String, Object>> listConversation(String username) {
        List<Conversation> result = conversationRepository.findByUser1OrUser2OrderByLatestTimestampDesc(username, username);
        List<Map<String, Object>> conversationList = new ArrayList<>();
        List<GetUnreadCountQuery> queryList = new ArrayList<>();

        // gRPC, fetch unread counts
        for (Conversation conversation : result) {
            GetUnreadCountQuery query = GetUnreadCountQuery.newBuilder().setConversationId(conversation.id.toString()).setTimestamp(username.equals(conversation.user1) ? conversation.lastReadUser1 : conversation.lastReadUser2).build();

            queryList.add(query);
        }

        GetUnreadCountRequest request = GetUnreadCountRequest.newBuilder().setUsername(username).addAllQuery(queryList).build();
        GetUnreadCountResponse response = messageService.getUnreadCount(request);

        int index = 0;
        for (Conversation conversation : result) {
            Map<String, Object> body = new HashMap<>();
            body.put("id", conversation.id);
            body.put("user1", conversation.user1);
            body.put("user2", conversation.user2);
            body.put("latestMessage", conversation.latestMessage);
            body.put("latestTimestamp", conversation.latestTimestamp);
            body.put("lastReadUser1", conversation.lastReadUser1);
            body.put("lastReadUser2", conversation.lastReadUser2);
            body.put("unreadCount", response.getUnreadCount(index++));

            conversationList.add(body);
        }

        return conversationList;
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
        String sender = request.getSender();
        String receiver = request.getReceiver();
        String latestMessage = request.getLatestMessage();
        Long latestTimestamp = request.getLatestTimestamp();

        updateLatestMessage(sender, receiver, latestMessage, latestTimestamp);

        log.info("🟢 gRPC conversation service updateLatestMessage: {}, {}", sender, receiver);

        UpdateLatestMessageResponse response = UpdateLatestMessageResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        publishNewMessageEvent(sender, receiver, latestMessage, latestTimestamp);
    }

    /**
     * Publish "newmessage" event to Kafka.
     */
    private void publishNewMessageEvent(String sender, String receiver, String latestMessage, Long latestTimestamp){
        String topic = KafkaConfig.NEW_MESSAGE_TOPIC;
        String key = String.format("%s.%s", sender, receiver);
        NewMessageEvent newMessageEvent = new NewMessageEvent(latestMessage, latestTimestamp, sender, receiver);

        String message = "";

        try{
            message = new ObjectMapper().writeValueAsString(newMessageEvent);
            kafkaTemplate.send(topic, key, message);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * gRPC method, wrap internal function {@link ConversationService#updateLastRead(String, String, Long, String) updateLatestMessage}
     */
    @Transactional
    @Override
    public void updateLastRead(UpdateLastReadRequest request, StreamObserver<UpdateLastReadResponse> responseObserver){
        String sender = request.getSender();
        String receiver = request.getReceiver();
        Long timestamp = request.getTimestamp();

        updateLastRead(sender, receiver, timestamp, sender);

        log.info("🟢 gRPC conversation service updateRead: {}, {}", sender, receiver);

        UpdateLastReadResponse response = UpdateLastReadResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        publishReadEvent(sender, receiver, timestamp);
    }

    /**
     * Update last read.
     */
    @Transactional
    private void updateLastRead(String user1, String user2, Long timestamp, String sender){
        // swap to ensure user1 < user2
        if (user1.compareTo(user2) > 0){
            String tmp = user1;
            user1 = user2;
            user2 = tmp;
        }

        if(sender.equals(user1))
            conversationRepository.updateLastRead1(user1, user2, timestamp);
        else
            conversationRepository.updateLastRead2(user1, user2, timestamp);
    }

    /**
     * Publish "read" event to Kafka.
     */
    private void publishReadEvent(String sender, String receiver, Long timestamp){
        String topic = KafkaConfig.READ_TOPIC;
        String key = String.format("%s.%s", sender, receiver);
        ReadEvent event = new ReadEvent(timestamp, sender, receiver);

        String message = "";

        try{
            message = new ObjectMapper().writeValueAsString(event);
            kafkaTemplate.send(topic, key, message);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}
